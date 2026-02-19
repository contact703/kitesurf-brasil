const express = require('express');
const router = express.Router();
const { getDb } = require('../models/database');

// GET conversas do usuário
router.get('/conversations/:userId', (req, res) => {
  const db = getDb();
  const userId = parseInt(req.params.userId);
  
  const conversations = db.prepare(`
    SELECT c.*,
           CASE WHEN c.user1_id = ? THEN u2.id ELSE u1.id END as other_user_id,
           CASE WHEN c.user1_id = ? THEN u2.name ELSE u1.name END as other_user_name,
           CASE WHEN c.user1_id = ? THEN u2.username ELSE u1.username END as other_user_username,
           CASE WHEN c.user1_id = ? THEN u2.avatar_url ELSE u1.avatar_url END as other_user_avatar,
           (SELECT content FROM messages WHERE conversation_id = c.id ORDER BY created_at DESC LIMIT 1) as last_message,
           (SELECT COUNT(*) FROM messages WHERE conversation_id = c.id AND sender_id != ? AND read_at IS NULL) as unread_count
    FROM conversations c
    JOIN users u1 ON c.user1_id = u1.id
    JOIN users u2 ON c.user2_id = u2.id
    WHERE c.user1_id = ? OR c.user2_id = ?
    ORDER BY c.last_message_at DESC
  `).all(userId, userId, userId, userId, userId, userId, userId);
  
  res.json(conversations);
});

// GET mensagens de uma conversa
router.get('/conversation/:conversationId', (req, res) => {
  const db = getDb();
  const { userId, limit = 50, offset = 0 } = req.query;
  
  // Verificar se o usuário faz parte da conversa
  const conv = db.prepare(`
    SELECT * FROM conversations WHERE id = ? AND (user1_id = ? OR user2_id = ?)
  `).get(req.params.conversationId, userId, userId);
  
  if (!conv) {
    return res.status(403).json({ error: 'Acesso negado' });
  }
  
  const messages = db.prepare(`
    SELECT m.*, u.name as sender_name, u.username as sender_username, u.avatar_url as sender_avatar
    FROM messages m
    JOIN users u ON m.sender_id = u.id
    WHERE m.conversation_id = ?
    ORDER BY m.created_at DESC
    LIMIT ? OFFSET ?
  `).all(req.params.conversationId, parseInt(limit), parseInt(offset));
  
  // Marcar como lidas
  if (userId) {
    db.prepare(`
      UPDATE messages SET read_at = CURRENT_TIMESTAMP 
      WHERE conversation_id = ? AND sender_id != ? AND read_at IS NULL
    `).run(req.params.conversationId, userId);
  }
  
  res.json(messages.reverse());
});

// POST enviar mensagem
router.post('/send', (req, res) => {
  const db = getDb();
  const { sender_id, recipient_id, content, media_url } = req.body;
  
  if (!sender_id || !recipient_id || !content) {
    return res.status(400).json({ error: 'sender_id, recipient_id e content são obrigatórios' });
  }
  
  // Buscar ou criar conversa
  let conversation = db.prepare(`
    SELECT * FROM conversations 
    WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)
  `).get(sender_id, recipient_id, recipient_id, sender_id);
  
  if (!conversation) {
    const result = db.prepare(`
      INSERT INTO conversations (user1_id, user2_id, last_message_at)
      VALUES (?, ?, CURRENT_TIMESTAMP)
    `).run(Math.min(sender_id, recipient_id), Math.max(sender_id, recipient_id));
    conversation = { id: result.lastInsertRowid };
  } else {
    db.prepare('UPDATE conversations SET last_message_at = CURRENT_TIMESTAMP WHERE id = ?').run(conversation.id);
  }
  
  // Inserir mensagem
  const result = db.prepare(`
    INSERT INTO messages (conversation_id, sender_id, content, media_url)
    VALUES (?, ?, ?, ?)
  `).run(conversation.id, sender_id, content, media_url);
  
  res.status(201).json({
    id: result.lastInsertRowid,
    conversation_id: conversation.id,
    message: 'Mensagem enviada!'
  });
});

// GET buscar usuários para iniciar conversa
router.get('/search-users', (req, res) => {
  const db = getDb();
  const { q, userId } = req.query;
  
  if (!q || q.length < 2) {
    return res.json([]);
  }
  
  const users = db.prepare(`
    SELECT id, name, username, avatar_url, verified
    FROM users
    WHERE (name LIKE ? OR username LIKE ?) AND id != ?
    LIMIT 20
  `).all(`%${q}%`, `%${q}%`, userId || 0);
  
  res.json(users);
});

// DELETE mensagem
router.delete('/:messageId', (req, res) => {
  const db = getDb();
  const { user_id } = req.body;
  
  const message = db.prepare('SELECT sender_id FROM messages WHERE id = ?').get(req.params.messageId);
  
  if (!message || message.sender_id !== user_id) {
    return res.status(403).json({ error: 'Sem permissão' });
  }
  
  db.prepare('DELETE FROM messages WHERE id = ?').run(req.params.messageId);
  res.json({ message: 'Mensagem removida!' });
});

module.exports = router;
