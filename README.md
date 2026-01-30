# Projeto fullstack - Seplag

Projeto desenvolvido como parte do desafio técnico Full stack senior Java + react

## Aplicação full stack para gerenciamento de artistas e albuns, com:

- Backend em Java
- Frontend em React + TS
- Auth JWT
- Upload de imagens
- Real time com Websockets
- Orquestração com Docker


## Armazenamento de Imagens (MinIO)

As capas dos álbuns são armazenadas em um serviço MinIO, compatível com a API S3.

- Bucket: `album-covers`
- Upload via endpoint `/api/v1/albums/{id}/cover`
- As imagens não são expostas diretamente pela API
- Acesso ocorre via URL pré-assinada com expiração de 30 minutos
- Os arquivos são versionados por UUID