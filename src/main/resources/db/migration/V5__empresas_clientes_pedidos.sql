CREATE TABLE empresas (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome             VARCHAR(255) NOT NULL,
    cnpj             VARCHAR(18)  NOT NULL,
    email            VARCHAR(255) NULL,
    telefone         VARCHAR(20)  NULL,
    ativo            TINYINT(1)   NULL,
    logradouro       VARCHAR(255) NULL,
    cep              VARCHAR(9)   NULL,
    cidade           VARCHAR(255) NULL,
    estado           VARCHAR(2)   NULL,
    data_criacao     DATETIME(6)  NOT NULL,
    data_atualizacao DATETIME(6)  NULL,
    CONSTRAINT uk_empresas_cnpj UNIQUE (cnpj)
);

ALTER TABLE clientes
    ADD COLUMN empresa_id BIGINT NULL,
    ADD CONSTRAINT fk_clientes_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id);

ALTER TABLE pedidos
    ADD COLUMN empresa_nota_id BIGINT NULL,
    ADD CONSTRAINT fk_pedidos_empresa_nota FOREIGN KEY (empresa_nota_id) REFERENCES empresas (id);

CREATE INDEX idx_clientes_empresa_id ON clientes (empresa_id);
CREATE INDEX idx_pedidos_empresa_nota_id ON pedidos (empresa_nota_id);
