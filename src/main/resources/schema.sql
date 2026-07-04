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