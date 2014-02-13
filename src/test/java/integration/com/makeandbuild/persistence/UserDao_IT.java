package com.makeandbuild.persistence;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class UserDao_IT extends AbstractTestNGSpringContextTests {

    @Autowired 
    UserDao userDao;
    
    @AfterMethod
    public void cleanUp() {
        userDao.deleteAll();
    }

    @Test
    public void testAll() throws JsonGenerationException, JsonMappingException, IOException{
        User user= new User();
        user.setCreatedAt(new Date());
        user.setLatitude(33.801078);
        user.setLongitude(-84.436287);
        user.setLoginCount(1);
        user.setUsername("azuercher");
        user.setUserType(UserType.simple);
        
        user = userDao.create(user);
        Long createdUserId = user.getId(); 
        assertNotNull(createdUserId);

        user.setUserType(UserType.admin);
        user = userDao.save(user);
        
        user = userDao.find(createdUserId);
        
        assertEquals(UserType.admin, user.getUserType());
        assertNotNull(user.getCreatedAt());
    }
}
