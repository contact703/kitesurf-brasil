const express = require('express');
const router = express.Router();
const { getDb } = require('../models/database');

// POST criar/registrar usuário
router.post('/', (req, res) => {
  const db = getDb();
  const { name, username, email, phone, bio, level, location, instagram } = req.body;
  
  if (!name || !username) {
    return res.status(400).json({ error: 'name e username são obrigatórios' });
  }
  
  try {
    const result = db.prepare(`
      INSERT INTO users (name, username, email, phone, bio, level, location, instagram)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `).run(name, username.toLowerCase(), email, phone, bio, level || 'iniciante', location, instagram);
    
    res.status(201).json({ 
      id: result.lastInsertRowid, 
      message: 'Usuário criado!',
      username: username.toLowerCase()
    });
  } catch (error) {
    if (error.code === 'SQLITE_CONSTRAINT_UNIQUE') {
      return res.status(409).json({ error: 'Username ou email já existe' });
    }
    throw error;
  }
});

// GET buscar usuário por ID
router.get('/:id', (req, res) => {
  const db = getDb();
  const { currentUserId } = req.query;
  
  const user = db.prepare(`
    SELECT id, name, username, bio, avatar_url, cover_url, level, location, 
           website, instagram, followers_count, following_count, posts_count, verified, created_at
    FROM users WHERE id = ?
  `).get(req.params.id);
  
  if (!user) {
    return res.status(404).json({ error: 'Usuário não encontrado' });
  }
  
  // Verificar se o usuário atual segue este perfil
  if (currentUserId) {
    const follows = db.prepare('SELECT 1 FROM follows WHERE follower_id = ? AND following_id = ?')
      .get(currentUserId, req.params.id);
    user.is_following = !!follows;
  }
  
  res.json(user);
});

// GET buscar usuário por username
router.get('/username/:username', (req, res) => {
  const db = getDb();
  const { currentUserId } = req.query;
  
  const user = db.prepare(`
    SELECT id, name, username, bio, avatar_url, cover_url, level, location, 
           website, instagram, followers_count, following_count, posts_count, verified, created_at
    FROM users WHERE username = ?
  `).get(req.params.username.toLowerCase());
  
  if (!user) {
    return res.status(404).json({ error: 'Usuário não encontrado' });
  }
  
  if (currentUserId) {
    const follows = db.prepare('SELECT 1 FROM follows WHERE follower_id = ? AND following_id = ?')
      .get(currentUserId, user.id);
    user.is_following = !!follows;
  }
  
  res.json(user);
});

// PATCH atualizar perfil
router.patch('/:id', (req, res) => {
  const db = getDb();
  const { name, bio, avatar_url, cover_url, level, location, website, instagram } = req.body;
  
  const updates = [];
  const params = [];
  
  if (name) { updates.push('name = ?'); params.push(name); }
  if (bio !== undefined) { updates.push('bio = ?'); params.push(bio); }
  if (avatar_url) { updates.push('avatar_url = ?'); params.push(avatar_url); }
  if (cover_url) { updates.push('cover_url = ?'); params.push(cover_url); }
  if (level) { updates.push('level = ?'); params.push(level); }
  if (location) { updates.push('location = ?'); params.push(location); }
  if (website !== undefined) { updates.push('website = ?'); params.push(website); }
  if (instagram !== undefined) { updates.push('instagram = ?'); params.push(instagram); }
  
  if (updates.length === 0) {
    return res.status(400).json({ error: 'Nenhum campo para atualizar' });
  }
  
  params.push(req.params.id);
  db.prepare(`UPDATE users SET ${updates.join(', ')} WHERE id = ?`).run(...params);
  
  res.json({ message: 'Perfil atualizado!' });
});

// POST seguir usuário
router.post('/:id/follow', (req, res) => {
  const db = getDb();
  const { follower_id } = req.body;
  const following_id = parseInt(req.params.id);
  
  if (!follower_id || follower_id === following_id) {
    return res.status(400).json({ error: 'Operação inválida' });
  }
  
  try {
    db.prepare('INSERT INTO follows (follower_id, following_id) VALUES (?, ?)').run(follower_id, following_id);
    db.prepare('UPDATE users SET followers_count = followers_count + 1 WHERE id = ?').run(following_id);
    db.prepare('UPDATE users SET following_count = following_count + 1 WHERE id = ?').run(follower_id);
    res.json({ message: 'Seguindo!', following: true });
  } catch (e) {
    // Já segue - deixar de seguir
    db.prepare('DELETE FROM follows WHERE follower_id = ? AND following_id = ?').run(follower_id, following_id);
    db.prepare('UPDATE users SET followers_count = followers_count - 1 WHERE id = ?').run(following_id);
    db.prepare('UPDATE users SET following_count = following_count - 1 WHERE id = ?').run(follower_id);
    res.json({ message: 'Deixou de seguir', following: false });
  }
});

// GET seguidores
router.get('/:id/followers', (req, res) => {
  const db = getDb();
  const { limit = 50, offset = 0 } = req.query;
  
  const followers = db.prepare(`
    SELECT u.id, u.name, u.username, u.avatar_url, u.bio, u.verified
    FROM follows f
    JOIN users u ON f.follower_id = u.id
    WHERE f.following_id = ?
    ORDER BY f.created_at DESC
    LIMIT ? OFFSET ?
  `).all(req.params.id, parseInt(limit), parseInt(offset));
  
  res.json(followers);
});

// GET seguindo
router.get('/:id/following', (req, res) => {
  const db = getDb();
  const { limit = 50, offset = 0 } = req.query;
  
  const following = db.prepare(`
    SELECT u.id, u.name, u.username, u.avatar_url, u.bio, u.verified
    FROM follows f
    JOIN users u ON f.following_id = u.id
    WHERE f.follower_id = ?
    ORDER BY f.created_at DESC
    LIMIT ? OFFSET ?
  `).all(req.params.id, parseInt(limit), parseInt(offset));
  
  res.json(following);
});

// GET buscar usuários
router.get('/search/:query', (req, res) => {
  const db = getDb();
  const q = req.params.query;
  
  const users = db.prepare(`
    SELECT id, name, username, avatar_url, bio, verified, followers_count
    FROM users
    WHERE name LIKE ? OR username LIKE ?
    ORDER BY verified DESC, followers_count DESC
    LIMIT 30
  `).all(`%${q}%`, `%${q}%`);
  
  res.json(users);
});

// GET usuários sugeridos (para seguir)
router.get('/suggestions/:userId', (req, res) => {
  const db = getDb();
  
  const suggestions = db.prepare(`
    SELECT id, name, username, avatar_url, bio, verified, followers_count
    FROM users
    WHERE id != ? AND id NOT IN (
      SELECT following_id FROM follows WHERE follower_id = ?
    )
    ORDER BY verified DESC, followers_count DESC
    LIMIT 10
  `).all(req.params.userId, req.params.userId);
  
  res.json(suggestions);
});

module.exports = router;
