import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import toast from 'react-hot-toast';

class WebSocketService {
    private client: Client | null = null;
    private connected = false;

    connect() {
        if (this.connected) return;

        this.client = new Client({
            webSocketFactory: () => new SockJS('http://64.23.178.251:8080/ws'),
            debug: (str) => {
                console.log('STOMP:', str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                console.log('WebSocket conectado!');
                this.connected = true;
                this.subscribeToAlbums();
            },
            onDisconnect: () => {
                console.log('WebSocket desconectado');
                this.connected = false;
            },
            onStompError: (frame) => {
                console.error('Erro STOMP:', frame);
            }
        });

        this.client.activate();
    }

    private subscribeToAlbums() {
        if (!this.client) return;

        this.client.subscribe('/topic/albums', (message) => {
            const notification = JSON.parse(message.body);

            toast.success(notification.message, {
                duration: 5000,
                icon: '🎵',
            });
        });
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.connected = false;
        }
    }

    isConnected() {
        return this.connected;
    }
}

export const webSocketService = new WebSocketService();
