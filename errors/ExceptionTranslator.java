package com.nals.rw360.errors;

import com.nals.rw360.client.SlackClient;
import com.nals.rw360.config.ApplicationProperties;
import com.nals.rw360.config.SentryProperties;
import com.nals.rw360.errors.annotation.ErrorMapping;
import com.nals.rw360.errors.annotation.ErrorMappings;
import com.nals.rw360.helpers.StringHelper;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.violations.ConstraintViolationProblem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nals.rw360.config.Constants.USER_ID_LOG_NAME;
import static com.nals.rw360.errors.ErrorCodes.BAD_REQUEST;
import static com.nals.rw360.errors.ErrorCodes.FORBIDDEN;
import static com.nals.rw360.errors.ErrorCodes.INTERNAL_SERVER;
import static com.nals.rw360.errors.ErrorCodes.METHOD_NOT_ALLOWED;
import static com.nals.rw360.errors.ErrorCodes.NOT_ACCEPTABLE;
import static com.nals.rw360.errors.ErrorCodes.NOT_FOUND;
import static com.nals.rw360.errors.ErrorCodes.NOT_IMPLEMENTED;
import static com.nals.rw360.errors.ErrorCodes.SECURITY;
import static com.nals.rw360.errors.ErrorCodes.UNAUTHORIZED;
import static com.nals.rw360.errors.ErrorCodes.UNSUPPORTED_MEDIA_TYPE;
import static com.nals.rw360.errors.ErrorType.RW_CONCURRENCY_FAILURE;
import static com.nals.rw360.errors.ErrorType.RW_HTTP;
import static com.nals.rw360.errors.ErrorType.RW_NOT_FOUND;
import static com.nals.rw360.errors.ErrorType.RW_SECURITY;
import static com.nals.rw360.errors.ErrorType.RW_VALIDATION;
import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionTranslator
    implements ProblemHandling {

    private static final String REQUEST_ID_ATTRIBUTE_KEY = "requestId";

    private final ApplicationProperties applicationProperties;
    private final SentryProperties sentryProperties;
    private final SlackClient slackClient;

    @Override
    public ResponseEntity<Problem> process(@Nullable final ResponseEntity<Problem> entity,
                                           @Nonnull final NativeWebRequest request) {
        if (entity == null || entity.getBody() == null) {
            return entity;
        }

        Problem problem = entity.getBody();
        ResponseEntity<Problem> res;
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            res = entity;
        } else {
            problem = buildProblem(entity, request, problem);
            res = new ResponseEntity<>(problem, entity.getHeaders(), entity.getStatusCode());
        }

        if (INTERNAL_SERVER_ERROR.equals(entity.getStatusCode())
            && (slackClient.isEnabled() || sentryProperties.isEnabled())) {
            sendErrorNotify((Throwable) request.getAttribute(ERROR_EXCEPTION, SCOPE_REQUEST), request, problem);
        }

        return res;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Problem> handleForbiddenFailure(final AccessDeniedException ex,
                                                          final NativeWebRequest request) {

        Problem problem = Problem.builder()
                                 .withType(RW_SECURITY.getType())
                                 .withStatus(Status.FORBIDDEN)
                                 .with(ProblemKey.MESSAGE, RW_SECURITY.getMessage())
                                 .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Problem> handleConcurrencyFailure(final ConcurrencyFailureException ex,
                                                            final NativeWebRequest request) {
        Problem problem = Problem.builder()
                                 .withType(RW_CONCURRENCY_FAILURE.getType())
                                 .withStatus(Status.CONFLICT)
                                 .with(ProblemKey.MESSAGE, RW_CONCURRENCY_FAILURE.getMessage())
                                 .build();
        return create(ex, problem, request);
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                final @Nonnull NativeWebRequest request) {
        var result = ex.getBindingResult();
        Map<String, String> errorCodeMap = new HashMap<>();

        Optional.ofNullable(result.getTarget()).ifPresent(target -> {
            for (Field field : target.getClass().getDeclaredFields()) {
                var errorMapping = field.getAnnotation(ErrorMapping.class);
                if (Objects.nonNull(errorMapping)) {
                    errorCodeMap.put(getKey(field, errorMapping), errorMapping.code());
                    continue;
                }

                var errorMappings = field.getAnnotation(ErrorMappings.class);
                if (Objects.nonNull(errorMappings)) {
                    for (ErrorMapping errMapping : errorMappings.value()) {
                        errorCodeMap.put(getKey(field, errMapping), errMapping.code());
                    }
                }
            }
        });

        var errors = result.getFieldErrors().stream().map(fieldError -> {
            var error = new ErrorProblem();
            error.setField(fieldError.getField());
            error.setMessage(getMessage(RW_VALIDATION, fieldError.getField(), fieldError.getCode()));
            String key = String.format("%s-%s", error.getField(), fieldError.getCode());
            error.setErrorCode(errorCodeMap.getOrDefault(key, BAD_REQUEST));
            return error;
        }).collect(Collectors.toList());

        Problem problem = Problem.builder()
                                 .withType(RW_VALIDATION.getType())
                                 .withStatus(defaultConstraintViolationStatus())
                                 .with(ProblemKey.MESSAGE, RW_VALIDATION.getMessage())
                                 .with(ProblemKey.ERROR_CODE, BAD_REQUEST)
                                 .with(ProblemKey.ERRORS, errors)
                                 .build();

        return create(ex, problem, request);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Problem> handleSecurity(final SecurityException ex,
                                                  final NativeWebRequest request) {
        Problem problem = Problem.builder()
                                 .withType(RW_SECURITY.getType())
                                 .withStatus(defaultConstraintViolationStatus())
                                 .with(ProblemKey.MESSAGE, getMessage(RW_SECURITY, ex.getMessage()))
                                 .with(ProblemKey.ERROR_CODE, SECURITY)
                                 .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Problem> handleAuthentication(final AuthenticationException ex,
                                                        final NativeWebRequest request) {

        var error = new ErrorProblem();
        error.setMessage(ex.getMessage());
        error.setErrorCode(UNAUTHORIZED);

        Problem problem = Problem.builder()
                                 .withType(RW_SECURITY.getType())
                                 .withStatus(Status.UNAUTHORIZED)
                                 .with(ProblemKey.MESSAGE, RW_SECURITY.getMessage())
                                 .with(ProblemKey.ERROR_CODE, UNAUTHORIZED)
                                 .with(ProblemKey.ERRORS, List.of(error))
                                 .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(ValidatorException.class)
    public ResponseEntity<Problem> handleValidator(final ValidatorException ex,
                                                   final NativeWebRequest request) {
        Problem problem = Problem.builder()
                                 .withType(RW_VALIDATION.getType())
                                 .withStatus(defaultConstraintViolationStatus())
                                 .with(ProblemKey.MESSAGE, ex.getMessage())
                                 .with(ProblemKey.ERROR_CODE, BAD_REQUEST)
                                 .with(ProblemKey.ERRORS, ex.getErrors())
                                 .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Problem> handleObjectNotFound(final NotFoundException ex,
                                                        final NativeWebRequest request) {
        Problem problem = Problem.builder()
                                 .withType(RW_NOT_FOUND.getType())
                                 .withStatus(Status.NOT_FOUND)
                                 .with(ProblemKey.MESSAGE, ex.getMessage())
                                 .with(ProblemKey.ERROR_CODE, NOT_FOUND)
                                 .with(ProblemKey.ERRORS, ex.getErrors())
                                 .build();
        return create(ex, problem, request);
    }

    private String getMessage(final ErrorType errorType, final String message) {
        return MessageFormat.format("{0}.{1}",
                                    errorType.getMessage(),
                                    StringHelper.uppercaseToUnderscore(message));
    }

    private String getMessage(final ErrorType errorType, final String fieldName, final String message) {
        return MessageFormat.format("{0}.{1}.{2}",
                                    errorType.getMessage(),
                                    StringHelper.uppercaseToUnderscore(fieldName),
                                    StringHelper.uppercaseToUnderscore(message));
    }

    private String getKey(final Field field, final ErrorMapping errorMapping) {
        return String.format("%s-%s",
                             field.getName(),
                             errorMapping.value().getSimpleName());
    }

    private Problem buildProblem(final ResponseEntity<Problem> entity,
                                 final NativeWebRequest request,
                                 final Problem problem) {

        ProblemBuilder builder = Problem
            .builder()
            .withType(problem.getType())
            .withStatus(problem.getStatus())
            .withTitle(problem.getTitle())
            .with(ProblemKey.PATH, Objects.requireNonNull(request.getNativeRequest(HttpServletRequest.class))
                                          .getRequestURI());

        if (problem instanceof ConstraintViolationProblem) {
            builder.with(ProblemKey.VIOLATIONS, ((ConstraintViolationProblem) problem).getViolations())
                   .with(ProblemKey.MESSAGE, RW_VALIDATION.getMessage());

            if (problem.getParameters().containsKey(ProblemKey.ERROR_CODE)) {
                builder.with(ProblemKey.ERROR_CODE, problem.getParameters().get(ProblemKey.ERROR_CODE));
            }
        } else {
            builder.withCause(((DefaultProblem) problem).getCause())
                   .withDetail(problem.getDetail())
                   .withInstance(problem.getInstance());

            problem.getParameters().forEach(builder::with);

            if (!problem.getParameters().containsKey(ProblemKey.MESSAGE) && Objects.nonNull(problem.getStatus())) {
                builder.with(ProblemKey.MESSAGE, MessageFormat.format("{0}.{1}",
                                                                      RW_HTTP.getMessage(),
                                                                      problem.getStatus().getStatusCode()));
            }

            if (!problem.getParameters().containsKey(ProblemKey.ERROR_CODE)) {
                switch (entity.getStatusCode()) {
                    case BAD_REQUEST:
                        builder.with(ProblemKey.ERROR_CODE, BAD_REQUEST);
                        break;
                    case UNAUTHORIZED:
                        builder.with(ProblemKey.ERROR_CODE, UNAUTHORIZED);
                        break;
                    case FORBIDDEN:
                        builder.with(ProblemKey.ERROR_CODE, FORBIDDEN);
                        break;
                    case NOT_FOUND:
                        builder.with(ProblemKey.ERROR_CODE, NOT_FOUND);
                        break;
                    case METHOD_NOT_ALLOWED:
                        builder.with(ProblemKey.ERROR_CODE, METHOD_NOT_ALLOWED);
                        break;
                    case NOT_ACCEPTABLE:
                        builder.with(ProblemKey.ERROR_CODE, NOT_ACCEPTABLE);
                        break;
                    case UNSUPPORTED_MEDIA_TYPE:
                        builder.with(ProblemKey.ERROR_CODE, UNSUPPORTED_MEDIA_TYPE);
                        break;
                    case NOT_IMPLEMENTED:
                        builder.with(ProblemKey.ERROR_CODE, NOT_IMPLEMENTED);
                        break;
                    default:
                        builder.with(ProblemKey.ERROR_CODE, INTERNAL_SERVER)
                               .withDetail("Internal Server Error");
                }
            }
        }

        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE_KEY, SCOPE_REQUEST);
        if (Optional.ofNullable(requestId).isPresent()) {
            builder.with(ProblemKey.REQUEST_ID, requestId);
        }

        return builder.build();
    }

    private void sendErrorNotify(final Throwable throwable,
                                 final NativeWebRequest request,
                                 final Problem problem) {

        String endpoint = Strings.EMPTY;
        HttpServletRequest httpServletRequest = request.getNativeRequest(HttpServletRequest.class);
        if (Objects.nonNull(httpServletRequest)) {
            endpoint = String.format("%s %s", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        }

        Map<String, Object> problemParameters = problem.getParameters();
        String requestId = (String) problemParameters.getOrDefault(ProblemKey.REQUEST_ID, MDC.get(USER_ID_LOG_NAME));
        String errorCode = (String) problemParameters.getOrDefault(ProblemKey.ERROR_CODE, INTERNAL_SERVER);

        if (slackClient.isEnabled()) {
            slackClient.captureError(throwable, endpoint, requestId, errorCode);
        }

        if (sentryProperties.isEnabled()) {
            SentryEvent event = new SentryEvent();
            event.setTag("request_id", requestId);
            event.setTag("error_code", errorCode);
            event.setTag("error_message", throwable.getMessage());
            event.setTag("service_name", applicationProperties.getServiceName());
            event.setTag("endpoint", endpoint);
            event.setThrowable(throwable);

            Sentry.captureEvent(event);
        }
    }

    public static class ProblemKey {
        public static final String PATH = "path";
        public static final String MESSAGE = "message";
        public static final String VIOLATIONS = "violations";
        public static final String REQUEST_ID = "request_id";
        public static final String ERRORS = "errors";
        public static final String ERROR_CODE = "error_code";
    }
}
