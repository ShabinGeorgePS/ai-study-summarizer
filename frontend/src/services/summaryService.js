import api from './api';
import { parseApiError } from '../utils/errorHandler';

export const summaryService = {
    // Get all summaries for the current user
    getAllSummaries: async () => {
        try {
            const response = await api.get('/summaries');
            return response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },

    // Get a specific summary by ID
    getSummaryById: async (id) => {
        try {
            const response = await api.get(`/summaries/${id}`);
            return response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },
};
