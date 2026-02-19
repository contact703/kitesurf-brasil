const express = require('express');
const router = express.Router();
const { getDb } = require('../models/database');

// GET categorias do fórum
router.get('/categories', (req, res) => {
  const db = getDb();
  
  const categories = db.prepare(`
    SELECT c.*, 
           (SELECT COUNT(*) FROM forum_topics WHERE category_id = c.id) as topics_count
    FROM forum_categories c
    ORDER BY c.id
  `).all();
  
  res.json(categories);
});

// GET tópicos de uma categoria
router.get('/category/:categoryId', (req, res) => {
  const db = getDb();
  const { limit = 20, offset = 0 } = req.query;
  
  const category = db.prepare('SELECT * FROM forum_categories WHERE id = ?').get(req.params.categoryId);
  
  if (!category) {
    return res.status(404).json({ error: 'Categoria não encontrada' });
  }
  
  const topics = db.prepare(`
    SELECT t.*, u.name as author_name, u.username as author_username, u.avatar_url as author_avatar
    FROM forum_topics t
    JOIN users u ON t.user_id = u.id
    WHERE t.category_id = ?
    ORDER BY t.pinned DESC, t.last_reply_at DESC, t.created_at DESC
    LIMIT ? OFFSET ?
  `).all(req.params.categoryId, parseInt(limit), parseInt(offset));
  
  res.json({ category, topics });
});

// GET tópico específico com respostas
router.get('/topic/:topicId', (req, res) => {
  const db = getDb();
  
  const topic = db.prepare(`
    SELECT t.*, u.name as author_name, u.username as author_username, u.avatar_url as author_avatar,
           c.name as category_name
    FROM forum_topics t
    JOIN users u ON t.user_id = u.id
    JOIN forum_categories c ON t.category_id = c.id
    WHERE t.id = ?
  `).get(req.params.topicId);
  
  if (!topic) {
    return res.status(404).json({ error: 'Tópico não encontrado' });
  }
  
  // Incrementar views
  db.prepare('UPDATE forum_topics SET views_count = views_count + 1 WHERE id = ?').run(req.params.topicId);
  
  const replies = db.prepare(`
    SELECT r.*, u.name as author_name, u.username as author_username, u.avatar_url as author_avatar
    FROM forum_replies r
    JOIN users u ON r.user_id = u.id
    WHERE r.topic_id = ?
    ORDER BY r.created_at ASC
  `).all(req.params.topicId);
  
  res.json({ ...topic, replies });
});

// POST criar tópico
router.post('/topic', (req, res) => {
  const db = getDb();
  const { user_id, category_id, title, content, media_url } = req.body;
  
  if (!user_id || !category_id || !title || !content) {
    return res.status(400).json({ error: 'user_id, category_id, title e content são obrigatórios' });
  }
  
  const result = db.prepare(`
    INSERT INTO forum_topics (user_id, category_id, title, content, media_url, last_reply_at)
    VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
  `).run(user_id, category_id, title, content, media_url);
  
  db.prepare('UPDATE forum_categories SET posts_count = posts_count + 1 WHERE id = ?').run(category_id);
  
  res.status(201).json({ id: result.lastInsertRowid, message: 'Tópico criado!' });
});

// POST responder tópico
router.post('/topic/:topicId/reply', (req, res) => {
  const db = getDb();
  const { user_id, content, media_url } = req.body;
  
  if (!user_id || !content) {
    return res.status(400).json({ error: 'user_id e content são obrigatórios' });
  }
  
  const topic = db.prepare('SELECT id, locked FROM forum_topics WHERE id = ?').get(req.params.topicId);
  
  if (!topic) {
    return res.status(404).json({ error: 'Tópico não encontrado' });
  }
  
  if (topic.locked) {
    return res.status(403).json({ error: 'Tópico trancado' });
  }
  
  const result = db.prepare(`
    INSERT INTO forum_replies (topic_id, user_id, content, media_url)
    VALUES (?, ?, ?, ?)
  `).run(req.params.topicId, user_id, content, media_url);
  
  db.prepare(`
    UPDATE forum_topics 
    SET replies_count = replies_count + 1, last_reply_at = CURRENT_TIMESTAMP 
    WHERE id = ?
  `).run(req.params.topicId);
  
  res.status(201).json({ id: result.lastInsertRowid, message: 'Resposta adicionada!' });
});

// POST curtir tópico
router.post('/topic/:topicId/like', (req, res) => {
  const db = getDb();
  const { user_id } = req.body;
  
  // Simplificado - só incrementa (em produção, teria tabela de likes separada)
  db.prepare('UPDATE forum_topics SET likes_count = likes_count + 1 WHERE id = ?').run(req.params.topicId);
  res.json({ message: 'Curtido!' });
});

// GET buscar no fórum
router.get('/search', (req, res) => {
  const db = getDb();
  const { q, limit = 20 } = req.query;
  
  if (!q || q.length < 2) {
    return res.json([]);
  }
  
  const topics = db.prepare(`
    SELECT t.*, u.name as author_name, u.username as author_username,
           c.name as category_name
    FROM forum_topics t
    JOIN users u ON t.user_id = u.id
    JOIN forum_categories c ON t.category_id = c.id
    WHERE t.title LIKE ? OR t.content LIKE ?
    ORDER BY t.created_at DESC
    LIMIT ?
  `).all(`%${q}%`, `%${q}%`, parseInt(limit));
  
  res.json(topics);
});

// GET tópicos recentes (para home)
router.get('/recent', (req, res) => {
  const db = getDb();
  const { limit = 10 } = req.query;
  
  const topics = db.prepare(`
    SELECT t.*, u.name as author_name, u.username as author_username, u.avatar_url as author_avatar,
           c.name as category_name, c.color as category_color
    FROM forum_topics t
    JOIN users u ON t.user_id = u.id
    JOIN forum_categories c ON t.category_id = c.id
    ORDER BY t.last_reply_at DESC, t.created_at DESC
    LIMIT ?
  `).all(parseInt(limit));
  
  res.json(topics);
});

module.exports = router;
