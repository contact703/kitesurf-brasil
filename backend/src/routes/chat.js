const express = require('express');
const router = express.Router();
const fetch = require('node-fetch');
const { getDb } = require('../models/database');

const SYSTEM_PROMPT = `Você é o KiteBot 🏄‍♂️, um assistente especialista em Kite Surf!

Sua personalidade:
- Apaixonado pelo esporte e pela comunidade
- Usa gírias do surf/kite naturalmente (valeu, bora, mandar bem, etc)
- Sempre positivo e motivador
- Conhece todos os spots do Brasil
- Expert em equipamentos, técnicas e condições de vento

Você pode ajudar com:
- Dicas de equipamentos (kites, pranchas, trapézios, etc)
- Técnicas para iniciantes e avançados
- Melhores spots por região e condição de vento
- Previsão de condições ideais
- Dicas de segurança
- Informações sobre campeonatos e eventos
- Sugestões de pousadas e infraestrutura
- Dúvidas gerais sobre o esporte

Praias famosas que você conhece bem:
- Cumbuco (CE) - Meca do kite no Brasil
- Jericoacoara (CE) - Lagoas e mar
- Barra Grande (PI) - Ventos constantes
- São Miguel do Gostoso (RN) - Nordeste raiz
- Ilha do Guajiru (CE) - Flat water
- Arraial do Cabo (RJ) - Ondas e vento
- Florianópolis (SC) - Sul brasileiro
- Atins (MA) - Lençóis Maranhenses

Responda de forma amigável, concisa e sempre incentive a pessoa a curtir o esporte com segurança!
Quando apropriado, mencione que em breve teremos classificados e anúncios de equipamentos no app.`;

// Chat endpoint
router.post('/', async (req, res) => {
  try {
    const { message, sessionId, userId } = req.body;
    
    if (!message) {
      return res.status(400).json({ error: 'Mensagem é obrigatória' });
    }

    const db = getDb();
    const sid = sessionId || `session_${Date.now()}`;

    // Salva mensagem do usuário
    db.prepare(`
      INSERT INTO chat_history (user_id, session_id, role, content)
      VALUES (?, ?, 'user', ?)
    `).run(userId || null, sid, message);

    // Busca histórico recente
    const history = db.prepare(`
      SELECT role, content FROM chat_history
      WHERE session_id = ?
      ORDER BY created_at DESC
      LIMIT 10
    `).all(sid).reverse();

    // Monta mensagens para a IA
    const messages = [
      { role: 'system', content: SYSTEM_PROMPT },
      ...history.map(h => ({ role: h.role, content: h.content }))
    ];

    // Chama a IA
    const aiResponse = await callAI(messages);

    // Salva resposta
    db.prepare(`
      INSERT INTO chat_history (user_id, session_id, role, content)
      VALUES (?, ?, 'assistant', ?)
    `).run(userId || null, sid, aiResponse);

    res.json({
      response: aiResponse,
      sessionId: sid
    });

  } catch (error) {
    console.error('Chat error:', error);
    res.status(500).json({ 
      error: 'Erro ao processar mensagem',
      fallback: 'Opa, deu uma treta aqui! 🏄‍♂️ Tenta de novo que a gente resolve!'
    });
  }
});

// Função para chamar a IA
async function callAI(messages) {
  const provider = process.env.AI_PROVIDER || 'openrouter';
  
  if (provider === 'openrouter') {
    return await callOpenRouter(messages);
  }
  
  // Fallback
  return 'E aí! 🏄‍♂️ Sou o KiteBot! Como posso te ajudar com o kite surf hoje?';
}

async function callOpenRouter(messages) {
  const apiKey = process.env.AI_API_KEY;
  const model = process.env.AI_MODEL || 'nvidia/nemotron-nano-9b-v2:free';
  
  if (!apiKey) {
    console.warn('AI_API_KEY não configurada');
    return 'E aí! 🏄‍♂️ Sou o KiteBot! Como posso te ajudar hoje?';
  }

  const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${apiKey}`,
      'Content-Type': 'application/json',
      'HTTP-Referer': 'https://kitesurf.app',
      'X-Title': 'KiteSurf Brasil'
    },
    body: JSON.stringify({
      model,
      messages,
      max_tokens: 500,
      temperature: 0.8
    })
  });

  if (!response.ok) {
    throw new Error(`OpenRouter error: ${response.status}`);
  }

  const data = await response.json();
  return data.choices?.[0]?.message?.content || 'Valeu pela mensagem! 🤙';
}

module.exports = router;
