import { api } from './axios';

export interface Regional {
    id: number;
    nome: string;
    ativo: boolean;
}

export interface RegionalExterna {
    id: number;
    nome: string;
}

export interface SincronizacaoResult {
    inseridos: number;
    atualizados: number;
    inativados: number;
    mensagem: string;
}

const API_EXTERNA_URL = 'https://integrador-argus-api.geia.vip/v1/regionais';

export function getRegionais() {
    return api.get<Regional[]>('/regionais');
}

export function getRegionaisAtivas() {
    return api.get<Regional[]>('/regionais/ativas');
}

export async function sincronizarRegionais() {
    try {
        // Tenta sincronizar diretamente (se o backend conseguir acessar a API)
        return await api.post<SincronizacaoResult>('/regionais/sincronizar');
    } catch (error: any) {
        // Se falhar, busca do cliente e envia para importação
        if (error?.response?.status === 500) {
            console.log('Backend não conseguiu acessar API externa. Buscando do cliente...');

            const response = await fetch(API_EXTERNA_URL);
            if (!response.ok) {
                throw new Error('Erro ao buscar regionais da API externa');
            }

            const regionaisExternas: RegionalExterna[] = await response.json();
            return await api.post<SincronizacaoResult>('/regionais/importar', regionaisExternas);
        }
        throw error;
    }
}
