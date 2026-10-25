package com.jubo.cameriere.domain.model;

public enum AcaoAuditoria {
    LOGIN_SUCESSO,
    LOGIN_FALHA,
    LOGIN_BLOQUEADO,
    LOGOUT,
    MFA_FALHA,
    MFA_ATIVADO,
    MFA_DESATIVADO,
    MFA_REDEFINIDO,
    CODIGO_RECUPERACAO_USADO,
    ALTERACAO
}
