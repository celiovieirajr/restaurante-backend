# Guia de Segurança e Autenticação (JWT)

Este documento descreve como testar o sistema de autenticação e autorização implementado.

## Usuários de Teste

| Nome de Usuário | E-mail | Senha | Cargos (Roles) | Permissões |
| :--- | :--- | :--- | :--- | :--- |
| `usuario` | `usuario@email.com` | `password123` | `ROLE_USUARIO` | Consultas públicas (GET) |
| `administrador` | `admin@email.com` | `password123` | `ROLE_ADMINISTRADOR` | CRUD de produtos e clientes |
| `master` | `master@email.com` | `password123` | `ROLE_MASTER` | Acesso total + Endpoints sensíveis |

## Como Obter Tokens (Login)

Use o comando `curl` abaixo para fazer login e obter os tokens de **Acesso** e **Refresh**.

### Exemplo para o Administrador:
```bash
curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "administrador",
       "password": "password123"
     }'
```

**Resposta Esperada:**
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG...",
  "type": "Bearer",
  "role": "ADMINISTRADOR",
  "username": "administrador",
  "accessTokenExpiresIn": 3600,
  "refreshTokenExpiresIn": 604800
}
```

## Como Usar o Token de Acesso (Chamada Protegida)

Adicione o header `Authorization: Bearer <SEU_ACCESS_TOKEN>` nas suas requisições.

```bash
curl -X GET http://localhost:8080/api/admin/dashboard \
     -H "Authorization: Bearer <SEU_ACCESS_TOKEN>"
```

## Como Renovar o Token (Refresh)

Quando o `accessToken` expirar, use o `refreshToken` para gerar um novo par.

```bash
curl -X POST http://localhost:8080/auth/refresh \
     -H "Content-Type: application/json" \
     -d '{
       "refreshToken": "<SEU_REFRESH_TOKEN>"
     }'
```

## Como Revogar o Token (Logout)

```bash
curl -X POST http://localhost:8080/auth/logout \
     -H "Authorization: Bearer <SEU_ACCESS_TOKEN>"
```

## Variáveis Configuráveis (`application.yml`)

- `jwt.expiration.usuario`: Expiração do usuário comum em segundos (Padrão: 1800s / 30m).
- `jwt.expiration.administrador`: Expiração do administrador em segundos (Padrão: 3600s / 1h).
- `jwt.expiration.refresh`: Expiração do token de renovação (Padrão: 604800s / 7 dias).
- `jwt.secret`: Chave secreta para assinatura dos tokens.
