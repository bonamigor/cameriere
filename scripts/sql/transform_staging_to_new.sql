-- Migra dados do banco staging (schema antigo Railway) para o schema Flyway atual.
-- Bancos padrão: jubo_staging -> jubo_new (substituídos pelo script Python se necessário).

SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

DELETE FROM jubo_new.item_pedido;
DELETE FROM jubo_new.pedidos;
DELETE FROM jubo_new.estante_produto;
DELETE FROM jubo_new.estantes;
DELETE FROM jubo_new.produtos;
DELETE FROM jubo_new.usuarios;
DELETE FROM jubo_new.clientes;
DELETE FROM jubo_new.unidades_medida;

DROP TABLE IF EXISTS jubo_staging.ep_map;
CREATE TABLE jubo_staging.ep_map (
    old_estante_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    new_ep_id BIGINT NOT NULL,
    PRIMARY KEY (old_estante_id, produto_id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS jubo_staging.client_remap;
CREATE TABLE jubo_staging.client_remap (
    old_id BIGINT NOT NULL,
    canonical_id BIGINT NOT NULL,
    formatted_cnpj VARCHAR(18) NOT NULL,
    PRIMARY KEY (old_id),
    KEY idx_client_remap_canonical (canonical_id),
    KEY idx_client_remap_cnpj (formatted_cnpj)
) ENGINE=InnoDB;

INSERT INTO jubo_staging.client_remap (old_id, canonical_id, formatted_cnpj)
SELECT
    c.id,
    (
        SELECT MIN(c2.id)
        FROM jubo_staging.clientes c2
        WHERE LPAD(SUBSTRING(REPLACE(REPLACE(REPLACE(TRIM(c2.cnpj), '.', ''), '/', ''), '-', ''), 1, 14), 14, '0')
            = LPAD(SUBSTRING(REPLACE(REPLACE(REPLACE(TRIM(c.cnpj), '.', ''), '/', ''), '-', ''), 1, 14), 14, '0')
    ),
    CONCAT(
        SUBSTRING(LPAD(SUBSTRING(REPLACE(REPLACE(REPLACE(TRIM(c.cnpj), '.', ''), '/', ''), '-', ''), 1, 14), 14, '0'), 1, 2), '.',
        SUBSTRING(LPAD(SUBSTRING(REPLACE(REPLACE(REPLACE(TRIM(c.cnpj), '.', ''), '/', ''), '-', ''), 1, 14), 14, '0'), 3, 3), '.',
        SUBSTRING(LPAD(SUBSTRING(REPLACE(REPLACE(REPLACE(TRIM(c.cnpj), '.', ''), '/', ''), '-', ''), 1, 14), 14, '0'), 6, 3), '/',
        SUBSTRING(LPAD(SUBSTRING(REPLACE(REPLACE(REPLACE(TRIM(c.cnpj), '.', ''), '/', ''), '-', ''), 1, 14), 14, '0'), 9, 4), '-',
        SUBSTRING(LPAD(SUBSTRING(REPLACE(REPLACE(REPLACE(TRIM(c.cnpj), '.', ''), '/', ''), '-', ''), 1, 14), 14, '0'), 13, 2)
    )
FROM jubo_staging.clientes c;

INSERT INTO jubo_new.unidades_medida (sigla, descricao, data_criacao, data_atualizacao)
SELECT DISTINCT
    LEFT(p.unidade_medida, 4),
    LEFT(p.unidade_medida, 50),
    NOW(6),
    NOW(6)
FROM jubo_staging.produtos p
WHERE p.unidade_medida IS NOT NULL
  AND TRIM(p.unidade_medida) <> ''
ORDER BY 1;

INSERT INTO jubo_new.clientes (
    id, nome, cnpj, email, telefone, ativo,
    logradouro, cep, cidade, estado,
    data_criacao, data_atualizacao
)
SELECT
    c.id,
    c.nome,
    cr.formatted_cnpj,
    c.email,
    LEFT(c.telefone, 20),
    c.ativo,
    c.endereco,
    CASE
        WHEN REPLACE(REPLACE(REPLACE(COALESCE(c.cep, ''), '.', ''), '-', ''), ' ', '') REGEXP '^[0-9]{8}$' THEN
            CONCAT(
                SUBSTRING(REPLACE(REPLACE(REPLACE(COALESCE(c.cep, ''), '.', ''), '-', ''), ' ', ''), 1, 5),
                '-',
                SUBSTRING(REPLACE(REPLACE(REPLACE(COALESCE(c.cep, ''), '.', ''), '-', ''), ' ', ''), 6, 3)
            )
        ELSE LEFT(c.cep, 9)
    END,
    c.cidade,
    LEFT(UPPER(COALESCE(c.estado, '')), 2),
    NOW(6),
    NOW(6)
FROM jubo_staging.clientes c
JOIN jubo_staging.client_remap cr ON cr.old_id = c.id
WHERE cr.old_id = cr.canonical_id;

INSERT INTO jubo_new.produtos (
    id, nome, preco_custo, unidade_medida, ativo, data_criacao, data_atualizacao
)
SELECT
    p.id,
    p.nome,
    p.preco_custo,
    LEFT(p.unidade_medida, 4),
    p.ativo,
    NOW(6),
    NOW(6)
FROM jubo_staging.produtos p;

INSERT INTO jubo_new.estantes (
    id, periodo, observacao, ativa, empresa, cliente_id
)
SELECT
    e.id,
    e.periodo,
    e.observacao,
    COALESCE(e.ativa, 1),
    e.empresa,
    cr.canonical_id
FROM jubo_staging.estantes e
JOIN jubo_staging.client_remap cr ON cr.old_id = e.cliente_id;

INSERT INTO jubo_new.estante_produto (
    estante_id, produto_id, preco_venda, quantidade, ativo, data_cadastro, data_atualizacao
)
SELECT
    ep.estante_id,
    ep.produto_id,
    CAST(pq.preco_venda AS DECIMAL(10, 2)),
    pq.quantidade,
    COALESCE(ep.ativo, 1),
    NOW(6),
    NOW(6)
FROM jubo_staging.estante_produto ep
JOIN jubo_staging.preco_quantidade pq ON pq.id = ep.preco_quantidade_id;

INSERT INTO jubo_staging.ep_map (old_estante_id, produto_id, new_ep_id)
SELECT estante_id, produto_id, id
FROM jubo_new.estante_produto;

INSERT INTO jubo_new.estante_produto (
    estante_id, produto_id, preco_venda, quantidade, ativo, data_cadastro, data_atualizacao
)
SELECT
    ip.estante_id,
    ip.produto_id,
    CAST(ROUND(MAX(ip.preco_total / NULLIF(ip.quantidade, 0)), 2) AS DECIMAL(10, 2)),
    0,
    0,
    NOW(6),
    NOW(6)
FROM jubo_staging.item_pedido ip
LEFT JOIN jubo_staging.estante_produto ep
    ON ep.estante_id = ip.estante_id
   AND ep.produto_id = ip.produto_id
WHERE ep.estante_id IS NULL
GROUP BY ip.estante_id, ip.produto_id;

INSERT INTO jubo_staging.ep_map (old_estante_id, produto_id, new_ep_id)
SELECT ep.estante_id, ep.produto_id, ep.id
FROM jubo_new.estante_produto ep
LEFT JOIN jubo_staging.ep_map m
    ON m.old_estante_id = ep.estante_id
   AND m.produto_id = ep.produto_id
WHERE m.old_estante_id IS NULL;

INSERT INTO jubo_new.pedidos (
    id, status, data_confirmacao, data_previsao_entrega, data_cancelamento, data_entrega,
    valor_total, entregue, observacao, obs_cancelamento, is_finalizado, cliente_id,
    data_criacao, data_atualizacao
)
SELECT
    p.id,
    CASE
        WHEN p.status = 'CRIADO' THEN 'PENDENTE'
        WHEN p.status IN ('PENDENTE', 'CONFIRMADO', 'CANCELADO', 'ENTREGUE') THEN p.status
        ELSE 'PENDENTE'
    END,
    CASE WHEN p.data_confirmacao IS NULL THEN NULL ELSE FROM_UNIXTIME(p.data_confirmacao / 1000) END,
    CASE
        WHEN p.status IN ('CONFIRMADO', 'ENTREGUE') AND p.data_entrega IS NOT NULL THEN
            DATE(FROM_UNIXTIME(p.data_entrega / 1000))
        WHEN p.status IN ('CONFIRMADO', 'ENTREGUE') AND p.data_confirmacao IS NOT NULL THEN
            DATE(FROM_UNIXTIME(p.data_confirmacao / 1000))
        ELSE NULL
    END,
    CASE WHEN p.data_cancelamento IS NULL THEN NULL ELSE FROM_UNIXTIME(p.data_cancelamento / 1000) END,
    CASE WHEN p.data_entrega IS NULL THEN NULL ELSE FROM_UNIXTIME(p.data_entrega / 1000) END,
    CAST(p.valor_total AS DECIMAL(19, 2)),
    p.entregue,
    p.observacao,
    p.obsCancelamento,
    p.is_finalizado,
    cr.canonical_id,
    CASE WHEN p.data_criacao IS NULL THEN NOW(6) ELSE FROM_UNIXTIME(p.data_criacao / 1000) END,
    NOW(6)
FROM jubo_staging.pedidos p
JOIN jubo_staging.client_remap cr ON cr.old_id = p.cliente_id;

INSERT INTO jubo_new.item_pedido (
    id, quantidade, preco_total, observacao, pedido_id, produto_id, estante_id,
    data_criacao, data_atualizacao
)
SELECT
    ip.id,
    ip.quantidade,
    CAST(ip.preco_total AS DECIMAL(19, 2)),
    ip.observacao,
    ip.pedido_id,
    ip.produto_id,
    m.new_ep_id,
    NOW(6),
    NOW(6)
FROM jubo_staging.item_pedido ip
JOIN jubo_staging.ep_map m
    ON m.old_estante_id = ip.estante_id
   AND m.produto_id = ip.produto_id;

DROP TABLE IF EXISTS jubo_staging.user_remap;
CREATE TABLE jubo_staging.user_remap (
    old_id BIGINT NOT NULL,
    keep_id BIGINT NOT NULL,
    canonical_cliente_id BIGINT NULL,
    PRIMARY KEY (old_id)
) ENGINE=InnoDB;

INSERT INTO jubo_staging.user_remap (old_id, keep_id, canonical_cliente_id)
SELECT
    u.id,
    (
        SELECT MIN(u_keep.id)
        FROM jubo_staging.users u_keep
        WHERE LOWER(u_keep.email) = LOWER(u.email)
    ),
    CASE WHEN u.admin = 1 THEN NULL ELSE cr.canonical_id END
FROM jubo_staging.users u
LEFT JOIN jubo_staging.client_remap cr ON cr.old_id = u.cliente_id;

INSERT INTO jubo_new.usuarios (
    id, nome, email, senha, admin, cliente_id, data_criacao, data_atualizacao
)
SELECT
    u.id,
    u.nome,
    u.email,
    u.senha,
    u.admin,
    CASE
        WHEN u.admin = 1 THEN NULL
        WHEN ur.canonical_cliente_id IS NULL THEN NULL
        WHEN u.id = (
            SELECT MIN(u2.id)
            FROM jubo_staging.users u2
            JOIN jubo_staging.user_remap ur2 ON ur2.old_id = u2.id
            WHERE ur2.old_id = ur2.keep_id
              AND u2.admin = 0
              AND ur2.canonical_cliente_id = ur.canonical_cliente_id
        ) THEN ur.canonical_cliente_id
        ELSE NULL
    END,
    NOW(6),
    NOW(6)
FROM jubo_staging.users u
JOIN jubo_staging.user_remap ur ON ur.old_id = u.id
WHERE ur.old_id = ur.keep_id;

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;

SELECT 'clientes' AS tabela, COUNT(*) AS registros FROM jubo_new.clientes
UNION ALL SELECT 'produtos', COUNT(*) FROM jubo_new.produtos
UNION ALL SELECT 'estantes', COUNT(*) FROM jubo_new.estantes
UNION ALL SELECT 'estante_produto', COUNT(*) FROM jubo_new.estante_produto
UNION ALL SELECT 'pedidos', COUNT(*) FROM jubo_new.pedidos
UNION ALL SELECT 'item_pedido', COUNT(*) FROM jubo_new.item_pedido
UNION ALL SELECT 'usuarios', COUNT(*) FROM jubo_new.usuarios
UNION ALL SELECT 'unidades_medida', COUNT(*) FROM jubo_new.unidades_medida;
