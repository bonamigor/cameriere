package com.jubo.cameriere.api.exceptionhandler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;

        // Verifica se é erro de CNPJ duplicado
        if (ex.getMessage().contains("CNPJ já cadastrado")) {
            problemType = ProblemType.ENTIDADE_EM_USO;
        }
        // Verifica se é cliente não encontrado
        else if (ex.getMessage().contains("Cliente não encontrado")) {
            status = HttpStatus.NOT_FOUND;
            problemType = ProblemType.RECURSO_NAO_ENCONTRADO;
        }

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;

        Problem problem = Problem.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .type(problemType.getUri())
                .title(problemType.getTitle())
                .detail("Ocorreu um erro interno inesperado no sistema. Tente novamente e se o "
                        + "problema persistir, entre em contato com o administrador do sistema.")
                .userMessage("Ocorreu um erro interno inesperado no sistema. Tente novamente e se o "
                        + "problema persistir, entre em contato com o administrador do sistema.")
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