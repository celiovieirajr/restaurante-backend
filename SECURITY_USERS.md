# Documentação de Usuários e Autenticação JWT

Este documento descreve os usuários pré-configurados no sistema, suas permissões e como obter os tokens para testes manuais.

## 👥 Perfis e Usuários de Teste

O sistema possui três níveis de permissão (Roles). Os usuários abaixo são criados automaticamente pelo Liquibase em ambientes de homologação/desenvolvimento.

| Usuário | Senha | Role | Permissões Principais |
| :--- | :--- | :--- | :--- |
| `usuario` | `password123` | `USUARIO` | Consultas (GET), criação de vendas (POST /api/sales). |
| `administrador` | `password123` | `ADMINISTRADOR` | CRUD completo de produtos, categorias e clientes. Gerenciamento de vendas. |
| `master` | `password123` | `MASTER` | Acesso total, incluindo exclusão sensível e gerenciamento de roles (futuro). |

---

## 🔑 Como obter os Tokens (Fluxo de Autenticação)

### 1. Login (Obter Token Inicial)
Para obter o token, faça uma requisição POST para o endpoint de login.

**Endpoint:** `POST /auth/login`

**Exemplo de Corpo (JSON):**
```json
{
    "username": "administrador",
    "password": "password123"
}
```

**Resposta de Sucesso:**
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "role": "ADMINISTRADOR",
    "username": "administrador",
    "accessTokenExpiresIn": 3600,
    "refreshTokenExpiresIn": 604800
}
```

### 2. Usando o Access Token
Para acessar endpoints protegidos, adicione o header `Authorization` em suas requisições.

**Header:** `Authorization: Bearer <accessToken>`

### 3. Refresh Token (Renovar Acesso)
Se o seu Access Token expirar, você pode usar o Refresh Token para obter um novo par de tokens sem precisar logar novamente.

**Endpoint:** `POST /auth/refresh`

**Corpo (JSON):**
```json
{
    "refreshToken": "seu_refresh_token_aqui"
}
```

---

## 🛠️ Teste Rápido com cURL

### Login como Administrador:
```bash
curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "administrador", "password": "password123"}'
```

### Listar Clientes (Requer ADMIN ou MASTER):
```bash
curl -X GET http://localhost:8080/api/customers \
     -H "Authorization: Bearer <COPIE_O_ACCESS_TOKEN_AQUI>"
```

### Logout:
```bash
curl -X POST http://localhost:8080/auth/logout \
     -H "Authorization: Bearer <COPIE_O_ACCESS_TOKEN_AQUI>"
```

---

## ⚙️ Configurações de Expiração
Atualmente, as expirações estão configuradas no `application.yml`:
- **USUARIO:** 30 minutos
- **ADMINISTRADOR:** 60 minutos
- **MASTER:** 120 minutos
- **Refresh Token:** 7 dias
