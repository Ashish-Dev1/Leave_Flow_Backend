# LeaveFlow Backend

> A Spring Boot backend for managing employee leave requests, approvals, authentication, and manager-side reporting.

## Why This Project Exists

`LeaveFlow` is a role-based leave management API built for teams that need a clear workflow between employees and managers.

It handles:

- user registration and login with JWT authentication
- leave application and personal leave history
- manager approval and rejection workflows
- filtered reporting for users and leave records
- email notifications on leave events
- Swagger/OpenAPI documentation for frontend integration

## At a Glance

| Area | Stack |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Security | Spring Security + JWT |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| API Docs | SpringDoc OpenAPI + Swagger UI |
| Mail | Spring Mail |
| Testing | JUnit + H2 |

## Core Workflow

```text
User registers or logs in
        |
        v
JWT token is issued
        |
        v
User applies for leave
        |
        v
Managers receive email notification
        |
        v
Manager approves or rejects request
        |
        v
User receives status update email
```

## Roles in the System

| Role | What it can do |
|---|---|
| `USER` | Apply for leave, view own leaves, update profile, change password |
| `MANAGER` | Review requests, approve/reject leaves, view users, stats, and filtered reports |
| `ADMIN` | Present in enum, but current route protections are mainly built around `USER` and `MANAGER` |

## Main Features

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`
- JWT-based stateless authentication
- Swagger supports bearer token authorization

### User Features

- apply for leave
- view own leave history
- update profile
- change password

Base path: `/api/v1/user`

### Manager Features

- view pending leave requests
- view all leave requests
- approve or reject leave requests
- filter leave records with multiple query parameters
- inspect users and leave statistics

Base path: `/api/manager`

## API Surface

### Auth Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Create a user account |
| `POST` | `/api/auth/login` | Get JWT token |

### User Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/v1/user/leaves` | Apply for leave |
| `GET` | `/api/v1/user/leaves/my` | Get current user's leaves |
| `PUT` | `/api/v1/user/profile` | Update profile |
| `PATCH` | `/api/v1/user/change-password` | Change password |

### Manager Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/manager/leaves` | Pending leave requests |
| `GET` | `/api/manager/leaves/all` | All leave requests |
| `GET` | `/api/manager/leaves/enhanced-filter` | Advanced leave filters |
| `PUT` | `/api/manager/leaves/{id}/approve` | Approve a leave |
| `PUT` | `/api/manager/leaves/{id}/reject` | Reject a leave |
| `GET` | `/api/manager/users` | List all users |
| `GET` | `/api/manager/users/filter` | Filter users by leave data |
| `GET` | `/api/manager/users/enhanced-filter` | Advanced user filtering |
| `GET` | `/api/manager/users/{userId}/leaves` | Get one user's leaves |
| `GET` | `/api/manager/users/{userId}/leaves/filter` | Filter one user's leaves |
| `GET` | `/api/manager/users/{userId}/leaves/status` | Filter one user's leaves by status |
| `GET` | `/api/manager/users/stats` | Aggregate leave statistics |

## Leave Model

The main leave entity includes:

- `startDate`
- `endDate`
- `reason`
- `leaveType`
- `status`
- `managerComment`
- `leaveSession` for half-day use cases

Current leave statuses:

- `PENDING`
- `APPROVED`
- `REJECTED`

Declared leave and session types in the project:

- leave types enum: `CASUAL_LEAVE`, `HALF_DAY`, `PRIVILEGE_LEAVE`
- session types enum: `FIRST_HALF`, `SECOND_HALF`

Note: the current `LeaveRequest` entity stores `leaveType` as `String`, so API callers should stay consistent with the values expected by the service layer and frontend.

## Project Structure

```text
src/main/java/com/leavemanage
+-- config        # security, OpenAPI, seed data
+-- controller    # REST APIs
+-- dto           # request/response payloads
+-- exception     # custom exceptions and global handler
+-- model         # JPA entities and enums
+-- repository    # Spring Data repositories
+-- service       # business logic
\-- util          # JWT and user-details helpers
```

