package com.xatkit.shell;

/**
 * Xatkit shell top-level exception.
 * <p>
 * This exception is used to print errors in the Xatkit shell.
 */
public class XatkitShellException extends RuntimeException {

    /**
     * Constructs a new {@link XatkitShellException}.
     *
     * @see RuntimeException#RuntimeException()
     */
    public XatkitShellException() {
        super();
    }

    /**
     * Constructs a new {@link XatkitShellException} from the provided {@code message}.
     *
     * @param message the exception's message
     * @see RuntimeException#RuntimeException(String)
     */
    public XatkitShellException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link XatkitShellException} from the provided {@code message} and {@code cause}.
     *
     * @param message the exception's message
     * @param cause   the exception's cause
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public XatkitShellException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link XatkitShellException} from the provided {@code cause}.
     *
     * @param cause the exception's cause
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public XatkitShellException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link XatkitShellException} from the provided {@code message}, {@code cause}, {@code
     * enableSuppression}, and {@code writableStackTrace}.
     *
     * @param message            the exception's message
     * @param cause              the exception's cause
     * @param enableSuppression  whether or not suppression is enabled
     * @param writableStackTrace whether or not the stack trace should be writable
     * @see RuntimeException#RuntimeException(String, Throwable, boolean, boolean)
     */
    protected XatkitShellException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
