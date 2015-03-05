package com.makeandbuild.vessl.propconfig;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class SpringEnvironmentPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
    private static SpringEnvironmentPropertyPlaceholderConfigurer instance;
    private Properties merged;
    private boolean silent = false;

    private SpringEnvironmentPropertyPlaceholderConfigurer(Map<String, String> args) throws IOException {
        init(args);
    }

    public SpringEnvironmentPropertyPlaceholderConfigurer() {
        Map<String, String> args = new HashMap<String, String>();
        args.put("defaultResourceLocation", "/config.properties");
        args.put("propertyfileSystemPropertyName", "environmentFilename");
        args.put("environmentSystemPropertyname", "environmentName");
        init(args);
    }

    public static SpringEnvironmentPropertyPlaceholderConfigurer createInstance(Map<String, String> args) throws IOException {
        if (instance == null) {
            instance = new SpringEnvironmentPropertyPlaceholderConfigurer(args);
        }
        return instance;
    }

    public static SpringEnvironmentPropertyPlaceholderConfigurer getInstance() {
        if (instance == null) {
            throw new RuntimeException("placeholder not properly configured");
        }
        return instance;
    }
    public static String getEnvironmentPropertyResource(String defaultResourceLocation, String environmentName) {
        return defaultResourceLocation.replaceAll("\\.properties", "") + "-"+environmentName+".properties";
    }
    private void init(Map<String, String> constructorArgs) {
        ArrayList<Resource> locationsList = new ArrayList<Resource>();

        String silentValue = constructorArgs.get("silent");
        if (silentValue != null && silentValue.equals("true")) {
            silent = true;
        }

        String defaultResourceLocation = constructorArgs.get("defaultResourceLocation");
        locationsList.add(new ClassPathResource(defaultResourceLocation));

        String environmentSystemPropertyname = constructorArgs.get("environmentSystemPropertyname");
        if (environmentSystemPropertyname != null) {
            String environment = System.getProperty(environmentSystemPropertyname);
            if (environment != null && !"".equals(environment)) {
                print("environmentName is " + environment);
                String environmentPropertyResource = getEnvironmentPropertyResource(defaultResourceLocation,environment);
                InputStream is = SpringEnvironmentPropertyPlaceholderConfigurer.class.getResourceAsStream(environmentPropertyResource);
                if (is != null) {
                    locationsList.add(new ClassPathResource(environmentPropertyResource));
                }
            }
        }

        String propertyfileSystemPropertyName = constructorArgs.get("propertyfileSystemPropertyName");
        if (propertyfileSystemPropertyName != null) {
            String filename = System.getProperty(propertyfileSystemPropertyName);
            if (filename != null && !"".equals(filename)) {
                print("environmentFilename is " + filename);
                File file = new File(filename);
                if (file.exists()) {
                    locationsList.add(new FileSystemResource(filename));
                }
            }
        }
        Resource[] locations = locationsList.toArray(new Resource[locationsList.size()]);
        this.setLocations(locations);
        print("set locations: ");
        for (Resource resource : locations) {
            try {
                print("" + resource.getURL());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            merged = this.mergeProperties();
        } catch (IOException e) {
            e.printStackTrace();
            logger.fatal("problem loading properties", e);
            throw new RuntimeException("problem loading properties", e);
        }
    }

    public Properties getMerged() {
        return merged;
    }

    private void printOptional(String message) {
        logger.debug(message);
    }

    private void print(String message) {
        System.out.println(message);
        logger.debug(message);
    }

    public void printProperties() {

        printOptional("EnvironmentPropertyPlaceholderConfigurer properties:");
        for (Entry<Object, Object> entry : merged.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            printOptional(key + "=" + value);
        }
    }

    public String getProperty(String name) {
        return this.merged.getProperty(name);
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

}
