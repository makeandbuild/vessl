package com.makeandbuild.persistence;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
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
    @Test
    public void testSpecialized() throws JsonGenerationException, JsonMappingException, IOException{
        AdminUser user= new AdminUser();
        user.setCreatedAt(new Date());
        user.setLatitude(33.801078);
        user.setLongitude(-84.436287);
        user.setLoginCount(1);
        user.setUsername("azuercher");
        user.setUserType(UserType.simple);
        user.setApiKey("123123");
        user = (AdminUser) userDao.create(user);
        Long createdUserId = user.getId(); 
        assertNotNull(createdUserId);

        user.setUserType(UserType.admin);
        user = (AdminUser) userDao.save(user);
        
        user = (AdminUser) userDao.find(createdUserId);
        
        assertEquals(UserType.admin, user.getUserType());
        assertEquals("123123", user.getApiKey());
        assertNotNull(user.getCreatedAt());

        ApplicantUser applicantUser= new ApplicantUser();
        applicantUser.setCreatedAt(new Date());
        applicantUser.setLatitude(33.801078);
        applicantUser.setLongitude(-84.436287);
        applicantUser.setLoginCount(1);
        applicantUser.setUsername("azuercher");
        applicantUser.setUserType(UserType.simple);
        applicantUser.setApplicantId("11221123132");
        applicantUser = (ApplicantUser) userDao.create(applicantUser);
        createdUserId = applicantUser.getId(); 
        assertNotNull(createdUserId);

        applicantUser.setUserType(UserType.admin);
        applicantUser = (ApplicantUser) userDao.save(applicantUser);
        
        applicantUser = (ApplicantUser) userDao.find(createdUserId);
        
        assertEquals(UserType.admin, applicantUser.getUserType());
        assertEquals("11221123132", applicantUser.getApplicantId());
        assertNotNull(applicantUser);
    }
}
