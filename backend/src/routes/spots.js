const express = require('express');
const router = express.Router();
const { getDb } = require('../models/database');

// Listar todos os spots
router.get('/', (req, res) => {
  const db = getDb();
  const { difficulty, location } = req.query;
  
  let query = 'SELECT * FROM spots WHERE 1=1';
  const params = [];
  
  if (difficulty) {
    query += ' AND difficulty = ?';
    params.push(difficulty);
  }
  if (location) {
    query += ' AND location LIKE ?';
    params.push(`%${location}%`);
  }
  
  query += ' ORDER BY rating DESC';
  
  const spots = db.prepare(query).all(...params);
  res.json(spots);
});

// Buscar spot por ID
router.get('/:id', (req, res) => {
  const db = getDb();
  const spot = db.prepare('SELECT * FROM spots WHERE id = ?').get(req.params.id);
  
  if (!spot) {
    return res.status(404).json({ error: 'Spot não encontrado' });
  }
  
  // Busca reviews do spot
  const reviews = db.prepare(`
    SELECT r.*, u.name as user_name 
    FROM spot_reviews r 
    JOIN users u ON r.user_id = u.id 
    WHERE r.spot_id = ? 
    ORDER BY r.created_at DESC
  `).all(req.params.id);
  
  res.json({ ...spot, reviews });
});

// Criar novo spot
router.post('/', (req, res) => {
  const db = getDb();
  const { 
    name, description, location, latitude, longitude,
    wind_direction, best_months, difficulty, amenities, photos, created_by
  } = req.body;
  
  if (!name || !location) {
    return res.status(400).json({ error: 'Nome e localização são obrigatórios' });
  }
  
  const result = db.prepare(`
    INSERT INTO spots (name, description, location, latitude, longitude, 
      wind_direction, best_months, difficulty, amenities, photos, created_by)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(name, description, location, latitude, longitude,
    wind_direction, best_months, difficulty, amenities, 
    JSON.stringify(photos || []), created_by);
  
  res.status(201).json({ id: result.lastInsertRowid, message: 'Spot criado!' });
});

// Avaliar spot
router.post('/:id/review', (req, res) => {
  const db = getDb();
  const { user_id, rating, comment, wind_conditions, visited_at } = req.body;
  
  if (!user_id || !rating) {
    return res.status(400).json({ error: 'user_id e rating são obrigatórios' });
  }
  
  // Insere review
  db.prepare(`
    INSERT INTO spot_reviews (spot_id, user_id, rating, comment, wind_conditions, visited_at)
    VALUES (?, ?, ?, ?, ?, ?)
  `).run(req.params.id, user_id, rating, comment, wind_conditions, visited_at);
  
  // Atualiza média do spot
  const stats = db.prepare(`
    SELECT AVG(rating) as avg_rating, COUNT(*) as count 
    FROM spot_reviews WHERE spot_id = ?
  `).get(req.params.id);
  
  db.prepare(`
    UPDATE spots SET rating = ?, rating_count = ? WHERE id = ?
  `).run(stats.avg_rating, stats.count, req.params.id);
  
  res.json({ message: 'Avaliação registrada!', newRating: stats.avg_rating });
});

module.exports = router;
