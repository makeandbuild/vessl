package com.makeandbuild.vessl.validation;

import org.springframework.context.ApplicationContextAware;

import com.makeandbuild.vessl.persistence.Dao;


@SuppressWarnings({ "rawtypes" })
public interface ValidationProxyManager
    extends ApplicationContextAware {

    public abstract Object newBeanValidatorProxy(Dao obj);

    public abstract Object newSecurityValidatorProxy(Dao obj);

    public abstract Object newBeanValidatorProxy(Dao obj, String... validationTypes);

}
