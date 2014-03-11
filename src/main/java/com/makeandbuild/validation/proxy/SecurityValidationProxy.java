package com.makeandbuild.validation.proxy;

import com.makeandbuild.persistence.jdbc.BaseDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Proxy to examine the Security context and ensure that
 *
 * User: Jeremy Dyer
 * Date: 3/7/14
 * Time: 1:42 PM
 */
public class SecurityValidationProxy
        implements InvocationHandler {

    Log logger = LogFactory.getLog(getClass());

    private BaseDao obj;

    public static Object newInstance(BaseDao obj) {
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new SecurityValidationProxy(obj));
    }

    private SecurityValidationProxy(BaseDao obj) {
        this.obj = obj;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        Object result;

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                result = m.invoke(obj, args);
            } else {
                throw new SecurityException("Authentication Required");
            }

        } catch (InvocationTargetException e) {
            logger.error("problem with method " + m.getName(), e);
            throw e.getTargetException();
        } finally {
            logger.debug("after method " + m.getName());
        }

        return result;
    }
}
