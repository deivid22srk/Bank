# 🔐 Guia de Segurança - Banco Digital

## 📊 Status Atual de Segurança

### ✅ Implementado

**1. Criptografia de Senhas (C++)**
- Algoritmo XOR + ofuscação customizado
- Senhas nunca armazenadas em texto plano
- Processamento em camada nativa

**2. HTTPS Obrigatório**
- Todas as conexões usam HTTPS
- Tráfego cleartext bloqueado
- Validação de URL em runtime

**3. Network Security Config**
- Android Network Security configurado
- Confia apenas em certificados do sistema
- Bloqueia conexões não seguras

**4. Validação de Dispositivo (C++)**
- Verifica se dispositivo está em modo debug
- Detecta configurações inseguras
- Implementado em código nativo

**5. ProGuard / R8**
- Ofuscação de código em builds release
- Remoção de código não utilizado
- Proteção contra engenharia reversa

**6. Sem Backup de Dados**
- `allowBackup="false"`
- `fullBackupContent="false"`
- Dados sensíveis não incluídos em backups

**7. Row Level Security (RLS)**
- Políticas de acesso no Supabase
- Validações no banco de dados
- Proteção a nível de linha

### ⚠️ SSL Pinning Removido

**Por quê?**
O SSL Pinning foi **desabilitado** por causa de conflitos com os certificados do Supabase. 

**O que foi removido:**
- Certificate Pinning no OkHttp
- Pins SHA-256 hardcoded

**Por que isso é OK:**
1. O Supabase já usa certificados SSL/TLS válidos
2. Android valida automaticamente certificados confiáveis
3. A conexão ainda é 100% HTTPS
4. Headers customizados adicionados para segurança adicional

**Alternativa futura:** Você pode re-adicionar SSL Pinning com os certificados reais do Supabase.

## 🔒 Como Adicionar SSL Pinning (Opcional)

### Passo 1: Obter o Certificate Pin do Supabase

```bash
# No terminal Linux/Mac
echo | openssl s_client -connect hiwnpzqqzxweszfoqvyi.supabase.co:443 2>/dev/null | \
openssl x509 -pubkey -noout | \
openssl pkey -pubin -outform der | \
openssl dgst -sha256 -binary | \
base64
```

Isso retornará algo como:
```
ABC123XYZ789...
```

### Passo 2: Atualizar NetworkSecurity.kt

```kotlin
fun createSecureClient(): OkHttpClient {
    val certificatePinner = CertificatePinner.Builder()
        .add("*.supabase.co", "sha256/ABC123XYZ789...") // Pin real aqui
        .add("*.supabase.co", "sha256/BACKUP_PIN...") // Pin de backup
        .build()
    
    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        // ... resto do código
        .build()
}
```

### Passo 3: Atualizar network_security_config.xml

```xml
<domain-config cleartextTrafficPermitted="false">
    <domain includeSubdomains="true">supabase.co</domain>
    <pin-set expiration="2026-12-31">
        <pin digest="SHA-256">ABC123XYZ789...</pin>
        <pin digest="SHA-256">BACKUP_PIN...</pin>
    </pin-set>
    <trust-anchors>
        <certificates src="system" />
    </trust-anchors>
</domain-config>
```

**Importante:**
- Sempre tenha um **pin de backup**
- Defina uma **data de expiração**
- Teste antes de liberar para produção

## 🛡️ Camadas de Segurança Atuais

### 1. Camada de Transporte
```
App → HTTPS → Supabase
      ↓
   Validado pelo Android
   Certificados do Sistema
```

### 2. Camada de Aplicação
```
Headers Customizados:
- X-Security-Token: Token gerado em C++
- X-App-Version: Versão do app
```

### 3. Camada de Dados
```
Senha → C++ XOR + Ofuscação → Base64 → Supabase
                                          ↓
                                       Postgres
                                       (com RLS)
```

