package org.wixpress.hoopoe.lambda;

/**
 * @author Yoav
 * @since 10/3/11
 */
public class LambdaException extends RuntimeException {
    public LambdaException(String message, Object ... args) {
        super(String.format(message, args));
    }

    public LambdaException(String message, Throwable cause, Object ... args) {
        super(String.format(message, args), cause);
    }

}
