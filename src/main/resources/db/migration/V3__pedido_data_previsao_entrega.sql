-- Admins não podem estar vinculados a clientes; apenas operadores.
UPDATE usuarios
SET cliente_id = NULL
WHERE admin = 1
  AND cliente_id IS NOT NULL;

ALTER TABLE pedidos
    ADD COLUMN data_previsao_entrega DATE NULL AFTER data_confirmacao;
