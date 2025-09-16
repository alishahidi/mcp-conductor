package net.alishahidi.mcpconductor.exception;

public class ValidationException extends RuntimeException {

    private final String field;
    private final Object rejectedValue;
    private final String validationRule;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.rejectedValue = null;
        this.validationRule = null;
    }

    public ValidationException(String field, Object rejectedValue, String message) {
        super(String.format("Validation failed for field '%s': %s (rejected value: %s)",
                field, message, rejectedValue));
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.validationRule = null;
    }

    public ValidationException(String field, Object rejectedValue, String validationRule, String message) {
        super(String.format("Validation failed for field '%s': %s (rule: %s, rejected value: %s)",
                field, message, validationRule, rejectedValue));
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.validationRule = validationRule;
    }

    public String getField() {
        return field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public String getValidationRule() {
        return validationRule;
    }
}