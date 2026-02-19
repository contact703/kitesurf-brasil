const express = require('express');
const router = express.Router();
const { getDb } = require('../models/database');

// Categorias disponíveis
const CATEGORIES = ['kites', 'pranchas', 'trapezios', 'acessorios', 'roupas', 'servicos', 'aulas', 'outros'];

// Listar classificados
router.get('/', (req, res) => {
  const db = getDb();
  const { category, location, minPrice, maxPrice, status = 'active' } = req.query;
  
  let query = 'SELECT c.*, u.name as seller_name FROM classifieds c JOIN users u ON c.user_id = u.id WHERE c.status = ?';
  const params = [status];
  
  if (category) {
    query += ' AND c.category = ?';
    params.push(category);
  }
  if (location) {
    query += ' AND c.location LIKE ?';
    params.push(`%${location}%`);
  }
  if (minPrice) {
    query += ' AND c.price >= ?';
    params.push(parseFloat(minPrice));
  }
  if (maxPrice) {
    query += ' AND c.price <= ?';
    params.push(parseFloat(maxPrice));
  }
  
  query += ' ORDER BY c.created_at DESC';
  
  const classifieds = db.prepare(query).all(...params);
  res.json(classifieds);
});

// Buscar por ID
router.get('/:id', (req, res) => {
  const db = getDb();
  const item = db.prepare(`
    SELECT c.*, u.name as seller_name, u.phone as seller_phone
    FROM classifieds c 
    JOIN users u ON c.user_id = u.id 
    WHERE c.id = ?
  `).get(req.params.id);
  
  if (!item) {
    return res.status(404).json({ error: 'Anúncio não encontrado' });
  }
  
  res.json(item);
});

// Criar anúncio
router.post('/', (req, res) => {
  const db = getDb();
  const { 
    user_id, title, description, category, price, 
    condition, photos, location, contact 
  } = req.body;
  
  if (!user_id || !title || !category) {
    return res.status(400).json({ error: 'user_id, title e category são obrigatórios' });
  }
  
  if (!CATEGORIES.includes(category)) {
    return res.status(400).json({ 
      error: 'Categoria inválida', 
      validCategories: CATEGORIES 
    });
  }
  
  const result = db.prepare(`
    INSERT INTO classifieds (user_id, title, description, category, price, condition, photos, location, contact)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(user_id, title, description, category, price, condition, 
    JSON.stringify(photos || []), location, contact);
  
  res.status(201).json({ id: result.lastInsertRowid, message: 'Anúncio criado!' });
});

// Atualizar status (vendido, pausado, etc)
router.patch('/:id/status', (req, res) => {
  const db = getDb();
  const { status, user_id } = req.body;
  
  // Verifica se o anúncio pertence ao usuário
  const item = db.prepare('SELECT user_id FROM classifieds WHERE id = ?').get(req.params.id);
  
  if (!item) {
    return res.status(404).json({ error: 'Anúncio não encontrado' });
  }
  
  if (item.user_id !== user_id) {
    return res.status(403).json({ error: 'Sem permissão para alterar este anúncio' });
  }
  
  db.prepare('UPDATE classifieds SET status = ? WHERE id = ?').run(status, req.params.id);
  res.json({ message: 'Status atualizado!' });
});

// Deletar anúncio
router.delete('/:id', (req, res) => {
  const db = getDb();
  const { user_id } = req.body;
  
  const item = db.prepare('SELECT user_id FROM classifieds WHERE id = ?').get(req.params.id);
  
  if (!item) {
    return res.status(404).json({ error: 'Anúncio não encontrado' });
  }
  
  if (item.user_id !== user_id) {
    return res.status(403).json({ error: 'Sem permissão' });
  }
  
  db.prepare('DELETE FROM classifieds WHERE id = ?').run(req.params.id);
  res.json({ message: 'Anúncio removido!' });
});

// Listar categorias
router.get('/meta/categories', (req, res) => {
  res.json(CATEGORIES);
});

module.exports = router;
