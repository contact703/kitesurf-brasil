require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { initDatabase } = require('./models/database');

// Routes
const chatRoutes = require('./routes/chat');
const spotsRoutes = require('./routes/spots');
const classifiedsRoutes = require('./routes/classifieds');
const usersRoutes = require('./routes/users');
const feedRoutes = require('./routes/feed');
const messagesRoutes = require('./routes/messages');
const forumRoutes = require('./routes/forum');
const accommodationsRoutes = require('./routes/accommodations');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Initialize database
initDatabase();

// Routes
app.use('/api/chat', chatRoutes);
app.use('/api/spots', spotsRoutes);
app.use('/api/classifieds', classifiedsRoutes);
app.use('/api/users', usersRoutes);
app.use('/api/feed', feedRoutes);
app.use('/api/messages', messagesRoutes);
app.use('/api/forum', forumRoutes);
app.use('/api/accommodations', accommodationsRoutes);

// Privacy Policy page
app.get('/privacy', (req, res) => {
  res.send(`<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Política de Privacidade - Kite-me</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
    .container { max-width: 800px; margin: 0 auto; padding: 40px 20px; }
    .card { background: white; border-radius: 16px; padding: 40px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
    h1 { color: #1D4E5F; margin-bottom: 10px; font-size: 28px; }
    h2 { color: #1D4E5F; margin-top: 30px; margin-bottom: 15px; font-size: 20px; border-bottom: 2px solid #3DBDB5; padding-bottom: 8px; }
    h3 { color: #1D4E5F; margin-top: 20px; margin-bottom: 10px; font-size: 16px; }
    p { margin-bottom: 15px; }
    ul { margin-left: 20px; margin-bottom: 15px; }
    li { margin-bottom: 8px; }
    .updated { color: #666; font-size: 14px; margin-bottom: 30px; }
    .logo { width: 60px; height: 60px; margin-bottom: 20px; }
    .contact { background: #f0f9f8; padding: 20px; border-radius: 8px; margin-top: 30px; }
    .contact strong { color: #1D4E5F; }
  </style>
</head>
<body>
  <div class="container">
    <div class="card">
      <h1>🪁 Política de Privacidade - Kite-me</h1>
      <p class="updated">Última atualização: 19 de Fevereiro de 2026</p>
      
      <h2>1. Introdução</h2>
      <p>O aplicativo Kite-me ("nós", "nosso" ou "aplicativo") respeita sua privacidade e está comprometido em proteger seus dados pessoais. Esta política de privacidade explica como coletamos, usamos e protegemos suas informações.</p>
      
      <h2>2. Informações que Coletamos</h2>
      <h3>2.1 Informações fornecidas por você:</h3>
      <ul>
        <li><strong>Dados de cadastro:</strong> nome, email, telefone (opcional)</li>
        <li><strong>Perfil:</strong> foto, biografia, nível de experiência, localização</li>
        <li><strong>Conteúdo:</strong> posts, comentários, mensagens, avaliações de spots</li>
        <li><strong>Classificados:</strong> informações de anúncios que você criar</li>
      </ul>
      <h3>2.2 Informações coletadas automaticamente:</h3>
      <ul>
        <li><strong>Dados de uso:</strong> interações com o app, funcionalidades utilizadas</li>
        <li><strong>Informações do dispositivo:</strong> modelo, sistema operacional, identificadores</li>
      </ul>
      
      <h2>3. Como Usamos suas Informações</h2>
      <p>Utilizamos suas informações para:</p>
      <ul>
        <li>Fornecer e melhorar nossos serviços</li>
        <li>Permitir interação entre usuários (rede social, chat, fórum)</li>
        <li>Exibir spots, classificados e pousadas relevantes</li>
        <li>Fornecer suporte através do KiteBot (assistente IA)</li>
        <li>Enviar notificações sobre atividades relevantes</li>
        <li>Garantir a segurança da plataforma</li>
      </ul>
      
      <h2>4. Compartilhamento de Informações</h2>
      <p><strong>Não vendemos seus dados pessoais.</strong></p>
      <p>Podemos compartilhar informações com:</p>
      <ul>
        <li>Outros usuários do app (conforme suas configurações de privacidade)</li>
        <li>Provedores de serviços que nos ajudam a operar o app</li>
        <li>Autoridades legais quando exigido por lei</li>
      </ul>
      
      <h2>5. Segurança dos Dados</h2>
      <p>Implementamos medidas de segurança para proteger suas informações:</p>
      <ul>
        <li>Criptografia de dados em trânsito (HTTPS)</li>
        <li>Armazenamento seguro de senhas</li>
        <li>Acesso restrito aos dados</li>
      </ul>
      
      <h2>6. Seus Direitos</h2>
      <p>Você tem direito a:</p>
      <ul>
        <li>Acessar seus dados pessoais</li>
        <li>Corrigir informações incorretas</li>
        <li>Solicitar exclusão de seus dados</li>
        <li>Exportar seus dados</li>
      </ul>
      <p>Para exercer esses direitos, entre em contato: contact@titaniofilms.com</p>
      
      <h2>7. Retenção de Dados</h2>
      <p>Mantemos seus dados enquanto sua conta estiver ativa. Após exclusão da conta, removemos seus dados em até 30 dias, exceto quando necessário para cumprir obrigações legais.</p>
      
      <h2>8. Menores de Idade</h2>
      <p>O app é destinado a maiores de 18 anos. Não coletamos intencionalmente dados de menores.</p>
      
      <h2>9. Alterações nesta Política</h2>
      <p>Podemos atualizar esta política periodicamente. Notificaremos sobre mudanças significativas através do app.</p>
      
      <div class="contact">
        <h2 style="margin-top: 0;">10. Contato</h2>
        <p>Para dúvidas sobre esta política:</p>
        <p><strong>Email:</strong> contact@titaniofilms.com</p>
        <p><strong>Empresa:</strong> Titanio Films</p>
        <p><strong>Localização:</strong> Belo Horizonte, MG, Brasil</p>
      </div>
      
      <p style="margin-top: 30px; text-align: center; color: #666; font-size: 14px;">© 2026 Titanio Films. Todos os direitos reservados.</p>
    </div>
  </div>
</body>
</html>`);
});

