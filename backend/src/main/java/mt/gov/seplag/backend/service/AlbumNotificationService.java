package mt.gov.seplag.backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AlbumNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public AlbumNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyNewAlbum(Long albumId, String title, String artistName) {
        Map<String, Object> notification = Map.of(
            "type", "NEW_ALBUM",
            "albumId", albumId,
            "title", title,
            "artistName", artistName,
            "message", "Novo álbum '" + title + "' de " + artistName + " foi adicionado!",
            "timestamp", System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/albums", notification);
    }
}
