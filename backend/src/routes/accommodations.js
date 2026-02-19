const express = require('express');
const router = express.Router();
const { getDb } = require('../models/database');

// GET listar pousadas
router.get('/', (req, res) => {
  const db = getDb();
  const { state, near_spot, minPrice, maxPrice, limit = 20, offset = 0 } = req.query;
  
  let query = 'SELECT * FROM accommodations WHERE 1=1';
  const params = [];
  
  if (state) {
    query += ' AND state = ?';
    params.push(state);
  }
  
  if (near_spot) {
    query += ' AND near_spots LIKE ?';
    params.push(`%${near_spot}%`);
  }
  
  if (minPrice) {
    query += ' AND price_min >= ?';
    params.push(parseFloat(minPrice));
  }
  
  if (maxPrice) {
    query += ' AND price_max <= ?';
    params.push(parseFloat(maxPrice));
  }
  
  query += ' ORDER BY rating DESC, verified DESC LIMIT ? OFFSET ?';
  params.push(parseInt(limit), parseInt(offset));
  
  const accommodations = db.prepare(query).all(...params);
  
  accommodations.forEach(a => {
    a.photos = a.photos ? JSON.parse(a.photos) : [];
    a.amenities = a.amenities ? JSON.parse(a.amenities) : [];
    a.near_spots = a.near_spots ? JSON.parse(a.near_spots) : [];
  });
  
  res.json(accommodations);
});

// GET pousada específica
router.get('/:id', (req, res) => {
  const db = getDb();
  
  const accommodation = db.prepare('SELECT * FROM accommodations WHERE id = ?').get(req.params.id);
  
  if (!accommodation) {
    return res.status(404).json({ error: 'Pousada não encontrada' });
  }
  
  accommodation.photos = accommodation.photos ? JSON.parse(accommodation.photos) : [];
  accommodation.amenities = accommodation.amenities ? JSON.parse(accommodation.amenities) : [];
  accommodation.near_spots = accommodation.near_spots ? JSON.parse(accommodation.near_spots) : [];
  
  res.json(accommodation);
});

// POST criar pousada
router.post('/', (req, res) => {
  const db = getDb();
  const { 
    name, description, location, state, latitude, longitude,
    near_spots, amenities, price_range, price_min, price_max,
    contact_phone, contact_whatsapp, contact_email, website, instagram, photos
  } = req.body;
  
  if (!name || !location) {
    return res.status(400).json({ error: 'name e location são obrigatórios' });
  }
  
  const result = db.prepare(`
    INSERT INTO accommodations (
      name, description, location, state, latitude, longitude,
      near_spots, amenities, price_range, price_min, price_max,
      contact_phone, contact_whatsapp, contact_email, website, instagram, photos
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(
    name, description, location, state, latitude, longitude,
    JSON.stringify(near_spots || []), JSON.stringify(amenities || []),
    price_range, price_min, price_max,
    contact_phone, contact_whatsapp, contact_email, website, instagram,
    JSON.stringify(photos || [])
  );
  
  res.status(201).json({ id: result.lastInsertRowid, message: 'Pousada cadastrada!' });
});

// GET buscar pousadas
router.get('/search/:query', (req, res) => {
  const db = getDb();
  const q = req.params.query;
  
  const accommodations = db.prepare(`
    SELECT * FROM accommodations
    WHERE name LIKE ? OR location LIKE ? OR state LIKE ?
    ORDER BY rating DESC
    LIMIT 20
  `).all(`%${q}%`, `%${q}%`, `%${q}%`);
  
  accommodations.forEach(a => {
    a.photos = a.photos ? JSON.parse(a.photos) : [];
  });
  
  res.json(accommodations);
});

// GET pousadas por estado
router.get('/state/:state', (req, res) => {
  const db = getDb();
  
  const accommodations = db.prepare(`
    SELECT * FROM accommodations WHERE state = ? ORDER BY rating DESC
  `).all(req.params.state);
  
  accommodations.forEach(a => {
    a.photos = a.photos ? JSON.parse(a.photos) : [];
    a.near_spots = a.near_spots ? JSON.parse(a.near_spots) : [];
  });
  
  res.json(accommodations);
});

module.exports = router;
