CREATE TABLE artists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

CREATE TABLE albums (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    artist_id BIGINT NOT NULL,
    cover_object_name VARCHAR(255),
    CONSTRAINT fk_album_artist
        FOREIGN KEY(artist_id)
        REFERENCES artists (id)
);
);