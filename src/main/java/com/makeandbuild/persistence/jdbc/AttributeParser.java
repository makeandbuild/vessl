package com.makeandbuild.persistence.jdbc;

public class AttributeParser {
    public static String getDomainName(String attribute){
        if (attribute.indexOf(".") == -1){
            return null;
        } else {
            return attribute.substring(0, attribute.lastIndexOf("."));
        }
    }
    public static String getAttribute(String attribute){
        if (attribute.indexOf(".") == -1){
            return attribute;
        } else {
            return attribute.substring(attribute.lastIndexOf(".")+1);
        }
    }

}
