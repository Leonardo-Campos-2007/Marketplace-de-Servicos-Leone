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
- **Validação:** Bean Validation via `jakarta.validation` (nunca `javax.validation` — o projeto está no Spring Boot 4.x, que já migrou completamente para o namespace `jakarta`)
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

### 4.1. Implementado

**`User`** — identidade única do sistema.
- `tipoConta`: enum `PESSOA_FISICA` ou `PESSOA_JURIDICA`.
- `cpf` e `cnpj`: ambos nullable no banco, mas a obrigatoriedade é condicional ao `tipoConta` (PF exige CPF, PJ exige CNPJ). Essa regra **não está nas anotações da entidade** porque depende de outro campo; está validada explicitamente no `UserService`.
- `role`: enum `COMPRADOR`, `PRESTADOR`, `ADMIN`. Default `COMPRADOR` no cadastro. Uma conta pode evoluir de comprador para prestador sem criar uma conta nova.
- `senha`: sempre armazenada como hash BCrypt, nunca em texto plano. Anotada com `@JsonProperty(access = WRITE_ONLY)` para nunca vazar no JSON de resposta.
- Autenticação via e-mail + senha, token JWT emitido em `/auth/login`.

**`PerfilPrestador`** — dados comerciais de quem vende.
- Relação **um-para-um** com `User` (constraint `UNIQUE` em `usuario_id`, não apenas uma FK comum).
- `statusAprovacao`: enum `PENDENTE`, `APROVADO`, `REJEITADO`. Todo prestador passa por aprovação de admin antes de poder publicar serviços.
- `avaliacaoMedia` e `totalAvaliacoes`: campos gerados, ainda sem lógica de cálculo (dependem do módulo de avaliações, que ainda não existe).

**`CategoriaServico`** — taxonomia do catálogo.
- Estrutura hierárquica por autorreferência (`categoriaPaiId` aponta para `categoria_servico.id`). `categoriaPaiId = null` significa categoria raiz.
- **Taxonomia fechada e curada pelo admin.** Prestadores não criam categorias livremente; eles **sugerem** via `POST /categorias/sugerir`, o que gera uma categoria com `statusAprovacao = PENDENTE` e `criadoPorUsuarioId` preenchido. Um admin aprova (`PATCH /{id}/aprovar`) ou rejeita (`DELETE /{id}/rejeitar`, que remove a sugestão).
- Unicidade de nome é validada **por nível hierárquico**, não globalmente: duas categorias "Limpeza" podem existir em ramos diferentes da árvore, mas não como duas raízes idênticas nem como duas filhas do mesmo pai. Isso é reforçado tanto por constraint `UNIQUE (nome, categoria_pai_id)` no banco quanto por checagem explícita no `Service`, porque `NULL` em `categoria_pai_id` não é comparável a si mesmo pela constraint sozinha.

**`Servico`** — em construção no momento deste documento.
- Pertence a exatamente um `PerfilPrestador` e a exatamente uma `CategoriaServico`.
- Totalmente editável após criação (nome, descrição, preço, tempo estimado).
- Quando um serviço é deletado, solicitações já ativas mantêm uma **cópia** dos dados do serviço no momento da contratação, para preservar o histórico do que foi efetivamente contratado. Essa cópia ainda não está implementada; a estrutura para suportá-la deve ser levada em conta ao desenhar `SolicitacaoServico`.

### 4.2. Planejado (ver seção 6, Roadmap)

`Carrinho`, `ItemCarrinho`, `SolicitacaoServico`, `ItemSolicitacao`, `Chat`, `Mensagem`, `Notificacao`, `Anexo`, `Pagamento`, `HistoricoStatusSolicitacao`.

---

## 5. Regras de Negócio Consolidadas

1. **Um usuário, múltiplos papéis.** Comprador e prestador coexistem na mesma conta. Não criar fluxo de conta separada para vender.
2. **CPF/CNPJ é condicional ao tipo de conta**, validado em `Service`, nunca assumido como sempre obrigatório ou sempre opcional.
3. **Todo prestador passa por aprovação de admin** antes de poder publicar serviços. Isso é modelado via `PerfilPrestador.statusAprovacao`, não via `role` do `User` (o `role` muda para `PRESTADOR` no momento em que a conta *solicita* virar vendedor, não no momento da aprovação — a aprovação afeta apenas o perfil, não a identidade).
4. **Categorias são curadas centralmente.** Nenhum prestador cria categoria diretamente; o fluxo é sempre sugestão → aprovação/rejeição por admin. Isso existe para evitar fragmentação de taxonomia (múltiplas categorias equivalentes com nomes diferentes), que prejudica a descoberta de prestadores pelo comprador.
5. **Serviço é editável, histórico de solicitação não é.** Editar um `Servico` não deve alterar o registro de uma `SolicitacaoServico` já criada a partir dele.
6. **Endpoints de moderação (aprovar/rejeitar categoria, listar pendentes, criar categoria direto) são restritos a `ADMIN`.**

