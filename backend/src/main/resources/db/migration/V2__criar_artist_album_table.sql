CREATE TABLE artists (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

CREATE TABLE albums (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    artist_id INTEGER NOT NULL,
    cover_object_name VARCHAR(255),
    CONSTRAINT fk_album_artist
        FOREIGN KEY(artist_id)
        REFERENCES artists (id)
);