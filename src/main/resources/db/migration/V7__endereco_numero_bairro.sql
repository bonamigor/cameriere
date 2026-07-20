ALTER TABLE clientes
    ADD COLUMN numero VARCHAR(20) NULL AFTER logradouro,
    ADD COLUMN bairro VARCHAR(255) NULL AFTER numero;

ALTER TABLE empresas
    ADD COLUMN numero VARCHAR(20) NULL AFTER logradouro,
    ADD COLUMN bairro VARCHAR(255) NULL AFTER numero;
