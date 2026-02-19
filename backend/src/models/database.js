const Database = require('better-sqlite3');
const path = require('path');

const dbPath = process.env.DATABASE_PATH || path.join(__dirname, '../../data/kitesurf.db');
let db;

function getDb() {
  if (!db) {
    db = new Database(dbPath);
    db.pragma('journal_mode = WAL');
  }
  return db;
}

function initDatabase() {
  const db = getDb();
  
  // Tabela de usuários
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      email TEXT UNIQUE,
      phone TEXT,
      bio TEXT,
      avatar_url TEXT,
      level TEXT DEFAULT 'iniciante',
      location TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Tabela de spots/praias
  db.exec(`
    CREATE TABLE IF NOT EXISTS spots (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      description TEXT,
      location TEXT,
      latitude REAL,
      longitude REAL,
      wind_direction TEXT,
      best_months TEXT,
      difficulty TEXT DEFAULT 'iniciante',
      amenities TEXT,
      photos TEXT,
      rating REAL DEFAULT 0,
      rating_count INTEGER DEFAULT 0,
      created_by INTEGER,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (created_by) REFERENCES users(id)
    )
  `);

  // Tabela de avaliações de spots
  db.exec(`
    CREATE TABLE IF NOT EXISTS spot_reviews (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      spot_id INTEGER NOT NULL,
      user_id INTEGER NOT NULL,
      rating INTEGER NOT NULL,
      comment TEXT,
      wind_conditions TEXT,
      visited_at DATE,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (spot_id) REFERENCES spots(id),
      FOREIGN KEY (user_id) REFERENCES users(id)
    )
  `);

  // Tabela de classificados
  db.exec(`
    CREATE TABLE IF NOT EXISTS classifieds (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      title TEXT NOT NULL,
      description TEXT,
      category TEXT NOT NULL,
      price REAL,
      condition TEXT,
      photos TEXT,
      location TEXT,
      contact TEXT,
      status TEXT DEFAULT 'active',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id)
    )
  `);

  // Tabela de pousadas
  db.exec(`
    CREATE TABLE IF NOT EXISTS accommodations (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      description TEXT,
      location TEXT,
      latitude REAL,
      longitude REAL,
      near_spots TEXT,
      amenities TEXT,
      price_range TEXT,
      contact TEXT,
      website TEXT,
      photos TEXT,
      rating REAL DEFAULT 0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Tabela de posts (rede social)
  db.exec(`
    CREATE TABLE IF NOT EXISTS posts (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      content TEXT,
      media_url TEXT,
      spot_id INTEGER,
      likes INTEGER DEFAULT 0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id),
      FOREIGN KEY (spot_id) REFERENCES spots(id)
    )
  `);

  // Tabela de histórico de chat
  db.exec(`
    CREATE TABLE IF NOT EXISTS chat_history (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER,
      session_id TEXT,
      role TEXT NOT NULL,
      content TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  console.log('📦 Database initialized');
}

module.exports = { getDb, initDatabase };
