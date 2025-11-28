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
- 🔥 **Firebase Backend**: Banco de dados em tempo real distribuído e gratuito
- 🚀 **Jetpack Compose**: Interface moderna e responsiva

## 🛠️ Tecnologias

- **Kotlin** - Linguagem principal
- **C++** - Camada nativa de segurança
- **Jetpack Compose** - UI moderna
- **Firebase Realtime Database** - Backend distribuído
- **OkHttp** - Cliente HTTP seguro com SSL Pinning
- **Material Design 3** - Design system

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

4. **Histórico**
   - Todas as transações enviadas e recebidas
   - Organização cronológica
   - Detalhes completos

## 🔧 Configuração

### Pré-requisitos

- Android Studio Arctic Fox ou superior
- JDK 17
- Android SDK 34
- NDK para compilação C++

### Firebase Setup

1. Crie um projeto no [Firebase Console](https://console.firebase.google.com/)
2. Adicione um app Android com o package name `com.bancoapp`
3. Baixe o arquivo `google-services.json`
4. Substitua o arquivo `app/google-services.json` pelo seu

### Build

```bash
# Clone o repositório
git clone <seu-repositorio>

# Entre na pasta
cd BancoApp

# Compile o projeto
./gradlew assembleDebug

# APK estará em: app/build/outputs/apk/debug/app-debug.apk
```

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
│   │   │   │   │   ├── screens/
│   │   │   │   │   └── theme/
│   │   │   │   ├── viewmodel/    # ViewModels
│   │   │   │   ├── BancoApplication.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/              # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── google-services.json
├── .github/
│   └── workflows/
│       └── build.yml             # GitHub Actions
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🔒 Segurança

### Camada Nativa C++

- **Criptografia**: Algoritmo customizado de ofuscação
- **Validação de Dispositivo**: Verifica se o dispositivo é seguro
- **Proteção de Endpoint**: Valida todas as conexões de rede

### Network Security

- **SSL Pinning**: Previne ataques man-in-the-middle
- **Network Security Config**: Bloqueia tráfego cleartext
- **Headers Customizados**: Token de segurança em todas requisições

### Dados

- **Senhas Criptografadas**: Nunca armazenadas em texto plano
- **Sem Backup**: Dados não são incluídos em backups do sistema
- **Criptografia End-to-End**: Dados sensíveis sempre criptografados

## 🚀 GitHub Actions

O projeto inclui CI/CD automático que:

1. Compila o app em cada push
2. Executa testes
3. Gera APK debug e release
4. Disponibiliza os artifacts para download

## 📝 Licença

Este projeto é livre para uso pessoal e entre amigos.

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📧 Contato

Para dúvidas ou sugestões, abra uma issue no repositório.

## 🎨 Screenshots

_Adicione screenshots do seu app aqui quando estiver rodando!_

---

Desenvolvido com ❤️ usando Kotlin, Jetpack Compose e C++
