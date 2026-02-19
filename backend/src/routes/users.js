const express = require('express');
const router = express.Router();
const { getDb } = require('../models/database');

// Criar/registrar usuário
router.post('/', (req, res) => {
  const db = getDb();
  const { name, email, phone, bio, level, location } = req.body;
  
  if (!name) {
    return res.status(400).json({ error: 'Nome é obrigatório' });
  }
  
  try {
    const result = db.prepare(`
      INSERT INTO users (name, email, phone, bio, level, location)
      VALUES (?, ?, ?, ?, ?, ?)
    `).run(name, email, phone, bio, level || 'iniciante', location);
    
    res.status(201).json({ 
      id: result.lastInsertRowid, 
      message: 'Usuário criado!',
      name,
      level: level || 'iniciante'
    });
  } catch (error) {
    if (error.code === 'SQLITE_CONSTRAINT_UNIQUE') {
      return res.status(409).json({ error: 'Email já cadastrado' });
    }
    throw error;
  }
});

// Buscar usuário por ID
router.get('/:id', (req, res) => {
  const db = getDb();
  const user = db.prepare(`
    SELECT id, name, bio, avatar_url, level, location, created_at
    FROM users WHERE id = ?
  `).get(req.params.id);
  
  if (!user) {
    return res.status(404).json({ error: 'Usuário não encontrado' });
  }
  
  // Estatísticas do usuário
  const stats = {
    posts: db.prepare('SELECT COUNT(*) as count FROM posts WHERE user_id = ?').get(req.params.id).count,
    reviews: db.prepare('SELECT COUNT(*) as count FROM spot_reviews WHERE user_id = ?').get(req.params.id).count,
    classifieds: db.prepare('SELECT COUNT(*) as count FROM classifieds WHERE user_id = ?').get(req.params.id).count
  };
  
  res.json({ ...user, stats });
});

// Atualizar perfil
router.patch('/:id', (req, res) => {
  const db = getDb();
  const { name, bio, avatar_url, level, location } = req.body;
  
  const user = db.prepare('SELECT id FROM users WHERE id = ?').get(req.params.id);
  if (!user) {
    return res.status(404).json({ error: 'Usuário não encontrado' });
  }
  
  const updates = [];
  const params = [];
  
  if (name) { updates.push('name = ?'); params.push(name); }
  if (bio !== undefined) { updates.push('bio = ?'); params.push(bio); }
  if (avatar_url) { updates.push('avatar_url = ?'); params.push(avatar_url); }
  if (level) { updates.push('level = ?'); params.push(level); }
  if (location) { updates.push('location = ?'); params.push(location); }
  
  if (updates.length === 0) {
    return res.status(400).json({ error: 'Nenhum campo para atualizar' });
  }
  
  params.push(req.params.id);
  db.prepare(`UPDATE users SET ${updates.join(', ')} WHERE id = ?`).run(...params);
  
  res.json({ message: 'Perfil atualizado!' });
});

// Posts do usuário
router.get('/:id/posts', (req, res) => {
  const db = getDb();
  const posts = db.prepare(`
    SELECT p.*, s.name as spot_name 
    FROM posts p 
    LEFT JOIN spots s ON p.spot_id = s.id
    WHERE p.user_id = ?
    ORDER BY p.created_at DESC
  `).all(req.params.id);
  
  res.json(posts);
});

// Criar post
router.post('/:id/posts', (req, res) => {
  const db = getDb();
  const { content, media_url, spot_id } = req.body;
  
  if (!content && !media_url) {
    return res.status(400).json({ error: 'Conteúdo ou mídia é obrigatório' });
  }
  
  const result = db.prepare(`
    INSERT INTO posts (user_id, content, media_url, spot_id)
    VALUES (?, ?, ?, ?)
  `).run(req.params.id, content, media_url, spot_id);
  
  res.status(201).json({ id: result.lastInsertRowid, message: 'Post criado!' });
});

module.exports = router;
