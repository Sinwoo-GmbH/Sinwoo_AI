# SINWOO Development Environment Setup Guide

## 1. Purpose

This document provides the standard onboarding procedure for a new developer to run the SINWOO project locally.

## 2. Project Overview

- Project name: `Sinwoo AI`
- Backend: Spring Boot 3.3.x / Java 21
- Frontend: Next.js / Tailwind CSS / shadcn/ui-style structure
- Database: MariaDB
- Supporting services: Redis, MinIO
- Migration tool: Flyway
- DB naming: abbreviation-based standard

## 3. Required Software

Install the following tools before starting:

- Git
- Java 21
- IntelliJ IDEA
- Node.js LTS
- Docker Desktop
- HeidiSQL

## 4. Repository

Repository root:

```text
C:\Users\JuyongLee\Sinwoo_AI
```

Clone command:

```bash
git clone https://github.com/Sinwoo-GmbH/Sinwoo_AI.git
cd Sinwoo_AI
```

## 5. Local Infrastructure

Start local containers:

```bash
docker compose up -d
```

Expected containers:

- `sinwoo-mariadb`
- `sinwoo-redis`
- `sinwoo-minio`

## 6. Database Connection

HeidiSQL connection settings:

- Host: `127.0.0.1`
- Port: `3306`
- User: `sinwoo`
- Password: `sinwoo`
- Database: `sinwoo`

## 6.1 Local DB Reset

Because the bootstrap schema is managed by Flyway from the initial migration, if migration history or schema state becomes inconsistent during early development, reset the local database volume and recreate the containers.

Recommended local reset flow:

```bash
docker compose down -v
docker compose up -d
```

Then restart the backend so Flyway can rebuild the schema from the current `V1__init_schema.sql`.

## 7. Backend Setup

### 7.1 IntelliJ Project Settings

- Project SDK: Java 21
- Project language level: 21
- Gradle JVM: Java 21
- Bytecode target: 21

### 7.2 Backend Run

Main application:

[`SinwooBackendApplication.java`](C:\Users\JuyongLee\Sinwoo_AI\src\main\java\com\sinwoo\SinwooBackendApplication.java)

Run options:

- IntelliJ run button
- Or Gradle wrapper

Command:

```bash
.\gradlew.bat bootRun
```

### 7.3 Backend Validation

Health check:

```text
http://localhost:8080/actuator/health
```

Ping endpoint:

```text
http://localhost:8080/api/v1/system/ping
```

Expected ping response:

```json
{
  "timestamp": "2026-04-04T06:12:34.407495300Z",
  "status": "ok"
}
```

## 8. Frontend Setup

Frontend root:

```text
C:\Users\JuyongLee\Sinwoo_AI\frontend
```

Install packages:

```bash
cd frontend
npm install
```

Run frontend:

```bash
npm run dev
```

Frontend URL:

```text
http://localhost:3000
```

## 9. IntelliJ Frontend Run Configuration

If npm configuration is available in IntelliJ:

- Name: `frontend-dev`
- Package file: [`frontend/package.json`](C:\Users\JuyongLee\Sinwoo_AI\frontend\package.json)
- Command: `run`
- Script: `dev`

If npm configuration is not available, use IntelliJ Terminal:

```bash
cd frontend
npm run dev
```

## 10. Common Issues and Fixes

### 10.1 Node.js not found

Symptom:

```text
node: command not found
```

Action:

- Install Node.js LTS
- Restart terminal

### 10.2 Gradle not found

Symptom:

```text
gradle: command not found
```

Action:

- Use `gradlew.bat`

### 10.3 Java version mismatch

Symptom:

- Java 17 detected
- Application configured for Java 21

Action:

- Install Java 21
- Set IntelliJ SDK and Gradle JVM to Java 21

### 10.4 Docker engine not running

Symptom:

```text
failed to connect to the docker API
```

Action:

- Start Docker Desktop
- Wait for engine ready status

### 10.5 Virtualization not detected

Symptom:

```text
Virtualization support not detected
```

Action:

- Enable CPU virtualization in BIOS
- Enable WSL2 / Virtual Machine Platform

### 10.6 Port 8080 already in use

Action:

- Stop previous backend process
- Restart application from a single runtime only

## 11. Current Standard Runtime Ports

- Backend: `8080`
- Frontend: `3000`
- MariaDB: `3306`
- Redis: `6379`
- MinIO API: `9000`
- MinIO Console: `9001`

## 12. Current Status

Current local environment has been verified for:

- Backend startup
- Frontend startup
- MariaDB connectivity
- Docker-based infrastructure
- IntelliJ integration

## 13. Change Management

Any developer changing setup steps or required tools must update this document.
