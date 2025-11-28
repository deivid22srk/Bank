# 🚀 COMO CONFIGURAR O SUPABASE - GUIA RÁPIDO

## 📋 Passo 1: Acessar o SQL Editor

1. Vá para: **https://supabase.com/dashboard**
2. Faça login na sua conta
3. Selecione o projeto: **hiwnpzqqzxweszfoqvyi**
4. No menu lateral esquerdo, clique em: **SQL Editor**
5. Clique no botão: **New query**

## 📝 Passo 2: Executar o Script SQL

1. Abra o arquivo: **`supabase_schema.sql`**
2. **Copie TODO o conteúdo** do arquivo (Ctrl+A, Ctrl+C)
3. **Cole no SQL Editor** do Supabase (Ctrl+V)
4. Clique no botão **RUN** (ou pressione Ctrl+Enter)

O script vai criar:
- ✅ Tabela `users` (usuários)
- ✅ Tabela `transactions` (transações)
- ✅ Índices para performance
- ✅ Row Level Security (RLS)
- ✅ Função `process_transfer()` para transferências atômicas
- ✅ Triggers automáticos

## ✅ Passo 3: Verificar

1. No menu lateral, clique em: **Table Editor**
2. Você deve ver 2 tabelas:
   - 📊 **users**
   - 📊 **transactions**

3. Clique em **users** para ver a estrutura:
   - `id` (UUID)
   - `username` (texto, único)
   - `password_hash` (texto)
   - `balance` (número decimal)
   - `created_at` (data/hora)
   - `updated_at` (data/hora)

## 🧪 Passo 4: Testar (Opcional)

No **SQL Editor**, execute:

```sql
-- Ver as tabelas
SELECT * FROM users;
SELECT * FROM transactions;

-- Criar 2 usuários de teste
INSERT INTO users (username, password_hash, balance) 
VALUES 
    ('alice', 'senha_teste_alice', 1000.00),
    ('bob', 'senha_teste_bob', 1000.00);

-- Fazer uma transferência de teste
SELECT process_transfer('alice', 'bob', 50.00);

-- Ver os saldos atualizados
SELECT username, balance FROM users;

-- Ver a transação criada
SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 1;
```

## ⚠️ Importante: Habilitar Realtime

Para que o app receba atualizações em tempo real:

1. No menu lateral, vá em: **Database** → **Replication**
2. Encontre a tabela **`users`** e clique no toggle para **habilitar**
3. Encontre a tabela **`transactions`** e clique no toggle para **habilitar**

Agora suas tabelas terão sincronização em tempo real! ⚡

## 🎉 Pronto!

Agora você pode:
1. Abrir o projeto no Android Studio
2. Compilar e rodar o app
3. Criar usuários e fazer transferências

O app já está configurado com as credenciais corretas:
- **URL**: `https://hiwnpzqqzxweszfoqvyi.supabase.co`
- **API Key**: Já incluída no código

## 📚 Mais Informações

- 📖 Documentação completa: **SUPABASE.md**
- 🛠️ Guia de instalação: **SETUP.md**
- 📱 README principal: **README.md**

---

**Dúvidas?** Consulte os arquivos de documentação ou abra uma issue! 🚀
