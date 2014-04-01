package com.makeandbuild.validation.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.makeandbuild.persistence.AdminUser;
import com.makeandbuild.persistence.User;
import com.makeandbuild.validation.ValidationType;

@ValidationType("security")
public class SecurityValidator implements Validator {

    private int count = 0;
    
    @Override
    public boolean supports(Class<?> clazz) {
        return AdminUser.class.equals(clazz) || User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        count++;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