---

## 6. Segurança — Padrões Estabelecidos

- **Autenticação:** JWT stateless (`SessionCreationPolicy.STATELESS`), sem uso de `httpBasic()`.
- **Hash de senha:** sempre `BCryptPasswordEncoder`, aplicado no `Service`, nunca no Controller nem na entidade.
- **`BCryptPasswordEncoder` fica em `EncoderConfig`, uma classe própria, separada de `SecurityConfig`.** Colocar o bean dentro de `SecurityConfig` cria dependência circular quando outros beans (como `JwtAuthenticationFilter`) dependem de `UserService`, que depende do encoder, que estaria dentro do próprio `SecurityConfig`.
- **`CustomUserDetailsService` lê o `role` real do usuário no banco** (`user.getRole().name()`) para montar a `GrantedAuthority`. Nunca fixar um role hardcoded tipo `.roles("USER")` — isso quebra qualquer checagem de admin de forma silenciosa.
- **Autorização por papel:** migrando de checagem manual (`exigirAdmin()` dentro do Controller, lendo `Authentication` e comparando authorities manualmente) para `@PreAuthorize("hasRole('ADMIN')")` declarativo, com `@EnableMethodSecurity` habilitado em `SecurityConfig`. Novos endpoints administrativos devem usar `@PreAuthorize` diretamente; não replicar o padrão manual antigo.
- **Erros de JWT (token expirado, malformado, assinatura inválida) são capturados no próprio `JwtAuthenticationFilter`** via `catch (JwtException ex)`, retornando 401 com corpo JSON, em vez de deixar a exceção subir e virar 500 genérico.
- **DTOs de entrada e saída são obrigatórios em qualquer novo Controller.** A entidade nunca deve ser exposta diretamente em `@RequestBody` ou como retorno de endpoint. Motivo: acoplar o contrato da API ao schema do banco significa que qualquer mudança de coluna vira, sem querer, uma mudança de contrato público.
- **Campos gerados/controlados pelo servidor (`statusAprovacao`, `criadoPorUsuarioId`, `role`, `dataCadastro`, `dataCriacao`) nunca devem ser lidos do corpo da requisição do cliente.** Eles são sempre definidos explicitamente dentro do `Service`, depois de qualquer validação, nunca herdados de um objeto que veio do `@RequestBody`. Confiar no valor que o cliente envia para esses campos é uma falha de segurança (permite, por exemplo, um usuário se autopromover a admin ou forjar uma categoria já aprovada).
- **Mensagens de erro em contexto público (cadastro, login) não devem revelar qual campo especificamente falhou** quando isso permite enumerar dados sensíveis (ex.: confirmar que um e-mail existe). Em contexto autenticado (usuário já provou identidade), mensagens específicas são aceitáveis.

---

## 7. O Que NÃO Fazer

- **Não usar `javax.validation`.** É sempre `jakarta.validation` neste projeto (Spring Boot 4.x).
- **Não confiar em `CREATE TABLE IF NOT EXISTS` para alterar uma tabela existente.** Esse comando não faz nada se a tabela já existe, mesmo que o `schema.sql` tenha sido atualizado com colunas novas. Durante o desenvolvimento, quando o schema de uma entidade muda, é necessário `DROP TABLE IF EXISTS <tabela>;` manualmente antes de subir a aplicação. Isso é aceitável agora porque não há dados de produção; não será aceitável depois do primeiro deploy real, quando será necessário migrar para uma ferramenta de migração versionada (Flyway ou Liquibase).
- **Não expor a entidade diretamente em Controllers.** Sempre passar por DTO de request e DTO de response.
- **Não colocar lógica de negócio no Controller.** O Controller delega para o Service e traduz o resultado em `ResponseEntity`; validação condicional, regras de duplicidade e orquestração pertencem ao Service.
- **Não deixar um `Service` derivado duplicar validação que já existe em outro método do mesmo `Service`.** Extrair para um método privado compartilhado (ver `validarPaiENome` em `CategoriaServicoService` como padrão a seguir).
- **Não usar `int` para campos de ID.** Sempre `Long`, porque `int` não aceita `null`, e o Spring Data JDBC depende de o ID ser `null` para saber que deve fazer `INSERT` em vez de tentar um `UPDATE` num id inexistente.
- **Não permitir que prestadores criem categorias livremente.** Ver regra de negócio 4.
- **Não reintroduzir `.roles("USER")` fixo ou qualquer authority hardcoded** no `CustomUserDetailsService`. A authority sempre deve refletir o `role` real gravado no banco.
- **Não usar nomes de tabela reservados sem aspas ou sem renomear** (ex.: `user` é palavra reservada em vários bancos; a tabela é `users`).
- **Não aplicar `@PreAuthorize` sem antes confirmar que `@EnableMethodSecurity` está habilitado em `SecurityConfig`** — sem isso, a anotação é silenciosamente ignorada e o endpoint fica aberto.

