# Task Management REST API (Spring Boot + PostgreSQL)

A clean, dockerized CRUD backend that demonstrates **REST design, security (JWT + roles), persistence (JPA/Hibernate), pagination & filtering, OpenAPI docs, and testing with Testcontainers**.

---

## Features

* **Users & Auth**

  * Register / Login with **JWT** (`/api/auth/register`, `/api/auth/login`)
  * Roles: **USER** and **ADMIN** (admin can access everyone’s tasks)
* **Tasks**

  * CRUD with: `id, title, description, status (TODO|DOING|DONE), dueDate, owner, createdAt, updatedAt`
  * **Ownership** enforced (non-admins see only their tasks)
  * **Search & filter**: `q` (title/description), `status`
  * **Pagination & sorting** via Spring Data (stable DTO page serialization)
* **API Docs**

  * **Swagger UI** at `/swagger-ui.html` (with **Authorize** button for JWT)
* **Database**

  * **PostgreSQL** (Docker Compose)
* **Testing**

  * **JUnit 5**, Spring Boot test slices
  * **Testcontainers** Postgres for integration tests
* **Dockerized**

  * `docker compose up --build` starts **app + db**
  * Multi-stage Dockerfile (Maven build → slim JRE image)
* **Dev Container (optional)**

  * VS Code Dev Container config included

---

## Quick Start

### Run with Docker Compose

```bash
docker compose down -v
docker compose up --build
```

* API: `http://localhost:8080`
* Swagger: `http://localhost:8080/swagger-ui.html`
* Postgres: `localhost:5432` (db: `tasks`, user: `taskuser`, pass: `taskpass`)

**Environment (Compose already sets these):**

```
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/tasks
SPRING_DATASOURCE_USERNAME=taskuser
SPRING_DATASOURCE_PASSWORD=taskpass
JWT_SECRET=change-me-super-long-random-secret   # 32+ chars
```

> ✅ `JWT_SECRET` must be a strong 32+ char secret (HS256). Change it for prod.

---

## Try the API (cURL)

```bash
# Register (set admin to true only when you want an admin)
curl -s http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"secret","admin":false}'

# Or login to get a fresh token
TOKEN=$(curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"secret"}' | jq -r .token)

# Create a task
curl -s http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Write docs","description":"swagger docs","status":"TODO"}'

# List my tasks (paged + filtered)
curl -s "http://localhost:8080/api/tasks?page=0&size=10&status=TODO&q=docs" \
  -H "Authorization: Bearer $TOKEN"
```

In **Swagger UI**, click **Authorize**, paste `Bearer <token>` from login, then try endpoints.

---

## API Summary

| Method | Path                   | Auth       | Notes                                                              |
| -----: | ---------------------- | ---------- | ------------------------------------------------------------------ |
|    GET | `/ping`                | Public     | Health probe (`pong`)                                              |
|   POST | `/api/auth/register`   | Public     | `{ username, password, admin? } → { token }`                       |
|   POST | `/api/auth/login`      | Public     | `{ username, password } → { token }`                               |
|    GET | `/api/me`              | User/Admin | Who am I? `{ name, roles }`                                        |
|    GET | `/api/tasks`           | User/Admin | Lists **my** tasks; supports `q`, `status`, `page`, `size`, `sort` |
|   POST | `/api/tasks`           | User/Admin | Create task (owner auto-set to current user)                       |
|    GET | `/api/tasks/{id}`      | User/Admin | Must be owner or admin                                             |
|    PUT | `/api/tasks/{id}`      | User/Admin | Must be owner or admin                                             |
| DELETE | `/api/tasks/{id}`      | User/Admin | Must be owner or admin                                             |
|    GET | `/api/tasks/admin/all` | **Admin**  | List **all** users’ tasks (paged)                                  |

**Task JSON Example**

```json
{
  "id": 1,
  "title": "Write docs",
  "description": "swagger docs",
  "status": "TODO",
  "dueDate": "2025-12-31T23:59:59Z",
  "createdAt": "2025-10-22T10:20:06Z",
  "updatedAt": "2025-10-22T10:20:06Z",
  "owner": { "id": 1, "username": "alice" }
}
```

---

## Project Structure

Mirrors your repo exactly (per screenshots):

```
.devcontainer/
  devcontainer.json

.github/workflows/
  ci.yml

src/
└── main/
    ├── java/com/example/tasks/
    │   ├── TaskApiApplication.java
    │   ├── HealthController.java
    │   ├── config/
    │   │   ├── OpenApiConfig.java
    │   │   └── WebConfig.java
    │   ├── debug/
    │   │   └── WhoAmIController.java
    │   ├── security/
    │   │   ├── AuthController.java
    │   │   ├── JwtAuthFilter.java
    │   │   ├── JwtService.java
    │   │   └── SecurityConfig.java
    │   ├── task/
    │   │   ├── Task.java
    │   │   ├── TaskController.java
    │   │   ├── TaskRepository.java
    │   │   ├── TaskSpecifications.java
    │   │   └── TaskStatus.java
    │   └── user/
    │       ├── AppUser.java
    │       ├── AppUserDetailsService.java
    │       ├── AppUserRepository.java
    │       ├── Role.java
    │       └── RoleRepository.java
    └── resources/
        └── application.yml

└── test/java/com/example/tasks/
    ├── api/
    │   └── AuthAndTaskFlowTest.java
    ├── it/
    │   └── AbstractIntegrationTest.java
    └── task/
        └── TaskRepositoryTest.java

docker-compose.yml
Dockerfile
pom.xml
```

---

## Development

### Build & Run (without Compose)

```bash
mvn -q -DskipTests package
java -jar target/task-api-*.jar
```

Provide DB env vars (`SPRING_DATASOURCE_*`) and **JWT_SECRET**.

### Dev Container (optional)

Open in VS Code → “Reopen in Container” to get Java, Maven, Docker tooling preconfigured.

---

## Testing

* **Integration tests**: Testcontainers Postgres (Docker required)
* Run:

  ```bash
  mvn -q clean test
  ```

---

## Docker

### Local image

```bash
docker build -t task-api:local .
```

### Compose (recommended)

```bash
docker compose up --build
```

`.dockerignore` (recommended):

```
target/
.git
```

---

## CI/CD (GitHub Actions)

Workflow at `.github/workflows/ci.yml`:

* **Build & test** with Maven
* On `main`, **build & push** a Docker image to **GHCR**:

  * `ghcr.io/<your-user-or-org>/task-api:latest`

---

## Security Notes

* Set a strong `JWT_SECRET` (≥32 chars).
* **Ownership checks** enforced in `TaskController`.
* Use migrations (Flyway/Liquibase) and hardened CORS/headers for production.

---

## Troubleshooting

* **`Could not resolve placeholder 'JWT_SECRET'`**
  Export the env var or run via Docker Compose.
* **Swagger 401**
  Click **Authorize** and paste `Bearer <token>` from `/api/auth/login`.
* **Ports busy**
  Change port mappings in `docker-compose.yml`.

---

## License

Add your preferred license (e.g., MIT).
