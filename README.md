# Leone - Marketplace de Serviços 🚀

> **Status:** ⚙️ Em Desenvolvimento

Leone é um **marketplace de dois lados** que conecta compradores com prestadores de serviços. Inspirado no modelo da Hotmart, o projeto permite que qualquer usuário seja, simultaneamente, comprador e prestador de serviços na mesma plataforma.

---

## 📋 Sobre o Projeto

Leone facilita a contratação e oferta de serviços de forma simples e segura. O sistema foi desenvolvido com foco em:

- ✅ **Segurança**: Autenticação robusta com JWT e criptografia BCrypt
- ✅ **Validação**: Validação de dados em tempo real com Bean Validation
- ✅ **Escalabilidade**: Arquitetura moderna baseada em Spring Boot
- ✅ **Documentação**: API documentada com Swagger/OpenAPI
- ✅ **Comunicação em Tempo Real**: Suporte a WebSocket para interações dinâmicas

### Principais Características

- **Contas Unificadas**: Um usuário pode ser comprador e prestador na mesma conta
- **Perfil de Prestador**: Criação opcional de perfil para oferecer serviços
- **Sistema de Roles**: Controle de acesso baseado em papéis
- **API RESTful**: Endpoints bem estruturados e documentados
- **Persistência**: Banco de dados MySQL com Spring Data JDBC

---

## 🛠️ Tecnologias Implementadas

### Backend
| Tecnologia | Versão | Descrição |
|---|---|---|
| **Java** | 21 | Linguagem principal |
| **Spring Boot** | 4.1.0 | Framework web e injeção de dependência |
| **Spring Security** | Latest | Autenticação e autorização |
| **Spring Data JDBC** | Latest | Acesso a dados sem ORM completo |
| **MySQL** | - | Banco de dados relacional |
| **JWT (JJWT)** | 0.12.7 | Geração e validação de tokens |
| **Swagger/OpenAPI** | 3.0.3 | Documentação interativa da API |
| **WebSocket** | Latest | Comunicação em tempo real |
| **Bean Validation** | Latest | Validação de dados |
| **Maven** | - | Gerenciamento de dependências |

### Segurança
- ✔️ **JWT Stateless**: Autenticação sem sessão
- ✔️ **BCrypt**: Hash seguro de senhas
- ✔️ **Spring Security**: Filtros de segurança configurados
- ✔️ **Validação**: Validação de entrada em múltiplas camadas

---

## 📁 Estrutura do Projeto

```
com.br.leone
├── entity/          # Entidades de domínio (mapeadas para tabelas)
├── enums/           # Enumerações compartilhadas
├── repository/      # Interfaces Spring Data JDBC
├── service/         # Lógica de negócio
├── controller/      # Endpoints REST
├── dto/             # Objetos de transferência de dados
├── exception/       # Exceções customizadas + Handler global
├── security/        # JWT, filtros e UserDetailsService
└── config/          # Configurações (Security, Encoder, etc)
```

---


## ⚠️ Status de Desenvolvimento

Este projeto está **em desenvolvimento ativo**. Algumas funcionalidades podem estar:

- ⚙️ Sob revisão
- 🔧 Em refatoração
- 📝 Documentação em progresso
- 🧪 Testes em expansão

---

## 📜 Licença

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 👤 Autor

**Leone Marketplace**  
Desenvolvido com ❤️ para conectar compradores e prestadores de serviços.

---


---

**Última atualização:** Agosto de 2026  
**Status:** ⚙️ Em Desenvolvimento Contínuo