---

## 8. Roadmap (por fase, conforme documento de arquitetura original)

**Fase 1 (em andamento):** `User`, `PerfilPrestador`, `CategoriaServico`, `Servico`.

**Fase 2:** `Carrinho`, `ItemCarrinho`, `SolicitacaoServico`, `ItemSolicitacao`. Pontos já sinalizados para essa fase:
- `Carrinho` precisa de campo `status` (`ATIVO`/`CONVERTIDO`) para distinguir carrinho corrente de histórico.
- `SolicitacaoServico` precisa separar valor bruto, comissão da plataforma e valor líquido do prestador (o negócio é marketplace, não venda direta — a plataforma intermedia o pagamento).
- Considerar `HistoricoStatusSolicitacao` para auditoria de mudança de estado ao longo do fluxo `PENDENTE → ... → CONCLUIDO/CANCELADO`.

**Fase 3:** `Chat`, `Mensagem`, vinculados a `SolicitacaoServico`, entre comprador e prestador (não entre cliente e atendente interno — o modelo original previa atendente da empresa, o modelo atual é peer-to-peer mediado pela plataforma).

**Fase 4:** `Notificacao` (com campo de tipo/enum, não texto livre, para o frontend não depender de parsing de string), `Anexo`, WebSocket para chat em tempo real.

**Transversal, sem fase definida ainda:** `Pagamento`, obrigatório antes de qualquer fluxo de dinheiro real (não existe hoje no modelo).

---

## 9. Convenções de Código

- Nomes de entidade, campo de domínio e exceção em **português** (`CategoriaServico`, `UsuarioNaoEncontradoException`, `DadosJaCadastradosException`).
- Nomes de infraestrutura Spring (classes de config, filtros, beans técnicos) seguem convenção padrão do framework, em inglês, quando é isso que a comunidade Spring usa (`SecurityConfig`, `JwtAuthenticationFilter`).
- Todo domínio segue o ciclo: **Entity → Repository → Exceptions → Service → Controller → DTOs**, implementado e testado (aplicação sobe sem erro, endpoint testado no Postman) um arquivo de cada vez, nunca em lote. Motivo: quando várias entidades novas sobem juntas e a aplicação falha, o log aponta um erro entre múltiplas causas possíveis; um arquivo de cada vez mantém o log apontando exatamente uma causa.
- Toda entidade nova precisa de: construtor vazio (`protected`, para o Spring Data JDBC popular via reflection) e um construtor completo.
- `schema.sql` é a fonte de verdade do schema atual esperado; ele deve ser atualizado **junto** com qualquer mudança de entidade, no mesmo commit/ciclo de trabalho.

---

## 10. Segurança Básica para Qualquer Novo CRUD

Checklist mínimo antes de considerar um novo domínio (entidade + CRUD) pronto:

- [ ] Senhas ou dados sensíveis equivalentes nunca aparecem em resposta JSON (`@JsonProperty(access = WRITE_ONLY)` ou DTO que simplesmente não carrega o campo).
- [ ] Campos que representam estado controlado pelo servidor (aprovação, role, timestamps gerados) são setados explicitamente no `Service`, nunca aceitos do `@RequestBody` do cliente.
- [ ] Endpoints de escrita (`POST`, `PUT`, `DELETE`, `PATCH`) que afetam dado de outro usuário ou dado administrativo estão protegidos por `@PreAuthorize` ou equivalente, não deixados abertos "por enquanto" sem anotação alguma.
- [ ] Toda entrada de usuário (`@RequestBody`) passa por `@Valid` no Controller e tem anotações de `jakarta.validation` na entidade ou DTO correspondente.
- [ ] Exceções de domínio específicas (não `RuntimeException` genérica) são lançadas para cada caso de erro esperado, e cada uma tem handler correspondente em `GlobalExceptionHandler` com o status HTTP correto.
- [ ] Se o endpoint é público (não exige autenticação), a mensagem de erro não vaza informação que ajude enumeração de dados de outros usuários.

---

*Este documento deve ser atualizado sempre que uma decisão de arquitetura, regra de negócio ou padrão de segurança novo for estabelecido ao longo do desenvolvimento. Ele é o ponto de partida para qualquer IA ou desenvolvedor que retome o trabalho neste repositório.*