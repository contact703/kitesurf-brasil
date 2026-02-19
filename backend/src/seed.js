const { getDb, initDatabase } = require('./models/database');

function seed() {
  console.log('🌱 Iniciando seed do banco de dados...');
  
  initDatabase();
  const db = getDb();
  
  // Limpar dados existentes
  db.exec('DELETE FROM forum_replies');
  db.exec('DELETE FROM forum_topics');
  db.exec('DELETE FROM forum_categories');
  db.exec('DELETE FROM messages');
  db.exec('DELETE FROM conversations');
  db.exec('DELETE FROM comments');
  db.exec('DELETE FROM likes');
  db.exec('DELETE FROM follows');
  db.exec('DELETE FROM posts');
  db.exec('DELETE FROM spot_reviews');
  db.exec('DELETE FROM classifieds');
  db.exec('DELETE FROM accommodations');
  db.exec('DELETE FROM spots');
  db.exec('DELETE FROM users');
  
  // ========== USUÁRIOS ==========
  console.log('👥 Criando usuários...');
  
  const users = [
    { name: 'Pedro Kiter', username: 'pedrokiter', bio: '🏄‍♂️ Kitesurfista há 8 anos | Cumbuco é minha casa | Instrutor IKO Level 2', level: 'avançado', location: 'Fortaleza, CE', instagram: '@pedrokiter', verified: 1, avatar_url: 'https://randomuser.me/api/portraits/men/32.jpg' },
    { name: 'Marina Waves', username: 'marinawaves', bio: 'Apaixonada pelo vento e pelo mar 🌊 | Jeri lover | Fotógrafa de kite', level: 'intermediário', location: 'São Paulo, SP', instagram: '@marinawaves', verified: 1, avatar_url: 'https://randomuser.me/api/portraits/women/44.jpg' },
    { name: 'Lucas Nordeste', username: 'lucasnordeste', bio: 'Vivendo o sonho no Ceará ☀️ | Kitesurf + Windsurf | Sempre no rolê', level: 'avançado', location: 'Cumbuco, CE', instagram: '@lucasnordeste', verified: 0, avatar_url: 'https://randomuser.me/api/portraits/men/45.jpg' },
    { name: 'Julia Vento', username: 'juliavento', bio: 'Iniciante empolgada! 🪁 Começando essa jornada incrível', level: 'iniciante', location: 'Rio de Janeiro, RJ', instagram: '@juliavento', verified: 0, avatar_url: 'https://randomuser.me/api/portraits/women/68.jpg' },
    { name: 'Rafael Pro', username: 'rafaelpro', bio: '🏆 Atleta profissional | 3x campeão brasileiro | Embaixador Duotone', level: 'profissional', location: 'Natal, RN', instagram: '@rafaelprokite', verified: 1, avatar_url: 'https://randomuser.me/api/portraits/men/22.jpg' },
    { name: 'Carla Beach', username: 'carlabeach', bio: 'Vida de praia 🏖️ | Kite + Yoga | Instrutora em Jeri', level: 'avançado', location: 'Jericoacoara, CE', instagram: '@carlabeach', verified: 0, avatar_url: 'https://randomuser.me/api/portraits/women/33.jpg' },
    { name: 'Thiago Wind', username: 'thiagowind', bio: 'Engenheiro de dia, kitesurfista nos finais de semana 💨', level: 'intermediário', location: 'Belo Horizonte, MG', instagram: '@thiagowind', verified: 0, avatar_url: 'https://randomuser.me/api/portraits/men/55.jpg' },
    { name: 'Amanda Kite', username: 'amandakite', bio: 'Mãe, empresária e kitesurfista 🪁 Provando que dá pra fazer tudo!', level: 'intermediário', location: 'Florianópolis, SC', instagram: '@amandakite', verified: 0, avatar_url: 'https://randomuser.me/api/portraits/women/22.jpg' },
    { name: 'Bruno Radical', username: 'brunoradical', bio: 'Big air é meu negócio 🚀 | Megaloop addict | Patrocinado F-ONE', level: 'profissional', location: 'Caucaia, CE', instagram: '@brunoradical', verified: 1, avatar_url: 'https://randomuser.me/api/portraits/men/67.jpg' },
    { name: 'Fernanda Mar', username: 'fernandamar', bio: 'Descobrindo o kite aos 40 🌅 Nunca é tarde pra começar!', level: 'iniciante', location: 'Salvador, BA', instagram: '@fernandamar', verified: 0, avatar_url: 'https://randomuser.me/api/portraits/women/55.jpg' }
  ];
  
  const insertUser = db.prepare(`
    INSERT INTO users (name, username, bio, level, location, instagram, verified, avatar_url, followers_count, following_count, posts_count)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);
  
  users.forEach((u, i) => {
    insertUser.run(u.name, u.username, u.bio, u.level, u.location, u.instagram, u.verified, u.avatar_url,
      Math.floor(Math.random() * 5000) + 100, Math.floor(Math.random() * 500) + 50, Math.floor(Math.random() * 100) + 5);
  });
  
  // ========== SPOTS ==========
  console.log('🏖️ Criando spots...');
  
  const spots = [
    { name: 'Cumbuco', description: 'A meca do kitesurf no Brasil! Ventos constantes de julho a dezembro, infraestrutura completa com escolas, lojas e restaurantes.', location: 'Caucaia, Ceará', state: 'CE', lat: -3.6263, lng: -38.7355, wind: 'E/SE', months: 'Jul-Dez', difficulty: 'todos', amenities: ['escolas', 'resgate', 'restaurantes', 'hospedagem', 'lojas'] },
    { name: 'Jericoacoara', description: 'Paraíso com lagoas de água doce perfeitas para flat water e praia com ondas. Cenário de tirar o fôlego!', location: 'Jijoca de Jericoacoara, Ceará', state: 'CE', lat: -2.7975, lng: -40.5137, wind: 'E/SE', months: 'Ago-Dez', difficulty: 'intermediário', amenities: ['escolas', 'lagoas', 'restaurantes', 'hospedagem'] },
    { name: 'Barra Grande', description: 'Ventos constantes e fortes o ano todo. Flat water na maré baixa, perfeito para freestyle.', location: 'Cajueiro da Praia, Piauí', state: 'PI', lat: -2.9033, lng: -41.4108, wind: 'E/NE', months: 'Jul-Jan', difficulty: 'todos', amenities: ['escolas', 'resgate', 'pousadas'] },
    { name: 'São Miguel do Gostoso', description: 'Vento forte e constante, águas rasas. Ótimo para aprender e evoluir. Menos lotado que Cumbuco.', location: 'São Miguel do Gostoso, Rio Grande do Norte', state: 'RN', lat: -5.1244, lng: -35.6319, wind: 'E/SE', months: 'Ago-Jan', difficulty: 'iniciante', amenities: ['escolas', 'pousadas', 'restaurantes'] },
    { name: 'Ilha do Guajiru', description: 'Flat water perfeito! Lagoa rasa ideal para manobras e iniciantes. Vento consistente.', location: 'Itarema, Ceará', state: 'CE', lat: -2.9167, lng: -39.9167, wind: 'E/SE', months: 'Jul-Dez', difficulty: 'iniciante', amenities: ['escolas', 'flat water', 'pousadas'] },
    { name: 'Atins', description: 'Porta de entrada para os Lençóis Maranhenses. Cenário único com dunas e lagoas cristalinas.', location: 'Barreirinhas, Maranhão', state: 'MA', lat: -2.5833, lng: -42.7333, wind: 'NE', months: 'Jul-Dez', difficulty: 'intermediário', amenities: ['pousadas', 'passeios', 'natureza'] },
    { name: 'Arraial do Cabo', description: 'O Caribe brasileiro! Águas cristalinas e ondas. Vento menos constante mas cenário incrível.', location: 'Arraial do Cabo, Rio de Janeiro', state: 'RJ', lat: -22.9661, lng: -42.0278, wind: 'NE', months: 'Set-Mar', difficulty: 'avançado', amenities: ['ondas', 'mergulho', 'restaurantes'] },
    { name: 'Lagoa dos Patos', description: 'Maior lagoa do Brasil! Flat water gigante, vento forte no inverno. Água doce.', location: 'São José do Norte, Rio Grande do Sul', state: 'RS', lat: -31.7833, lng: -51.8667, wind: 'NE/SW', months: 'Set-Mar', difficulty: 'todos', amenities: ['flat water', 'espaço'] },
    { name: 'Praia do Preá', description: 'Vizinha de Jeri, menos lotada. Downwind épico até Jericoacoara!', location: 'Cruz, Ceará', state: 'CE', lat: -2.8000, lng: -40.4167, wind: 'E/SE', months: 'Jul-Dez', difficulty: 'intermediário', amenities: ['downwind', 'pousadas', 'tranquilidade'] },
    { name: 'Paracuru', description: 'Spot local com boa infraestrutura. Menos turístico, mais autêntico. Ventos fortes!', location: 'Paracuru, Ceará', state: 'CE', lat: -3.4000, lng: -39.0333, wind: 'E/SE', months: 'Jul-Dez', difficulty: 'todos', amenities: ['escolas', 'local vibe', 'pousadas'] }
  ];
  
  const insertSpot = db.prepare(`
    INSERT INTO spots (name, description, location, state, latitude, longitude, wind_direction, best_months, difficulty, amenities, rating, rating_count, created_by)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);
  
  spots.forEach((s, i) => {
    insertSpot.run(s.name, s.description, s.location, s.state, s.lat, s.lng, s.wind, s.months, s.difficulty, 
      JSON.stringify(s.amenities), (Math.random() * 1.5 + 3.5).toFixed(1), Math.floor(Math.random() * 200) + 20, 1);
  });
  
  // ========== CLASSIFICADOS ==========
  console.log('🛒 Criando classificados...');
  
  const classifieds = [
    { user_id: 1, title: 'Kite Duotone Rebel 12m 2024', description: 'Kite em excelente estado, usado apenas 20 sessões. Acompanha bag original. Vendo por motivo de upgrade.', category: 'kites', price: 8500, condition: 'seminovo', brand: 'Duotone', size: '12m' },
    { user_id: 2, title: 'Prancha Twin Tip Cabrinha 138', description: 'Prancha perfeita para intermediários. Pads e straps novos. Pequenos sinais de uso normal.', category: 'pranchas', price: 2200, condition: 'usado', brand: 'Cabrinha', size: '138cm' },
    { user_id: 3, title: 'Trapézio Mystic Majestic X', description: 'Trapézio top de linha, tamanho M. Usado uma temporada. Muito confortável!', category: 'trapezios', price: 1800, condition: 'seminovo', brand: 'Mystic', size: 'M' },
    { user_id: 5, title: 'Kit Completo Iniciante', description: 'Kite F-One Bandit 10m + Barra + Prancha 140cm + Trapézio. Ideal pra quem tá começando!', category: 'kites', price: 12000, condition: 'usado', brand: 'F-One', size: '10m' },
    { user_id: 4, title: 'Wetsuit Rip Curl 3/2 Feminino', description: 'Roupa de neoprene tamanho P. Usada poucas vezes, como nova!', category: 'roupas', price: 800, condition: 'seminovo', brand: 'Rip Curl', size: 'P' },
    { user_id: 6, title: 'Barra North Click Bar 24m', description: 'Barra universal, funciona com qualquer kite. Linhas novas, chicken loop perfeito.', category: 'acessorios', price: 1500, condition: 'seminovo', brand: 'North', size: '24m' },
    { user_id: 7, title: 'Aulas de Kite em Cumbuco', description: 'Instrutor certificado IKO. Aulas particulares ou em grupo. Material incluso. Consulte valores!', category: 'aulas', price: 350, condition: 'novo', brand: '', size: '' },
    { user_id: 8, title: 'Kite Core XR7 9m', description: 'Kite de ondas, excelente pra wave riding. Estado impecável, zero reparos.', category: 'kites', price: 7200, condition: 'seminovo', brand: 'Core', size: '9m' },
    { user_id: 9, title: 'Prancha Surfboard 5\'10 Carbono', description: 'Prancha strapless de carbono. Super leve e responsiva. Pra quem curte onda!', category: 'pranchas', price: 4500, condition: 'usado', brand: 'Custom', size: '5\'10' },
    { user_id: 10, title: 'Capacete Gath com viseira', description: 'Capacete de proteção com viseira removível. Tamanho G. Pouco uso.', category: 'acessorios', price: 450, condition: 'seminovo', brand: 'Gath', size: 'G' },
    { user_id: 1, title: 'Foil Completo Moses 633', description: 'Foil de alumínio, asa 633. Perfeito pra começar no foil! Mastro 75cm.', category: 'acessorios', price: 6800, condition: 'usado', brand: 'Moses', size: '633' },
    { user_id: 3, title: 'Bomba Kite Best Double Action', description: 'Bomba dupla ação, infla kite em menos de 2 minutos. Nova na caixa!', category: 'acessorios', price: 180, condition: 'novo', brand: 'Best', size: '' }
  ];
  
  const insertClassified = db.prepare(`
    INSERT INTO classifieds (user_id, title, description, category, price, condition, brand, size, location, status, views_count)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active', ?)
  `);
  
  classifieds.forEach(c => {
    const user = db.prepare('SELECT location FROM users WHERE id = ?').get(c.user_id);
    insertClassified.run(c.user_id, c.title, c.description, c.category, c.price, c.condition, c.brand, c.size, 
      user?.location || 'Brasil', Math.floor(Math.random() * 500) + 10);
  });
  
  // ========== POUSADAS ==========
  console.log('🏨 Criando pousadas...');
  
  const accommodations = [
    { name: 'Pousada Ventos do Cumbuco', description: 'A 50m da praia! Café da manhã incluso, piscina, depósito de kite. Pacotes especiais para kitesurfistas.', location: 'Cumbuco, Caucaia', state: 'CE', near_spots: ['Cumbuco'], amenities: ['wifi', 'piscina', 'café', 'depósito kite', 'ar condicionado'], price_range: '$$', price_min: 180, price_max: 350, phone: '(85) 99999-1111', whatsapp: '5585999991111', instagram: '@ventoscumbuco' },
    { name: 'Vila Kalango', description: 'Pousada boutique em Jericoacoara. Vista pro mar, suítes de luxo. Experiência premium!', location: 'Jericoacoara', state: 'CE', near_spots: ['Jericoacoara', 'Preá'], amenities: ['wifi', 'piscina', 'spa', 'restaurante', 'transfer'], price_range: '$$$', price_min: 800, price_max: 2000, phone: '(88) 99999-2222', whatsapp: '5588999992222', instagram: '@vilakalango' },
    { name: 'Kite House Barra Grande', description: 'Pousada especializada em kitesurfistas! Escola própria, aluguel de equipamento, vibe radical.', location: 'Barra Grande', state: 'PI', near_spots: ['Barra Grande'], amenities: ['wifi', 'escola kite', 'aluguel equip', 'churrasqueira', 'slackline'], price_range: '$$', price_min: 150, price_max: 280, phone: '(86) 99999-3333', whatsapp: '5586999993333', instagram: '@kitehousebg' },
    { name: 'Pousada Maré Alta', description: 'Aconchegante pousada familiar em São Miguel do Gostoso. Ótimo custo-benefício!', location: 'São Miguel do Gostoso', state: 'RN', near_spots: ['São Miguel do Gostoso'], amenities: ['wifi', 'café', 'estacionamento', 'ar condicionado'], price_range: '$', price_min: 100, price_max: 180, phone: '(84) 99999-4444', whatsapp: '5584999994444', instagram: '@mareaalta' },
    { name: 'Rancho do Kite', description: 'Rústico e charmoso! Chalés de madeira a 100m do spot. Ideal pra galera do kite.', location: 'Ilha do Guajiru', state: 'CE', near_spots: ['Ilha do Guajiru'], amenities: ['wifi', 'churrasqueira', 'rede', 'depósito kite'], price_range: '$', price_min: 90, price_max: 150, phone: '(88) 99999-5555', whatsapp: '5588999995555', instagram: '@ranchodokite' },
    { name: 'Atins Lodge', description: 'Eco-lodge nos Lençóis Maranhenses. Sustentável, confortável e com vista única!', location: 'Atins, Barreirinhas', state: 'MA', near_spots: ['Atins'], amenities: ['eco', 'café orgânico', 'passeios', 'yoga'], price_range: '$$$', price_min: 500, price_max: 1200, phone: '(98) 99999-6666', whatsapp: '5598999996666', instagram: '@atinslodge' }
  ];
  
  const insertAccommodation = db.prepare(`
    INSERT INTO accommodations (name, description, location, state, near_spots, amenities, price_range, price_min, price_max, contact_phone, contact_whatsapp, instagram, rating, rating_count, verified)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);
  
  accommodations.forEach(a => {
    insertAccommodation.run(a.name, a.description, a.location, a.state, JSON.stringify(a.near_spots), JSON.stringify(a.amenities),
      a.price_range, a.price_min, a.price_max, a.phone, a.whatsapp, a.instagram,
      (Math.random() * 1 + 4).toFixed(1), Math.floor(Math.random() * 100) + 10, Math.random() > 0.5 ? 1 : 0);
  });
  
  // ========== POSTS (FEED) ==========
  console.log('📱 Criando posts...');
  
  const posts = [
    { user_id: 1, content: 'Sessão épica hoje em Cumbuco! 🏄‍♂️ Vento perfeito, 22 nós constante. Quem mais tava lá?', spot_id: 1, likes: 234, comments: 18 },
    { user_id: 5, content: 'Treino pesado hoje! Trabalhando no megaloop 🚀 Aos poucos vai saindo... #kitelife #bigair', spot_id: 1, likes: 567, comments: 45 },
    { user_id: 2, content: 'Jericoacoara, você é demais! 😍 Lagoa Azul tava perfeita hoje. Flat water + pôr do sol = combinação perfeita', spot_id: 2, likes: 891, comments: 67 },
    { user_id: 6, content: 'Primeira vez ensinando uma turma de 5 alunos! Todos conseguiram fazer water start 🙌 Orgulho define!', spot_id: 2, likes: 345, comments: 28 },
    { user_id: 3, content: 'Alguém mais pegou essa rajada de 30 nós ontem? Meu kite 7m tava voando! 💨', spot_id: 1, likes: 123, comments: 15 },
    { user_id: 4, content: 'Uma semana aprendendo kite e já estou apaixonada! 🪁 Dica: não desistam nas primeiras tentativas, vale muito a pena!', spot_id: 4, likes: 456, comments: 52 },
    { user_id: 7, content: 'Escapada de fim de semana pra Arraial! Água cristalina, vento inconstante mas valeu demais 🌊', spot_id: 7, likes: 278, comments: 21 },
    { user_id: 8, content: 'Mãe solo que vela! 👩‍👧 Levei minha filha pra ver mamãe no kite hoje. Ela ficou impressionada haha', spot_id: null, likes: 712, comments: 89 },
    { user_id: 9, content: 'Testando o novo Core XR8! Review completo em breve, mas já adianto: ANIMAL 🔥', spot_id: 1, likes: 934, comments: 76 },
    { user_id: 10, content: 'Aos 42 anos fiz minha primeira navegada sozinha! 🎉 Prova de que idade é só um número. Bora, galera!', spot_id: 4, likes: 1205, comments: 134 }
  ];
  
  const insertPost = db.prepare(`
    INSERT INTO posts (user_id, content, spot_id, likes_count, comments_count)
    VALUES (?, ?, ?, ?, ?)
  `);
  
  posts.forEach(p => {
    insertPost.run(p.user_id, p.content, p.spot_id, p.likes, p.comments);
  });
  
  // ========== FOLLOWS ==========
  console.log('👥 Criando follows...');
  
  const insertFollow = db.prepare('INSERT OR IGNORE INTO follows (follower_id, following_id) VALUES (?, ?)');
  
  // Criar algumas conexões entre usuários
  for (let i = 1; i <= 10; i++) {
    for (let j = 1; j <= 10; j++) {
      if (i !== j && Math.random() > 0.5) {
        insertFollow.run(i, j);
      }
    }
  }
  
  // ========== FÓRUM ==========
  console.log('💬 Criando fórum...');
  
  const categories = [
    { name: 'Equipamentos', description: 'Discussões sobre kites, pranchas, trapézios e acessórios', icon: '🪁', color: '#2196F3' },
    { name: 'Spots e Viagens', description: 'Dicas de destinos, relatos de viagem e condições dos spots', icon: '🏖️', color: '#4CAF50' },
    { name: 'Técnicas e Manobras', description: 'Aprenda e ensine técnicas, do básico ao avançado', icon: '🎯', color: '#FF9800' },
    { name: 'Compra e Venda', description: 'Negocie equipamentos (anúncios vão pros Classificados)', icon: '💰', color: '#9C27B0' },
    { name: 'Segurança', description: 'Discussões sobre segurança, auto-resgate e prevenção', icon: '⚠️', color: '#F44336' },
    { name: 'Geral', description: 'Qualquer assunto relacionado a kitesurf', icon: '💬', color: '#607D8B' }
  ];
  
  const insertCategory = db.prepare('INSERT INTO forum_categories (name, description, icon, color) VALUES (?, ?, ?, ?)');
  categories.forEach(c => insertCategory.run(c.name, c.description, c.icon, c.color));
  
  const topics = [
    { category_id: 1, user_id: 1, title: 'Duotone Rebel vs Core XR8: qual escolher?', content: 'Tô em dúvida entre esses dois kites pra freeride/freestyle. Alguém já testou os dois? Qual a opinião de vocês?\n\nMeu nível é intermediário avançado, peso 75kg, vento médio aqui é 18-22 nós.' },
    { category_id: 2, user_id: 2, title: 'Roteiro de 15 dias no Ceará - dicas!', content: 'Vou fazer uma trip de 15 dias pelo Ceará em setembro. Pensei em:\n- 5 dias Cumbuco\n- 5 dias Jeri\n- 5 dias Barra Grande (PI)\n\nO que acham? Dicas de pousadas e logística?' },
    { category_id: 3, user_id: 5, title: 'Tutorial: como fazer seu primeiro back roll', content: 'Galera, depois de muito pedido, fiz um tutorial completo de back roll!\n\n1. Velocidade: mantenha velocidade constante\n2. Edge: carrega bem no edge antes do pop\n3. Olhar: vira a cabeça primeiro\n4. Kite: timing do kite é tudo!\n\nQuem tiver dúvidas, manda aí!' },
    { category_id: 5, user_id: 6, title: 'IMPORTANTE: Auto-resgate - você sabe fazer?', content: 'Pessoal, ontem vi uma situação perigosa no mar. Um iniciante não sabia fazer auto-resgate e quase deu ruim.\n\nVAMOS REVISAR:\n1. Acionar o quick release\n2. Enrolar as linhas no bar\n3. Usar o kite como vela\n\nPRATIQUEM em águas rasas!' },
    { category_id: 1, user_id: 7, title: 'Melhor tamanho de kite para 80kg?', content: 'Pessoal, tenho 80kg e tô montando meu quiver. Pensando em:\n- 9m pra vento forte\n- 12m pra vento médio\n\nTá certo ou preciso de mais tamanhos?' },
    { category_id: 6, user_id: 8, title: 'Mulheres no kite: grupo de WhatsApp', content: 'Oi meninas! 👋\n\nCriei um grupo de WhatsApp só para mulheres kitesurfistas. A ideia é trocar experiências, marcar sessions e se apoiar!\n\nQuem quiser entrar, comenta aqui que mando o link!' }
  ];
  
  const insertTopic = db.prepare(`
    INSERT INTO forum_topics (category_id, user_id, title, content, views_count, replies_count, likes_count, last_reply_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
  `);
  
  topics.forEach(t => {
    insertTopic.run(t.category_id, t.user_id, t.title, t.content, 
      Math.floor(Math.random() * 500) + 50, Math.floor(Math.random() * 30) + 2, Math.floor(Math.random() * 50) + 5);
  });
  
  // Respostas do fórum
  const replies = [
    { topic_id: 1, user_id: 5, content: 'Já testei os dois! O Rebel é mais fácil de pilotar, o XR8 tem mais potência. Pro seu nível, eu iria de XR8.' },
    { topic_id: 1, user_id: 3, content: 'Tenho o Rebel 12m e amo! Super estável e o relaunch é perfeito. Nunca testei o Core mas ouço coisas boas.' },
    { topic_id: 2, user_id: 6, content: 'Roteiro perfeito! Em Jeri, fica na Vila Kalango se puder. Em Barra Grande, o Kite House é sensacional!' },
    { topic_id: 3, user_id: 4, content: 'Obrigada pelo tutorial! Tô travada no back roll há meses. Vou tentar essas dicas semana que vem!' },
    { topic_id: 4, user_id: 1, content: 'Post importantíssimo! Eu pratico auto-resgate todo início de temporada. Nunca se sabe quando vai precisar.' },
    { topic_id: 5, user_id: 9, content: '80kg aqui também! Meu quiver é 8m, 10m e 13m. Funciona perfeito pra 90% das condições.' }
  ];
  
  const insertReply = db.prepare('INSERT INTO forum_replies (topic_id, user_id, content) VALUES (?, ?, ?)');
  replies.forEach(r => insertReply.run(r.topic_id, r.user_id, r.content));
  
  console.log('✅ Seed completo!');
  console.log(`
📊 Dados criados:
- ${users.length} usuários
- ${spots.length} spots
- ${classifieds.length} classificados
- ${accommodations.length} pousadas
- ${posts.length} posts
- ${categories.length} categorias do fórum
- ${topics.length} tópicos do fórum
- ${replies.length} respostas do fórum
  `);
}

// Executar se chamado diretamente
if (require.main === module) {
  seed();
}

module.exports = { seed };
