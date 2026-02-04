-- Drop da tabela antiga se existir
DROP TABLE IF EXISTS regional;

-- Nova estrutura com ID interno e externo
CREATE TABLE regional (
    id BIGSERIAL PRIMARY KEY,
    id_externo INTEGER NOT NULL,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Índices para performance
CREATE INDEX idx_regional_id_externo ON regional(id_externo);
CREATE INDEX idx_regional_ativo ON regional(ativo);
CREATE INDEX idx_regional_id_externo_ativo ON regional(id_externo, ativo);
