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
            }

            const response = await api.post('/documents/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return response.data;
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

            const response = await api.post('/documents/url', payload);
            return response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },

    // Summarize document with MCQ count
    summarizeDocument: async (documentId, mcqCount = 5) => {
        try {
            const response = await api.post(`/summarize/${documentId}`, { mcqCount });
            return response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },
};
