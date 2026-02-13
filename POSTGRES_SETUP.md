# PostgreSQL Setup Instructions

## Issue
Backend cannot start because PostgreSQL database is not running.

## Error
```
Connection refused: getsockopt
Unable to connect to postgresql://localhost:5432/ai_study_summarizer
```

## Solutions

### Option 1: Start PostgreSQL if Already Installed

**Check if PostgreSQL is installed:**
```powershell
# Check for PostgreSQL installation
Get-ChildItem "C:\Program Files\PostgreSQL" -ErrorAction SilentlyContinue

# Or check for pg_ctl
where.exe pg_ctl
```

**If installed, start the service:**
```powershell
# Find the service name
Get-Service | Where-Object {$_.DisplayName -like '*PostgreSQL*'}

# Start the service (replace with actual service name)
Start-Service -Name "postgresql-x64-<version>"
```

---

### Option 2: Use Docker (Recommended for Development)

**1. Create docker-compose.yml in project root:**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:17-alpine
    container_name: ai-study-summarizer-db
    environment:
      POSTGRES_DB: ai_study_summarizer
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: examly
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

**2. Start PostgreSQL:**
```powershell
docker-compose up -d
```

**3. Verify it's running:**
```powershell
docker ps
```

**4. Stop when done:**
```powershell
docker-compose down
```

---

### Option 3: Install PostgreSQL

**Download and install:**
1. Go to https://www.postgresql.org/download/windows/
2. Download PostgreSQL 17 installer
3. Run installer with these settings:
   - Password: `examly`
   - Port: `5432`
   - Locale: Default

**After installation:**
```powershell
# Start PostgreSQL service
Start-Service postgresql-x64-17

# Create database
psql -U postgres
CREATE DATABASE ai_study_summarizer;
\q
```

---

## After PostgreSQL is Running

**Start the backend:**
```powershell
cd backend
./mvnw spring-boot:run
```

**Expected output:**
```
Started AiStudySummarizerApplication in X.XXX seconds
Tomcat started on port 8080
```

---

## Quick Test

**Test database connection:**
```powershell
# Using psql
psql -U postgres -h localhost -p 5432 -d ai_study_summarizer

# Or using Docker
docker exec -it ai-study-summarizer-db psql -U postgres -d ai_study_summarizer
```
