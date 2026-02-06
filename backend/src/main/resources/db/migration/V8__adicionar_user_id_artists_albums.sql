-- Adicionar coluna user_id nas tabelas artists e albums para isolamento de dados por usuário

-- Adicionar coluna user_id na tabela artists
ALTER TABLE artists 
ADD COLUMN user_id BIGINT;

-- Adicionar coluna user_id na tabela albums
ALTER TABLE albums 
ADD COLUMN user_id BIGINT;

-- Criar foreign key para users em artists
ALTER TABLE artists
ADD CONSTRAINT fk_artist_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;

-- Criar foreign key para users em albums
ALTER TABLE albums
ADD CONSTRAINT fk_album_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;

-- Criar índices para otimizar queries filtradas por user_id
CREATE INDEX idx_artists_user_id ON artists(user_id);
CREATE INDEX idx_albums_user_id ON albums(user_id);

-- Comentários explicativos
COMMENT ON COLUMN artists.user_id IS 'ID do usuário proprietário do artista. NULL indica dados compartilhados (legado)';
COMMENT ON COLUMN albums.user_id IS 'ID do usuário proprietário do álbum. NULL indica dados compartilhados (legado)';
