import { api } from './axios';

export interface Album {
    id: number;
    title: string;
    artist: {
        id: number;
        name: string;
    };
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

export function getAlbumCoverUrl(id: number) {
    return api.get<{ url: string }>(`/albums/${id}/cover-url`);
}
