package com.makeandbuild.vessl.propconfig;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;

public class SpringLoggingPropertyOverrideConfigurer extends PropertyOverrideConfigurer {

    @SuppressWarnings("rawtypes")
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
        logger.info("Loading override properties...");
        super.processProperties(beanFactory, props);

        logger.info("Properties loaded:");
        for (Map.Entry entry : props.entrySet()) {
            logger.info(entry.getKey() + ": " + entry.getValue());
        }

    }
}
