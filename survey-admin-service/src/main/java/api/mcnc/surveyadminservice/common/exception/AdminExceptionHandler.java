package api.mcnc.surveyadminservice.common.exception;

import api.mcnc.surveyadminservice.common.enums.AdminErrorCode;
import api.mcnc.surveyadminservice.common.result.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;

/**
 * please explain class!
 *
 * @author :Uheejoon
 * @since :2024-11-26 오전 10:30
 */
@RestControllerAdvice
public class AdminExceptionHandler {
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Api<String>> handleRuntimeException(RuntimeException e) {
    Api<String> response = Api.fail(AdminErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    e.printStackTrace();
    return ResponseEntity.ok(response);
  }

  @ExceptionHandler(AdminException.class)
  public ResponseEntity<Api<String>> handleResponseException(AdminException e) {
    Api<String> response = Api.fail(e.getCode(), e.getMessage());
    return ResponseEntity.ok(response);
  }

  @ExceptionHandler(TokenException.class)
  public ResponseEntity<Api<String>> handleTokenException(TokenException e) {
    Api<String> response = Api.fail(e.getCode(), e.getMessage());
    return ResponseEntity.ok(response);
  }

  @ExceptionHandler(AuthException.class)
  public ResponseEntity<Api<String>> handleAuthException(AuthException e) {
    Api<String> response = Api.fail(e.getCode(), e.getMessage());
    return ResponseEntity.ok(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Api<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    List<ErrorField> errorList = e.getBindingResult()
      .getAllErrors()
      .stream()
      .map(ErrorField::toErrorField)
      .toList();
    Api<Object> response = Api.fail(AdminErrorCode.INVALID_REQUEST, errorList);
    return ResponseEntity.ok(response);
  }


  // validation error field
  private record ErrorField(String field, String value) {
    static ErrorField toErrorField(ObjectError error) {
      FieldError fieldError = (FieldError) error;
      String field = fieldError.getField();
      String message = Objects.requireNonNull(error.getDefaultMessage());
      return new ErrorField(field, message);
    }
  }
}