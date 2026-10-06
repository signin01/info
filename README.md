# 🛡️ InnSpark Monitor — Activity & Threat Intelligence Platform

An enterprise-grade **Login Activity & Threat Intelligence Security Operations Center (SOC)** built with **Spring Boot 3**, **Spring Security**, **JWT**, **Bucket4j**, and **MySQL 8**.

---

## 🌟 Key Features

1. **Real-Time Login Telemetry:** Ingress authentication tracking with IP resolution, timestamping, user-agent logging, and status auditing.
2. **Automated Anomaly Detection Engine:**
   - **Brute Force Detection:** Identifies &ge; 5 consecutive failures originating from a single IP within a sliding 10-minute window.
   - **Credential Stuffing Detection:** Flags multi-IP password sprays targeting specific accounts.
   - **Unusual Success Anomaly:** Alerts on successful logins immediately following repeated failed attempts.
3. **Token-Bucket Rate Limiting:** Enforces per-IP request throttling (5 req/min) returning `HTTP 429 Too Many Requests` on burst attacks.
4. **Enterprise SOC Dashboard:** Modern, high-contrast, responsive interface with live charts, threat telemetry feeds, interactive attack simulation lab, and flyout incident investigation drawers.
5. **Role-Based Access Control (RBAC):** Tiered permissions (`ADMIN`, `USER`, `SUPERADMIN`) with BCrypt password hashing.
6. **Session Management & Expiry:** Bulk session tracking with automatic background cleanup of inactive sessions.
7. **Two-Factor Authentication (2FA/TOTP):** Time-Based One-Time Password support compatible with Google Authenticator.

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8.0+

### 1. Database Setup
```sql
CREATE DATABASE login_monitor;
```

### 2. Configure Database
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/login_monitor?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Run Application
```bash
mvn spring-boot:run
```

Access the dashboard at:
👉 **`http://localhost:8080/dashboard`**

---

## 🐳 Docker Deployment
```bash
docker-compose up --build
```

---

## 🧪 REST API Reference

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new user | Public |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT token | Public |
| `POST` | `/api/login` | Ingest login attempt (Rate-limited) | Public |
| `GET` | `/api/login` | Filter login attempts (`?status=`, `?ip=`, `?username=`) | Admin |
| `GET` | `/api/suspicious` | Fetch detected suspicious events | Admin |
| `POST` | `/api/suspicious/detect` | Manually trigger detection heuristics | Admin |
| `GET` | `/api/dashboard/stats` | Aggregated telemetry statistics | Admin |
| `GET` | `/api/sessions` | View active user sessions | Admin |
| `DELETE` | `/api/sessions/{id}` | Terminate specific user session | Admin |

---

## 🔒 Mandatory Class
- `InnsparkLogin.java` — Core invariant class maintained in root package.
