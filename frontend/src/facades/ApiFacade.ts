import * as artistService from '../api/artistService';
import * as albumService from '../api/albumService';
import * as authService from '../api/authService';
import { artistStore } from '../store/artistStore';
import { albumStore } from '../store/albumStore';
import toast from 'react-hot-toast';

class ApiFacade {
    // Artist operations
    async getArtists(params: Parameters<typeof artistService.getArtists>[0]) {
        try {
            await artistStore.loadArtists(params);
            return { success: true };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao carregar artistas';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async createArtist(data: Parameters<typeof artistService.createArtist>[0]) {
        try {
            const response = await artistService.createArtist(data);
            toast.success('Artista criado com sucesso!');
            return { success: true, data: response.data };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao criar artista';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async updateArtist(id: number, data: Parameters<typeof artistService.updateArtist>[1]) {
        try {
            const response = await artistService.updateArtist(id, data);
            toast.success('Artista atualizado com sucesso!');
            return { success: true, data: response.data };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao atualizar artista';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async deleteArtist(id: number) {
        try {
            await artistService.deleteArtist(id);
            toast.success('Artista excluído com sucesso!');
            return { success: true };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao excluir artista';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async getArtistById(id: number) {
        try {
            const response = await artistService.getArtistById(id);
            return { success: true, data: response.data };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao carregar artista';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    // Album operations
    async getAlbums(params: Parameters<typeof albumService.getAlbums>[0]) {
        try {
            await albumStore.loadAlbums(params);
            return { success: true };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao carregar álbuns';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async createAlbum(data: Parameters<typeof albumService.createAlbum>[0]) {
        try {
            const response = await albumService.createAlbum(data);
            toast.success('Álbum criado com sucesso!');
            return { success: true, data: response.data };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao criar álbum';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async updateAlbum(id: number, data: Parameters<typeof albumService.updateAlbum>[1]) {
        try {
            const response = await albumService.updateAlbum(id, data);
            toast.success('Álbum atualizado com sucesso!');
            return { success: true, data: response.data };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao atualizar álbum';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async deleteAlbum(id: number) {
        try {
            await albumService.deleteAlbum(id);
            albumStore.removeAlbum(id);
            toast.success('Álbum excluído com sucesso!');
            return { success: true };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao excluir álbum';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async uploadAlbumCover(id: number, file: File) {
        try {
            const response = await albumService.uploadAlbumCover(id, file);
            toast.success('Capa enviada com sucesso!');
            return { success: true, data: response.data };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao enviar capa';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async getAlbumCoverUrl(id: number) {
        try {
            const response = await albumService.getAlbumCoverUrl(id);
            return { success: true, data: response.data };
        } catch (error: any) {
            return { success: false, error: 'Sem capa' };
        }
    }

    // Auth operations
    async login(credentials: Parameters<typeof authService.login>[0]) {
        try {
            const response = await authService.login(credentials);
            toast.success('Login realizado com sucesso!');
            return { success: true, data: response };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao fazer login';
            toast.error(message);
            return { success: false, error: message };
        }
    }

    async register(credentials: Parameters<typeof authService.register>[0]) {
        try {
            const response = await authService.register(credentials);
            toast.success('Cadastro realizado com sucesso!');
            return { success: true, data: response };
        } catch (error: any) {
            const message = error?.response?.data?.message || 'Erro ao registrar';
            toast.error(message);
            return { success: false, error: message };
        }
    }
}

export const apiFacade = new ApiFacade();
