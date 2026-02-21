# Production-Grade Frontend Refactoring - Implementation Guide

## 📋 Overview

This guide provides step-by-step instructions for refactoring the React frontend to work with the new backend API and implement production-ready features.

---

## 🏗 Folder Structure Reorganization

### Current Structure → New Structure

```
frontend/src/
├── App.jsx
├── main.jsx
├── index.css
│
├── api/                          ← NEW
│   ├── client.js                 ← Axios configuration
│   ├── summarizer.js             ← Summary API calls
│   ├── documents.js              ← Document upload API
│   └── auth.js                   ← Authentication API
│
├── components/
│   ├── common/
│   │   ├── Button.jsx
│   │   ├── Card.jsx
│   │   ├── Input.jsx
│   │   ├── ErrorAlert.jsx        ← NEW/IMPROVED
│   │   ├── LoadingSpinner.jsx    ← IMPROVED
│   │   └── Skeleton.jsx          ← NEW
│   │
│   ├── forms/                    ← NEW
│   │   ├── FileUploader.jsx
│   │   └── UrlForm.jsx
│   │
│   ├── layout/                   ← NEW
│   │   ├── Header.jsx
│   │   ├── Sidebar.jsx
│   │   └── Footer.jsx
│   │
│   └── summary/                  ← NEW
│       ├── SummaryCard.jsx
│       ├── ModeSelector.jsx
│       └── SummaryExport.jsx
│
├── pages/
│   ├── Login.jsx
│   ├── Register.jsx
│   ├── ForgotPassword.jsx
│   ├── ResetPassword.jsx
│   ├── Dashboard.jsx             ← REFACTORED
│   ├── Upload.jsx                ← REFACTORED
│   └── Results.jsx               ← REFACTORED
│
├── context/
│   └── AuthContext.jsx
│
├── hooks/                        ← NEW
│   ├── useApi.js
│   ├── usePagination.js
│   └── useToast.js
│
├── types/                        ← NEW
│   └── index.js
│
├── styles/
│   ├── index.css                 ← Global styles
│   ├── components.css             ← Component styles
│   └── animations.css             ← Animation utilities
│
├── utils/
│   ├── errorHandler.js           ← IMPROVED
│   ├── validators.js
│   ├── tokenStorage.js
│   └── constants.js
│
├── constants/                    ← NEW
│   ├── api.js
│   ├── config.js
│   └── messages.js
│
└── config/                       ← NEW
    └── api.config.js
```

---

## 📦 Phase 1: API Layer (`frontend/src/api/`)

### 1. Create `client.js` - Axios Configuration

```javascript
// frontend/src/api/client.js
import axios from 'axios';
import { tokenStorage } from '../utils/tokenStorage';

const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
    timeout: import.meta.env.VITE_API_TIMEOUT || 30000,
});

// Request interceptor - Add auth token
client.interceptors.request.use(
    (config) => {
        const token = tokenStorage.getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor - Handle 401 and errors
client.interceptors.response.use(
    (response) => response.data,
    (error) => {
        if (error.response?.status === 401) {
            tokenStorage.clearAll();
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default client;
```

### 2. Create `summarizer.js` - Summary API

