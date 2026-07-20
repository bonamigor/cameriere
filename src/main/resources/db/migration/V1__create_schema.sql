CREATE TABLE unidades_medida (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    sigla            VARCHAR(4)  NOT NULL,
    descricao        VARCHAR(50) NOT NULL,
    data_criacao     DATETIME(6) NOT NULL,
    data_atualizacao DATETIME(6) NULL,
    CONSTRAINT uk_unidades_medida_sigla UNIQUE (sigla)
);

CREATE TABLE clientes (
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
    CONSTRAINT uk_clientes_cnpj UNIQUE (cnpj)
);

CREATE TABLE usuarios (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome             VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    senha            VARCHAR(255) NOT NULL,
    admin            TINYINT(1)   NOT NULL,
    cliente_id       BIGINT       NULL,
    data_criacao     DATETIME(6)  NOT NULL,
    data_atualizacao DATETIME(6)  NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);

CREATE TABLE produtos (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome             VARCHAR(255) NOT NULL,
    preco_custo      DOUBLE       NOT NULL,
    unidade_medida   VARCHAR(4)   NOT NULL,
    ativo            TINYINT(1)   NULL,
    data_criacao     DATETIME(6)  NOT NULL,
    data_atualizacao DATETIME(6)  NULL,
    CONSTRAINT fk_produtos_unidade_medida FOREIGN KEY (unidade_medida) REFERENCES unidades_medida (sigla)
);

CREATE TABLE estantes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    periodo    VARCHAR(7)   NOT NULL,
    observacao VARCHAR(255) NULL,
    ativa      TINYINT(1)   NOT NULL,
    empresa    INT          NULL,
    cliente_id BIGINT       NOT NULL,
    CONSTRAINT fk_estantes_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);

CREATE TABLE estante_produto (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    estante_id       BIGINT         NOT NULL,
    produto_id       BIGINT         NOT NULL,
    preco_venda      DECIMAL(10, 2) NOT NULL,
    quantidade       INT            NOT NULL,
    ativo            TINYINT(1)     NOT NULL,
    data_cadastro    DATETIME(6)    NULL,
    data_atualizacao DATETIME(6)    NULL,
    CONSTRAINT uk_estante_produto UNIQUE (estante_id, produto_id),
    CONSTRAINT fk_estante_produto_estante FOREIGN KEY (estante_id) REFERENCES estantes (id),
    CONSTRAINT fk_estante_produto_produto FOREIGN KEY (produto_id) REFERENCES produtos (id)
);

CREATE TABLE pedidos (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    status            VARCHAR(20)    NULL,
    data_confirmacao  DATETIME(6)    NULL,
    data_cancelamento DATETIME(6)    NULL,
    data_entrega      DATETIME(6)    NULL,
    valor_total       DECIMAL(19, 2) NULL,
    entregue          TINYINT(1)     NULL,
    observacao        VARCHAR(255)   NULL,
    obs_cancelamento  VARCHAR(255)   NULL,
    is_finalizado     TINYINT(1)     NULL,
    cliente_id        BIGINT         NOT NULL,
    data_criacao      DATETIME(6)    NOT NULL,
    data_atualizacao  DATETIME(6)    NULL,
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);

CREATE TABLE item_pedido (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantidade       DECIMAL(10, 4) NOT NULL,
    preco_total      DECIMAL(19, 2) NOT NULL,
    observacao       VARCHAR(255)   NULL,
    pedido_id        BIGINT         NOT NULL,
    produto_id       BIGINT         NOT NULL,
    estante_id       BIGINT         NOT NULL,
    data_criacao     DATETIME(6)    NOT NULL,
    data_atualizacao DATETIME(6)    NULL,
    CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id),
    CONSTRAINT fk_item_pedido_produto FOREIGN KEY (produto_id) REFERENCES produtos (id),
    CONSTRAINT fk_item_pedido_estante_produto FOREIGN KEY (estante_id) REFERENCES estante_produto (id)
);

CREATE INDEX idx_pedidos_cliente_id ON pedidos (cliente_id);
CREATE INDEX idx_pedidos_status ON pedidos (status);
CREATE INDEX idx_pedidos_data_criacao ON pedidos (data_criacao);
CREATE INDEX idx_estantes_cliente_id ON estantes (cliente_id);
CREATE INDEX idx_estante_produto_estante_id ON estante_produto (estante_id);
CREATE INDEX idx_item_pedido_pedido_id ON item_pedido (pedido_id);
