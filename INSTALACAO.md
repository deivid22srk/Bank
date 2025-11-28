# 📱 Guia de Instalação - Banco Seguro

## Para Usuários (Instalar o APK)

### Passo 1: Baixar o APK

- Baixe o arquivo `app-debug.apk` que foi compartilhado com você
- Ou baixe dos GitHub Releases

### Passo 2: Permitir Instalação de Fontes Desconhecidas

#### Android 8.0+

1. Vá em **Configurações** do Android
2. **Segurança & Privacidade**
3. **Instalar apps desconhecidos**
4. Selecione o app que você usará para instalar (ex: Chrome, Arquivos)
5. Ative **Permitir desta fonte**

#### Android 7.0 e anteriores

1. Vá em **Configurações**
2. **Segurança**
3. Ative **Fontes desconhecidas**

### Passo 3: Instalar o APK

1. Abra o arquivo APK baixado
2. Clique em **Instalar**
3. Aguarde a instalação
4. Clique em **Abrir**

### Passo 4: Primeiro Uso

1. Ao abrir o app, você verá a tela de login
2. Clique em **"Criar Conta"**
3. Digite um **nome de usuário** (mínimo 3 caracteres)
4. Digite uma **senha** (mínimo 6 caracteres)
5. Clique em **"Criar Conta"**
6. Pronto! Você começa com R$ 1.000,00

### Passo 5: Transferir para Amigos

1. Seus amigos também precisam instalar o app e criar uma conta
2. Na tela principal, clique no **botão azul** (canto inferior direito)
3. Digite o **nome de usuário** do seu amigo
4. Digite o **valor** que deseja transferir
5. Clique em **"Transferir"**
6. A transferência é instantânea!

## Para Desenvolvedores (Build do Projeto)

### Requisitos

- **Android Studio** 2023.1+
- **JDK 17**
- **Android SDK** (API 26+)
- **Android NDK** r25c+
- **Rust** (stable)

### Configuração

1. Clone o repositório:
```bash
git clone <repo-url>
cd BancoSeguro
```

2. Instale o Rust:
```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

3. Adicione targets Android:
```bash
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android i686-linux-android
```

4. Configure o NDK:
```bash
# No Android Studio: Tools > SDK Manager > SDK Tools > NDK
export ANDROID_NDK_HOME=/path/to/ndk
```

5. Build a biblioteca nativa:
```bash
cd app/src/main/rust
chmod +x build.sh
./build.sh
```

6. Build o APK:
```bash
cd ../../../..
./gradlew assembleDebug
```

O APK estará em: `app/build/outputs/apk/debug/app-debug.apk`

### Usando GitHub Actions

1. Faça push para o GitHub
2. Vá em **Actions**
3. Aguarde o build completar
4. Baixe o APK dos **Artifacts**

## Solução de Problemas

### "Não é possível instalar o app"

- Certifique-se de que permitiu fontes desconhecidas
- Verifique se há espaço suficiente (50 MB)
- Tente desinstalar versões antigas primeiro

### "O app não abre"

- Verifique se seu Android é 8.0+ (API 26+)
- Tente limpar dados do app: Configurações > Apps > Banco Seguro > Limpar dados
- Reinstale o app

### "Não consigo transferir para meus amigos"

- Verifique se você e seus amigos estão conectados à internet
- Certifique-se de que digitou o nome de usuário correto
- Verifique se você tem saldo suficiente

### "Esqueci minha senha"

- Infelizmente, não há recuperação de senha
- Você precisará desinstalar e reinstalar o app
- Isso criará uma nova conta com novo saldo inicial

## Dicas de Uso

1. **Anote sua senha**: Não há recuperação de senha!
2. **Nome de usuário único**: Escolha um nome fácil para seus amigos lembrarem
3. **Mesma rede**: Para melhor performance P2P, use na mesma WiFi
4. **Notificações**: Permita notificações para ser avisado de transações
5. **Backup**: Não há backup automático, tome cuidado ao desinstalar

## Suporte

Se encontrar problemas:
1. Leia este guia completamente
2. Verifique os **Problemas Conhecidos** no README.md
3. Abra uma **Issue** no GitHub (se for desenvolvedor)

---

**Aproveite seu banco digital!** 💚🏦
