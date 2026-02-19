require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { initDatabase } = require('./models/database');

// Routes
const chatRoutes = require('./routes/chat');
const spotsRoutes = require('./routes/spots');
const classifiedsRoutes = require('./routes/classifieds');
const usersRoutes = require('./routes/users');
const feedRoutes = require('./routes/feed');
const messagesRoutes = require('./routes/messages');
const forumRoutes = require('./routes/forum');
const accommodationsRoutes = require('./routes/accommodations');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Initialize database
initDatabase();

// Routes
app.use('/api/chat', chatRoutes);
app.use('/api/spots', spotsRoutes);
app.use('/api/classifieds', classifiedsRoutes);
app.use('/api/users', usersRoutes);
app.use('/api/feed', feedRoutes);
app.use('/api/messages', messagesRoutes);
app.use('/api/forum', forumRoutes);
app.use('/api/accommodations', accommodationsRoutes);

// Health check
app.get('/', (req, res) => {
  res.json({
    name: 'KiteSurf Brasil API',
    version: '2.0.0',
    status: 'online',
    endpoints: {
      chat: '/api/chat',
      feed: '/api/feed',
      users: '/api/users',
      spots: '/api/spots',
      classifieds: '/api/classifieds',
      accommodations: '/api/accommodations',
      messages: '/api/messages',
      forum: '/api/forum'
    },
    features: [
      'Rede social com posts e comentários',
      'Mensagens diretas entre usuários',
      'Fórum de discussão',
      'Classificados de equipamentos',
      'Guia de spots e pousadas',
      'Chat IA com KiteBot'
    ]
  });
});

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

// Error handling
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({ error: 'Erro interno do servidor' });
});

app.listen(PORT, () => {
  console.log(`🏄 KiteSurf Brasil API v2.0 running on port ${PORT}`);
  console.log('📱 Endpoints: feed, users, spots, classifieds, accommodations, messages, forum, chat');
});
