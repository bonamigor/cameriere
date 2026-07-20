-- Status legado da API antiga: CRIADO equivale a PENDENTE na nova API.
UPDATE pedidos
SET status = 'PENDENTE'
WHERE status = 'CRIADO';
