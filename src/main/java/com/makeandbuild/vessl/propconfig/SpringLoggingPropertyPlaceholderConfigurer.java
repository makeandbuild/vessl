package com.makeandbuild.vessl.propconfig;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class SpringLoggingPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    @SuppressWarnings("rawtypes")
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
        logger.info("Loading default properties...");
        super.processProperties(beanFactory, props);

        logger.info("Properties loaded:");
        for (Map.Entry entry : props.entrySet()) {
            logger.info(entry.getKey() + ": " + entry.getValue());
        }
    }
}
