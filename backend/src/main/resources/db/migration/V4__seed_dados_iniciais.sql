-- Seed de dados iniciais
-- Artistas
INSERT INTO artists (name) VALUES
('AC/DC'),
('Metallica'),
('Serj Tankian'),
('Mike Shinoda'),
('Michel Teló'),
('Guns N'' Roses'),
('The Beatles'),
('Pink Floyd'),
('Led Zeppelin'),
('Queen'),
('The Rolling Stones'),
('David Bowie'),
('Radiohead'),
('Nirvana');


-- Álbuns
INSERT INTO albums (title, artist_id, cover_object_name) VALUES

-- AC/DC
('Back in Black', (SELECT id FROM artists WHERE name = 'AC/DC'), null),
('Highway to Hell', (SELECT id FROM artists WHERE name = 'AC/DC'), null),

-- Metallica
('Master of Puppets', (SELECT id FROM artists WHERE name = 'Metallica'), null),
('Ride the Lightning', (SELECT id FROM artists WHERE name = 'Metallica'), null),

-- Serj Tankian
('Harakiri', (SELECT id FROM artists WHERE name = 'Serj Tankian'), null),
('Black Blooms', (SELECT id FROM artists WHERE name = 'Serj Tankian'), null),
('The Rough Dog', (SELECT id FROM artists WHERE name = 'Serj Tankian'), null),

-- Mike Shinoda
('The Rising Tied', (SELECT id FROM artists WHERE name = 'Mike Shinoda'), null),
('Post Traumatic', (SELECT id FROM artists WHERE name = 'Mike Shinoda'), null),
('Post Traumatic EP', (SELECT id FROM artists WHERE name = 'Mike Shinoda'), null),
('Where''d You Go', (SELECT id FROM artists WHERE name = 'Mike Shinoda'), null),

-- Michel Teló
('Bem Sertanejo', (SELECT id FROM artists WHERE name = 'Michel Teló'), null),
('Bem Sertanejo - O Show (Ao Vivo)', (SELECT id FROM artists WHERE name = 'Michel Teló'), null),
('Bem Sertanejo - (1ª Temporada) - EP', (SELECT id FROM artists WHERE name = 'Michel Teló'), null),

-- Guns N' Roses
('Use Your Illusion I', (SELECT id FROM artists WHERE name = 'Guns N'' Roses'), null),
('Use Your Illusion II', (SELECT id FROM artists WHERE name = 'Guns N'' Roses'), null),
('Greatest Hits', (SELECT id FROM artists WHERE name = 'Guns N'' Roses'), null),

-- The Beatles
('Abbey Road', (SELECT id FROM artists WHERE name = 'The Beatles'), null),
('Sgt. Pepper''s Lonely Hearts Club Band', (SELECT id FROM artists WHERE name = 'The Beatles'), null),
('Revolver', (SELECT id FROM artists WHERE name = 'The Beatles'), null),

-- Pink Floyd
('The Dark Side of the Moon', (SELECT id FROM artists WHERE name = 'Pink Floyd'), null),
('The Wall', (SELECT id FROM artists WHERE name = 'Pink Floyd'), null),
('Wish You Were Here', (SELECT id FROM artists WHERE name = 'Pink Floyd'), null),

-- Led Zeppelin
('Led Zeppelin IV', (SELECT id FROM artists WHERE name = 'Led Zeppelin'), null),
('Physical Graffiti', (SELECT id FROM artists WHERE name = 'Led Zeppelin'), null),

-- Queen
('A Night at the Opera', (SELECT id FROM artists WHERE name = 'Queen'), null),
('The Game', (SELECT id FROM artists WHERE name = 'Queen'), null),

-- The Rolling Stones
('Exile on Main St.', (SELECT id FROM artists WHERE name = 'The Rolling Stones'), null),
('Let It Bleed', (SELECT id FROM artists WHERE name = 'The Rolling Stones'), null),

-- David Bowie
('The Rise and Fall of Ziggy Stardust', (SELECT id FROM artists WHERE name = 'David Bowie'), null),
('Heroes', (SELECT id FROM artists WHERE name = 'David Bowie'), null),

-- Radiohead
('OK Computer', (SELECT id FROM artists WHERE name = 'Radiohead'), null),
('Kid A', (SELECT id FROM artists WHERE name = 'Radiohead'), null),

-- Nirvana
('Nevermind', (SELECT id FROM artists WHERE name = 'Nirvana'), null),
('In Utero', (SELECT id FROM artists WHERE name = 'Nirvana'), null);