## Important Files

| File | Why it matters |
|---|---|
| [pom.xml](/C:/Users/kafle/Leave_Management_Backend/pom.xml) | dependencies and build setup |
| [application.properties](/C:/Users/kafle/Leave_Management_Backend/src/main/resources/application.properties) | runtime configuration |
| [SecurityConfig.java](/C:/Users/kafle/Leave_Management_Backend/src/main/java/com/leavemanage/config/SecurityConfig.java) | route protection and JWT filter wiring |
| [AuthController.java](/C:/Users/kafle/Leave_Management_Backend/src/main/java/com/leavemanage/controller/AuthController.java) | auth endpoints |
| [UserController.java](/C:/Users/kafle/Leave_Management_Backend/src/main/java/com/leavemanage/controller/UserController.java) | employee actions |
| [ManagerController.java](/C:/Users/kafle/Leave_Management_Backend/src/main/java/com/leavemanage/controller/ManagerController.java) | manager actions |
| [UserService.java](/C:/Users/kafle/Leave_Management_Backend/src/main/java/com/leavemanage/service/UserService.java) | leave workflow and notifications |
| [DataInitializer.java](/C:/Users/kafle/Leave_Management_Backend/src/main/java/com/leavemanage/config/DataInitializer.java) | default manager seeding |

## Local Setup

### Prerequisites

- Java 21
- Maven Wrapper included in repo
- PostgreSQL database

### 1. Configure Environment

The app loads values from `.env` through:

```properties
spring.config.import=optional:file:.env[.properties]
```

Currently used from `.env`:

```env
PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/leaveflow_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
```

Important: in the current codebase, mail credentials and JWT settings are still defined directly in `application.properties`, not read from environment variables yet.

### 2. Start the Application

```powershell
.\mvnw.cmd spring-boot:run
```

### 3. Run Tests

```powershell
.\mvnw.cmd test
```

## API Documentation

Once the app is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Authentication Example

### Login Request

```json
{
  "email": "john@example.com",
  "password": "StrongPassword123"
}
```

### Login Response

```json
{
  "token": "your-jwt-token",
  "email": "john@example.com",
  "roles": ["USER"],
  "expiresAt": "2026-02-25T10:30:00"
}
```

Use the token in requests:

```http
Authorization: Bearer <your-token>
```

## Default Seeded Managers

On startup, the app seeds manager accounts if no manager exists in the database. This is handled in [DataInitializer.java](/C:/Users/kafle/Leave_Management_Backend/src/main/java/com/leavemanage/config/DataInitializer.java).

For real deployments, replace seeded credentials and move all secrets out of source control.

## Security Notes

The current repository contains sensitive configuration values in committed files. Before using this project in any shared or production environment:

1. rotate database, mail, and JWT secrets
2. move credentials to environment variables or secret storage
3. avoid committing `.env` and runtime secrets
4. review CORS policy and narrow allowed origins

## Testing

Test configuration uses H2 in-memory database via:

- [application-test.properties](/C:/Users/kafle/Leave_Management_Backend/src/test/resources/application-test.properties)

This makes tests independent from the production PostgreSQL database.

## What Makes This Backend Easy to Extend

- clean package split between controller, service, repository, and model
- DTOs already exist for major request and response flows
- OpenAPI annotations are already added across controllers
- role-based route protection is centralized in security config
- filtering endpoints already support dashboard-style reporting

## Improvement Ideas

- replace hardcoded secrets with environment-driven config
- align `leaveType` persistence with enum usage
- add role-based tests for secured endpoints
- add Docker support for easier onboarding
- introduce Flyway or Liquibase for schema migration
- add refresh tokens or token revocation strategy

## Summary

This project is a solid foundation for a leave management platform: authentication, user self-service, manager approvals, reporting, and notifications are already in place. If you are new to the codebase, start with the controllers, then `UserService`, then `SecurityConfig` to understand the full request flow quickly.
