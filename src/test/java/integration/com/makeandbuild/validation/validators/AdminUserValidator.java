package com.makeandbuild.validation.validators;

import com.makeandbuild.persistence.AdminUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Provides lower level validation for the AdminUser than can be presented in the JSR-303 annotations
 * that decorate the AdminUser class. Example connecting to DB to determine if AdminUser permissions
 * have been invoked for this user or not, or connecting to a microcontroller device and make sure the
 * admin isn't drunk logging into the production environment, etc.
 *
 * User: Jeremy Dyer
 * Date: 3/7/14
 * Time: 8:55 AM
 */
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
