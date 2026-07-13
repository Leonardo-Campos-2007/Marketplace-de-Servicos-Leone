# Contexto do Projeto — Leone

> **Leia este arquivo por completo antes de criar, editar ou remover qualquer código neste repositório.**
> Ele existe para que qualquer IA ou desenvolvedor que entre no projeto sem o histórico de decisões não repita erros já resolvidos nem quebre padrões já estabelecidos.

---

## 1. Visão Geral

Leone é um marketplace de serviços de dois lados, no modelo da Hotmart: conecta **compradores** que buscam contratar um serviço a **prestadores** (pessoas físicas ou empresas) que oferecem esses serviços. Não é uma loja com catálogo próprio; cada serviço publicado pertence a um prestador específico, que define preço, descrição e prazo.

O mesmo usuário pode ser comprador e prestador na mesma conta. Não existem contas separadas por papel: existe uma identidade única (`User`) com um `role` que determina o que a conta pode fazer, e um `PerfilPrestador` opcional vinculado a essa conta quando ela decide vender.

---

## 2. Stack Tecnológica

- **Backend:** Spring Boot 4.1.0, Java 21
- **Persistência:** Spring Data JDBC (não é JPA/Hibernate — atenção às diferenças de comportamento, especialmente em cascata e lazy loading, que não existem aqui)
- **Banco de dados:** MySQL
- **Segurança:** Spring Security com JWT stateless (biblioteca `jjwt`), BCrypt para hash de senha
- **Validação:** Bean Validation via `jakarta.validation` (nunca `javax.validation`)
- **Frontend (Ecxaldo, projeto irmão):** React com Vite, fora do escopo deste documento

---

## 3. Estrutura de Pacotes

```
com.br.leone
├── entity          // Entidades de domínio (mapeadas para tabelas)
├── enums           // Enums compartilhados entre entidades
├── repository      // Interfaces Spring Data JDBC
├── service         // Regras de negócio
├── controller       // Endpoints REST
├── dto             // Objetos de entrada/saída da API, desacoplados da entidade
├── exception       // Exceções de domínio + GlobalExceptionHandler
├── security        // JWT, filtros, UserDetailsService
└── config          // Beans de configuração (SecurityConfig, EncoderConfig)
```

---

## 4. Modelo de Domínio

### 4.1. Implementado e testado de ponta a ponta

**`User`** — identidade única do sistema.
- `tipoConta`: enum `PESSOA_FISICA` ou `PESSOA_JURIDICA`.
- `cpf` e `cnpj`: ambos nullable no banco, obrigatoriedade condicional ao `tipoConta`, validada no `UserService`.
- `role`: enum `COMPRADOR`, `PRESTADOR`, `ADMIN`. Default `COMPRADOR` no cadastro.
- `senha`: hash BCrypt, `@JsonProperty(access = WRITE_ONLY)`.
- Autenticação via e-mail + senha, token JWT emitido em `/auth/login`.

**`PerfilPrestador`** — dados comerciais de quem vende.
- Relação **um-para-um** com `User` (constraint `UNIQUE` em `usuario_id`).
- `statusAprovacao`: enum `PENDENTE`, `APROVADO`, `REJEITADO`.
- Ao aprovar o perfil (`PATCH /prestadores/{id}/aprovar`), o `Service` também promove o `role` do `User` vinculado para `PRESTADOR`.
- `avaliacaoMedia` e `totalAvaliacoes`: campos gerados, sem lógica de cálculo ainda.

**`CategoriaServico`** — taxonomia do catálogo.
- Estrutura hierárquica por autorreferência (`categoriaPaiId`). `null` significa raiz.
- **Taxonomia fechada e curada pelo admin.** Prestadores sugerem via `POST /categorias/sugerir` (`statusAprovacao = PENDENTE`); admin aprova ou rejeita.
- Unicidade por nível hierárquico via `existsByNomeAndCategoriaPaiId` (query indexada).
- Limite de profundidade: 3 níveis.
- Aprovar uma subcategoria exige que a categoria-pai já esteja aprovada.

