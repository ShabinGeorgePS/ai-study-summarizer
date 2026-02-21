import api from './api';
import { parseApiError } from '../utils/errorHandler';

export const documentService = {
    // Upload PDF file
    uploadPDF: async (file, title = null) => {
        try {
            const formData = new FormData();
            formData.append('file', file);

            if (title) {
                formData.append('title', title);
            } else {
                let defaultTitle = file.name;
                const lastDot = defaultTitle.lastIndexOf('.');
                if (lastDot > 0) {
                    defaultTitle = defaultTitle.substring(0, lastDot);
                }
                formData.append('title', defaultTitle);
            }

            const response = await api.post('/v1/documents/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return response.data.data || response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },

    // Upload document via URL
    uploadURL: async (url, title = null) => {
        try {
            const payload = { url };
            if (title) {
                payload.title = title;
            }

            const response = await api.post('/v1/documents/from-url', payload);
            return response.data.data || response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },

    // Summarize document with MCQ count
    summarizeDocument: async (documentId, mcqCount = 5, summaryMode = 'detailed', bulletPointCount = 10) => {
        try {
            const response = await api.post(`/v1/summaries/generate`, {
                documentId,
                mcqCount,
                summaryMode,
                bulletPointCount
            });
            return response.data.data || response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },
};
