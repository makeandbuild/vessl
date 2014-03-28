package com.makeandbuild.validation;

import org.springframework.context.ApplicationContextAware;

import com.makeandbuild.persistence.Dao;

/**
 * User: Jeremy Dyer
 * Date: 3/6/14
 * Time: 4:59 PM
 */
@SuppressWarnings({ "rawtypes" })
public interface ValidationProxyManager
    extends ApplicationContextAware {

    public abstract Object newBeanValidatorProxy(Dao obj);

    public abstract Object newSecurityValidatorProxy(Dao obj);
    
    public abstract Object newBeanValidatorProxy(Dao obj, String... validationTypes);

}
