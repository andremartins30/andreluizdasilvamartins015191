CREATE TABLE regional (
    id INTEGER PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE INDEX idx_regional_ativo ON regional(ativo);
