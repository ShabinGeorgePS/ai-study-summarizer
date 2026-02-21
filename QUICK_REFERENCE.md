# Quick Reference - Refactoring Changes

## 🎯 What Changed & Why

### Problem: Generic Errors
```java
// OLD - User gets confused
throw new RuntimeException("not found");

// NEW - Clear, actionable
throw new EntityNotFoundException("Document", docId);
// Response: 404 JSON with timestamp and message
```

### Problem: Inconsistent API Responses
```javascript
// OLD - Different format on each endpoint
{message: "Error"}
{error: "Something went wrong"}

// NEW - Consistent everywhere
{
  success: false,
  message: "Error details",
  timestamp: "2025-02-21T12:00:00"
}
```

### Problem: No Input Validation
```java
// OLD - Accepts any file
documentService.upload(file);

// NEW - Validates everything
validationService.validateFile(file);     // Size, type, content
validationService.validateTitle(title);   // Length, content
```

### Problem: One failure = app breaks
```java
// OLD - Single API failure means fail
String summary = geminiService.generateSummary(text);

// NEW - Auto-retry with smart backoff
String summary = RetryUtil.executeWithRetry(
    () -> geminiService.generateSummary(text),
    "Summary Generation"
);
// Tries 3 times with 1s, 2s, 4s delays
```

### Problem: Loading 10,000 summaries at once
```java
// OLD - Get all summaries (crash if too many)
List<SummaryResponse> getUserSummaries()

// NEW - Get 20 at a time with pagination
Page<SummaryResponse> getUserSummaries(Pageable)
// Load page 1, page 2, etc. as needed
```

---

## 📍 New Backend Architecture

```
API Request
    ↓
Controller (validates with @Valid)
    ↓
DocumentValidationService (extra checks)
    ↓
Service (business logic)
    ↓
Repository (database)
    ↓
GlobalExceptionHandler (catches + standardizes errors)
    ↓
API Response (ApiResponse<T> wrapper)
```

---

## 🔑 Key Files to Know

### Must Understand
1. **`ApiResponse.java`** - Every API response uses this
2. **`SummaryService.java`** - Core business logic
3. **`GlobalExceptionHandler.java`** - How errors are handled
4. **`DocumentValidationService.java`** - Security checks

### Should Review
5. **Controllers** - Now use v1 API paths and return ApiResponse
6. **DTOs** - Request validation annotations
7. **Exception classes** - Each has specific HTTP status code

---

## 🔄 API Changes

### Endpoints Changed

```bash
# SUMMARY ENDPOINTS
OLD: POST /api/summarize/{documentId}
NEW: POST /api/v1/summaries/generate

OLD: GET /api/summaries
NEW: GET /api/v1/summaries?page=0&size=20

OLD: POST /api/summaries/{id}/generate-more/mcqs
NEW: POST /api/v1/summaries/{id}/mcqs

# DOCUMENT ENDPOINTS
OLD: POST /api/documents/upload
NEW: POST /api/v1/documents/upload

OLD: POST /api/documents/url
NEW: POST /api/v1/documents/from-url
```

---

## 💻 Code Examples

### Creating Exception (Old vs New)

```java
// OLD
throw new RuntimeException("User not found");

// NEW - Use specific exception
throw new EntityNotFoundException("User", userId.toString());
```

### Making API Call (Frontend)

```javascript
// OLD - Direct axios call in component
const response = await axios.post('/api/summaries/generate', {...});

// NEW - Use API service
import { summarizerApi } from '../api/summarizer';
const response = await summarizerApi.generate(docId, options);
```

### Handling File Upload (Old vs New)

```java
// OLD - No validation
public void upload(MultipartFile file) {...}

// NEW - Comprehensive validation
validationService.validateFile(file);          // Size + type
validationService.validateTitle(title);        // Required + length
documentService.uploadDocument(file, title);   // Process
```

---

## ✅ Testing Your Changes

