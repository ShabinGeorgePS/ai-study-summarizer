# AI Study Summarizer - Integration Guide

## Overview

This document provides instructions for setting up and running the AI Study Summarizer application with proper frontend-backend integration.

## Prerequisites

- **Backend:** Java 21, PostgreSQL, OpenAI API key
- **Frontend:** Node.js 18+, npm

## Backend Setup

### 1. Database Configuration

Create a PostgreSQL database:
```sql
CREATE DATABASE ai_study_summarizer;
```

### 2. Application Properties

Create `backend/src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/ai_study_summarizer
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your-secret-key-here-minimum-256-bits-for-hs256-algorithm
jwt.expiration=86400000

# OpenAI Configuration
openai.api.key=your-openai-api-key-here
openai.model=gpt-4

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

> **Important:** Replace placeholder values with your actual credentials.

### 3. Run Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend will start on `http://localhost:8080`

## Frontend Setup

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Environment Configuration

The `.env` file is already created with default values:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=30000
VITE_ENABLE_API_LOGGING=true
```

For production, create `.env.production`:

```env
VITE_API_BASE_URL=https://your-production-api.com/api
VITE_API_TIMEOUT=30000
VITE_ENABLE_API_LOGGING=false
```

### 3. Run Frontend

```bash
npm run dev
```

Frontend will start on `http://localhost:5173`

## API Endpoints

### Authentication

- **POST** `/api/auth/register` - Register new user
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
  Response: `"User registered successfully"`

- **POST** `/api/auth/login` - Login user
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
  Response:
  ```json
  {
    "accessToken": "jwt-token-here",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "uuid",
      "email": "user@example.com"
    }
  }
  ```

### Documents

- **POST** `/api/documents/upload` - Upload PDF (multipart/form-data)
  - Form field: `file` (PDF file)
  - Form field: `title` (optional)
  
- **POST** `/api/documents/url` - Process URL
  ```json
  {
    "url": "https://example.com/document.pdf",
    "title": "Optional title"
  }
  ```

### Summaries

- **POST** `/api/summarize/{documentId}` - Generate summary
- **GET** `/api/summaries` - Get all user summaries
- **GET** `/api/summaries/{id}` - Get specific summary

## Authentication Flow

1. User registers via `/register` page
2. Success message shown, redirected to `/login`
3. User logs in via `/login` page
4. JWT token stored in localStorage with expiration
5. Token automatically attached to all API requests
6. On token expiration, user automatically logged out

## Error Handling

The application uses centralized error handling:

- **Network errors:** Automatic retry (3 attempts)
- **Validation errors:** Field-specific error messages
- **401 Unauthorized:** Automatic logout and redirect to login
- **Other errors:** User-friendly error messages

## CORS Configuration

Backend allows requests from:
- `http://localhost:5173` (Vite dev server)
- `http://localhost:3000` (Alternative React port)
- `http://localhost:4173` (Vite preview)

For production, update `CorsConfig.java` to include your production frontend URL.

## Testing the Integration

### 1. Test Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### 2. Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### 3. Test Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/summaries \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Common Issues

### CORS Errors
- Ensure backend is running on port 8080
- Check CORS configuration includes your frontend URL
- Verify `setAllowCredentials(true)` is set

### 401 Unauthorized
- Check JWT secret is properly configured
- Verify token is being sent in Authorization header
- Check token hasn't expired

### Network Errors
- Ensure both backend and frontend are running
- Check firewall settings
- Verify API base URL in `.env`

## Production Deployment

### Backend
1. Update `application.properties` with production database
2. Set secure JWT secret (minimum 256 bits)
3. Configure production OpenAI API key
4. Update CORS to allow production frontend URL
5. Build: `./mvnw clean package`
6. Run: `java -jar target/ai-study-summarizer.jar`

### Frontend
1. Create `.env.production` with production API URL
2. Build: `npm run build`
3. Deploy `dist` folder to hosting service (Vercel, Netlify, etc.)

## Security Considerations

- Never commit `.env` files to version control
- Use strong JWT secrets in production
- Enable HTTPS in production
- Rotate JWT secrets periodically
- Implement rate limiting for API endpoints
- Validate file uploads (size, type, content)
