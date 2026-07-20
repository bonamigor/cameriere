UPDATE usuarios u
JOIN usuarios existente
    ON existente.cliente_id = u.cliente_id
   AND existente.id < u.id
SET u.cliente_id = NULL
WHERE u.cliente_id IS NOT NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_cliente UNIQUE (cliente_id);
