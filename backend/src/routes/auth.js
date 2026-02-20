const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const { getDb } = require('../models/database');

// POST /api/auth/register - Cadastrar novo usuário
router.post('/register', async (req, res) => {
  try {
    const { name, username, email, password } = req.body;
    
    if (!name || !username || !email || !password) {
      return res.status(400).json({ 
        error: true, 
        message: 'Todos os campos são obrigatórios: name, username, email, password' 
      });
    }
    
    // Validate username format
    if (!/^[a-zA-Z0-9_]{3,20}$/.test(username)) {
      return res.status(400).json({ 
        error: true, 
        message: 'Username deve ter 3-20 caracteres (letras, números e _)' 
      });
    }
    
    // Validate email format
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      return res.status(400).json({ 
        error: true, 
        message: 'Email inválido' 
      });
    }
    
    // Validate password length
    if (password.length < 6) {
      return res.status(400).json({ 
        error: true, 
        message: 'Senha deve ter pelo menos 6 caracteres' 
      });
    }
    
    const db = getDb();
    
    // Check if username already exists
    const existingUsername = db.prepare('SELECT id FROM users WHERE LOWER(username) = ?').get(username.toLowerCase());
    
    if (existingUsername) {
      return res.status(400).json({ 
        error: true, 
        message: 'Este username já está em uso' 
      });
    }
    
    // Check if email already exists
    const existingEmail = db.prepare('SELECT id FROM users WHERE LOWER(email) = ?').get(email.toLowerCase());
    
    if (existingEmail) {
      return res.status(400).json({ 
        error: true, 
        message: 'Este email já está cadastrado' 
      });
    }
    
    // Hash password
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);
    
    // Insert new user
    const stmt = db.prepare(
      `INSERT INTO users (name, username, email, password_hash, level, created_at) 
       VALUES (?, ?, ?, ?, ?, datetime('now'))`
    );
    
    const result = stmt.run(name, username.toLowerCase(), email.toLowerCase(), hashedPassword, 'iniciante');
    
    // Get the created user
    const user = db.prepare(
      'SELECT id, name, username, email, avatar_url, level FROM users WHERE id = ?'
    ).get(result.lastInsertRowid);
    
    res.status(201).json({
      success: true,
      message: 'Conta criada com sucesso!',
      user: user
    });
    
  } catch (error) {
    console.error('Register error:', error);
    res.status(500).json({ 
      error: true, 
      message: 'Erro ao criar conta. Tente novamente.' 
    });
  }
});

// POST /api/auth/login - Login
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    
    if (!username || !password) {
      return res.status(400).json({ 
        error: true, 
        message: 'Username/email e senha são obrigatórios' 
      });
    }
    
    const db = getDb();
    
    // Find user by username or email
    const user = db.prepare(
      `SELECT id, name, username, email, password_hash, avatar_url, level, bio, location, verified 
       FROM users WHERE LOWER(username) = ? OR LOWER(email) = ?`
    ).get(username.toLowerCase(), username.toLowerCase());
    
    if (!user) {
      return res.status(401).json({ 
        error: true, 
        message: 'Usuário não encontrado' 
      });
    }
    
    // Check if user has a password (old users from seed might not have)
    if (!user.password_hash) {
      return res.status(401).json({ 
        error: true, 
        message: 'Conta antiga sem senha. Por favor, crie uma nova conta.' 
      });
    }
    
    // Check password
    const validPassword = await bcrypt.compare(password, user.password_hash);
    
    if (!validPassword) {
      return res.status(401).json({ 
        error: true, 
        message: 'Senha incorreta' 
      });
    }
    
    // Return user data (without password)
    res.json({
      success: true,
      message: 'Login realizado com sucesso!',
      user: {
        id: user.id,
        name: user.name,
        username: user.username,
        email: user.email,
        avatar_url: user.avatar_url,
        level: user.level,
        bio: user.bio,
        location: user.location,
        verified: user.verified
      }
    });
    
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ 
      error: true, 
      message: 'Erro ao fazer login. Tente novamente.' 
    });
  }
});

// PUT /api/auth/update-profile - Atualizar perfil
router.put('/update-profile', async (req, res) => {
  try {
    const { user_id, name, bio, location, level, instagram } = req.body;
    
    if (!user_id) {
      return res.status(400).json({ error: true, message: 'user_id é obrigatório' });
    }
    
    const db = getDb();
    
    const stmt = db.prepare(
      `UPDATE users SET 
        name = COALESCE(?, name),
        bio = COALESCE(?, bio),
        location = COALESCE(?, location),
        level = COALESCE(?, level),
        instagram = COALESCE(?, instagram)
       WHERE id = ?`
    );
    
    stmt.run(name || null, bio || null, location || null, level || null, instagram || null, user_id);
    
    res.json({ success: true, message: 'Perfil atualizado!' });
    
  } catch (error) {
    console.error('Update profile error:', error);
    res.status(500).json({ error: true, message: 'Erro ao atualizar perfil' });
  }
});

// GET /api/auth/check - Verificar se sessão é válida
router.get('/check/:userId', (req, res) => {
  try {
    const { userId } = req.params;
    const db = getDb();
    
    const user = db.prepare(
      'SELECT id, name, username, email, avatar_url, level, bio, location, verified FROM users WHERE id = ?'
    ).get(userId);
    
    if (!user) {
      return res.status(404).json({ error: true, message: 'Usuário não encontrado' });
    }
    
    res.json({ success: true, user });
    
  } catch (error) {
    console.error('Check user error:', error);
    res.status(500).json({ error: true, message: 'Erro ao verificar usuário' });
  }
});

module.exports = router;