**`Servico`**
- Pertence a exatamente um `PerfilPrestador` e uma `CategoriaServico`.
- Editável (nome, descrição, preço, tempo estimado); `perfilPrestadorId` e `categoriaServicoId` são imutáveis após criação.
- `SUSPENSO` (moderação) não pode ser editado nem reativado pelo prestador.
- Apenas o dono do perfil vinculado pode editar/deletar.

**`Carrinho` / `ItemCarrinho`**
- Um carrinho `ATIVO` por usuário; checkout esvazia os itens, o carrinho é reaproveitado como "ativo vazio" na consulta seguinte.
- Validações: serviço `ATIVO`, perfil `APROVADO`, prestador não compra o próprio serviço (`CompraProprioServicoException`).
- `ItemCarrinhoResponseDTO` calcula `precoAlterado` comparando preço gravado vs. preço atual do serviço.
- Mitigação de IDOR: item de outro usuário retorna 404, não 403.

**`SolicitacaoServico` / `ItemSolicitacao` / `HistoricoStatusSolicitacao`**
- Checkout agrupa itens **por prestador**: carrinho misto gera uma solicitação por prestador.
- Cada solicitação separa `valorBruto`, `comissaoPlataforma` (10%), `valorLiquidoPrestador`, com `RoundingMode.HALF_UP`.
- `ItemSolicitacao` guarda **snapshot** do serviço no momento da compra, desacoplado do `Servico` original.
- Máquina de estados (`StatusSolicitacao`): `PENDENTE → ACEITA → EM_ANDAMENTO → CONCLUIDA`, com `CANCELADA` acessível conforme quem cancela e em qual estado. Validação via `switch` exaustivo (ver seção 7).
- Cada mudança de status gera entrada em `HistoricoStatusSolicitacao`.
- `SolicitacaoResponseDTO` busca itens em lote (`findBySolicitacaoIdIn`) ao listar múltiplas solicitações, evitando N+1.

### 4.2. Planejado (ver seção 8, Roadmap)

`Chat`, `Mensagem`, `Notificacao`, `Anexo`, `Pagamento`.

---

## 5. Regras de Negócio Consolidadas

1. **Um usuário, múltiplos papéis.** Comprador e prestador coexistem na mesma conta.
2. **CPF/CNPJ é condicional ao tipo de conta**, validado em `Service`.
3. **Todo prestador passa por aprovação de admin**, e a aprovação sincroniza `role` do `User` para `PRESTADOR`.
4. **Categorias são curadas centralmente.** Fluxo sempre sugestão → aprovação/rejeição por admin.
5. **Serviço é editável, histórico de solicitação não é.** `ItemSolicitacao` guarda snapshot próprio.
6. **Endpoints de moderação restritos a `ADMIN`.**
7. **Prestador nunca compra o próprio serviço.**
8. **Transições de status de solicitação seguem máquina de estados fechada.** Nenhuma transição fora do conjunto permitido é aceita.
9. **Checkout agrupa por prestador.** Nunca uma solicitação com itens de vendedores diferentes.

---

## 6. Segurança — Padrões Estabelecidos

- **Autenticação:** JWT stateless (`SessionCreationPolicy.STATELESS`), sem `httpBasic()`.
- **Hash de senha:** sempre `BCryptPasswordEncoder`, aplicado no `Service`.
- **`BCryptPasswordEncoder` em `EncoderConfig`, separado de `SecurityConfig`**, para evitar dependência circular.
- **`CustomUserDetailsService` lê o `role` real do usuário no banco.** Nunca fixar role hardcoded.
- **Autorização via `@PreAuthorize("hasRole('ADMIN')")` declarativo**, com `@EnableMethodSecurity` habilitado. Extração do usuário sempre via `@AuthenticationPrincipal UserDetails`, nunca cast manual de `Authentication`.
- **Erros de JWT capturados no `JwtAuthenticationFilter`** via `catch (JwtException ex)`, retornando 401 com corpo JSON.
- **DTOs obrigatórios em qualquer Controller**, preferencialmente como `record` (usar construtor compacto quando precisar de lógica de cálculo, evitar classe mutável tradicional).
- **Campos controlados pelo servidor nunca lidos do `@RequestBody` do cliente.** Sempre definidos explicitamente no `Service`.
- **Mensagens de erro em contexto público não revelam qual campo falhou** quando isso permite enumeração.

