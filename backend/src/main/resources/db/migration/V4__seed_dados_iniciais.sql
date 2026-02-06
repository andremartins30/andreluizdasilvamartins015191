-- Seed de dados iniciais (apenas dados do edital)
-- Inserir artistas conforme especificação do edital
DO $$
BEGIN
    INSERT INTO artists (name) VALUES ('Serj Tankian') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Mike Shinoda') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Michel Teló') ON CONFLICT DO NOTHING;
    INSERT INTO artists (name) VALUES ('Guns N'' Roses') ON CONFLICT DO NOTHING;
END $$;

-- Inserir álbuns conforme especificação do edital
DO $$
DECLARE
    v_serj_id BIGINT;
    v_mike_id BIGINT;
    v_michel_id BIGINT;
    v_guns_id BIGINT;
BEGIN
    -- Obter IDs dos artistas
    SELECT id INTO v_serj_id FROM artists WHERE name = 'Serj Tankian' LIMIT 1;
    SELECT id INTO v_mike_id FROM artists WHERE name = 'Mike Shinoda' LIMIT 1;
    SELECT id INTO v_michel_id FROM artists WHERE name = 'Michel Teló' LIMIT 1;
    SELECT id INTO v_guns_id FROM artists WHERE name = 'Guns N'' Roses' LIMIT 1;
    
    -- Serj Tankian (3 álbuns)
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Harakiri', v_serj_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Black Blooms', v_serj_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('The Rough Dog', v_serj_id, null);
    
    -- Mike Shinoda (4 álbuns)
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('The Rising Tied', v_mike_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Post Traumatic', v_mike_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Post Traumatic EP', v_mike_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Where''d You Go', v_mike_id, null);
    
    -- Michel Teló (3 álbuns)
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Bem Sertanejo', v_michel_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Bem Sertanejo - O Show (Ao Vivo)', v_michel_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Bem Sertanejo - (1ª Temporada) - EP', v_michel_id, null);
    
    -- Guns N' Roses (3 álbuns)
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Use Your Illusion I', v_guns_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Use Your Illusion II', v_guns_id, null);
    INSERT INTO albums (title, artist_id, cover_object_name) VALUES ('Greatest Hits', v_guns_id, null);
END $$;

