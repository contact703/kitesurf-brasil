# KiteSurf Brasil 🏄‍♂️

App completo para a comunidade de Kite Surf no Brasil.

## Funcionalidades

### Core
- **Rede Social** - Feed, posts, fotos, vídeos, seguir atletas
- **Classificados** - Compra/venda de equipamentos, serviços
- **Spots/Praias** - Cadastro, avaliação, condições de vento
- **Pousadas** - Guia de hospedagem próximo aos spots
- **Guia do Esporte** - Informações, técnicas, equipamentos

### Destaque
- **Chat IA (KiteBot)** - Assistente em primeiro plano que:
  - Tira dúvidas sobre equipamentos
  - Dá dicas de técnicas
  - Sugere melhores spots por condição
  - Informa sobre condições de vento
  - Direciona para produtos/anúncios

## Arquitetura

```
kitesurf-app/
├── backend/           # API Node.js + Express
│   ├── src/
│   │   ├── routes/    # Endpoints REST
│   │   ├── models/    # Schemas do banco
│   │   ├── services/  # Lógica de negócio
│   │   └── ai/        # Integração IA (chat)
│   └── package.json
├── android/           # App Android (Kotlin)
│   └── app/
└── docs/              # Documentação
```

## Stack

- **Backend:** Node.js, Express, SQLite/PostgreSQL
- **IA:** OpenRouter (modelo gratuito)
- **Android:** Kotlin, Jetpack Compose
- **Deploy:** Render/Railway

## Status

- [ ] Backend API
- [ ] Chat IA
- [ ] App Android
- [ ] Deploy

---

Criado em: 2026-02-19
