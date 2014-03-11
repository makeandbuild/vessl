package com.makeandbuild.validation;

import com.makeandbuild.persistence.jdbc.BaseDao;

import org.springframework.context.ApplicationContextAware;

/**
 * User: Jeremy Dyer
 * Date: 3/6/14
 * Time: 4:59 PM
 */
public interface ValidationProxyManager
    extends ApplicationContextAware {

    public abstract Object newBeanValidatorProxy(BaseDao obj);

    public abstract Object newSecurityValidatorProxy(BaseDao obj);
}
