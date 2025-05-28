import axios from 'axios';

const API_URL = 'http://localhost:8085/api/v1/category';

export const getCategories = async () => {
    try {
        const response = await axios.get(API_URL);
        return response.data;
    } catch (error) {
        console.error('Error fetching categories:', error);
        throw error;
    }
};