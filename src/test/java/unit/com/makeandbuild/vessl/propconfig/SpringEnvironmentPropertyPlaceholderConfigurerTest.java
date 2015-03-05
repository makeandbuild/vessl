package com.makeandbuild.vessl.propconfig;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringEnvironmentPropertyPlaceholderConfigurerTest {
	@Test
	public void testDefault() {
		System.setProperty("environmentName", "");
		System.setProperty("environmentFilename","");
        ApplicationContext context = new FileSystemXmlApplicationContext(new String[] { "src/test/resources/spring-propconfig.xml" });
        TestBean mybean = (TestBean) context.getBean("mybean");
        Assert.assertEquals("base", mybean.getFoo());
        Assert.assertEquals("base1", mybean.getFoo1());
        Assert.assertEquals("base2", mybean.getFoo2());
	}
	@Test
	public void testEnvironmentName() {
		System.setProperty("environmentName", "dev");
		System.setProperty("environmentFilename","");
        ApplicationContext context = new FileSystemXmlApplicationContext(new String[] { "src/test/resources/spring-propconfig.xml" });
        TestBean mybean = (TestBean) context.getBean("mybean");
        Assert.assertEquals("dev", mybean.getFoo());
        Assert.assertEquals("base1", mybean.getFoo1());
        Assert.assertEquals("dev2", mybean.getFoo2());
	}
	@Test
	public void testEnvironmentFilename() {
		System.setProperty("environmentName", "");
		System.setProperty("environmentFilename","./src/test/resources/loadertest.properties");
        ApplicationContext context = new FileSystemXmlApplicationContext(new String[] { "src/test/resources/spring-propconfig.xml" });
        TestBean mybean = (TestBean) context.getBean("mybean");
        Assert.assertEquals("lt", mybean.getFoo());
        Assert.assertEquals("base1", mybean.getFoo1());
        Assert.assertEquals("lt2", mybean.getFoo2());
	}
}