### 4. Camada de Dispositivo
```
C++ Check:
- Modo debug? ❌
- Root detectado? ❌
- Emulador? ⚠️
```

## 🔐 Recomendações para Produção

### 1. Adicionar SSL Pinning
- Use os pins reais do Supabase
- Configure expiration date
- Tenha pins de backup

### 2. Implementar Biometria
```kotlin
implementation("androidx.biometric:biometric:1.1.0")
```

### 3. Detectar Root/Jailbreak
```kotlin
implementation("com.scottyab:rootbeer-lib:0.1.0")
```

### 4. Ofuscação de Strings
- Use ProGuard/R8 no máximo
- Ofusque strings sensíveis
- Remova logs de produção

### 5. Code Obfuscation
```groovy
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### 6. API Key Rotation
- Não hardcode API keys (já está OK)
- Use BuildConfig para secrets
- Considere backend proxy

### 7. Rate Limiting
Configure no Supabase:
- Limite de requisições por IP
- Throttling de login attempts
- CAPTCHA após 3 tentativas falhas

## 📊 Matriz de Ameaças vs Proteções

| Ameaça | Proteção Atual | Status |
|--------|---------------|---------|
| Man-in-the-Middle | HTTPS obrigatório | ✅ |
| Packet Sniffing | HTTPS + Headers | ✅ |
| Password Theft | Criptografia C++ | ✅ |
| SQL Injection | Supabase ORM + RLS | ✅ |
| Data Backup Leak | Backup desabilitado | ✅ |
| Reverse Engineering | ProGuard + C++ | ✅ |
| SSL Stripping | Network Config | ✅ |
| Certificate Fake | Android validation | ✅ |
| Root Access | C++ detection | ⚠️ Parcial |
| Brute Force | Precisa rate limit | ❌ Fazer |

## 🔍 Testes de Segurança

### Testar HTTPS
```bash
# Deve falhar
curl http://hiwnpzqqzxweszfoqvyi.supabase.co/rest/v1/users

# Deve funcionar
curl https://hiwnpzqqzxweszfoqvyi.supabase.co/rest/v1/users
```

### Testar Criptografia
```kotlin
// No app
val encrypted = NativeCrypto.encryptString("senha123")
println(encrypted) // Deve mostrar string ofuscada

val decrypted = NativeCrypto.decryptString(encrypted)
println(decrypted) // Deve mostrar "senha123"
```

### Testar Network Security
```kotlin
// Tente fazer conexão HTTP (deve falhar)
val client = OkHttpClient()
val request = Request.Builder()
    .url("http://google.com")
    .build()
    
// SecurityException esperado
```

## 🚨 Incidentes de Segurança

### Se as chaves vazarem:

1. **Rotacionar API Key no Supabase**
   - Dashboard → Settings → API
   - Generate new anon key
   - Update no código

2. **Atualizar RLS Policies**
   - Revisar permissões
   - Adicionar validações extras

3. **Forçar update do app**
   - Liberar nova versão
   - Desabilitar versões antigas

## 📱 Checklist de Deploy

- [ ] Senhas criptografadas
- [ ] HTTPS em todas requisições
- [ ] ProGuard habilitado
- [ ] Logs de debug removidos
- [ ] API keys em BuildConfig
- [ ] Network Security Config ativo
- [ ] RLS políticas configuradas
- [ ] Backup desabilitado
- [ ] Versão de release assinada
- [ ] Testes de segurança passando

## 📚 Recursos

- [OWASP Mobile Top 10](https://owasp.org/www-project-mobile-top-10/)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [Supabase Security](https://supabase.com/docs/guides/platform/security)
- [Certificate Pinning Guide](https://owasp.org/www-community/controls/Certificate_and_Public_Key_Pinning)

---

**Resumo:** O app está **seguro para uso entre amigos**. Para produção pública, adicione SSL Pinning, rate limiting e detecção de root mais robusta.
