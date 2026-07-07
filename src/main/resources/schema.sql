CREATE TABLE IF NOT EXISTS users (
                                     id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name         VARCHAR(100)  NOT NULL,
    email        VARCHAR(150)  NOT NULL UNIQUE,
    senha        VARCHAR(255)  NOT NULL,
    telefone     VARCHAR(11),
    cpf          VARCHAR(11)   UNIQUE,
    cnpj         VARCHAR(14)   UNIQUE,
    tipo_conta   VARCHAR(20)   NOT NULL,
    role         VARCHAR(20)   NOT NULL DEFAULT 'COMPRADOR',
    data_cadastro DATETIME     NOT NULL
    );

CREATE TABLE IF NOT EXISTS perfil_prestador (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id       BIGINT        NOT NULL UNIQUE,
    nome_fantasia    VARCHAR(150)  NOT NULL,
    descricao        TEXT,
    area_atuacao     VARCHAR(100)  NOT NULL,
    avaliacao_media  DOUBLE        NOT NULL DEFAULT 0.0,
    total_avaliacoes INT           NOT NULL DEFAULT 0,
    status_aprovacao VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
    data_solicitacao DATETIME      NOT NULL,
    data_aprovacao   DATETIME,
    CONSTRAINT fk_perfil_usuario FOREIGN KEY (usuario_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS categoria_servico (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome                  VARCHAR(100) NOT NULL,
    descricao             TEXT,
    categoria_pai_id      BIGINT,
    status_aprovacao      VARCHAR(20)  NOT NULL DEFAULT 'APROVADO',
    criado_por_usuario_id BIGINT,
    CONSTRAINT fk_categoria_pai FOREIGN KEY (categoria_pai_id) REFERENCES categoria_servico(id),
    CONSTRAINT fk_categoria_criador FOREIGN KEY (criado_por_usuario_id) REFERENCES users(id),
    CONSTRAINT uq_nome_pai UNIQUE (nome, categoria_pai_id)
    );

CREATE TABLE IF NOT EXISTS servico (
                                       id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       perfil_prestador_id BIGINT        NOT NULL,
                                       categoria_servico_id BIGINT       NOT NULL,
                                       nome                VARCHAR(150)  NOT NULL,
    descricao           TEXT          NOT NULL,
    preco_base          DECIMAL(10,2) NOT NULL,
    tempo_estimado      INT           NOT NULL,
    status_publicacao   VARCHAR(20)   NOT NULL DEFAULT 'ATIVO',
    data_criacao        DATETIME      NOT NULL,
    CONSTRAINT fk_servico_prestador FOREIGN KEY (perfil_prestador_id) REFERENCES perfil_prestador(id),
    CONSTRAINT fk_servico_categoria FOREIGN KEY (categoria_servico_id) REFERENCES categoria_servico(id)
    );

CREATE TABLE IF NOT EXISTS carrinho (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id       BIGINT        NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'ATIVO',
    data_criacao     DATETIME      NOT NULL,
    data_atualizacao DATETIME      NOT NULL,
    CONSTRAINT fk_carrinho_usuario FOREIGN KEY (usuario_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS item_carrinho (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    carrinho_id     BIGINT        NOT NULL,
    servico_id      BIGINT        NOT NULL,
    quantidade      INT           NOT NULL DEFAULT 1,
    preco_unitario  DECIMAL(10,2) NOT NULL,
    data_adicionado DATETIME      NOT NULL,
    CONSTRAINT fk_item_carrinho_parent FOREIGN KEY (carrinho_id) REFERENCES carrinho(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_servico FOREIGN KEY (servico_id) REFERENCES servico(id),
    CONSTRAINT uq_carrinho_servico UNIQUE (carrinho_id, servico_id)
);

CREATE TABLE IF NOT EXISTS solicitacao_servico (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprador_id            BIGINT         NOT NULL,
    perfil_prestador_id     BIGINT         NOT NULL,
    status                  VARCHAR(20)    NOT NULL DEFAULT 'PENDENTE',
    valor_bruto             DECIMAL(10,2)  NOT NULL,
    comissao_plataforma     DECIMAL(10,2)  NOT NULL,
    valor_liquido_prestador DECIMAL(10,2)  NOT NULL,
    data_criacao            DATETIME       NOT NULL,
    data_atualizacao        DATETIME       NOT NULL,
    CONSTRAINT fk_solicitacao_comprador FOREIGN KEY (comprador_id) REFERENCES users(id),
    CONSTRAINT fk_solicitacao_prestador FOREIGN KEY (perfil_prestador_id) REFERENCES perfil_prestador(id)
);

CREATE TABLE IF NOT EXISTS item_solicitacao (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitacao_id          BIGINT         NOT NULL,
    servico_id              BIGINT,
    nome_servico_snapshot   VARCHAR(150)   NOT NULL,
    descricao_servico_snapshot TEXT           NOT NULL,
    preco_unitario          DECIMAL(10,2)  NOT NULL,
    quantidade              INT            NOT NULL DEFAULT 1,
    tempo_estimado_snapshot INT            NOT NULL,
    CONSTRAINT fk_item_solicitacao_parent FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_servico(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_solicitacao_servico FOREIGN KEY (servico_id) REFERENCES servico(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS historico_status_solicitacao (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitacao_id         BIGINT         NOT NULL,
    status_anterior        VARCHAR(20),
    status_novo            VARCHAR(20)    NOT NULL,
    data_alteracao         DATETIME       NOT NULL,
    observacao             TEXT,
    usuario_responsavel_id BIGINT         NOT NULL,
    CONSTRAINT fk_historico_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_servico(id) ON DELETE CASCADE,
    CONSTRAINT fk_historico_responsavel FOREIGN KEY (usuario_responsavel_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS chat (
                                    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    solicitacao_id BIGINT    NOT NULL UNIQUE,
                                    ativo          BOOLEAN   NOT NULL DEFAULT TRUE,
                                    data_criacao   DATETIME  NOT NULL,
                                    CONSTRAINT fk_chat_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_servico(id)
    );

CREATE TABLE IF NOT EXISTS mensagem (
                                        id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        chat_id      BIGINT       NOT NULL,
                                        remetente_id BIGINT       NOT NULL,
                                        conteudo     TEXT         NOT NULL,
                                        data_envio   DATETIME     NOT NULL,
                                        lida         BOOLEAN      NOT NULL DEFAULT FALSE,
                                        CONSTRAINT fk_mensagem_chat FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE,
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (remetente_id) REFERENCES users(id)
    );