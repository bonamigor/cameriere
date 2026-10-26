-- Saldo do edital passa a aceitar frações (ex.: 35,5 kg), com a mesma precisão do item do pedido
-- usada na baixa (3 casas). Valores inteiros existentes são preservados (50 → 50.000).
-- 12 dígitos inteiros comportam qualquer valor do INT anterior (até 2.147.483.647); com
-- DECIMAL(12, 3) saldos a partir de 1 bilhão estouravam e a migração falhava.
ALTER TABLE estante_produto MODIFY quantidade DECIMAL(15, 3) NOT NULL;
