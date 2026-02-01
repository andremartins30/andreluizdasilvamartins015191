import axios from 'axios';

export const api = axios.create({
    baseURL: 'http://64.23.178.251:8080/api/v1',
});