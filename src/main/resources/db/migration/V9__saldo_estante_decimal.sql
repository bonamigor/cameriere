-- Saldo do edital passa a aceitar frações (ex.: 35,5 kg), com a mesma precisão do item do pedido
-- usada na baixa (3 casas). Valores inteiros existentes são preservados (50 → 50.000).
ALTER TABLE estante_produto MODIFY quantidade DECIMAL(12, 3) NOT NULL;