```javascript
// frontend/src/api/summarizer.js
import client from './client';

const ENDPOINTS = {
    GENERATE: '/summaries/generate',
    LIST: '/summaries',
    GET_ONE: (id) => `/summaries/${id}`,
    MCQ: (id) => `/summaries/${id}/mcqs`,
    FLASHCARDS: (id) => `/summaries/${id}/flashcards`,
    CONTENT: (id) => `/summaries/${id}/content`,
    DELETE: (id) => `/summaries/${id}`,
};

export const summarizerApi = {
    /**
     * Generate a new summary
     */
    generate: async (documentId, options = {}) => {
        return client.post(ENDPOINTS.GENERATE, {
            documentId,
            mcqCount: options.mcqCount || 5,
            summaryMode: options.mode || 'detailed',
            bulletPointCount: options.bulletPoints || 10,
        });
    },

    /**
     * Get user's summaries with pagination
     */
    list: async (page = 0, size = 20, sortBy = 'createdAt', direction = 'DESC') => {
        return client.get(ENDPOINTS.LIST, {
            params: { page, size, sortBy, direction },
        });
    },

    /**
     * Get specific summary
     */
    getOne: async (id) => {
        return client.get(ENDPOINTS.GET_ONE(id));
    },

    /**
     * Generate more MCQs
     */
    generateMoreMcqs: async (id) => {
        return client.post(ENDPOINTS.MCQ(id));
    },

    /**
     * Generate more flashcards
     */
    generateMoreFlashcards: async (id) => {
        return client.post(ENDPOINTS.FLASHCARDS(id));
    },

    /**
     * Generate more content
     */
    generateMoreContent: async (id) => {
        return client.post(ENDPOINTS.CONTENT(id));
    },

    /**
     * Delete summary
     */
    delete: async (id) => {
        return client.delete(ENDPOINTS.DELETE(id));
    },
};
```

### 3. Create `documents.js` - Document Upload API

```javascript
// frontend/src/api/documents.js
import client from './client';

const ENDPOINTS = {
    UPLOAD: '/documents/upload',
    FROM_URL: '/documents/from-url',
};

export const documentsApi = {
    /**
     * Upload a file with progress tracking
     */
    upload: async (file, title, onProgress) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('title', title);

        return client.post(ENDPOINTS.UPLOAD, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
            onUploadProgress: (progressEvent) => {
                const percentCompleted = Math.round(
                    (progressEvent.loaded * 100) / progressEvent.total
                );
                onProgress?.(percentCompleted);
            },
        });
    },

    /**
     * Process URL content
     */
    fromUrl: async (url, title) => {
        return client.post(ENDPOINTS.FROM_URL, {
            url,
            title: title || url,
        });
    },
};
```

### 4. Create `auth.js` - Authentication API

```javascript
// frontend/src/api/auth.js
import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const authClient = axios.create({
    baseURL,
    timeout: import.meta.env.VITE_API_TIMEOUT || 30000,
});

export const authApi = {
    register: (email, password) =>
        authClient.post('/auth/register', { email, password }),

    login: (email, password) =>
        authClient.post('/auth/login', { email, password }),

    forgotPassword: (email) =>
        authClient.post('/auth/forgot-password', { email }),

    resetPassword: (token, newPassword) =>
        authClient.post('/auth/reset-password', { token, newPassword }),
};
```

---

## 🎨 Phase 2: Reusable Components

### 1. Create `ErrorAlert.jsx` - Error Display

```javascript
// frontend/src/components/common/ErrorAlert.jsx
import React from 'react';

const ErrorAlert = ({ message, onClose, type = 'error' }) => {
    if (!message) return null;

    const bgColor = type === 'error' ? 'bg-red-500' : 'bg-yellow-500';
    const borderColor = type === 'error' ? 'border-red-700' : 'border-yellow-700';
    const textColor = type === 'error' ? 'text-red-100' : 'text-yellow-100';

    return (
        <div className={`${bgColor} border-l-4 ${borderColor} p-4 mb-4 rounded`}>
            <div className="flex justify-between items-center">
                <p className={textColor}>{message}</p>
                {onClose && (
                    <button
                        onClick={onClose}
                        className={`${textColor} hover:opacity-75`}
                        aria-label="Close error"
                    >
                        ✕
                    </button>
                )}
            </div>
        </div>
    );
};

export default ErrorAlert;
```

### 2. Create `LoadingOverlay.jsx` - Full-Screen Loader

```javascript
// frontend/src/components/common/LoadingOverlay.jsx
import React from 'react';

const LoadingOverlay = ({ isVisible, message = 'Loading...' }) => {
    if (!isVisible) return null;

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-8 text-center max-w-sm">
                <div className="inline-block animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
                <p className="text-gray-700 font-medium">{message}</p>
            </div>
        </div>
    );
};

export default LoadingOverlay;
```

### 3. Create `FileUploader.jsx` - File Upload Component

