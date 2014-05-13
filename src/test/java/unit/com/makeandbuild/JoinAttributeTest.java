package com.makeandbuild;

import org.junit.Assert;
import org.junit.Test;

import com.makeandbuild.persistence.jdbc.AttributeParser;

public class JoinAttributeTest {
    @Test
    public void testAttributed() {
        String className = AttributeParser.getDomainName("contact.firstName");
        String attributeName = AttributeParser.getAttribute("contact.firstName");
        Assert.assertEquals("contact", className);
        Assert.assertEquals("firstName", attributeName);
        
        className = AttributeParser.getDomainName("firstName");
        attributeName = AttributeParser.getAttribute("firstName");
        Assert.assertNull(className);
        Assert.assertEquals("firstName", attributeName);
        
    }
}
