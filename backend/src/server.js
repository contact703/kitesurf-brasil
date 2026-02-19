require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { initDatabase } = require('./models/database');
const chatRoutes = require('./routes/chat');
const spotsRoutes = require('./routes/spots');
const classifiedsRoutes = require('./routes/classifieds');
const usersRoutes = require('./routes/users');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Initialize database
initDatabase();

// Routes
app.use('/api/chat', chatRoutes);
app.use('/api/spots', spotsRoutes);
app.use('/api/classifieds', classifiedsRoutes);
app.use('/api/users', usersRoutes);

// Health check
app.get('/', (req, res) => {
  res.json({
    name: 'KiteSurf Brasil API',
    version: '1.0.0',
    status: 'online',
    endpoints: {
      chat: '/api/chat',
      spots: '/api/spots',
      classifieds: '/api/classifieds',
      users: '/api/users'
    }
  });
});

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(`🏄 KiteSurf API running on port ${PORT}`);
});
