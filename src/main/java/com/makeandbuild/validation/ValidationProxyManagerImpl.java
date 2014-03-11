package com.makeandbuild.validation;

import com.makeandbuild.persistence.jdbc.BaseDao;
import com.makeandbuild.validation.proxy.BeanValidationProxy;
import com.makeandbuild.validation.proxy.SecurityValidationProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.Validator;

import java.util.*;

/**
 * ValidationProxyManager implementation
 *
 * User: Jeremy Dyer
 * Date: 3/6/14
 * Time: 4:59 PM
 */
public class ValidationProxyManagerImpl
    implements ValidationProxyManager {

    Log logger = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;

    //Cached Map of Spring validator(s)
    private final Map<Class<?>, List<Validator>> cachedValidatorsMap = new HashMap<Class<?>, List<Validator>>();

    @Override
    public Object newBeanValidatorProxy(BaseDao obj) {
        return BeanValidationProxy.newInstance(obj, cachedValidatorsMap);
    }

    @Override
    public Object newSecurityValidatorProxy(BaseDao obj) {
        return SecurityValidationProxy.newInstance(obj);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        //Gets all of the Validator beans from the application Context.
        Map<String, Validator> beanValidators = applicationContext.getBeansOfType(Validator.class);
        String[] mnbDaoBeans = applicationContext.getBeanNamesForType(BaseDao.class);

        for (int i = 0; i < mnbDaoBeans.length; i++) {

            //Gets the Class type that the Dao handles
            BaseDao type = (BaseDao) applicationContext.getBean(mnbDaoBeans[i]);

            Iterator<String> itr = beanValidators.keySet().iterator();
            List<Validator> validators = new ArrayList<Validator>();
            while (itr.hasNext()) {
                String key = itr.next();
                Validator validator = beanValidators.get(key);

                if (validator.supports(type.getEntityClass())) {
                    validators.add(validator);
                }
            }

            if (validators.size() > 0) {
                this.cachedValidatorsMap.put(type.getEntityClass(), validators);
            }
        }

        logger.info("Validation Framework is bootstrapped and ready to roll");
    }
}