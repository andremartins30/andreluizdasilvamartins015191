import { BehaviorSubject, Observable } from 'rxjs';
import { getArtists, type Artist } from '../api/artistService';

export interface ArtistPage {
    content: Artist[];
    totalPages: number;
    totalElements: number;
    currentPage: number;
}

class ArtistStore {
    private artistsSubject = new BehaviorSubject<Artist[]>([]);
    private loadingSubject = new BehaviorSubject<boolean>(false);
    private errorSubject = new BehaviorSubject<string | null>(null);
    private totalPagesSubject = new BehaviorSubject<number>(0);
    private currentPageSubject = new BehaviorSubject<number>(0);

    public artists$: Observable<Artist[]> = this.artistsSubject.asObservable();
    public loading$: Observable<boolean> = this.loadingSubject.asObservable();
    public error$: Observable<string | null> = this.errorSubject.asObservable();
    public totalPages$: Observable<number> = this.totalPagesSubject.asObservable();
    public currentPage$: Observable<number> = this.currentPageSubject.asObservable();

    async loadArtists(params: {
        page?: number;
        size?: number;
        name?: string;
        sort?: string;
    } = {}) {
        this.loadingSubject.next(true);
        this.errorSubject.next(null);

        try {
            const response = await getArtists(params);
            this.artistsSubject.next(response.data.content || []);
            this.totalPagesSubject.next(response.data.totalPages);
            this.currentPageSubject.next(params.page || 0);
        } catch (error: any) {
            const errorMessage = error?.response?.data?.message || 'Erro ao carregar artistas';
            this.errorSubject.next(errorMessage);
            this.artistsSubject.next([]);
        } finally {
            this.loadingSubject.next(false);
        }
    }

    getCurrentArtists(): Artist[] {
        return this.artistsSubject.getValue();
    }

    getTotalPages(): number {
        return this.totalPagesSubject.getValue();
    }

    getCurrentPage(): number {
        return this.currentPageSubject.getValue();
    }

    clear() {
        this.artistsSubject.next([]);
        this.totalPagesSubject.next(0);
        this.currentPageSubject.next(0);
        this.errorSubject.next(null);
    }
}

export const artistStore = new ArtistStore();
