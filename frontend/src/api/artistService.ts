import { api } from './axios';

export interface Artist {
    id: number;
    name: string;
    albumsCount: number;
}

export interface ArtistPage {
    content: Artist[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export interface ArtistRequest {
    name: string;
}

export function getArtists(params: {
    page?: number;
    size?: number;
    name?: string;
    sort?: string;
}) {
    return api.get<ArtistPage>('/artists', {
        params: {
            page: params.page ?? 0,
            size: params.size ?? 10,
            name: params.name,
            sort: params.sort ?? 'name,asc'
        }
    });
}

export function getArtistById(id: number) {
    return api.get<Artist>(`/artists/${id}`);
}

export function createArtist(data: ArtistRequest) {
    return api.post<Artist>('/artists', data);
}

export function updateArtist(id: number, data: ArtistRequest) {
    return api.put<Artist>(`/artists/${id}`, data);
}

export function deleteArtist(id: number) {
    return api.delete(`/artists/${id}`);
}