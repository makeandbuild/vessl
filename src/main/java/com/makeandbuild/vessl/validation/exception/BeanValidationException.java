package com.makeandbuild.vessl.validation.exception;

import java.util.List;

import org.springframework.validation.ObjectError;

@SuppressWarnings("serial")
public class BeanValidationException
    extends RuntimeException {

    private List<ObjectError> errors;
    private Object validatedBean;

    public BeanValidationException(String message, List<ObjectError> errors, Object validatedBean) {
        super(message);
        this.setErrors(errors);
        this.setValidatedBean(validatedBean);
    }

    public List<ObjectError> getErrors() {
        return errors;
    }

    public void setErrors(List<ObjectError> errors) {
        this.errors = errors;
    }

    public Object getValidatedBean() {
        return validatedBean;
    }

    public void setValidatedBean(Object validatedBean) {
        this.validatedBean = validatedBean;
    }
}
