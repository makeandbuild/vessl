package com.makeandbuild.validation.validators;

import com.makeandbuild.persistence.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalTime;
import org.springframework.format.datetime.joda.LocalTimeParser;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Provides more low level validation that can occur on the User bean.
 *
 * User: Jeremy Dyer
 * Date: 3/6/14
 * Time: 6:01 PM
 */
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
