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
  const userMessage = messages[messages.length - 1]?.content?.toLowerCase() || '';
  
  // Tentar OpenRouter primeiro
  if (provider === 'openrouter' && process.env.AI_API_KEY) {
    try {
      return await callOpenRouter(messages);
    } catch (error) {
      console.log('OpenRouter falhou, usando fallback inteligente');
    }
  }
  
  // Fallback inteligente com respostas contextuais
  return getSmartResponse(userMessage);
}

// Respostas inteligentes pré-programadas
function getSmartResponse(message) {
  const msg = message.toLowerCase();
  
  // Saudações
  if (msg.match(/^(oi|olá|ola|eai|e ai|hey|opa|salve)/)) {
    return 'E aí! 🏄‍♂️ Sou o KiteBot, seu parceiro no kite surf!\n\nPosso te ajudar com:\n• Dicas de praias e spots\n• Equipamentos\n• Técnicas e segurança\n• Condições de vento\n\nManda sua pergunta! 🤙';
  }
  
  // Praias/Spots
  if (msg.match(/(praia|spot|onde|lugar|local|ceara|ceará|cumbuco|jeri|jericoacoara)/)) {
    if (msg.includes('ceara') || msg.includes('ceará') || msg.includes('cumbuco')) {
      return '🏖️ O Ceará é a MECA do kite no Brasil!\n\n**Top spots:**\n• **Cumbuco** - O mais famoso! Vento constante, infraestrutura completa\n• **Jericoacoara** - Paraíso! Lagoas flat + mar\n• **Ilha do Guajiru** - Flat water perfeito pra manobras\n• **Paracuru** - Menos lotado, ótimo vento\n\n**Melhor época:** Julho a Dezembro (ventos mais fortes)\n**Vento médio:** 18-25 nós\n\nQual desses te interessa mais? 🤙';
    }
    if (msg.includes('jeri')) {
      return '🌴 **Jericoacoara** é um sonho!\n\n• **Lagoa Azul/Paraíso** - Flat water, perfeito pra treinar\n• **Praia de Jeri** - Ondas + vento\n• **Guriú** - Menos gente, natureza preservada\n\n**Dica:** A lagoa seca em alguns meses. Melhor época: Set-Dez\n\n**Infraestrutura:** Pousadas, restaurantes, escolas de kite\n\nJá conhece ou vai ser sua primeira vez? 🏄‍♂️';
    }
    return '🏖️ **Melhores spots do Brasil:**\n\n**Nordeste (ventos fortes):**\n• Cumbuco (CE) - Meca do kite\n• Jericoacoara (CE) - Lagoas + mar\n• Barra Grande (PI) - Vento constante\n• São Miguel do Gostoso (RN)\n• Atins (MA) - Lençóis Maranhenses\n\n**Sudeste/Sul:**\n• Arraial do Cabo (RJ) - Ondas\n• Ilha do Mel (PR)\n• Florianópolis (SC)\n\nQual região te interessa? 🤙';
  }
  
  // Equipamentos
  if (msg.match(/(equipamento|kite|prancha|barra|trapezio|trapézio|comprar|tamanho|metros)/)) {
    if (msg.includes('iniciante') || msg.includes('começar') || msg.includes('comecar')) {
      return '🎓 **Kit iniciante recomendado:**\n\n**Kite:** 9-12m² (depende do seu peso)\n• Tipo: Híbrido ou Delta (mais estável)\n• Marcas: Duotone, Core, Cabrinha, F-One\n\n**Prancha:** Twin-tip 136-145cm\n• Maior = mais fácil\n\n**Trapézio:** Tipo seat (cadeirinha) pro começo\n\n**Investimento:** R$8-15k (kit usado)\n\n**Dica:** Faça aulas primeiro! Não compre antes de ter umas 10h de prática 🤙';
    }
    if (msg.includes('tamanho') || msg.includes('metros') || msg.match(/\d+m/)) {
      return '📏 **Guia de tamanho do kite:**\n\n**Por peso (vento médio 18-22 nós):**\n• 55-65kg → 7-9m²\n• 65-75kg → 9-11m²\n• 75-85kg → 10-12m²\n• 85-95kg → 12-14m²\n• 95kg+ → 14-17m²\n\n**Quiver ideal:** 2-3 kites\n• Vento fraco: +3m² do seu tamanho base\n• Vento forte: -3m² do seu tamanho base\n\nQual seu peso? Posso ser mais específico! 🏄‍♂️';
    }
    return '🛒 **Equipamentos de kite:**\n\n**Essenciais:**\n• Kite (pipa) - 9-12m² mais versátil\n• Barra de controle\n• Prancha twin-tip\n• Trapézio (seat ou waist)\n• Colete salva-vidas\n• Capacete\n\n**Marcas top:** Duotone, Core, Cabrinha, F-One, North, Slingshot\n\n**Onde comprar:**\n• Usado: Classificados do app (em breve!)\n• Novo: Lojas especializadas\n\nQuer dicas específicas de algum equipamento? 🤙';
  }
  
  // Vento/Condições
  if (msg.match(/(vento|nós|nos|condicao|condição|previsao|previsão|quando|melhor epoca|melhor época)/)) {
    return '💨 **Condições ideais para kite:**\n\n**Vento:**\n• Mínimo: 12 nós (iniciante com kite grande)\n• Ideal: 15-25 nós\n• Avançado: 25-35 nós\n\n**Melhor época no Nordeste:**\n• Jul-Dez: Ventos fortes (20-30 nós)\n• Jan-Jun: Mais fraco mas ainda rola\n\n**Apps de previsão:**\n• Windy\n• Windguru\n• Windfinder\n\n**Dica:** Cheque a direção do vento - side-shore é o ideal! 🌊';
  }
  
  // Segurança
  if (msg.match(/(seguranca|segurança|perigo|cuidado|acidente|medo)/)) {
    return '⚠️ **Segurança no kite é PRIORIDADE!**\n\n**Regras de ouro:**\n1. NUNCA vá sozinho\n2. Use colete e capacete\n3. Conheça o sistema de quick-release\n4. Cheque equipamento antes\n5. Respeite seus limites\n\n**Evite:**\n• Kite em praias lotadas\n• Offshore (vento do mar)\n• Tempestades/raios\n• Áreas com obstáculos\n\n**Dica:** Faça curso com instrutor certificado IKO/BKSA!\n\nSegurança primeiro, diversão depois! 🤙';
  }
  
  // Técnicas/Manobras
  if (msg.match(/(manobra|tecnica|técnica|salto|jump|trick|como fazer|aprender)/)) {
    return '🎯 **Progressão no kite:**\n\n**Básico:**\n1. Body drag (arrastar no corpo)\n2. Water start\n3. Navegar em ambas direções\n4. Subir contra o vento\n\n**Intermediário:**\n5. Transição/jibe\n6. Salto básico\n7. Back roll\n\n**Avançado:**\n8. Handle pass\n9. Kiteloop\n10. Strapless\n\n**Dica:** Domine cada fase antes de avançar!\n\nEm qual nível você está? 🏄‍♂️';
  }
  
  // Aulas/Curso
  if (msg.match(/(aula|curso|escola|aprender|instrutor|professor)/)) {
    return '🎓 **Aprender kitesurf:**\n\n**Curso básico:** 8-12 horas\n• Valor médio: R$1.500-2.500\n• Inclui: equipamento + instrutor\n\n**O que você aprende:**\n• Segurança e auto-resgate\n• Controle do kite\n• Body drag\n• Water start\n• Primeiras navegadas\n\n**Escolas top:**\n• Cumbuco tem dezenas!\n• Procure certificação IKO\n\n**Dica:** Não tente sozinho! É perigoso e você vai demorar muito mais 🤙';
  }
  
  // Default
  return '🏄‍♂️ Boa pergunta!\n\nPosso te ajudar com:\n• **Spots** - Melhores praias do Brasil\n• **Equipamento** - Kites, pranchas, tamanhos\n• **Técnicas** - Do básico ao avançado\n• **Segurança** - Regras importantes\n• **Condições** - Vento e previsão\n• **Aulas** - Onde aprender\n\nSobre o que quer saber? 🤙';
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
