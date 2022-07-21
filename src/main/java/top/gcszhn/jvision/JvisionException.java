package top.gcszhn.jvision;

import lombok.Getter;

public class JvisionException extends Exception {
    private @Getter Stage stage;
    public JvisionException(String message, Throwable cause, Stage stage) {
        super(message, cause);
        this.stage = stage;
    }
    public String getMessage() {
        return super.getMessage() + "[" + stage + "]";
    }
}
