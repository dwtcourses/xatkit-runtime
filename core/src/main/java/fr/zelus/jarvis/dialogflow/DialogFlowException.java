package fr.zelus.jarvis.dialogflow;

import fr.zelus.jarvis.core.JarvisException;

/**
 * Wraps all the exceptions returned by the DialogFlow API.
 */
public class DialogFlowException extends JarvisException {

    /**
     * Constructs a new {@link DialogFlowException}.
     *
     * @see RuntimeException#RuntimeException()
     */
    public DialogFlowException() {
        super();
    }

    /**
     * Constructs a new {@link DialogFlowException} frmo the provided {@code message}.
     *
     * @param message the exception's message
     * @see RuntimeException#RuntimeException(String)
     */
    public DialogFlowException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link DialogFlowException} from the provided {@code message} and {@code cause}.
     *
     * @param message the exception's message
     * @param cause   the exception's cause
     */
    public DialogFlowException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link DialogFlowException} from the provided {@code cause}.
     *
     * @param cause the exception's cause
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public DialogFlowException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@link DialogFlowException} from the provided {@code message}, {@code cause}, {@code
     * enableSuppression}, and {@code writableStackTrace}
     *
     * @param message            the exception's message
     * @param cause              the exception's cause
     * @param enableSuppression  whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     * @see RuntimeException#RuntimeException(String, Throwable, boolean, boolean)
     */
    protected DialogFlowException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
