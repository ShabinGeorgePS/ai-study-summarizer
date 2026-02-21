import api from './api';
import { parseApiError } from '../utils/errorHandler';

export const summaryService = {
    // Get all summaries for the current user
    getAllSummaries: async () => {
        try {
            const response = await api.get('/v1/summaries');
            const data = response.data.data || response.data;
            return data.content || data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },

    // Get a specific summary by ID
    getSummaryById: async (id) => {
        try {
            const response = await api.get(`/v1/summaries/${id}`);
            return response.data.data || response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },
};