### Test 1: Exception Handling
```bash
# Upload oversized file
curl -F "file=@huge.pdf" http://localhost:8080/api/v1/documents/upload

# Response should be:
{
  "success": false,
  "message": "File size 100000000 bytes exceeds maximum 52428800",
  "timestamp": "2025-02-21T12:00:00"
}
```

### Test 2: Pagination
```bash
# Get summaries page 2
curl "http://localhost:8080/api/v1/summaries?page=1&size=20"

# Response includes pagination metadata
{
  "success": true,
  "data": {
    "content": [...],
    "number": 1,
    "totalPages": 5,
    "totalElements": 100
  }
}
```

### Test 3: API Retry
```bash
# Generate summary (will retry if API fails)
curl -X POST http://localhost:8080/api/v1/summaries/generate \
  -d '{"documentId": "...", "mcqCount": 5}'

# If API fails, retries automatically
# Max 3 attempts with 1s, 2s, 4s delays
```

---

## 📦 Dependencies to Add

```xml
<!-- In pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

---

## ⚙️ Configuration to Add

```yaml
# In application.yml
spring:
  cache:
    type: simple
    cache-names:
      - userSummaries
      - summaryById

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## 🚀 Quick Implementation Checklist

### Backend (3-5 hours)
- [ ] Add Maven dependencies
- [ ] Update application.yml
- [ ] Create AsyncConfig.java
- [ ] Add database indexes
- [ ] Test `./mvnw clean package`
- [ ] Verify `/swagger-ui.html` loads

### Frontend (5-8 hours)
- [ ] Create API layer (client.js, summarizer.js, documents.js)
- [ ] Create components (ErrorAlert, LoadingOverlay, FileUploader, Toast)
- [ ] Create hooks (useApi, useToast)
- [ ] Update pages to use new API
- [ ] Test file upload with progress
- [ ] Test error messages

### Testing (2-3 hours)
- [ ] Test each API endpoint
- [ ] Test error scenarios
- [ ] Test file validation
- [ ] Test pagination
- [ ] Test on mobile

---

## 🆘 Common Issues & Fixes

### Issue: Backend won't compile
```
Error: NoClassDefFoundError: ISummaryService
→ Solution: Make sure all new classes are in correct packages
→ Run: ./mvnw clean install
```

### Issue: API returns 404
```
Error: 404 on /api/v1/summaries
→ Solution: Check endpoint path (was /api/summaries, now /api/v1/summaries)
→ Check server is running on :8080
```

### Issue: File upload fails with validation error
```
Error: "File size exceeds maximum allowed"
→ Solution: File must be < 50MB
→ Only PDF files supported
```

### Issue: Frontend API calls fail
```
Error: CORS error
→ Solution: Check CORS_ALLOWED_ORIGINS in backend .env
→ Verify frontend URL is in allowed list
```

---

## 📊 What You Get

### Reliability 📈
- Automatic retry on API failures (3 attempts)
- Chunking for large documents
- Graceful error handling

### Security 🔒
- File type validation
- File size validation (50MB)
- Path traversal prevention
- Ownership validation

### Performance ⚡
- Pagination for large lists
- Response caching
- Async background processing

### Maintainability 💻
- Type-safe exceptions
- Consistent API responses
- Clean layer separation
- Comprehensive documentation

---

## 📞 Where to Find Help

| Question | Answer Location |
|----------|-----------------|
| How does error handling work? | BACKEND_REFACTORING_GUIDE.md |
| How does pagination work? | BACKEND_REFACTORING_GUIDE.md |
| How do I update the frontend? | FRONTEND_REFACTORING_GUIDE.md |
| What are all the steps? | IMPLEMENTATION_PLAN.md |
| Code review findings? | TECHNICAL_AUDIT.md |
| How do I contribute? | CONTRIBUTING.md |

---

## 🎯 Bottom Line

**Before:** Generic errors, inconsistent responses, no validation, single-point failures

**After:** Specific errors, standardized responses, comprehensive validation, automatic retry

**Result:** More reliable, secure, and maintainable application

---

**Status**: Ready to implement remaining phases ✅

Next: See IMPLEMENTATION_PLAN.md for step-by-step instructions
