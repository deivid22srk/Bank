# 🚀 Guia de Configuração - Banco Digital App

## 📋 Pré-requisitos

### Software Necessário
- **Android Studio** (Hedgehog ou superior)
- **JDK 17** ou superior
- **Android SDK 34**
- **NDK** (Android Native Development Kit)
- **CMake** 3.22.1 ou superior

### Conhecimentos Recomendados
- Kotlin básico
- Conceitos de Android
- Firebase básico

## 🔥 Configuração do Firebase

### 1. Criar Projeto Firebase

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Clique em "Adicionar projeto"
3. Nomeie o projeto (ex: "BancoApp")
4. Desabilite Google Analytics (opcional)
5. Clique em "Criar projeto"

### 2. Adicionar App Android

1. No painel do Firebase, clique no ícone Android
2. **Package name**: `com.bancoapp`
3. **App nickname**: BancoApp (opcional)
4. **SHA-1**: Deixe em branco por enquanto
5. Clique em "Registrar app"

### 3. Baixar google-services.json

1. Baixe o arquivo `google-services.json`
2. Substitua o arquivo em: `app/google-services.json`

### 4. Configurar Realtime Database

1. No Firebase Console, vá em "Realtime Database"
2. Clique em "Criar banco de dados"
3. Escolha localização (ex: us-central1)
4. **Modo de segurança**: Comece no modo teste
5. Clique em "Ativar"

### 5. Regras de Segurança do Database

Vá em "Regras" e cole:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null",
        ".write": "auth != null || !data.exists()",
        "password": {
          ".read": "$uid === auth.uid",
          ".write": "$uid === auth.uid"
        },
        "balance": {
          ".validate": "newData.isNumber() && newData.val() >= 0"
        }
      }
    },
    "transactions": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$transactionId": {
        ".validate": "newData.hasChildren(['fromUser', 'toUser', 'amount', 'timestamp'])"
      }
    }
  }
}
```

**Importante**: Para produção, ajuste as regras para maior segurança!

## 🛠️ Configuração do Projeto

### 1. Clone/Extraia o Projeto

```bash
# Se estiver em ZIP
unzip BancoApp.zip
cd BancoApp

# Se estiver no Git
git clone <seu-repositorio>
cd BancoApp
```

### 2. Abrir no Android Studio

1. Abra Android Studio
2. File → Open
3. Selecione a pasta `BancoApp`
4. Aguarde o Gradle sync

### 3. Instalar NDK

1. Tools → SDK Manager
2. Aba "SDK Tools"
3. Marque "NDK (Side by side)"
4. Marque "CMake"
5. Clique em "Apply"

### 4. Build do Projeto

```bash
# Via linha de comando
./gradlew clean build

# Ou no Android Studio
Build → Make Project
```

## 🔐 Configuração de Segurança (Opcional)

### SSL Pinning

Para produção, obtenha os certificados reais:

1. Obter SHA-256 do seu servidor:
```bash
openssl s_client -connect firebaseio.com:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | base64
```

2. Atualize em `app/src/main/res/xml/network_security_config.xml`

### Ofuscação/ProGuard

Para release builds, o ProGuard já está configurado em `proguard-rules.pro`

## 📱 Executar o App

### Emulador

1. Tools → Device Manager
2. Create Device
3. Escolha um dispositivo (ex: Pixel 6)
4. API Level: 34 (Android 14)
5. Finish
6. Run → Run 'app'

### Dispositivo Físico

1. Ative "Opções do desenvolvedor" no Android
2. Ative "Depuração USB"
3. Conecte o dispositivo via USB
4. Autorize o computador no celular
5. Run → Run 'app'

## 🧪 Testar o App

### Primeiro Uso

1. **Registrar usuário**:
   - Nome: `usuario1`
   - Senha: `senha123`
   - Saldo inicial: R$ 1.000,00

2. **Registrar segundo usuário** (em outro dispositivo/emulador):
   - Nome: `usuario2`
   - Senha: `senha456`
   - Saldo inicial: R$ 1.000,00

3. **Fazer transferência**:
   - Login com `usuario1`
   - Ir em "Transferir"
   - Destinatário: `usuario2`
   - Valor: `100`
   - Confirmar

4. **Verificar histórico**:
   - Ambos usuários devem ver a transação
   - Saldos atualizados em tempo real

## 🐛 Solução de Problemas

### Erro: "SDK location not found"

Crie `local.properties`:
```properties
sdk.dir=/caminho/para/Android/Sdk
```

### Erro: "NDK not configured"

1. File → Project Structure
2. SDK Location
3. Android NDK location → Browse
4. Selecione a pasta do NDK

### Erro: "google-services.json missing"

Certifique-se de ter baixado e colocado o arquivo correto em `app/`

### Erro: "Firebase Database permission denied"

1. Verifique as regras no Firebase Console
2. Certifique-se que o modo teste está ativado
3. Verifique a URL do database em `google-services.json`

### App crasha ao iniciar

1. Verifique os logs: `adb logcat | grep BancoApp`
2. Certifique-se que o NDK foi compilado: `./gradlew clean build`
3. Verifique se o `google-services.json` está correto

## 📦 Gerar APK de Produção

### Debug APK (sem assinatura)

```bash
./gradlew assembleDebug
# APK em: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (com assinatura)

1. Gerar keystore:
```bash
keytool -genkey -v -keystore banco-release.keystore -alias banco -keyalg RSA -keysize 2048 -validity 10000
```

2. Criar `keystore.properties` na raiz:
```properties
storePassword=SUA_SENHA
keyPassword=SUA_SENHA_KEY
keyAlias=banco
storeFile=../banco-release.keystore
```

3. Atualizar `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(rootProject.file("keystore.properties")))
        
        storeFile = file(keystoreProperties["storeFile"] as String)
        storePassword = keystoreProperties["storePassword"] as String
        keyAlias = keystoreProperties["keyAlias"] as String
        keyPassword = keystoreProperties["keyPassword"] as String
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ...
    }
}
```

4. Build:
```bash
./gradlew assembleRelease
# APK em: app/build/outputs/apk/release/app-release.apk
```

## 🚀 Distribuição

### Google Play Store

1. Crie uma conta de desenvolvedor ($25)
2. No Play Console, crie um novo app
3. Preencha os detalhes do app
4. Upload do APK/AAB
5. Configure testes internos primeiro
6. Depois lance em produção

### Distribuição Direta (APK)

1. Gere o APK release assinado
2. Compartilhe o arquivo `.apk`
3. Usuários precisam habilitar "Fontes desconhecidas"
4. Instalar o APK manualmente

### Firebase App Distribution (Recomendado para testes)

1. No Firebase Console, vá em "App Distribution"
2. Upload do APK
3. Adicione testadores por email
4. Eles recebem link para download

## 📚 Próximos Passos

- [ ] Implementar autenticação Firebase Auth
- [ ] Adicionar foto de perfil
- [ ] Implementar QR Code para transferências
- [ ] Notificações push para transações
- [ ] Modo escuro
- [ ] Suporte a múltiplas moedas
- [ ] Gráficos de gastos
- [ ] Exportar histórico PDF
- [ ] Biometria para login
- [ ] Recuperação de senha

## 🤝 Suporte

Para problemas ou dúvidas:
- Abra uma issue no GitHub
- Consulte a documentação do Firebase
- Verifique os logs do Android Studio

## 📄 Licença

Este projeto é livre para uso pessoal e educacional.

---

Desenvolvido com ❤️ usando Kotlin + Jetpack Compose + Firebase + C++
