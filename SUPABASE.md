# 🚀 Configuração do Supabase

## 📋 Informações do Projeto

- **Project URL**: `https://hiwnpzqqzxweszfoqvyi.supabase.co`
- **Anon Key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imhpd25wenFxenh3ZXN6Zm9xdnlpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQzNjI5NDMsImV4cCI6MjA3OTkzODk0M30.ZZRwv26e7PsgLZWmicMlUljT-2TDgYI_GezMw5Jhjro`

## 🗄️ Configurar Banco de Dados

### 1. Acessar o SQL Editor do Supabase

1. Acesse [https://supabase.com/dashboard](https://supabase.com/dashboard)
2. Selecione seu projeto
3. Vá em **SQL Editor** no menu lateral
4. Clique em **New query**

### 2. Executar o Script SQL

Copie todo o conteúdo do arquivo `supabase_schema.sql` e cole no editor SQL do Supabase.

Clique em **Run** ou pressione `Ctrl+Enter` para executar.

### 3. Verificar Tabelas Criadas

No menu lateral, vá em **Table Editor** e você deve ver:
- ✅ `users` - Tabela de usuários
- ✅ `transactions` - Tabela de transações

### 4. O que foi criado:

#### 📊 Tabelas

**users**
- `id` (UUID) - ID único do usuário
- `username` (TEXT) - Nome de usuário único
- `password_hash` (TEXT) - Senha criptografada
- `balance` (DECIMAL) - Saldo do usuário (padrão: R$ 1.000,00)
- `created_at` (TIMESTAMPTZ) - Data de criação
- `updated_at` (TIMESTAMPTZ) - Data de atualização

**transactions**
- `id` (UUID) - ID único da transação
- `from_user` (TEXT) - Usuário que enviou
- `to_user` (TEXT) - Usuário que recebeu
- `amount` (DECIMAL) - Valor transferido
- `status` (TEXT) - Status (completed, pending, failed)
- `timestamp` (TIMESTAMPTZ) - Data e hora da transação

#### 🔒 Segurança (Row Level Security)

- **RLS habilitado** em todas as tabelas
- **Políticas de acesso** configuradas para permitir operações necessárias
- **Validações** de saldo e valores

#### ⚡ Função de Transferência Atômica

`process_transfer(sender_username, receiver_username, transfer_amount)`

Esta função garante que transferências sejam:
- **Atômicas**: Ou tudo acontece, ou nada
- **Seguras**: Valida saldo antes de transferir
- **Consistentes**: Atualiza ambos os usuários simultaneamente

#### 🔍 Índices

Criados para otimizar buscas:
- Por username
- Por transações de usuário
- Por data/hora

## 🧪 Testar o Banco de Dados

### Via SQL Editor

```sql
-- Ver todos os usuários
SELECT * FROM users;

-- Ver todas as transações
SELECT * FROM transactions;

-- Criar usuários de teste
INSERT INTO users (username, password_hash, balance) 
VALUES 
    ('alice', 'hash_teste_1', 1000.00),
    ('bob', 'hash_teste_2', 1000.00);

-- Testar transferência
SELECT process_transfer('alice', 'bob', 50.00);

-- Ver saldos atualizados
SELECT username, balance FROM users;

-- Ver transações
SELECT * FROM transactions ORDER BY timestamp DESC;
```

## 🔐 Segurança

### Row Level Security (RLS)

O RLS está habilitado e configurado para:

1. **Usuários podem se registrar** (INSERT sem autenticação)
2. **Usuários podem ver outros usuários** (para transferências)
3. **Usuários podem atualizar saldos** (via transferências)
4. **Transações são visíveis** para os envolvidos

### Melhorias Futuras de Segurança

Para produção, considere:

```sql
-- Exemplo: Permitir que usuários vejam apenas suas próprias transações
DROP POLICY IF EXISTS "Permitir leitura de transações" ON transactions;

CREATE POLICY "Ver apenas próprias transações"
    ON transactions FOR SELECT
    USING (
        auth.uid() = (SELECT id FROM users WHERE username = from_user)
        OR auth.uid() = (SELECT id FROM users WHERE username = to_user)
    );
```

## 📱 No App Android

As credenciais já estão configuradas em `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://hiwnpzqqzxweszfoqvyi.supabase.co\"")
buildConfigField("String", "SUPABASE_KEY", "\"sua_key_aqui\"")
```

## 🚀 Recursos do Supabase Utilizados

- ✅ **Postgrest**: API REST automática
- ✅ **Realtime**: Atualizações em tempo real
- ✅ **Row Level Security**: Segurança por linha
- ✅ **Stored Procedures**: Funções SQL customizadas
- ✅ **Triggers**: Atualização automática de timestamps

## 📚 Recursos Adicionais

- [Documentação Supabase](https://supabase.com/docs)
- [Supabase Kotlin Client](https://github.com/supabase-community/supabase-kt)
- [PostgreSQL Functions](https://www.postgresql.org/docs/current/sql-createfunction.html)

## 🆘 Problemas Comuns

### Erro: "permission denied for table users"

Execute novamente as políticas RLS:

```sql
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- Recriar políticas (ver arquivo SQL completo)
```

### Erro: "function process_transfer does not exist"

Execute novamente a criação da função no arquivo SQL.

### Realtime não funciona

Certifique-se de que o Realtime está habilitado:

1. Vá em **Database** → **Replication**
2. Habilite replicação para as tabelas `users` e `transactions`

---

Desenvolvido com ❤️ usando Supabase + Kotlin + Jetpack Compose + C++
