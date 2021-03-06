package com.makeandbuild.vessl.validation.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.makeandbuild.vessl.persistence.User;

public class UserValidator
    implements Validator {

    Log logger = LogFactory.getLog(getClass());

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        //Random but just make sure that the user wasn't created before 1900 or something?
        DateTime dt = new DateTime(user.getCreatedAt().getTime());
        if (dt.year().get() <= 1900) {
            errors.rejectValue("createdAt", "local.error.dateold", "User must have been created after 1900");
        }
    }
}
