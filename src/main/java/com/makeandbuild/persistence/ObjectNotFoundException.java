package com.makeandbuild.persistence;

@SuppressWarnings("serial")
public class ObjectNotFoundException extends DaoException {

    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectNotFoundException(Throwable cause) {
        super(cause);
    }

}