---

## 7. O Que NÃO Fazer

- **Não usar `javax.validation`.** Sempre `jakarta.validation`.
- **Não confiar em `CREATE TABLE IF NOT EXISTS` para alterar tabela existente.** Usar `DROP TABLE IF EXISTS` manual durante desenvolvimento (respeitando ordem de FK), migrar para Flyway/Liquibase no primeiro dado real que não pode ser perdido.
- **Não expor entidade diretamente em Controllers.** Sempre DTO de request e response.
- **Não colocar lógica de negócio no Controller.**
- **Não duplicar validação entre métodos do mesmo `Service`.** Extrair para método privado compartilhado.
- **Não usar `int` para IDs.** Sempre `Long`.
- **Não permitir que prestadores criem categorias livremente.**
- **Não reintroduzir authority hardcoded** no `CustomUserDetailsService`.
- **Não usar nomes de tabela reservados sem renomear** (`user` → `users`).
- **Não aplicar `@PreAuthorize` sem `@EnableMethodSecurity` habilitado.**
- **Não marcar `@Transactional(readOnly = true)` em método que, direta ou indiretamente, pode escrever no banco.** Bug real já cometido: `obterCarrinhoResponse` anotado `readOnly = true` chamava internamente `obterOuCriarCarrinhoAtivo`, que faz `INSERT` na primeira vez que o usuário acessa o carrinho. Como a chamada é self-invocation (mesma classe), o Spring não abre novo proxy transacional — ela herda o contexto já aberto como somente-leitura, e o driver MySQL rejeita a escrita com `Connection is read-only. Queries leading to data modification are not allowed`. **Regra prática: um método só pode ser `readOnly = true` se absolutamente nada que ele chama, direta ou indiretamente, grava dado. Na dúvida, não marque `readOnly`.**
- **Não validar máquina de estados com `if/else` que tem `else` permissivo por omissão.** Bug real já cometido: a validação antiga só bloqueava combinações específicas erradas e deixava passar qualquer combinação não prevista (permitindo pular de `PENDENTE` direto para `EM_ANDAMENTO` sem checar permissão). **Regra prática: sempre `switch` exaustivo sobre o estado atual, listando explicitamente os destinos permitidos; qualquer coisa fora da lista é rejeitada por padrão, nunca aceita por padrão.**
- **Não validar duplicidade/unicidade carregando a tabela inteira em memória com `stream().anyMatch(...)`.** Sempre um método derivado do repository (`existsByX`, `findByXAndY`), que vira query indexada.

---

## 8. Roadmap

**Fase 1 — concluída e testada:** `User`, `PerfilPrestador`, `CategoriaServico`, `Servico`.

**Fase 2 — concluída e testada:** `Carrinho`, `ItemCarrinho`, `SolicitacaoServico`, `ItemSolicitacao`, `HistoricoStatusSolicitacao`.

**Fase 3 — concluída e testada:** `Chat`, `Mensagem`, vinculados a `SolicitacaoServico`, peer-to-peer entre comprador e prestador. Encerramento automático do chat quando a solicitação atinge estado final; histórico permanece legível mesmo encerrado. Admin lê para moderação, mas não envia mensagem.

**Fase 4 — em andamento:** `Notificacao` **concluída e testada** (campo de tipo/enum, não texto livre). `Anexo` (vinculado apenas a `Mensagem` — ver nota de escopo abaixo) e WebSocket ainda pendentes.

