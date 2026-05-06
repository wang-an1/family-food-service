package com.familyfood.common;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handleApp(AppException ex) {
        return ResponseEntity.status(ex.status()).body(ApiResponse.error(ex.code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiResponse.ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldError)
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error("VALIDATION_ERROR", "请求参数校验失败", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex) {
        List<ApiResponse.ErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiResponse.ErrorDetail(
                        violation.getPropertyPath().toString(),
                        validationMessage(violation.getPropertyPath().toString(),
                                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                                violation.getConstraintDescriptor().getAttributes())))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error("VALIDATION_ERROR", "请求参数校验失败", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handleUnreadable() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", "请求体格式不正确"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        List<ApiResponse.ErrorDetail> errors = List.of(
                new ApiResponse.ErrorDetail(ex.getParameterName(), "缺少必填参数"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", "请求参数不完整", errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        List<ApiResponse.ErrorDetail> errors = List.of(
                new ApiResponse.ErrorDetail(ex.getName(), "参数类型不正确"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", "请求参数格式不正确", errors));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiResponse<Void>> handleMaxUploadSize() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("UPLOAD_TOO_LARGE", "文件大小超过限制"));
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<ApiResponse<Void>> handleIo(IOException ex) {
        log.error("io_error traceId={}", TraceIds.current(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "文件或网络读写失败，请稍后重试"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", "无权限"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("unexpected_error traceId={}", TraceIds.current(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "系统异常，请稍后重试"));
    }

    private ApiResponse.ErrorDetail fieldError(FieldError error) {
        return new ApiResponse.ErrorDetail(error.getField(), validationMessage(error));
    }

    private String validationMessage(FieldError error) {
        try {
            jakarta.validation.ConstraintViolation<?> violation = error.unwrap(jakarta.validation.ConstraintViolation.class);
            return validationMessage(error.getField(),
                    violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                    violation.getConstraintDescriptor().getAttributes());
        } catch (IllegalArgumentException ex) {
            return validationMessage(error.getField(), error.getCode(), Map.of());
        }
    }

    private String validationMessage(String field, String code, Map<String, Object> attributes) {
        String name = FieldNames.displayName(field);
        return switch (code == null ? "" : code) {
            case "NotBlank", "NotEmpty", "NotNull" -> name + "不能为空";
            case "Size" -> sizeMessage(name, attributes);
            case "Min" -> name + "不能小于 " + attributes.getOrDefault("value", "最小值");
            case "Max" -> name + "不能大于 " + attributes.getOrDefault("value", "最大值");
            case "Positive" -> name + "必须大于 0";
            case "Pattern" -> name + "格式不正确";
            case "Email" -> name + "邮箱格式不正确";
            case "Future" -> name + "必须是未来时间";
            case "FutureOrPresent" -> name + "不能早于当前时间";
            case "Past" -> name + "必须是过去时间";
            case "PastOrPresent" -> name + "不能晚于当前时间";
            default -> name + "不符合要求";
        };
    }

    private String sizeMessage(String name, Map<String, Object> attributes) {
        int min = number(attributes.get("min"), 0);
        int max = number(attributes.get("max"), Integer.MAX_VALUE);
        if (min <= 0 && max < Integer.MAX_VALUE) {
            return name + "不能超过 " + max + " 个字符";
        }
        if (max == Integer.MAX_VALUE) {
            return name + "不能少于 " + min + " 个字符";
        }
        return name + "长度需在 " + min + " 到 " + max + " 个字符之间";
    }

    private int number(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }
}
