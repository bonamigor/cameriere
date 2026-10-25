-- Autenticação em dois fatores (TOTP). O segredo é guardado criptografado (AES-GCM).
ALTER TABLE usuarios
    ADD COLUMN totp_segredo       VARCHAR(255) NULL,
    ADD COLUMN totp_ativo         TINYINT(1)   NOT NULL DEFAULT 0,
    ADD COLUMN totp_ultimo_passo  BIGINT       NULL;

CREATE TABLE usuario_codigos_recuperacao (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   BIGINT       NOT NULL,
    codigo_hash  VARCHAR(255) NOT NULL,
    usado_em     DATETIME(6)  NULL,
    CONSTRAINT fk_codigos_recuperacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE INDEX idx_codigos_recuperacao_usuario_id ON usuario_codigos_recuperacao (usuario_id);

-- Trilha de auditoria (LGPD: registro de acessos e alterações). Sem FK para manter o
-- histórico mesmo após a exclusão do usuário; nunca armazena corpo de requisição.
CREATE TABLE auditoria (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora      DATETIME(6)  NOT NULL,
    acao           VARCHAR(40)  NOT NULL,
    usuario_id     BIGINT       NULL,
    usuario_email  VARCHAR(255) NULL,
    metodo         VARCHAR(10)  NULL,
    recurso        VARCHAR(255) NULL,
    status_http    INT          NULL,
    ip             VARCHAR(45)  NULL,
    detalhe        VARCHAR(255) NULL
);

CREATE INDEX idx_auditoria_data_hora ON auditoria (data_hora);
CREATE INDEX idx_auditoria_usuario_id ON auditoria (usuario_id);
CREATE INDEX idx_auditoria_acao ON auditoria (acao);
