package com.jubo.cameriere.api.security;

import com.jubo.cameriere.api.dto.ConfiguracaoMfaDTO;
import com.jubo.cameriere.config.security.MfaProperties;
import com.jubo.cameriere.domain.exception.CodigoMfaInvalidoException;
import com.jubo.cameriere.domain.exception.NegocioException;
import com.jubo.cameriere.domain.exception.UsuarioNaoEncontradoException;
import com.jubo.cameriere.domain.model.CodigoRecuperacao;
import com.jubo.cameriere.domain.model.Usuario;
import com.jubo.cameriere.domain.repository.CodigoRecuperacaoRepository;
import com.jubo.cameriere.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;

/**
 * Ciclo de vida do 2FA: configurar → ativar (gera códigos de recuperação) → verificar no login.
 * A auditoria fica com quem chama, fora da transação, para que falhas também sejam registradas.
 */
@Service
@RequiredArgsConstructor
public class MfaService {

    private static final String ALFABETO_CODIGOS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int QUANTIDADE_CODIGOS = 8;

    private final UsuarioRepository usuarioRepository;
    private final CodigoRecuperacaoRepository codigoRecuperacaoRepository;
    private final TotpService totpService;
    private final CriptografiaService criptografiaService;
    private final PasswordEncoder passwordEncoder;
    private final MfaProperties mfaProperties;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public ConfiguracaoMfaDTO iniciarConfiguracao(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.isTotpAtivo()) {
            throw new NegocioException("A verificação em duas etapas já está ativa.");
        }
        String segredo = totpService.gerarSegredo();
        usuario.setTotpSegredo(criptografiaService.criptografar(segredo));
        usuario.setTotpUltimoPasso(null);
        return new ConfiguracaoMfaDTO(segredo, totpService.uriOtpAuth(mfaProperties.emissor(), usuario.getEmail(), segredo));
    }

    /** Confirma o primeiro código do app e devolve os códigos de recuperação (exibidos uma única vez). */
    @Transactional
    public List<String> ativar(Long usuarioId, String codigo) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.isTotpAtivo()) {
            throw new NegocioException("A verificação em duas etapas já está ativa.");
        }
        if (usuario.getTotpSegredo() == null) {
            throw new NegocioException("Inicie a configuração da verificação em duas etapas antes de ativá-la.");
        }
        if (!validarTotp(usuario, codigo)) {
            throw new CodigoMfaInvalidoException();
        }
        usuario.setTotpAtivo(true);
        return gerarCodigosRecuperacao(usuario.getId());
    }

    @Transactional
    public void desativar(Long usuarioId, String senha, String codigo) {
        Usuario usuario = buscar(usuarioId);
        if (!usuario.isTotpAtivo()) {
            throw new NegocioException("A verificação em duas etapas não está ativa.");
        }
        if (Boolean.TRUE.equals(usuario.getAdmin()) && mfaProperties.obrigatorioParaAdmin()) {
            throw new NegocioException("Administradores precisam manter a verificação em duas etapas ativa.");
        }
        if (senha == null || !passwordEncoder.matches(senha, usuario.getSenha()) || !validarSegundoFator(usuario, codigo)) {
            throw new NegocioException("Senha ou código inválido.");
        }
        limpar(usuario);
    }

    /** Usado por um administrador quando o usuário perdeu o celular e os códigos de recuperação. */
    @Transactional
    public void redefinir(Long usuarioId) {
        limpar(buscar(usuarioId));
    }

    @Transactional
    public boolean verificarCodigoLogin(Long usuarioId, String codigo) {
        Usuario usuario = buscar(usuarioId);
        return usuario.isTotpAtivo() && validarSegundoFator(usuario, codigo);
    }

    @Transactional(readOnly = true)
    public long codigosRecuperacaoRestantes(Long usuarioId) {
        return codigoRecuperacaoRepository.countByUsuarioIdAndUsadoEmIsNull(usuarioId);
    }

    private boolean validarSegundoFator(Usuario usuario, String codigo) {
        if (codigo == null) {
            return false;
        }
        String limpo = codigo.replaceAll("\\s", "");
        return limpo.matches("\\d{6}") ? validarTotp(usuario, limpo) : usarCodigoRecuperacao(usuario, limpo);
    }

    private boolean validarTotp(Usuario usuario, String codigo) {
        String segredo = criptografiaService.descriptografar(usuario.getTotpSegredo());
        OptionalLong passo = totpService.verificar(segredo, codigo, usuario.getTotpUltimoPasso());
        passo.ifPresent(usuario::setTotpUltimoPasso);
        return passo.isPresent();
    }

    private boolean usarCodigoRecuperacao(Usuario usuario, String codigo) {
        String normalizado = codigo.replace("-", "").toUpperCase(Locale.ROOT);
        for (CodigoRecuperacao candidato : codigoRecuperacaoRepository.findByUsuarioIdAndUsadoEmIsNull(usuario.getId())) {
            if (passwordEncoder.matches(normalizado, candidato.getCodigoHash())) {
                candidato.setUsadoEm(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }

    private List<String> gerarCodigosRecuperacao(Long usuarioId) {
        codigoRecuperacaoRepository.deleteByUsuarioId(usuarioId);
        List<String> codigos = new ArrayList<>(QUANTIDADE_CODIGOS);
        for (int i = 0; i < QUANTIDADE_CODIGOS; i++) {
            StringBuilder codigo = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                codigo.append(ALFABETO_CODIGOS.charAt(random.nextInt(ALFABETO_CODIGOS.length())));
            }
            codigoRecuperacaoRepository.save(new CodigoRecuperacao(usuarioId, passwordEncoder.encode(codigo.toString())));
            codigos.add(codigo.substring(0, 4) + "-" + codigo.substring(4));
        }
        return codigos;
    }

    private void limpar(Usuario usuario) {
        usuario.setTotpAtivo(false);
        usuario.setTotpSegredo(null);
        usuario.setTotpUltimoPasso(null);
        codigoRecuperacaoRepository.deleteByUsuarioId(usuario.getId());
    }

    private Usuario buscar(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado: " + usuarioId));
    }
}
