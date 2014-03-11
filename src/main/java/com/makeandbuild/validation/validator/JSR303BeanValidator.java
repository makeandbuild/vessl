package com.makeandbuild.validation.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.Errors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * JSR-303 spec bean validator. validates a bean based on the JSR-303 annotations that are present in the bean.
 *
 * User: Jeremy Dyer
 * Date: 3/7/14
 * Time: 11:19 AM
 */
public class JSR303BeanValidator
    implements org.springframework.validation.Validator, InitializingBean {

    private Validator validator;

    public void afterPropertiesSet() throws Exception {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    public boolean supports(Class clazz) {
        return true;
    }

    public void validate(Object target, Errors errors) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(target);
        for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
            //TODO: Need clever mechanism for injecting error messages either via the Annotation (already supported)
            // or from a properties file, URL, DB, Ask Jeeves, or whatever ....
            String propertyPath = constraintViolation.getPropertyPath().toString();
            String message = constraintViolation.getMessage();
            errors.rejectValue(propertyPath, "", message);
        }
    }
}