```javascript
// frontend/src/components/forms/FileUploader.jsx
import React, { useState } from 'react';
import { documentsApi } from '../../api/documents';
import ErrorAlert from '../common/ErrorAlert';

const FileUploader = ({ onSuccess, onError }) => {
    const [file, setFile] = useState(null);
    const [title, setTitle] = useState('');
    const [loading, setLoading] = useState(false);
    const [progress, setProgress] = useState(0);
    const [error, setError] = useState('');

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        if (selectedFile) {
            // Validate file size (50MB max)
            if (selectedFile.size > 50 * 1024 * 1024) {
                setError('File size exceeds 50MB limit');
                return;
            }
            setFile(selectedFile);
            setError('');
            setTitle(selectedFile.name.split('.')[0]); // Auto-set title from filename
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file || !title.trim()) {
            setError('Please select a file and enter a title');
            return;
        }

        try {
            setLoading(true);
            const response = await documentsApi.upload(file, title, setProgress);
            onSuccess?.(response.data);
        } catch (err) {
            const message = err.response?.data?.message || 'Upload failed';
            setError(message);
            onError?.(err);
        } finally {
            setLoading(false);
            setProgress(0);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            {error && <ErrorAlert message={error} onClose={() => setError('')} />}

            <div>
                <label className="block text-sm font-medium mb-2">Document Title</label>
                <input
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="Enter document title"
                    className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    required
                />
            </div>

            <div className="border-2 border-dashed rounded-lg p-6 text-center cursor-pointer hover:border-blue-500">
                <input
                    type="file"
                    onChange={handleFileChange}
                    accept=".pdf"
                    className="hidden"
                    id="file-input"
                />
                <label htmlFor="file-input" className="cursor-pointer">
                    {file ? (
                        <p className="text-green-600 font-medium">{file.name}</p>
                    ) : (
                        <>
                            <p className="text-gray-600">Drop PDF file here or click to select</p>
                            <p className="text-sm text-gray-500 mt-1">Max size: 50MB</p>
                        </>
                    )}
                </label>
            </div>

            {progress > 0 && progress < 100 && (
                <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                        className="bg-blue-500 h-2 rounded-full transition-all"
                        style={{ width: `${progress}%` }}
                    />
                </div>
            )}

            <button
                type="submit"
                disabled={loading || !file}
                className="w-full bg-blue-600 text-white py-2 rounded-lg font-medium hover:bg-blue-700 disabled:bg-gray-400"
            >
                {loading ? `Uploading... ${progress}%` : 'Upload and Analyze'}
            </button>
        </form>
    );
};

export default FileUploader;
```

### 4. Create `Toast.jsx` - Notification System

```javascript
// frontend/src/components/common/Toast.jsx
import React, { useEffect } from 'react';

const Toast = ({ message, type = 'info', duration = 3000, onClose }) => {
    useEffect(() => {
        if (duration > 0) {
            const timer = setTimeout(onClose, duration);
            return () => clearTimeout(timer);
        }
    }, [duration, onClose]);

    const bgColor = {
        success: 'bg-green-500',
        error: 'bg-red-500',
        info: 'bg-blue-500',
        warning: 'bg-yellow-500',
    }[type];

    const icon = {
        success: '✓',
        error: '✕',
        info: 'ℹ',
        warning: '⚠',
    }[type];

    return (
        <div className={`fixed bottom-4 right-4 ${bgColor} text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-3 animate-slide-in`}>
            <span className="text-xl">{icon}</span>
            <p>{message}</p>
        </div>
    );
};

export default Toast;
```

---

## 🪝 Phase 3: Custom React Hooks

### 1. Create `useApi.js` - API Data Fetching

```javascript
// frontend/src/hooks/useApi.js
import { useState, useCallback } from 'react';

export const useApi = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const execute = useCallback(async (apiCall) => {
        try {
            setLoading(true);
            setError(null);
            const response = await apiCall();
            return response;
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || 'An error occurred';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    return { execute, loading, error, setError };
};
```

### 2. Create `useToast.js` - Toast Management