**`Notificacao`** — implementada.
- Nunca criada por endpoint direto; sempre disparada internamente por `SolicitacaoServicoService.alterarStatus` (nas transições `ACEITA`, `EM_ANDAMENTO`, `CONCLUIDA`, `CANCELADA`) e por `ChatService.enviarMensagem` (toda mensagem nova).
- **Best-effort by design:** `NotificacaoService.notificar(...)` captura qualquer exceção internamente e só loga o erro, nunca deixa a falha de notificação reverter a transação da operação principal (enviar mensagem, mudar status). Isso é proposital — perder uma notificação é aceitável, perder a operação de negócio que a originou não é.
- **Destinatário nunca é quem executou a ação.** Quando comprador ou prestador agem, notifica só a outra parte. Quando **admin** age (ele pode fazer qualquer transição na máquina de estados), notifica **ambas** as partes — esse caso foi um bug real encontrado e corrigido durante a implementação: a lógica ingênua de "quem não é o autor" atribuía a notificação à parte errada quando o autor era admin, porque nem comprador nem prestador tinham agido de fato.
- **`referenciaId` sempre aponta para a entidade navegável pelo frontend**, não necessariamente para a entidade que gerou o evento. Ex.: notificação de `MENSAGEM_NOVA` referencia o `solicitacaoId`, não o `mensagemId`, porque não existe rota de "ver mensagem isolada", só de "ver chat da solicitação".
- **Ownership por design, não por validação condicional:** toda query já filtra por `usuarioId` do autenticado (nunca aceita `usuarioId` de parâmetro). Tentar marcar como lida notificação de outro usuário retorna 404 (`NotificacaoNaoEncontradaException`), não 403 — mesma mitigação de IDOR já usada em `ItemCarrinho`.
- Listagem sempre paginada (`Page`), com índice composto `(usuario_id, visualizada)` no banco desde a criação da tabela — não é otimização adiada, porque o volume de notificação cresce rápido (uma por mensagem de chat).

> **Nota de escopo — `Anexo` não é polimórfico nesta fase.** Cogitamos generalizar `Anexo` para servir tanto `Mensagem` quanto um futuro `Post` de divulgação (ver Fase 5), o que exigiria um modelo polimórfico (`entidadeTipo` + `entidadeId` em vez de FK direta), abrindo mão de integridade referencial garantida pelo banco em troca de flexibilidade. Decisão tomada: **não fazer isso agora.** `Anexo` fica vinculado só a `Mensagem`, com FK real. Quando a Fase 5 for iniciada de fato, a modelagem de `Anexo` é revisada com o desenho de `Post` já definido, evitando complexidade prematura para um requisito que ainda não tinha forma.

**Fase 5 — futura, ainda não modelada:** funcionalidade de "rede social leve" para prestadores divulgarem serviços. Inclui, no mínimo:
- **`Seguidor`** (ou nome equivalente): relação muitos-para-muitos entre `User` (quem segue) e `PerfilPrestador` (quem é seguido). Decisões em aberto: se é possível deixar de seguir, se gera notificação ao ganhar seguidor ou só ao postar.
- **`Post`**: divulgação de propaganda pelo prestador, com imagem (via `Anexo`, quando este for generalizado). Decisões em aberto: se pertence ao `PerfilPrestador` em geral ou a um `Servico` específico; se tem moderação (como `CategoriaServico`, pendente até aprovação) ou publica direto; se é editável/deletável depois de postado.
- Ao postar, `Notificacao` do tipo `NOVO_POST_PRESTADOR_SEGUIDO` (ou equivalente) para todos os seguidores daquele prestador.
- Esta fase só deve começar depois que as decisões acima estiverem fechadas com a mesma clareza que tivemos para `User`/`PerfilPrestador`/`CategoriaServico` antes de codificar.

**Transversal:** `Pagamento`. `SolicitacaoServico` já separa comissão e valor líquido, mas não há integração de gateway nem rastreio de recebimento real — os valores hoje são só calculados e armazenados.

---

## 9. Convenções de Código

- Nomes de entidade, campo de domínio e exceção em **português**.
- Nomes de infraestrutura Spring em inglês.
- Ciclo por domínio: **Entity → Repository → Exceptions → Service → Controller → DTOs**, um arquivo de cada vez, testado antes de avançar.
- Toda entidade nova precisa de construtor vazio (`protected`) e construtor completo com assinatura **consistente com o corpo** — conferir que todo parâmetro declarado é atribuído a um campo, e que nenhum campo é atribuído a partir de nome que não existe na assinatura. Bug real já cometido: construtores com assinatura divergente do corpo, que só se manifesta como erro de compilação (o projeto pode passar tempo sem compilar antes de alguém rodar).
- DTOs preferencialmente como `record`; usar construtor compacto quando precisar de lógica no construtor.
- `schema.sql` atualizado **junto** com qualquer mudança de entidade, no mesmo ciclo de trabalho.

