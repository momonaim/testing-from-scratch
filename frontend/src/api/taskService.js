import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/tasks';

export const taskService = {
    getAll: () => axios.get(API_BASE_URL).then(res => res.data),

    getById: (id) => axios.get(`${API_BASE_URL}/${id}`).then(res => res.data),

    create: (task) => axios.post(API_BASE_URL, task).then(res => res.data),

    update: (id, task) => axios.put(`${API_BASE_URL}/${id}`, task).then(res => res.data),

    delete: (id) => axios.delete(`${API_BASE_URL}/${id}`),
};
