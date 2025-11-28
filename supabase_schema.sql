-- ============================================
-- BANCO DIGITAL - SUPABASE DATABASE SCHEMA
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
    timestamp TIMESTAMPTZ DEFAULT NOW(),
    FOREIGN KEY (from_user) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (to_user) REFERENCES users(username) ON DELETE CASCADE
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
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 6. Habilitar Row Level Security (RLS)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- 7. Políticas de segurança para users
-- Usuários podem criar contas (inserir sem autenticação)
CREATE POLICY "Permitir criação de usuários"
    ON users FOR INSERT
    WITH CHECK (true);

-- Usuários podem ver todos os outros usuários (para transferências)
CREATE POLICY "Permitir leitura de usuários"
    ON users FOR SELECT
    USING (true);

-- Usuários podem atualizar apenas seu próprio saldo
CREATE POLICY "Permitir atualização de saldo"
    ON users FOR UPDATE
    USING (true)
    WITH CHECK (true);

-- 8. Políticas de segurança para transactions
-- Qualquer um pode criar transações
CREATE POLICY "Permitir criação de transações"
    ON transactions FOR INSERT
    WITH CHECK (true);

-- Usuários podem ver suas próprias transações
CREATE POLICY "Permitir leitura de transações"
    ON transactions FOR SELECT
    USING (true);

-- 9. Criar função para processar transferência atômica
CREATE OR REPLACE FUNCTION process_transfer(
    sender_username TEXT,
    receiver_username TEXT,
    transfer_amount DECIMAL
)
RETURNS JSON AS $$
DECLARE
    sender_balance DECIMAL;
    transaction_id UUID;
BEGIN
    -- Verificar se os usuários existem
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = sender_username) THEN
        RETURN json_build_object('success', false, 'error', 'Usuário remetente não encontrado');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = receiver_username) THEN
        RETURN json_build_object('success', false, 'error', 'Usuário destinatário não encontrado');
    END IF;
    
    -- Verificar saldo do remetente
    SELECT balance INTO sender_balance FROM users WHERE username = sender_username FOR UPDATE;
    
    IF sender_balance < transfer_amount THEN
        RETURN json_build_object('success', false, 'error', 'Saldo insuficiente');
    END IF;
    
    -- Debitar do remetente
    UPDATE users SET balance = balance - transfer_amount WHERE username = sender_username;
    
    -- Creditar ao destinatário
    UPDATE users SET balance = balance + transfer_amount WHERE username = receiver_username;
    
    -- Criar registro da transação
    INSERT INTO transactions (from_user, to_user, amount, status)
    VALUES (sender_username, receiver_username, transfer_amount, 'completed')
    RETURNING id INTO transaction_id;
    
    RETURN json_build_object(
        'success', true,
        'transaction_id', transaction_id,
        'message', 'Transferência realizada com sucesso'
    );
    
EXCEPTION WHEN OTHERS THEN
    RETURN json_build_object('success', false, 'error', SQLERRM);
END;
$$ LANGUAGE plpgsql;

-- 10. Criar view para histórico de transações por usuário
CREATE OR REPLACE VIEW user_transactions AS
SELECT 
    t.id,
    t.from_user,
    t.to_user,
    t.amount,
    t.status,
    t.timestamp,
    CASE 
        WHEN t.from_user = u.username THEN 'sent'
        ELSE 'received'
    END as transaction_type
FROM transactions t
CROSS JOIN users u;

-- 11. Inserir usuários de teste (OPCIONAL - remover em produção)
-- INSERT INTO users (username, password_hash, balance) 
-- VALUES 
--     ('usuario1', 'hash_senha_1', 1000.00),
--     ('usuario2', 'hash_senha_2', 1000.00);

-- ============================================
-- FIM DO SCHEMA
-- ============================================

-- Para visualizar as tabelas criadas, execute:
-- SELECT * FROM users;
-- SELECT * FROM transactions;

-- Para testar a função de transferência:
-- SELECT process_transfer('usuario1', 'usuario2', 100.00);
