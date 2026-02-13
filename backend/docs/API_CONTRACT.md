# AI Study Summarizer – API Contract

## Overview

This document defines the database entity structure and REST API response contracts for the production AI Study Summarizer backend.

---

## 1. Database Entity Structure

### 1.1 User

| Column      | Type          | Constraints              | Description                    |
|-------------|---------------|--------------------------|--------------------------------|
| id          | UUID          | PK, generated            | Unique identifier              |
| email       | VARCHAR(255)  | UNIQUE, NOT NULL         | Login identifier               |
| password    | VARCHAR(255)  | NOT NULL                 | BCrypt-hashed password         |
| role        | VARCHAR(20)   | NOT NULL, default USER   | USER \| ADMIN                  |
| created_at  | TIMESTAMP     | NOT NULL                 | Account creation time          |
| updated_at  | TIMESTAMP     | NOT NULL                 | Last modification time         |

### 1.2 Document

| Column            | Type          | Constraints     | Description                          |
|-------------------|---------------|-----------------|--------------------------------------|
| id                | UUID          | PK              | Unique identifier                    |
| user_id           | UUID          | FK, NOT NULL    | Owner (users.id)                     |
| title             | VARCHAR(500)  | NOT NULL        | Display title                        |
| source_type       | VARCHAR(20)   | NOT NULL        | PDF \| IMAGE \| URL \| TEXT          |
| source_url        | VARCHAR(2048) | nullable        | Original URL when source_type=URL    |
| original_filename | VARCHAR(500)  | nullable        | Original file name for uploads       |
| file_size_bytes   | BIGINT        | nullable        | File size for uploads                |
| extracted_text    | TEXT          | NOT NULL        | Parsed content for summarization     |
| created_at        | TIMESTAMP     | NOT NULL        | Creation time                        |

### 1.3 Summary

| Column       | Type         | Constraints  | Description                          |
|--------------|--------------|--------------|--------------------------------------|
| id           | UUID         | PK           | Unique identifier                    |
| user_id      | UUID         | FK, NOT NULL | Owner (users.id)                     |
| document_id  | UUID         | FK, NOT NULL | Source document (documents.id)       |
| summary_json | JSONB        | NOT NULL     | Structured summary content           |
| model_used   | VARCHAR(50)  | NOT NULL     | OpenAI model identifier              |
| tokens_used  | INTEGER      | nullable     | Token count for cost tracking        |
| created_at   | TIMESTAMP    | NOT NULL     | Creation time                        |

### Summary JSONB Schema (OpenAI output)

```json
{
  "executiveSummary": "string",
  "sectionSummary": ["string"],
  "keyTerms": [
    { "term": "string", "definition": "string" }
  ],
  "mcqs": [
    {
      "question": "string",
      "options": ["string", "string", "string", "string"],
      "answer": "string",
      "explanation": "string"
    }
  ],
  "flashcards": [
    { "front": "string", "back": "string" }
  ],
  "examInsights": ["string"]
}
```

---

## 2. REST API Endpoints & Response Contracts

### 2.1 Auth

| Method | Endpoint             | Auth | Description           |
|--------|----------------------|------|-----------------------|
| POST   | /api/auth/register   | No   | Register new user     |
| POST   | /api/auth/login      | No   | Login, returns JWT    |

**Register Request**

```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Login Request**

```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Auth Response** (login)

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "email": "user@example.com"
  }
}
```

---

### 2.2 Documents

| Method | Endpoint             | Auth | Description                    |
|--------|----------------------|------|--------------------------------|
| POST   | /api/documents/upload| Yes  | Upload PDF/image, extract text |
| POST   | /api/documents/url   | Yes  | Scrape URL, create document    |

**Upload** – `multipart/form-data`

- `file` (required): PDF or image file
- `title` (optional): Custom title
- `sourceType` (optional): PDF \| IMAGE, default PDF

**URL Request**

```json
{
  "url": "https://example.com/article",
  "title": "Optional custom title"
}
```

**Document Upload Response**

```json
{
  "documentId": "uuid",
  "title": "string",
  "sourceType": "PDF",
  "sourceUrl": "https://...",
  "createdAt": "2025-02-13T12:00:00"
}
```

---

### 2.3 Summaries

| Method | Endpoint                  | Auth | Description                  |
|--------|---------------------------|------|------------------------------|
| POST   | /api/summarize/{documentId}| Yes  | Generate summary for document|
| GET    | /api/summaries            | Yes  | List user's summaries        |
| GET    | /api/summaries/{id}       | Yes  | Get single summary           |

**Summary Response** (single or in list)

```json
{
  "id": "uuid",
  "documentId": "uuid",
  "documentTitle": "string",
  "content": {
    "executiveSummary": "string",
    "sectionSummary": ["string"],
    "keyTerms": [
      { "term": "string", "definition": "string" }
    ],
    "mcqs": [
      {
        "question": "string",
        "options": ["string"],
        "answer": "string",
        "explanation": "string"
      }
    ],
    "flashcards": [
      { "front": "string", "back": "string" }
    ],
    "examInsights": ["string"]
  },
  "modelUsed": "gpt-3.5-turbo-0125",
  "tokensUsed": 1500,
  "createdAt": "2025-02-13T12:00:00"
}
```

**Summary List Response** (GET /api/summaries)

```json
[
  {
    "id": "uuid",
    "documentId": "uuid",
    "documentTitle": "string",
    "content": { ... },
    "modelUsed": "string",
    "tokensUsed": 1500,
    "createdAt": "2025-02-13T12:00:00"
  }
]
```

---

## 3. Error Response Format

```json
{
  "timestamp": "2025-02-13T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation error message",
  "path": "/api/endpoint"
}
```