// Delete Account page
app.get('/delete-account', (req, res) => {
  res.send(`<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Excluir Conta - Kite-me</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
    .container { max-width: 600px; margin: 0 auto; padding: 40px 20px; }
    .card { background: white; border-radius: 16px; padding: 40px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
    h1 { color: #1D4E5F; margin-bottom: 10px; font-size: 28px; }
    h2 { color: #1D4E5F; margin-top: 30px; margin-bottom: 15px; font-size: 20px; }
    p { margin-bottom: 15px; }
    ul { margin-left: 20px; margin-bottom: 15px; }
    li { margin-bottom: 8px; }
    .subtitle { color: #666; font-size: 16px; margin-bottom: 30px; }
    .warning { background: #fff3cd; border: 1px solid #ffc107; padding: 20px; border-radius: 8px; margin: 20px 0; }
    .warning-title { color: #856404; font-weight: bold; margin-bottom: 10px; }
    .steps { background: #e8f5f3; padding: 25px; border-radius: 8px; margin: 20px 0; }
    .step { display: flex; margin-bottom: 15px; }
    .step-number { background: #1D4E5F; color: white; width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-right: 15px; font-weight: bold; flex-shrink: 0; }
    .contact { background: #f0f9f8; padding: 20px; border-radius: 8px; margin-top: 30px; }
    .contact strong { color: #1D4E5F; }
    .btn { display: inline-block; background: #1D4E5F; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; margin-top: 15px; }
    .btn:hover { background: #163d4a; }
  </style>
</head>
<body>
  <div class="container">
    <div class="card">
      <h1>🪁 Excluir Conta - Kite-me</h1>
      <p class="subtitle">Solicitação de exclusão de conta e dados pessoais</p>
      
      <h2>Como excluir sua conta</h2>
      
      <div class="steps">
        <div class="step">
          <span class="step-number">1</span>
          <div>
            <strong>Pelo aplicativo (recomendado)</strong><br>
            Acesse Perfil → Configurações → Conta → Excluir minha conta
          </div>
        </div>
        <div class="step">
          <span class="step-number">2</span>
          <div>
            <strong>Por email</strong><br>
            Envie um email para <strong>contact@titaniofilms.com</strong> com o assunto "Excluir conta Kite-me" incluindo o email cadastrado na conta.
          </div>
        </div>
      </div>
      
      <div class="warning">
        <p class="warning-title">⚠️ Atenção</p>
        <p>Ao excluir sua conta, os seguintes dados serão removidos permanentemente:</p>
        <ul>
          <li>Informações de perfil (nome, foto, bio)</li>
          <li>Posts e comentários publicados</li>
          <li>Mensagens enviadas e recebidas</li>
          <li>Anúncios de classificados</li>
          <li>Avaliações de spots</li>
          <li>Histórico de conversas com o KiteBot</li>
        </ul>
      </div>
      
      <h2>Prazo de exclusão</h2>
      <p>Após a solicitação, seus dados serão excluídos em até <strong>30 dias</strong>. Você receberá uma confirmação por email quando o processo for concluído.</p>
      
      <h2>Dados retidos</h2>
      <p>Alguns dados podem ser mantidos por motivos legais:</p>
      <ul>
        <li>Registros de transações (se aplicável)</li>
        <li>Dados necessários para cumprimento de obrigações legais</li>
      </ul>
      
      <div class="contact">
        <h2 style="margin-top: 0;">Precisa de ajuda?</h2>
        <p>Entre em contato conosco:</p>
        <p><strong>Email:</strong> contact@titaniofilms.com</p>
        <p><strong>Empresa:</strong> Titanio Films</p>
        <a href="mailto:contact@titaniofilms.com?subject=Excluir%20conta%20Kite-me" class="btn">Solicitar exclusão por email</a>
      </div>
      
      <p style="margin-top: 30px; text-align: center; color: #666; font-size: 14px;">© 2026 Titanio Films. Todos os direitos reservados.</p>
    </div>
  </div>
</body>
</html>`);
});

// Health check
app.get('/', (req, res) => {
  res.json({
    name: 'KiteSurf Brasil API',
    version: '2.0.0',
    status: 'online',
    endpoints: {
      chat: '/api/chat',
      feed: '/api/feed',
      users: '/api/users',
      spots: '/api/spots',
      classifieds: '/api/classifieds',
      accommodations: '/api/accommodations',
      messages: '/api/messages',
      forum: '/api/forum'
    },
    features: [
      'Rede social com posts e comentários',
      'Mensagens diretas entre usuários',
      'Fórum de discussão',
      'Classificados de equipamentos',
      'Guia de spots e pousadas',
      'Chat IA com KiteBot'
    ]
  });
});

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

// Error handling
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({ error: 'Erro interno do servidor' });
});

app.listen(PORT, () => {
  console.log(`🏄 KiteSurf Brasil API v2.0 running on port ${PORT}`);
  console.log('📱 Endpoints: feed, users, spots, classifieds, accommodations, messages, forum, chat');
});
