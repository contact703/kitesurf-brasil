const express = require('express');
const router = express.Router();
const { getDb } = require('../models/database');

// GET feed principal (posts de quem o usuário segue + próprios)
router.get('/', (req, res) => {
  const db = getDb();
  const { userId, limit = 20, offset = 0 } = req.query;
  
  let query = `
    SELECT p.*, u.name, u.username, u.avatar_url, u.verified,
           s.name as spot_name
    FROM posts p
    JOIN users u ON p.user_id = u.id
    LEFT JOIN spots s ON p.spot_id = s.id
  `;
  
  if (userId) {
    query += `
      WHERE p.user_id = ? OR p.user_id IN (
        SELECT following_id FROM follows WHERE follower_id = ?
      )
    `;
  }
  
  query += ` ORDER BY p.created_at DESC LIMIT ? OFFSET ?`;
  
  const posts = userId 
    ? db.prepare(query).all(userId, userId, parseInt(limit), parseInt(offset))
    : db.prepare(query.replace('WHERE p.user_id = ? OR p.user_id IN (SELECT following_id FROM follows WHERE follower_id = ?)', '')).all(parseInt(limit), parseInt(offset));
  
  // Adicionar info de likes para cada post
  posts.forEach(post => {
    if (userId) {
      const liked = db.prepare('SELECT 1 FROM likes WHERE user_id = ? AND post_id = ?').get(userId, post.id);
      post.liked_by_user = !!liked;
    }
    post.media_url = post.media_url ? JSON.parse(post.media_url) : [];
  });
  
  res.json(posts);
});

// GET posts de um usuário específico
router.get('/user/:userId', (req, res) => {
  const db = getDb();
  const { limit = 20, offset = 0 } = req.query;
  
  const posts = db.prepare(`
    SELECT p.*, u.name, u.username, u.avatar_url, u.verified,
           s.name as spot_name
    FROM posts p
    JOIN users u ON p.user_id = u.id
    LEFT JOIN spots s ON p.spot_id = s.id
    WHERE p.user_id = ?
    ORDER BY p.created_at DESC
    LIMIT ? OFFSET ?
  `).all(req.params.userId, parseInt(limit), parseInt(offset));
  
  posts.forEach(post => {
    post.media_url = post.media_url ? JSON.parse(post.media_url) : [];
  });
  
  res.json(posts);
});

// GET post específico com comentários
router.get('/:id', (req, res) => {
  const db = getDb();
  
  const post = db.prepare(`
    SELECT p.*, u.name, u.username, u.avatar_url, u.verified,
           s.name as spot_name
    FROM posts p
    JOIN users u ON p.user_id = u.id
    LEFT JOIN spots s ON p.spot_id = s.id
    WHERE p.id = ?
  `).get(req.params.id);
  
  if (!post) {
    return res.status(404).json({ error: 'Post não encontrado' });
  }
  
  post.media_url = post.media_url ? JSON.parse(post.media_url) : [];
  
  // Buscar comentários
  const comments = db.prepare(`
    SELECT c.*, u.name, u.username, u.avatar_url
    FROM comments c
    JOIN users u ON c.user_id = u.id
    WHERE c.post_id = ?
    ORDER BY c.created_at ASC
  `).all(req.params.id);
  
  res.json({ ...post, comments });
});

// POST criar novo post
router.post('/', (req, res) => {
  const db = getDb();
  const { user_id, content, media_url, media_type, spot_id } = req.body;
  
  if (!user_id || (!content && !media_url)) {
    return res.status(400).json({ error: 'user_id e (content ou media_url) são obrigatórios' });
  }
  
  const result = db.prepare(`
    INSERT INTO posts (user_id, content, media_url, media_type, spot_id)
    VALUES (?, ?, ?, ?, ?)
  `).run(user_id, content, JSON.stringify(media_url || []), media_type || 'image', spot_id);
  
  // Atualizar contagem de posts do usuário
  db.prepare('UPDATE users SET posts_count = posts_count + 1 WHERE id = ?').run(user_id);
  
  res.status(201).json({ 
    id: result.lastInsertRowid, 
    message: 'Post criado!' 
  });
});

// POST curtir post
router.post('/:id/like', (req, res) => {
  const db = getDb();
  const { user_id } = req.body;
  
  if (!user_id) {
    return res.status(400).json({ error: 'user_id é obrigatório' });
  }
  
  try {
    db.prepare('INSERT INTO likes (user_id, post_id) VALUES (?, ?)').run(user_id, req.params.id);
    db.prepare('UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?').run(req.params.id);
    res.json({ message: 'Curtido!', liked: true });
  } catch (e) {
    // Já curtiu - descurtir
    db.prepare('DELETE FROM likes WHERE user_id = ? AND post_id = ?').run(user_id, req.params.id);
    db.prepare('UPDATE posts SET likes_count = likes_count - 1 WHERE id = ?').run(req.params.id);
    res.json({ message: 'Descurtido!', liked: false });
  }
});

// POST comentar
router.post('/:id/comment', (req, res) => {
  const db = getDb();
  const { user_id, content, parent_id } = req.body;
  
  if (!user_id || !content) {
    return res.status(400).json({ error: 'user_id e content são obrigatórios' });
  }
  
  const result = db.prepare(`
    INSERT INTO comments (user_id, post_id, parent_id, content)
    VALUES (?, ?, ?, ?)
  `).run(user_id, req.params.id, parent_id, content);
  
  db.prepare('UPDATE posts SET comments_count = comments_count + 1 WHERE id = ?').run(req.params.id);
  
  res.status(201).json({ id: result.lastInsertRowid, message: 'Comentário adicionado!' });
});

// DELETE post
router.delete('/:id', (req, res) => {
  const db = getDb();
  const { user_id } = req.body;
  
  const post = db.prepare('SELECT user_id FROM posts WHERE id = ?').get(req.params.id);
  
  if (!post) {
    return res.status(404).json({ error: 'Post não encontrado' });
  }
  
  if (post.user_id !== user_id) {
    return res.status(403).json({ error: 'Sem permissão' });
  }
  
  db.prepare('DELETE FROM comments WHERE post_id = ?').run(req.params.id);
  db.prepare('DELETE FROM likes WHERE post_id = ?').run(req.params.id);
  db.prepare('DELETE FROM posts WHERE id = ?').run(req.params.id);
  db.prepare('UPDATE users SET posts_count = posts_count - 1 WHERE id = ?').run(user_id);
  
  res.json({ message: 'Post removido!' });
});

module.exports = router;
