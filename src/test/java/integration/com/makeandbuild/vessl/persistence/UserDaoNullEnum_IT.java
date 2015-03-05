package com.makeandbuild.vessl.persistence;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class UserDaoNullEnum_IT extends AbstractTestNGSpringContextTests {

    @Autowired 
    UserDao userDao;
    
    @AfterMethod
    public void cleanUp() {
        userDao.deleteAll();
    }

    @Test
    public void testNullEnum() throws JsonGenerationException, JsonMappingException, IOException{
        AdminUser user= new AdminUser();
        user.setCreatedAt(new Date());
        user.setLatitude(33.801078);
        user.setLongitude(-84.436287);
        user.setLoginCount(1);
        user.setUsername("azuercher");
        user.setUserType(null);
        user = (AdminUser) userDao.create(user);
        Long createdUserId = user.getId(); 
        assertNotNull(createdUserId);
        user = (AdminUser) userDao.find(createdUserId);
        assertNull(user.getUserType());
    }
}
