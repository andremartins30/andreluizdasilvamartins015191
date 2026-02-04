-- Criar tabela para múltiplas capas de álbum
CREATE TABLE IF NOT EXISTS album_covers (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    object_name VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cover_album
        FOREIGN KEY(album_id)
        REFERENCES albums (id)
        ON DELETE CASCADE
);

-- Migrar dados existentes de cover_object_name para a nova tabela
INSERT INTO album_covers (album_id, object_name)
SELECT id, cover_object_name
FROM albums
WHERE cover_object_name IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM album_covers WHERE album_covers.album_id = albums.id
);

