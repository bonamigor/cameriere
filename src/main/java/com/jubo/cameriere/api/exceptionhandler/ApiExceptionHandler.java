package com.jubo.cameriere.api.exceptionhandler;

import com.jubo.cameriere.domain.exception.EntidadeEmUsoException;
import com.jubo.cameriere.domain.exception.NegocioException;
import com.jubo.cameriere.domain.exception.RecursoNaoEncontradoException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        List<Problem.Object> problemObjects = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Problem.Object.builder()
                        .name(fieldError.getField())
                        .userMessage(fieldError.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        Problem problem = Problem.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .type(ProblemType.DADOS_INVALIDOS.getUri())
                .title(ProblemType.DADOS_INVALIDOS.getTitle())
                .detail("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.")
                .userMessage("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.")
                .objects(problemObjects)
                .build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        Problem problem = Problem.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .type(ProblemType.MENSAGEM_INCOMPREENSIVEL.getUri())
                .title(ProblemType.MENSAGEM_INCOMPREENSIVEL.getTitle())
                .detail("O corpo da requisição está inválido. Verifique a sintaxe.")
                .userMessage("O corpo da requisição está inválido. Verifique a sintaxe.")
                .build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex, WebRequest request) {
        return buildProblem(ex, ex.getStatus(), ProblemType.RECURSO_NAO_ENCONTRADO, request);
    }

    @ExceptionHandler(EntidadeEmUsoException.class)
    public ResponseEntity<Object> handleEntidadeEmUso(EntidadeEmUsoException ex, WebRequest request) {
        return buildProblem(ex, ex.getStatus(), ProblemType.ENTIDADE_EM_USO, request);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Object> handleNegocio(NegocioException ex, WebRequest request) {
        return buildProblem(ex, ex.getStatus(), ProblemType.ERRO_NEGOCIO, request);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFound(RuntimeException ex, WebRequest request) {
        return buildProblem(ex, HttpStatus.NOT_FOUND, ProblemType.RECURSO_NAO_ENCONTRADO, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex, WebRequest request) {
        return buildProblem(ex, HttpStatus.BAD_REQUEST, ProblemType.ERRO_NEGOCIO, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        Problem problem = Problem.builder()
                .status(HttpStatus.CONFLICT.value())
                .timestamp(OffsetDateTime.now())
                .type(ProblemType.ENTIDADE_EM_USO.getUri())
                .title(ProblemType.ENTIDADE_EM_USO.getTitle())
                .detail("Não foi possível concluir a operação porque o recurso está em uso ou viola uma restrição de integridade.")
                .userMessage("Não foi possível concluir a operação porque o recurso está em uso.")
                .build();
        return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        Problem problem = Problem.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(OffsetDateTime.now())
                .type(ProblemType.NAO_AUTENTICADO.getUri())
                .title(ProblemType.NAO_AUTENTICADO.getTitle())
                .detail("Email ou senha inválidos.")
                .userMessage("Email ou senha inválidos.")
                .build();
        return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, WebRequest request) {
        Problem problem = Problem.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(OffsetDateTime.now())
                .type(ProblemType.NAO_AUTENTICADO.getUri())
                .title(ProblemType.NAO_AUTENTICADO.getTitle())
                .detail(ex.getMessage())
                .userMessage("Falha na autenticação.")
                .build();
        return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        Problem problem = Problem.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(OffsetDateTime.now())
                .type(ProblemType.ACESSO_NEGADO.getUri())
                .title(ProblemType.ACESSO_NEGADO.getTitle())
                .detail("Você não possui permissão para acessar este recurso.")
                .userMessage("Seu perfil não permite executar esta operação.")
                .build();
        return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
        log.error("Erro inesperado", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Ocorreu um erro interno inesperado no sistema. Tente novamente e se o "
                + "problema persistir, entre em contato com o administrador do sistema.";

        Problem problem = Problem.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .type(ProblemType.ERRO_DE_SISTEMA.getUri())
                .title(ProblemType.ERRO_DE_SISTEMA.getTitle())
                .detail(message)
                .userMessage(message)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    private ResponseEntity<Object> buildProblem(Exception ex, HttpStatus status, ProblemType problemType, WebRequest request) {
        Problem problem = Problem.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .type(problemType.getUri())
                .title(problemType.getTitle())
                .detail(ex.getMessage())
                .userMessage(ex.getMessage())
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {

        if (body == null) {
            body = Problem.builder()
                    .status(statusCode.value())
                    .timestamp(OffsetDateTime.now())
                    .title(HttpStatus.valueOf(statusCode.value()).getReasonPhrase())
                    .userMessage("Ocorreu um erro inesperado. Tente novamente.")
                    .build();
        } else if (body instanceof String) {
            body = Problem.builder()
                    .status(statusCode.value())
                    .timestamp(OffsetDateTime.now())
                    .title((String) body)
                    .userMessage("Ocorreu um erro inesperado. Tente novamente.")
                    .build();
        }

        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }
}
