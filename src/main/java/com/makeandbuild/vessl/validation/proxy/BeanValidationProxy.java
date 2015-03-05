package com.makeandbuild.vessl.validation.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.makeandbuild.vessl.persistence.Dao;
import com.makeandbuild.vessl.persistence.jdbc.BaseDao;
import com.makeandbuild.vessl.validation.ValidationType;
import com.makeandbuild.vessl.validation.exception.BeanValidationException;

/**
 * Creates a dynamic proxy for a DAO that extends BaseDao. That dynamic proxy will in turn
 * validate any beans that passed to any BaseDao methods which the dao itself supports based
 * on it entity class.
 *
 * User: Jeremy Dyer
 * Date: 3/6/14
 * Time: 4:54 PM
 */
@SuppressWarnings({ "unused", "rawtypes" })
public class BeanValidationProxy
        implements InvocationHandler {

    Log logger = LogFactory.getLog(getClass());

    private Dao obj;
    private String[] validationTypes;

    //Cached Map of Spring validator(s)
    private Map<Class<?>, List<Validator>> cachedValidatorsMap = null;

    public static Object newInstance(Dao obj, Map<Class<?>, List<Validator>> validatorCacheMap) {
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new BeanValidationProxy(obj, validatorCacheMap));
    }

    private BeanValidationProxy(Dao obj, Map<Class<?>, List<Validator>> validatorCacheMap, String... validationTypes) {
        this.obj = obj;
        this.cachedValidatorsMap = validatorCacheMap;
        this.validationTypes = validationTypes;
    }
    public static Object newInstance(Dao obj, Map<Class<?>, List<Validator>> validatorCacheMap, String... validationTypes) {
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new BeanValidationProxy(obj, validatorCacheMap, validationTypes));
    }
    private boolean skip(Validator validator){
        if (validationTypes == null || validationTypes.length == 0){
            return false;
        } else {
            String type = "data";
            if (validator.getClass().isAnnotationPresent(ValidationType.class)){
                type = validator.getClass().getAnnotation(ValidationType.class).value();
            }
            for (String filter : validationTypes){
                if (filter.equals(type)){
                    return false;
                }
            }
            return true;
        }

    }
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        Object result;
        try {

            //Check method that is being invoked actually even contains the Class we are interested in.
            if (args != null) {
                List<Object> parameterBeansToValidate = null;
                for (int i = 0; i < args.length; i++) {
                    if (obj.getEntityClass().isAssignableFrom(args[i].getClass())) {
                        if (parameterBeansToValidate == null)
                            parameterBeansToValidate = new ArrayList<Object>();
                        parameterBeansToValidate.add(args[i]);
                    }
                }

                if (parameterBeansToValidate != null && parameterBeansToValidate.size() > 0) {

                    //Runs the validator against each bean that was in the parameters.
                    for (Object paramBean : parameterBeansToValidate) {

                        if (cachedValidatorsMap.containsKey(paramBean.getClass())) {

                            List<Validator> validators = cachedValidatorsMap.get(paramBean.getClass());

                            for (Validator validator : validators) {
                                if (skip(validator))
                                    continue;
                                List<ObjectError> validationErrors = null;
                                if (validator.supports(paramBean.getClass())) {
                                    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult
                                            (paramBean, paramBean.getClass().getName());
                                    ValidationUtils.invokeValidator(validator, paramBean, bindingResult);

                                    if (bindingResult.hasErrors()) {
                                        if (validationErrors == null)
                                            validationErrors = new ArrayList<ObjectError>();

                                        validationErrors.addAll(bindingResult.getAllErrors());
                                    }
                                }

                                if (validationErrors != null && validationErrors.size() > 0) {
                                    throw new BeanValidationException(paramBean.getClass().getName() + " " +
                                            "validation failed", validationErrors, paramBean);
                                }
                            }
                        } else {
                            logger.debug("No validators found for Bean - " + paramBean.getClass().getName());
                        }
                    }
                }
            }

            logger.debug("Invoking method " + m.getName() + " from dynamic proxy " + getClass().getName());
            result = m.invoke(obj, args);

        } catch (InvocationTargetException e) {
            logger.error("problem with method " + m.getName(), e);
            throw e.getTargetException();
        } finally {
            logger.debug("after method " + m.getName());
        }
        return result;
    }

}
