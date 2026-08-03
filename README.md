# Resume Builder API

Production-grade SaaS CV/Resume Builder REST API built with **Spring Boot 3.5**, **Java 21**, **PostgreSQL**, and **Google OAuth2**.

## Architecture: Hexagonal (Ports & Adapters)

```
resume-builder-api/
├── src/main/java/com/resumebuilder/
│   ├── auth/
│   │   ├── adapter/
│   │   │   ├── in/web/
│   │   │   │   ├── handler/   ← OAuth2SuccessHandler, OAuth2FailureHandler
│   │   │   │   └── dto/       ← TokenDtos
│   │   │   │   TokenController (refresh + logout)
│   │   │   └── out/persistence/  ← RefreshTokenPersistenceAdapter
│   │   └── application/
│   │       ├── port/in/          ← OAuth2LoginUseCase, TokenUseCase
│   │       ├── port/out/         ← AuthUserPort, RefreshTokenPort
│   │       └── service/          ← OAuth2AuthService
│   │
│   ├── user/
│   │   ├── adapter/out/persistence/  ← UserPersistenceAdapter
│   │   └── domain/                   ← User (provider, providerId, pictureUrl)
│   │
│   ├── resume/   ← (unchanged)
│   ├── file/     ← (unchanged)
│   ├── config/   ← SecurityConfig (OAuth2 + JWT), OpenApiConfig, WebMvcConfig
│   └── common/   ← JWT, exceptions, ApiResponse, @CurrentUser
│
└── src/main/resources/
    ├── application.yml
    └── db/migration/
        ├── V1__init_schema.sql
        └── V2__oauth2_support.sql
```

## Authentication Flow

```
Browser / React App
      │
      │  1. GET /oauth2/authorize/google?redirect_uri=http://localhost:3000/oauth2/redirect
      ▼
Spring Security ──────────────────────────────────► Google OAuth2
                                                           │
                                                           │  2. User grants consent
                                                           ▼
                              3. Google redirects to /oauth2/callback/google
                                           │
                                           ▼
                              OAuth2AuthenticationSuccessHandler
                                   │  - extracts sub, email, name, picture
                                   │  - calls OAuth2LoginUseCase
                                   │      → finds or creates User in DB
                                   │      → issues JWT access + refresh token
                                   ▼
      4. Redirect to http://localhost:3000/oauth2/redirect
                       ?token=<JWT_ACCESS>&refresh=<REFRESH_TOKEN>
                                           │
      ◄──────────────────────────────────────
      │
      │  5. Store tokens, call /api/v1/resumes with Authorization: Bearer <JWT>
      ▼
Spring Boot API validates JWT → returns data
```

## API Endpoints

### OAuth2 (handled by Spring Security)
| Flow | URI | Description |
|------|-----|-------------|
| Initiate login | `GET /oauth2/authorize/google?redirect_uri=<your-frontend>` | Redirect browser here to start Google login |
| Callback | `GET /oauth2/callback/google` | Google posts here (handled internally) |

### Token Management
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/refresh` | None | Exchange refresh token for new access token |
| POST | `/api/v1/auth/logout` | None | Revoke refresh token |

### Resumes (JWT required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/resumes` | Create a new resume |
| GET | `/api/v1/resumes` | List all resumes |
| GET | `/api/v1/resumes/{id}` | Get a resume |
| PUT | `/api/v1/resumes/{id}` | Full update |
| PATCH | `/api/v1/resumes/{id}/data` | Update JSON data only |
| DELETE | `/api/v1/resumes/{id}` | Delete a resume |
| POST | `/api/v1/resumes/{id}/upload` | Upload CV file (PDF/DOCX) |

## Google Cloud Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. **APIs & Services → Credentials → Create OAuth 2.0 Client ID**
3. Application type: **Web application**
4. Authorized redirect URIs — add:
   ```
   http://localhost:8080/oauth2/callback/google
   ```
   (for production, add your production domain too)
5. Copy the **Client ID** and **Client Secret** into your `.env`

## Quick Start

```bash
cp .env.example .env
# Fill in GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET

docker-compose up -d
```

API: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Run locally (no Docker)

```bash
docker-compose up -d postgres   # DB only

export $(cat .env | xargs)
./mvnw spring-boot:run
```

## Frontend Integration (React example)

```typescript
// 1. Kick off Google login
const login = () => {
  const redirectUri = encodeURIComponent('http://localhost:3000/oauth2/redirect');
  window.location.href =
    `http://localhost:8080/oauth2/authorize/google?redirect_uri=${redirectUri}`;
};

// 2. On the /oauth2/redirect page, extract tokens from URL
const params = new URLSearchParams(window.location.search);
const accessToken  = params.get('token');
const refreshToken = params.get('refresh');
localStorage.setItem('accessToken', accessToken);

// 3. Use the JWT on every API call
fetch('/api/v1/resumes', {
  headers: { Authorization: `Bearer ${accessToken}` }
});
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.5 |
| Language | Java 21 |
| Auth | Google OAuth2 (OIDC) + JWT (jjwt 0.12) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL 16 |
| JSONB | Hypersistence Utils |
| Migrations | Flyway |
| Docs | SpringDoc OpenAPI 3 |
| Testing | JUnit 5 + Mockito + Testcontainers |

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GOOGLE_CLIENT_ID` | ✅ | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | ✅ | Google OAuth2 client secret |
| `OAUTH2_REDIRECT_URIS` | ✅ | Comma-separated allowed frontend redirect URIs |
| `JWT_SECRET` | ✅ | HS256 signing secret (min 32 chars) |
| `DB_HOST` | ✅ | PostgreSQL host |
| `DB_PASSWORD` | ✅ | PostgreSQL password |
| `JWT_EXPIRATION_MS` | — | Access token TTL (default 24h) |
| `JWT_REFRESH_EXPIRATION_MS` | — | Refresh token TTL (default 7d) |
| `FILE_UPLOAD_DIR` | — | File storage path (default ./uploads) |
| `CORS_ALLOWED_ORIGINS` | — | Comma-separated CORS origins |


docker build \
--platform linux/amd64 \
-t resume-builder-api:latest \
.


UI

docker build --platform linux/amd64 -t resume-builder-ui .


