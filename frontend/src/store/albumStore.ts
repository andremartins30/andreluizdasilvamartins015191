import { BehaviorSubject, Observable } from 'rxjs';
import { getAlbums, type Album } from '../api/albumService';

export interface AlbumPage {
    content: Album[];
    totalPages: number;
    totalElements: number;
    currentPage: number;
}

class AlbumStore {
    private albumsSubject = new BehaviorSubject<Album[]>([]);
    private loadingSubject = new BehaviorSubject<boolean>(false);
    private errorSubject = new BehaviorSubject<string | null>(null);
    private totalPagesSubject = new BehaviorSubject<number>(0);

    public albums$: Observable<Album[]> = this.albumsSubject.asObservable();
    public loading$: Observable<boolean> = this.loadingSubject.asObservable();
    public error$: Observable<string | null> = this.errorSubject.asObservable();
    public totalPages$: Observable<number> = this.totalPagesSubject.asObservable();

    async loadAlbums(params: {
        page?: number;
        size?: number;
        artistName?: string;
    } = {}) {
        this.loadingSubject.next(true);
        this.errorSubject.next(null);

        try {
            const response = await getAlbums(params);
            this.albumsSubject.next(response.data.content || []);
            this.totalPagesSubject.next(response.data.totalPages);
        } catch (error: any) {
            const errorMessage = error?.response?.data?.message || 'Erro ao carregar álbuns';
            this.errorSubject.next(errorMessage);
            this.albumsSubject.next([]);
        } finally {
            this.loadingSubject.next(false);
        }
    }

    getCurrentAlbums(): Album[] {
        return this.albumsSubject.getValue();
    }

    addAlbum(album: Album) {
        const current = this.albumsSubject.getValue();
        this.albumsSubject.next([album, ...current]);
    }

    removeAlbum(albumId: number) {
        const current = this.albumsSubject.getValue();
        this.albumsSubject.next(current.filter(a => a.id !== albumId));
    }

    clear() {
        this.albumsSubject.next([]);
        this.totalPagesSubject.next(0);
        this.errorSubject.next(null);
    }
}

export const albumStore = new AlbumStore();
