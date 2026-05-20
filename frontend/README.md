# Star Wars Command Deck

Frontend Vue 3 para a API Star Wars.

## Variáveis de ambiente

- `VITE_API_BASE_URL`: URL pública do backend.

## Rodar localmente

```bash
npm install
npm run dev
```

## Deploy na Vercel

1. Crie um projeto novo na Vercel apontando para a pasta `frontend/`.
2. Configure:
   - Framework Preset: Vite
   - Build Command: `npm run build`
   - Output Directory: `dist`
3. Adicione a variável `VITE_API_BASE_URL` com a URL pública da API.
4. Faça deploy.

## Importante

Este frontend pode ficar 100% na Vercel.
O backend Spring Boot não fica 100% na Vercel sem uma reescrita para serverless functions.
