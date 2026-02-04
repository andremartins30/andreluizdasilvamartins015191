import { api } from './axios';

export interface Album {
    id: number;
    title: string;
    artistId: number;
    artistName: string;
}

export interface AlbumPage {
    content: Album[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export interface AlbumRequest {
    title: string;
    artistId: number;
}

export function getAlbums(params: {
    page?: number;
    size?: number;
    artistName?: string;
    sort?: string;
}) {
    return api.get<AlbumPage>('/albums', {
        params: {
            page: params.page ?? 0,
            size: params.size ?? 10,
            name: params.artistName,
            sort: params.sort ?? 'title,asc'
        }
    });
}

export function getAlbumById(id: number) {
    return api.get<Album>(`/albums/${id}`);
}

export function createAlbum(data: AlbumRequest) {
    return api.post<Album>('/albums', data);
}

export function updateAlbum(id: number, data: AlbumRequest) {
    return api.put<Album>(`/albums/${id}`, data);
}

export function deleteAlbum(id: number) {
    return api.delete(`/albums/${id}`);
}

export function uploadAlbumCover(id: number, file: File) {
    const formData = new FormData();
    formData.append('file', file);

    return api.post(`/albums/${id}/cover`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

export function uploadAlbumCovers(id: number, files: File[]) {
    const formData = new FormData();
    files.forEach(file => {
        formData.append('files', file);
    });

    return api.post(`/albums/${id}/covers`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

export function getAlbumCoverUrl(id: number) {
    return api.get<{ url: string }>(`/albums/${id}/cover-url`);
}

export function getAlbumCoverUrls(id: number) {
    return api.get<string[]>(`/albums/${id}/cover-urls`);
}
