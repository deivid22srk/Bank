-- ============================================
-- BANCO DIGITAL - SUPABASE DATABASE SCHEMA
-- IMPORTANTE: Execute este script no Supabase SQL Editor
-- ============================================

-- 1. Criar tabela de usuários
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 1000.00 CHECK (balance >= 0),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Criar tabela de transações
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_user TEXT NOT NULL,
    to_user TEXT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    status TEXT DEFAULT 'completed' CHECK (status IN ('pending', 'completed', 'failed')),
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_transactions_from_user ON transactions(from_user);
CREATE INDEX IF NOT EXISTS idx_transactions_to_user ON transactions(to_user);
CREATE INDEX IF NOT EXISTS idx_transactions_timestamp ON transactions(timestamp DESC);

-- 4. Criar função para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5. Criar trigger para atualizar updated_at
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 6. Desabilitar RLS temporariamente para facilitar desenvolvimento
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE transactions DISABLE ROW LEVEL SECURITY;

-- 7. Limpar políticas antigas (se existirem)
DROP POLICY IF EXISTS "Permitir criação de usuários" ON users;
DROP POLICY IF EXISTS "Permitir leitura de usuários" ON users;
DROP POLICY IF EXISTS "Permitir atualização de saldo" ON users;
DROP POLICY IF EXISTS "Permitir criação de transações" ON transactions;
DROP POLICY IF EXISTS "Permitir leitura de transações" ON transactions;

-- 8. IMPORTANTE: Habilitar Realtime nas tabelas
-- ATENÇÃO: Após executar este script, você DEVE:
-- 1. Ir em "Database" > "Replication" no Supabase Dashboard
-- 2. Habilitar "Realtime" para as tabelas "users" e "transactions"
-- 3. Ou execute o seguinte comando:

ALTER PUBLICATION supabase_realtime ADD TABLE users;
ALTER PUBLICATION supabase_realtime ADD TABLE transactions;

-- ============================================
-- CONFIGURAÇÃO ADICIONAL NECESSÁRIA
-- ============================================

-- Para garantir que o Realtime funcione, execute também:

-- Verificar se as tabelas estão publicadas
SELECT schemaname, tablename 
FROM pg_publication_tables 
WHERE pubname = 'supabase_realtime';

-- Se as tabelas não aparecerem, tente recriar a publicação:
-- DROP PUBLICATION IF EXISTS supabase_realtime;
-- CREATE PUBLICATION supabase_realtime FOR TABLE users, transactions;

-- ============================================
-- INSTRUÇÕES DE USO
-- ============================================

/*
PASSO A PASSO PARA CONFIGURAR:

1. Abra o Supabase Dashboard (https://supabase.com/dashboard)
2. Selecione seu projeto
3. Vá em "SQL Editor" no menu lateral
4. Cole este script completo
5. Clique em "Run" para executar
6. Vá em "Database" > "Replication" 
7. Encontre as tabelas "users" e "transactions"
8. Clique no botão de toggle para habilitar "Realtime" em ambas
9. Pronto! O histórico de transações deve funcionar agora

VERIFICAÇÃO:
- Execute: SELECT * FROM users;
- Execute: SELECT * FROM transactions;
- Ambas as tabelas devem aparecer vazias (ou com dados de teste)

TESTE:
- Crie uma conta no app
- Faça uma transferência
- Verifique se a transação aparece: SELECT * FROM transactions;
- O histórico deve aparecer no app automaticamente
*/

-- ============================================
-- FIM DO SCHEMA
-- ============================================