---

## 10. Segurança Básica para Qualquer Novo CRUD

- [ ] Dados sensíveis nunca aparecem em resposta JSON.
- [ ] Campos controlados pelo servidor setados explicitamente no `Service`.
- [ ] Endpoints de escrita sensíveis protegidos por `@PreAuthorize` ou equivalente.
- [ ] Toda entrada passa por `@Valid` e `jakarta.validation`.
- [ ] Exceções de domínio específicas, cada uma com handler em `GlobalExceptionHandler`.
- [ ] Endpoint público não vaza informação que ajude enumeração.
- [ ] Nenhum método `@Transactional(readOnly = true)` escreve, direta ou indiretamente, no banco.
- [ ] Máquina de estados usa `switch` exaustivo, nunca fallback permissivo.
- [ ] Listagens com dado relacionado buscam em lote (`findByXIdIn`), não em loop.

---

## 12. Documentação da API (Swagger/OpenAPI)

Integrado via `springdoc-openapi-starter-webmvc-ui`, versão **3.0.3** (não a linha 2.x — ver lição abaixo). Acessível em `/swagger-ui.html`, especificação JSON em `/v3/api-docs`.

- `OpenApiConfig.java` define metadados básicos e o esquema de segurança `bearerAuth`, que faz o Swagger UI mostrar um botão "Authorize" para colar o token JWT uma vez e reutilizá-lo em todas as requisições de teste feitas pela interface.
- Rotas `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs` e `/v3/api-docs/**` liberadas como públicas no `SecurityConfig`, para não exigir login só para ver a documentação em desenvolvimento.
- **Isso precisa ser revisado antes de qualquer deploy real.** Documentação de API aberta publicamente na internet expõe a superfície inteira do sistema (endpoints, DTOs, estrutura) para qualquer um. Em produção, considerar `springdoc.api-docs.enabled=false` e `springdoc.swagger-ui.enabled=false` (propriedades que o próprio Springdoc já expõe, confirmadas via warning no log de inicialização), ou manter acessível só via rede interna/VPN.

## 13. Lições de Depuração — Dependências e Versão

- **Ao adicionar qualquer biblioteca de terceiros, confirmar a linha de versão compatível com Spring Boot 4.x / Spring Framework 7.x antes de fixar a versão no `pom.xml`.** Bug real já cometido: `springdoc-openapi 2.6.0` (linha compatível com Spring Boot 3.x/Spring Framework 6.x) foi adicionado inicialmente, compilou e a aplicação subiu sem erro — mas quebrou em runtime com `NoSuchMethodError: ControllerAdviceBean.<init>` só ao acessar `/v3/api-docs`, porque essa classe do Spring Framework mudou de assinatura entre as versões 6.x e 7.x. **A lição prática:** compilar sem erro não é garantia de compatibilidade de runtime entre uma biblioteca de terceiros e uma versão major nova do Spring; a falha só aparece quando o código da biblioteca de fato executa o caminho que usa a API que mudou. A correção foi migrar para a linha 3.x do Springdoc (`3.0.3`), que já suporta Spring Boot 4 nativamente.
- **Um "403 Forbidden" reportado pelo navegador/cliente nem sempre é o status HTTP real vindo do backend.** Nesse mesmo episódio, o Swagger UI exibiu "response status is 403" enquanto o log do backend mostrava uma exceção não tratada (`NoSuchMethodError`) que deveria ter gerado 500. A causa provável é alguma camada intermediária (o próprio tratamento de erro do servlet container ou do cliente) mascarando o status real. **Regra prática: sempre confirmar a causa no log do servidor antes de aceitar o status reportado pela interface cliente como verdade absoluta**, principalmente quando o comportamento não bate com o que a configuração do código sugere.

---

*Este documento deve ser atualizado sempre que uma decisão de arquitetura, regra de negócio ou padrão de segurança novo for estabelecido. Ele é o ponto de partida para qualquer IA ou desenvolvedor que retome o trabalho neste repositório.*