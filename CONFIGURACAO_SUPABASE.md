# 🔧 CONFIGURAÇÃO OBRIGATÓRIA DO SUPABASE

## ⚠️ IMPORTANTE: Execute TODOS os passos abaixo para o app funcionar corretamente!

### 1. Executar o Schema SQL

1. Abra o [Supabase Dashboard](https://supabase.com/dashboard)
2. Selecione seu projeto
3. Vá em **"SQL Editor"** no menu lateral
4. Abra o arquivo `supabase_schema.sql` deste projeto
5. Copie TODO o conteúdo
6. Cole no SQL Editor do Supabase
7. Clique em **"Run"** para executar

### 2. Habilitar Realtime (ESSENCIAL para histórico funcionar)

**Opção A: Via Dashboard (Recomendado)**
1. No Supabase Dashboard, vá em **"Database"** > **"Replication"**
2. Você verá uma lista de todas as tabelas
3. Encontre a tabela **"users"** e clique no toggle para habilitar Realtime
4. Encontre a tabela **"transactions"** e clique no toggle para habilitar Realtime
5. Aguarde alguns segundos para as mudanças serem aplicadas

**Opção B: Via SQL Editor**
```sql
ALTER PUBLICATION supabase_realtime ADD TABLE users;
ALTER PUBLICATION supabase_realtime ADD TABLE transactions;
```

### 3. Verificar Configuração

Execute no SQL Editor:
```sql
-- Verificar se as tabelas foram criadas
SELECT tablename FROM pg_tables WHERE schemaname = 'public';

-- Verificar se Realtime está habilitado
SELECT schemaname, tablename 
FROM pg_publication_tables 
WHERE pubname = 'supabase_realtime';
```

Você deve ver `users` e `transactions` na lista!

### 4. Obter Credenciais do Supabase

1. No Supabase Dashboard, vá em **"Settings"** > **"API"**
2. Copie a **"Project URL"** (algo como: `https://xxxxx.supabase.co`)
3. Copie a **"anon public"** key (uma string longa)
4. Cole essas credenciais no arquivo `local.properties` do seu projeto Android:

```properties
SUPABASE_URL=https://seu-projeto.supabase.co
SUPABASE_KEY=sua-chave-anon-aqui
```

### 5. Testar o App

1. Compile e instale o app
2. Crie duas contas de teste (ex: "usuario1" e "usuario2")
3. Faça login com "usuario1"
4. Faça uma transferência para "usuario2"
5. Verifique:
   - ✅ A transferência foi bem-sucedida sem erro
   - ✅ O saldo foi atualizado em ambas as contas
   - ✅ O histórico mostra a transação
   - ✅ Uma notificação apareceu para "usuario2" (se estiver logado)

### 6. Problemas Comuns

**❌ Erro: "Serializer for class 'Any' is not found"**
- ✅ Corrigido nesta versão! Agora usa classes serializáveis

**❌ Histórico de transações vazio**
- Verifique se habilitou Realtime nas tabelas (Passo 2)
- Execute a query: `SELECT * FROM transactions;` no SQL Editor para ver se as transações estão sendo salvas

**❌ Notificações não aparecem**
- Verifique se concedeu permissão de notificações ao app
- No Android 13+, o app pede permissão automaticamente na primeira vez

**❌ Não consegue fazer login**
- Verifique se executou o schema SQL completo
- Verifique se as credenciais no `local.properties` estão corretas

### 7. Verificar Dados no Supabase

Execute estas queries no SQL Editor para debug:

```sql
-- Ver todos os usuários
SELECT username, balance FROM users;

-- Ver todas as transações
SELECT from_user, to_user, amount, timestamp, status FROM transactions ORDER BY timestamp DESC;

-- Ver transações de um usuário específico
SELECT * FROM transactions WHERE from_user = 'seu_usuario' OR to_user = 'seu_usuario';
```

## 🎉 Pronto!

Agora o app deve funcionar perfeitamente com:
- ✅ Persistência de login
- ✅ Transferências sem erro
- ✅ Histórico de transações atualizado em tempo real
- ✅ Notificações de pagamento recebido
- ✅ Moeda fictícia "Lunares" (◐)

---

**Dúvidas?** Verifique os logs do Android Studio para mais detalhes sobre erros.
