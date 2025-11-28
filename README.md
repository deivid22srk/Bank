# Banco Seguro 🏦🔒

Um aplicativo Android de banco digital P2P (peer-to-peer) com moeda virtual baseada no Real Brasileiro. Desenvolvido em Kotlin com segurança de rede implementada em Rust.

## 🌟 Características

- **Moeda Virtual**: Sistema de moeda digital privada baseada no Real (R$)
- **P2P**: Arquitetura peer-to-peer descentralizada sem servidor central
- **Segurança Máxima**: 
  - Criptografia AES-256-GCM implementada em Rust
  - Ofuscação de tráfego de rede
  - Banco de dados criptografado com SQLCipher
  - Hashing de senhas com 10.000 iterações
- **Interface Moderna**: UI bonita construída com Jetpack Compose
- **Transferências Rápidas**: Sistema de transferências instantâneas entre usuários
- **Histórico Completo**: Visualização de todas as transações
- **Sem Dados Pessoais**: Login apenas com usuário e senha

## 🛠️ Tecnologias Utilizadas

### Android
- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI moderna e reativa
- **Room Database** - Persistência de dados
- **SQLCipher** - Criptografia de banco de dados
- **Coroutines** - Programação assíncrona
- **Material Design 3** - Design system

### Segurança (Rust)
- **AES-256-GCM** - Criptografia de dados
- **ChaCha20-Poly1305** - Criptografia alternativa
- **SHA-256** - Hashing de senhas
- **Traffic Obfuscation** - Ofuscação de tráfego de rede

### Networking
- **P2P Service** - Serviço de rede peer-to-peer
- **Encrypted Socket Communication** - Comunicação criptografada
- **Peer Discovery** - Descoberta automática de peers

## 📋 Pré-requisitos

### Para Build Local

1. **Android Studio** (2023.1 ou superior)
2. **JDK 17**
3. **Android SDK** (API 26+)
4. **Android NDK** (r25c ou superior)
5. **Rust** (stable toolchain)

### Rust Targets Android

```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android i686-linux-android
```

## 🚀 Como Buildar

### 1. Build da Biblioteca Nativa Rust

```bash
cd app/src/main/rust
export ANDROID_NDK_HOME=/path/to/ndk
chmod +x build.sh
./build.sh
```

### 2. Build do APK Android

```bash
./gradlew assembleDebug
```

O APK será gerado em: `app/build/outputs/apk/debug/app-debug.apk`

## 📦 Build com GitHub Actions

O projeto inclui um workflow do GitHub Actions (`.github/workflows/build.yml`) que:

1. Configura o ambiente (JDK 17, Rust, Android NDK)
2. Compila a biblioteca nativa Rust para todas as arquiteturas
3. Builda o APK Android
4. Faz upload do APK como artefato

Para usar:
1. Faça push do código para o GitHub
2. O workflow será executado automaticamente
3. Baixe o APK dos artefatos da Action

## 🎯 Como Usar

### Primeiro Uso

1. **Criar Conta**
   - Abra o app
   - Clique em "Criar Conta"
   - Digite um nome de usuário (mínimo 3 caracteres)
   - Digite uma senha (mínimo 6 caracteres)
   - Clique em "Criar Conta"

2. **Saldo Inicial**
   - Toda nova conta começa com R$ 1.000,00

### Fazer Transferências

1. Clique no botão azul de "Enviar" (canto inferior direito)
2. Digite o nome de usuário do destinatário
3. Digite o valor a transferir
4. Clique em "Transferir"
5. A transferência é processada instantaneamente

### Ver Histórico

- O histórico de transações aparece na tela principal
- Transações recebidas aparecem em verde (+)
- Transações enviadas aparecem em vermelho (-)

## 🔒 Segurança

### Criptografia

- **Dados em Repouso**: SQLCipher com AES-256
- **Dados em Trânsito**: AES-256-GCM + Ofuscação
- **Senhas**: SHA-256 com 10.000 iterações + salt

### Privacidade

- Nenhum dado pessoal é coletado
- Apenas nome de usuário e senha são necessários
- Banco de dados local criptografado
- Tráfego de rede ofuscado para evitar análise

### P2P Network

- Comunicação direta entre dispositivos
- Sem servidor central
- Descoberta automática de peers
- Sincronização de transações entre peers

## 📱 Requisitos do Dispositivo

- Android 8.0 (API 26) ou superior
- 50 MB de espaço livre
- Conexão com internet (para P2P)

## 🏗️ Estrutura do Projeto

```
BancoSeguro/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bancoseguro/app/
│   │   │   │   ├── data/          # Models, Database, Repository
│   │   │   │   ├── network/       # P2P Service
│   │   │   │   ├── security/      # Crypto & Storage
│   │   │   │   ├── ui/            # Compose UI
│   │   │   │   ├── viewmodel/     # ViewModels
│   │   │   │   └── MainActivity.kt
│   │   │   ├── rust/              # Rust Native Library
│   │   │   │   ├── src/lib.rs
│   │   │   │   ├── Cargo.toml
│   │   │   │   └── build.sh
│   │   │   └── res/               # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── .github/
│   └── workflows/
│       └── build.yml              # GitHub Actions
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🤝 Distribuição

### Para Amigos

1. **Distribuição Manual**
   - Envie o APK diretamente
   - Ative "Fontes Desconhecidas" no Android
   - Instale o APK

2. **Via GitHub Releases**
   - Crie uma release no GitHub
   - Anexe o APK
   - Compartilhe o link

### Configuração de Peers

Por padrão, o app tenta conectar aos seguintes endereços:
- `10.0.2.2:8888` (emulador)
- `192.168.1.100:8888`
- `192.168.0.100:8888`

Para adicionar peers customizados, edite `P2PService.kt` e adicione os IPs dos dispositivos dos seus amigos.

## ⚠️ Aviso Importante

Este é um aplicativo de **moeda virtual privada** para uso entre amigos. Não é dinheiro real e não tem valor monetário fora do seu grupo. Use apenas para diversão e aprendizado.

## 📄 Licença

Este projeto é fornecido como está, sem garantias. Use por sua conta e risco.

## 🐛 Problemas Conhecidos

- A descoberta P2P funciona melhor na mesma rede local
- Conexões externas podem requerer port forwarding
- O app precisa de permissão de notificação no Android 13+

## 🔮 Futuras Melhorias

- [ ] Suporte a WebRTC para P2P através de NAT
- [ ] Backup e restauração de carteira
- [ ] Múltiplas moedas
- [ ] Gráficos de histórico
- [ ] Suporte a grupos
- [ ] Notificações de transações

## 👨‍💻 Desenvolvimento

Desenvolvido com ❤️ usando Kotlin, Rust e Jetpack Compose.

---

**Divirta-se com seu banco digital!** 🚀💰
