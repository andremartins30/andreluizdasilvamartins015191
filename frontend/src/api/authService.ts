import { api } from './axios';
import type { LoginRequest, AuthResponse, RegisterRequest } from '../types/auth';

export async function login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/login', data);
    return response.data.data;
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/register', data);
    return response.data.data;
}