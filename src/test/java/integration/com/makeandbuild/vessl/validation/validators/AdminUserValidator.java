package com.makeandbuild.vessl.validation.validators;

import com.makeandbuild.vessl.persistence.AdminUser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class AdminUserValidator
        implements Validator {

    Log logger = LogFactory.getLog(getClass());

    @Override
    public boolean supports(Class<?> aClass) {
        return AdminUser.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AdminUser adminUser = (AdminUser) o;
        //TODO: Move along ... nothing to see here
    }
}
