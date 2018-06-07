package fr.zelus.jarvis.dialogflow;

import fr.zelus.jarvis.core.JarvisException;

public class DialogFlowException extends JarvisException {

    public DialogFlowException() {
        super();
    }

    public DialogFlowException(String message) {
        super(message);
    }

    public DialogFlowException(String message, Throwable cause) {
        super(message, cause);
    }

    public DialogFlowException(Throwable cause) {
        super(cause);
    }

    protected DialogFlowException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