```javascript
// frontend/src/hooks/useToast.js
import { useState, useCallback } from 'react';

export const useToast = () => {
    const [toasts, setToasts] = useState([]);

    const show = useCallback((message, type = 'info', duration = 3000) => {
        const id = Date.now();
        setToasts((prev) => [...prev, { id, message, type, duration }]);

        if (duration > 0) {
            setTimeout(() => {
                setToasts((prev) => prev.filter((t) => t.id !== id));
            }, duration);
        }

        return id;
    }, []);

    const remove = useCallback((id) => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
    }, []);

    return { toasts, show, remove };
};
```

---

## 📄 Phase 4: Update Key Pages

### Update `Upload.jsx` to use new components:

```javascript
// frontend/src/pages/Upload.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import FileUploader from '../components/forms/FileUploader';
import ErrorAlert from '../components/common/ErrorAlert';
import { useApi } from '../hooks/useApi';
import { useToast } from '../hooks/useToast';

export default function Upload() {
    const navigate = useNavigate();
    const { show } = useToast();
    const { loading, error, setError } = useApi();

    const handleUploadSuccess = (data) => {
        show(`Document "${data.title}" uploaded successfully!`, 'success');
        navigate('/dashboard');
    };

    const handleUploadError = (err) => {
        show('Upload failed. Please try again.', 'error');
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 p-4">
            <div className="max-w-2xl mx-auto">
                <div className="bg-white rounded-lg shadow-lg p-8">
                    <h1 className="text-3xl font-bold mb-6">Upload Document</h1>
                    {error && <ErrorAlert message={error} onClose={() => setError('')} />}
                    <FileUploader
                        onSuccess={handleUploadSuccess}
                        onError={handleUploadError}
                    />
                </div>
            </div>
        </div>
    );
}
```

### Update `Dashboard.jsx` with pagination:

```javascript
// Key changes to Dashboard.jsx
const [page, setPage] = useState(0);
const [pageSize] = useState(20);
const { execute, loading } = useApi();

useEffect(() => {
    execute(async () => {
        const data = await summarizerApi.list(page, pageSize);
        setSummaries(data.content);
        setTotalPages(data.totalPages);
    });
}, [page, pageSize]);

// Add pagination controls
<div className="flex gap-2 mt-6">
    <button
        onClick={() => setPage(Math.max(0, page - 1))}
        disabled={page === 0}
    >
        Previous
    </button>
    <span>Page {page + 1} of {totalPages}</span>
    <button
        onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
        disabled={page >= totalPages - 1}
    >
        Next
    </button>
</div>
```

---

## 🔧 Configuration Files

### Create `frontend/src/constants/api.js`:
```javascript
export const API_ENDPOINTS = {
    SUMMARIES: '/summaries',
    DOCUMENTS: '/documents',
    AUTH: '/auth',
};

export const HTTP_STATUS = {
    OK: 200,
    CREATED: 201,
    BAD_REQUEST: 400,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    SERVER_ERROR: 500,
};
```

### Update `.env.example`:
```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_API_TIMEOUT=30000
VITE_ENABLE_API_LOGGING=true

# Feature Flags
VITE_ENABLE_DARK_MODE=true
VITE_ENABLE_EXPORT=true
```

---

## ✅ Testing Checklist

- [ ] File upload works with new API endpoint
- [ ] Error messages display correctly
- [ ] Progress bar shows during upload
- [ ] Pagination works on Dashboard
- [ ] Summary generation uses new endpoint
- [ ] Loading overlay appears during operations
- [ ] Toast notifications work
- [ ] API errors handled gracefully
- [ ] Token refresh works (if implemented)
- [ ] Responsive design on mobile

---

## 🎁 Additional Improvements (Optional but Recommended)

1. **Add offline support** - Service Worker caching
2. **Add dark mode** - Tailwind dark mode utilities
3. **Add keyboard shortcuts** - CMD+K for search, etc.
4. **Add export** - Export summaries as PDF
5. **Add sharing** - Share summarysummary via link
6. **Add history** - View past summaries and actions

---

**Status:** Frontend refactoring guide complete. Ready for implementation.
