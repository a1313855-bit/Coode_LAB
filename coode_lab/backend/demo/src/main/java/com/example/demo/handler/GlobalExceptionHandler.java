package com.example.demo.handler;

// ========== Java ==========
import java.util.Map;

// ========== Jakarta Validation ==========
import jakarta.validation.ConstraintViolationException;

// ========== Spring ==========
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全域例外處理。
 *
 * 把過去會回傳 500 的「參數錯誤 / 業務錯誤」，
 * 轉成前端可以解析並顯示中文訊息的 4xx 回應。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ======================================================
    // 1. Bean Validation 錯誤（@Valid @RequestBody DTO）
    // ======================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("請求參數格式錯誤");

        return error(HttpStatus.BAD_REQUEST, message);
    }

    // ======================================================
    // 2. 方法參數驗證錯誤（@Valid 用在 query object）
    // ======================================================
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidation(
            HandlerMethodValidationException ex) {

        String message = ex.getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .filter(text -> text != null && !text.isBlank())
                .orElse("請求參數格式錯誤");

        return error(HttpStatus.BAD_REQUEST, message);
    }

    // ======================================================
    // 3. ConstraintViolation（例如 method 參數上的註解）
    // ======================================================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ======================================================
    // 4. 缺少必填 query 參數
    // ======================================================
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex) {

        return error(HttpStatus.BAD_REQUEST, "缺少必要參數：" + ex.getParameterName());
    }

    // ======================================================
    // 5. 請求主體不是合法 JSON / 型別轉換失敗
    // ======================================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(
            HttpMessageNotReadableException ex) {

        return error(HttpStatus.BAD_REQUEST, "請求內容格式錯誤");
    }

    // ======================================================
    // 6. 資料庫完整性錯誤（例如 Email、名稱重複）
    // ======================================================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        return error(HttpStatus.CONFLICT, "資料衝突，可能有重複資料");
    }

    // ======================================================
    // 7. 找不到資源
    // ======================================================
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(
            NoResourceFoundException ex) {

        return error(HttpStatus.NOT_FOUND, "找不到資源");
    }

    // ======================================================
    // 8. 業務邏輯錯誤（例如 密碼錯誤、加入數量超過庫存）
    // ======================================================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return error(HttpStatus.BAD_REQUEST,
                ex.getMessage() == null ? "請求參數錯誤" : ex.getMessage());
    }

    // ======================================================
    // 9. 其它未預期錯誤（最後防線）
    // ======================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception ex) {

        return error(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器內部錯誤");
    }

    // ======================================================
    // 組錯誤回應主體，統一格式：
    // { "status": 400, "message": "..." }，前端讀 body.message
    // ======================================================
    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status,
            String message) {

        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "message", message == null ? "發生錯誤" : message
        ));
    }
}