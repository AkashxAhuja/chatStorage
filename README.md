# Chat Storage Microservice

A production-ready Spring Boot microservice that persists Retrieval-Augmented Generation (RAG) chat histories. It exposes APIs to manage chat sessions, messages, and metadata such as favorites, while enforcing API-key security, rate limiting, and centralized error handling.

## Features

- Create, list, rename, favorite/unfavorite, and delete chat sessions.
- Persist chat messages with sender attribution and optional retrieved context.
- Short-lived API key authentication with Base64-encoded keys and centralized error responses.
- Configurable per-key rate limiting and expiration windows.
- Global exception handling, structured logging, and CORS configuration.
- Health check endpoints (`/health`, `/actuator/health`).
- OpenAPI/Swagger documentation with API key security scheme.
- Dockerized application, PostgreSQL database, and Adminer management console.
- Sample unit test coverage for core session service logic.

## Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose
- Java 17 and Maven (for running locally without Docker)

### Environment Variables

All configuration is managed through environment variables. Copy `.env.example` to `.env` and adjust as needed:

```bash
cp .env.example .env
```

Key variables:

| Variable | Description |
| --- | --- |
| `SECURITY_API_KEY_SECRET` | Static string mixed with the timestamp when generating API keys. |
| `KEY_EXPIRATION_MS` | Lifetime of generated API keys in milliseconds. |
| `RATE_LIMIT_REQUESTS_PER_MINUTE` | Maximum requests allowed per API key before it is invalidated. |
| `SPRING_DATASOURCE_*` | JDBC connection details for PostgreSQL. |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins for CORS. |
| `POSTGRES_*` | Credentials used by the PostgreSQL container. |

### Run with Docker Compose

```bash
docker compose up --build
```

Services exposed:

- `http://localhost:8080` – Chat Storage API (Swagger UI at `/swagger-ui.html`)
- `http://localhost:8081` – Adminer database console (use the PostgreSQL credentials)

### Run Locally with Maven

1. Start a PostgreSQL instance (e.g., via Docker):
   ```bash
   docker run --name chat-postgres -e POSTGRES_DB=chatdb -e POSTGRES_USER=chat_user -e POSTGRES_PASSWORD=chat_password -p 5432:5432 -d postgres:16
   ```
2. Export the environment variables (or create an `.env` file and load it).
3. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Reference

All endpoints require a valid `X-API-KEY` header (except health checks and key generation). Swagger UI documents the full contract.

### API Key Management

| Method & Path | Description |
| --- | --- |
| `POST /api/v1/api-keys` | Generate a new short-lived API key. Response includes the Base64 key and its expiration timestamp. |

### Session Management

| Method & Path | Description |
| --- | --- |
| `POST /api/v1/sessions` | Create a session. Body: `{ "userId": "user-123", "title": "My chat" }` |
| `GET /api/v1/sessions?userId={id}` | List sessions for a user. |
| `PATCH /api/v1/sessions/{sessionId}/rename?userId={id}` | Rename a session. Body: `{ "title": "New name" }` |
| `PATCH /api/v1/sessions/{sessionId}/favorite?userId={id}` | Toggle favorites. Body: `{ "favorite": true }` |
| `DELETE /api/v1/sessions/{sessionId}?userId={id}` | Delete a session and its messages. |

### Message Management

| Method & Path | Description |
| --- | --- |
| `POST /api/v1/sessions/{sessionId}/messages?userId={id}` | Add a message. Body: `{ "sender": "USER", "content": "Hello", "context": "..." }` |
| `GET /api/v1/sessions/{sessionId}/messages?userId={id}&page=0&size=20` | Paginated history for a session. |

### Health Checks

- `GET /health`
- `GET /actuator/health`

## Testing

```bash
mvn test
```

## Project Structure

```
└── src
    ├── main
    │   ├── java/com/example/chatstorage
    │   │   ├── config        # Configuration properties, filters, OpenAPI
    │   │   ├── controller    # REST controllers
    │   │   ├── domain        # JPA entities
    │   │   ├── dto           # Request/response DTOs
    │   │   ├── error         # Global exception handling
    │   │   ├── mapper        # MapStruct mappers
    │   │   ├── repository    # Spring Data repositories
    │   │   └── service       # Business services & exceptions
    │   └── resources         # application.yml
    └── test
        └── java/com/example/chatstorage
            └── service       # Unit tests
```

## License

This project is provided as-is for the chat storage assignment.
