-- Seed de dados iniciais
-- Inserir artistas (ignorar duplicatas)
DO $$
BEGIN
    INSERT INTO artists (name) VALUES ('AC/DC') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Metallica') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Serj Tankian') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Mike Shinoda') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Michel Teló') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Guns N'' Roses') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('The Beatles') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Pink Floyd') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Led Zeppelin') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Queen') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('The Rolling Stones') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('David Bowie') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Radiohead') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Nirvana') ON CONFLICT DO NOTHING;
END $$;

-- Inserir álbuns
DO $$
DECLARE
    v_acdc_id BIGINT;
    v_metallica_id BIGINT;
    v_serj_id BIGINT;
    v_mike_id BIGINT;
    v_michel_id BIGINT;
    v_guns_id BIGINT;
    v_beatles_id BIGINT;
    v_floyd_id BIGINT;
    v_zeppelin_id BIGINT;
    v_queen_id BIGINT;
    v_stones_id BIGINT;
    v_bowie_id BIGINT;
    v_radiohead_id BIGINT;
    v_nirvana_id BIGINT;
BEGIN
    -- Obter IDs dos artistas
    SELECT id INTO v_acdc_id FROM artists WHERE name = 'AC/DC' LIMIT 1;
    SELECT id INTO v_metallica_id FROM artists WHERE name = 'Metallica' LIMIT 1;
    SELECT id INTO v_serj_id FROM artists WHERE name = 'Serj Tankian' LIMIT 1;
    SELECT id INTO v_mike_id FROM artists WHERE name = 'Mike Shinoda' LIMIT 1;
    SELECT id INTO v_michel_id FROM artists WHERE name = 'Michel Teló' LIMIT 1;
    SELECT id INTO v_guns_id FROM artists WHERE name = 'Guns N'' Roses' LIMIT 1;
    SELECT id INTO v_beatles_id FROM artists WHERE name = 'The Beatles' LIMIT 1;
    SELECT id INTO v_floyd_id FROM artists WHERE name = 'Pink Floyd' LIMIT 1;
    SELECT id INTO v_zeppelin_id FROM artists WHERE name = 'Led Zeppelin' LIMIT 1;
    SELECT id INTO v_queen_id FROM artists WHERE name = 'Queen' LIMIT 1;
    SELECT id INTO v_stones_id FROM artists WHERE name = 'The Rolling Stones' LIMIT 1;
    SELECT id INTO v_bowie_id FROM artists WHERE name = 'David Bowie' LIMIT 1;
    SELECT id INTO v_radiohead_id FROM artists WHERE name = 'Radiohead' LIMIT 1;
    SELECT id INTO v_nirvana_id FROM artists WHERE name = 'Nirvana' LIMIT 1;

    -- AC/DC
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Back in Black', v_acdc_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Highway to Hell', v_acdc_id, null);
    
    -- Metallica
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Master of Puppets', v_metallica_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Ride the Lightning', v_metallica_id, null);
    
    -- Serj Tankian
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Harakiri', v_serj_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Black Blooms', v_serj_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('The Rough Dog', v_serj_id, null);
    
    -- Mike Shinoda
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('The Rising Tied', v_mike_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Post Traumatic', v_mike_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Post Traumatic EP', v_mike_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Where''d You Go', v_mike_id, null);
    
    -- Michel Teló
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Bem Sertanejo', v_michel_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Bem Sertanejo - O Show (Ao Vivo)', v_michel_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Bem Sertanejo - (1ª Temporada) - EP', v_michel_id, null);
    
    -- Guns N' Roses
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Use Your Illusion I', v_guns_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Use Your Illusion II', v_guns_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Greatest Hits', v_guns_id, null);
    
    -- The Beatles
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Abbey Road', v_beatles_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Sgt. Pepper''s Lonely Hearts Club Band', v_beatles_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Revolver', v_beatles_id, null);
    
    -- Pink Floyd
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('The Dark Side of the Moon', v_floyd_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('The Wall', v_floyd_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Wish You Were Here', v_floyd_id, null);
    
    -- Led Zeppelin
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Led Zeppelin IV', v_zeppelin_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Physical Graffiti', v_zeppelin_id, null);
    
    -- Queen
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('A Night at the Opera', v_queen_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('The Game', v_queen_id, null);
    
    -- The Rolling Stones
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Exile on Main St.', v_stones_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Let It Bleed', v_stones_id, null);
    
    -- David Bowie
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('The Rise and Fall of Ziggy Stardust', v_bowie_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Heroes', v_bowie_id, null);
    
    -- Radiohead
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('OK Computer', v_radiohead_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Kid A', v_radiohead_id, null);
    
    -- Nirvana
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Nevermind', v_nirvana_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('In Utero', v_nirvana_id, null);
END $$;
