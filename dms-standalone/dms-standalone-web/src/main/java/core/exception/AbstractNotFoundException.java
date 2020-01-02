package core.exception;

import core.HttpStatusCode;

/**
 * @author Jokki
 */
public abstract class AbstractNotFoundException extends AbstractManagementException {

    private static final long serialVersionUID = -1905918739160622938L;

    @Override
    public int getHttpStatusCode() {
        return HttpStatusCode.NOT_FOUND_404;
    }

    @Override
    public String getMessage() {
        return "data not found";
    }
}
