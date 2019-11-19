package net.htwater.sesame.dms.core.exception;

import net.htwater.sesame.dms.core.HttpStatusCode;

/**
 * @author Jokki
 */
public abstract class AbstractManagementException extends RuntimeException {
    private static final long serialVersionUID = -6504782000757835534L;

    public AbstractManagementException() {
    }

    /**
     * @param cause the exception cause
     */
    public AbstractManagementException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message the exception message
     */
    public AbstractManagementException(String message) {
        super(message);
    }

    /**
     * @param message the exception message
     * @param cause the exception cause
     */
    public AbstractManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getHttpStatusCode() {
        return HttpStatusCode.INTERNAL_SERVER_ERROR_500;
    }
}
