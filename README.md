# Banco Digital App 🏦

Um aplicativo de banco digital Android completo com moeda virtual baseada no Real Brasileiro.

## ✨ Características

- 🎨 **Interface Bonita**: Design moderno com paleta de cores pastel
- 🔐 **Segurança Avançada**: 
  - Camada nativa C++ para criptografia
  - SSL Pinning
  - Ofuscação de tráfego de rede
  - Validação de dispositivo
- 💸 **Transferências Fáceis**: Sistema simples e seguro de transferência entre usuários
- 📊 **Histórico Completo**: Visualize todas suas transações
- ⚡ **Supabase Backend**: Banco de dados PostgreSQL em tempo real, distribuído e gratuito
- 🚀 **Jetpack Compose**: Interface moderna e responsiva

## 🛠️ Tecnologias

- **Kotlin** - Linguagem principal
- **C++** - Camada nativa de segurança
- **Jetpack Compose** - UI moderna
- **Supabase (PostgreSQL)** - Backend distribuído com realtime
- **OkHttp** - Cliente HTTP seguro com SSL Pinning
- **Material Design 3** - Design system
- **Ktor** - Cliente HTTP para Supabase
- **Kotlinx Serialization** - Serialização de dados

## 📱 Funcionalidades

1. **Autenticação**
   - Login com usuário e senha
   - Registro de novos usuários
   - Sem coleta de dados pessoais

2. **Dashboard**
   - Visualização do saldo em tempo real
   - Interface intuitiva
   - Animações suaves

3. **Transferências**
   - Transferência instantânea entre usuários
   - Validação de saldo
   - Confirmação visual
   - Transações atômicas

4. **Histórico**
   - Todas as transações enviadas e recebidas
   - Organização cronológica
   - Detalhes completos
   - Atualização em tempo real

## 🔧 Configuração Rápida

### 1. Configurar Supabase

Siga as instruções detalhadas em **[SUPABASE.md](SUPABASE.md)**

**Resumo:**
1. Acesse o SQL Editor do Supabase
2. Execute o script `supabase_schema.sql`
3. Verifique que as tabelas foram criadas

### 2. Build do Projeto

```bash
# Clone o repositório
git clone <seu-repositorio>
cd BancoApp

# Compile o projeto
./gradlew assembleDebug

# APK estará em: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Configuração Completa

Para setup detalhado, consulte **[SETUP.md](SETUP.md)**

## 🏗️ Estrutura do Projeto

```
BancoApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── cpp/              # Código C++ nativo
│   │   │   │   ├── native_crypto.cpp
│   │   │   │   ├── network_security.cpp
│   │   │   │   └── CMakeLists.txt
│   │   │   ├── java/com/bancoapp/
│   │   │   │   ├── data/         # Models e Repository
│   │   │   │   ├── security/     # JNI Wrappers
│   │   │   │   ├── ui/           # Compose UI
│   │   │   │   ├── viewmodel/    # ViewModels
│   │   │   │   ├── BancoApplication.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/              # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
├── supabase_schema.sql           # Schema SQL do banco
├── .github/workflows/build.yml   # CI/CD
├── README.md                      # Este arquivo
├── SUPABASE.md                    # Guia do Supabase
└── SETUP.md                       # Guia de instalação
```

## 🔒 Segurança

### Camada de Rede
- SSL Pinning em OkHttp
- Validação de URLs em C++
- Bloqueio de tráfego cleartext
- Headers customizados

### Camada de Dados
- Criptografia XOR + ofuscação em C++
- Senhas nunca em texto plano
- Sem backup automático
- Transações atômicas no banco

### Camada de App
- ProGuard configurado
- Ofuscação de código
- Validação de entrada
- Row Level Security no Supabase

## 📊 Banco de Dados

### Tabelas

**users**
- `id`, `username`, `password_hash`, `balance`, `created_at`, `updated_at`

**transactions**
- `id`, `from_user`, `to_user`, `amount`, `status`, `timestamp`

### Função SQL Customizada

`process_transfer(sender, receiver, amount)` - Transferência atômica com validações

### Tempo Real

- Atualização automática de saldos
- Histórico sincronizado entre dispositivos
- Notificações instantâneas de transações

## 🚀 GitHub Actions

O projeto inclui CI/CD automático que:
- Compila o app em cada push
- Executa testes
- Gera APK debug e release
- Disponibiliza os artifacts para download

## 🎨 Paleta de Cores Pastel

- **Roxo**: #E6CCFF / #CC99FF
- **Azul**: #CCE5FF / #99CCFF  
- **Rosa**: #FFD6E8 / #FFB3D9
- **Verde**: #CCFFDD / #99FFBB
- **Pêssego**: #FFE5CC
- **Amarelo**: #FFF9CC

## 🧪 Testes

### Criar Usuários de Teste

Via SQL Editor do Supabase:

```sql
INSERT INTO users (username, password_hash, balance) 
VALUES 
    ('usuario1', 'hash_teste_1', 1000.00),
    ('usuario2', 'hash_teste_2', 1000.00);
```

### Testar Transferência

```sql
SELECT process_transfer('usuario1', 'usuario2', 100.00);
```

## 📦 Dependências Principais

```kotlin
// Supabase
implementation("io.github.jan-tennert.supabase:postgrest-kt")
implementation("io.github.jan-tennert.supabase:realtime-kt")

// Ktor (HTTP Client)
implementation("io.ktor:ktor-client-android")

// Jetpack Compose
implementation("androidx.compose.material3:material3")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
```

## 📝 Licença

Este projeto é livre para uso pessoal e entre amigos.

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📧 Suporte

- 📖 Consulte [SUPABASE.md](SUPABASE.md) para configuração do banco
- 🛠️ Consulte [SETUP.md](SETUP.md) para instalação detalhada
- 🐛 Abra uma issue para reportar bugs

## 🎯 Roadmap

- [ ] Autenticação com biometria
- [ ] Exportar histórico em PDF
- [ ] Gráficos de gastos
- [ ] QR Code para transferências
- [ ] Notificações push
- [ ] Modo escuro
- [ ] Suporte a múltiplas moedas
- [ ] Backup e restauração

---

Desenvolvido com ❤️ usando Kotlin + Jetpack Compose + Supabase + C++
