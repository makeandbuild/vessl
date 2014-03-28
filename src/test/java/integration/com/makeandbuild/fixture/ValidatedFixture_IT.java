package com.makeandbuild.fixture;

import static org.testng.AssertJUnit.*;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.makeandbuild.persistence.EventDao;
import com.makeandbuild.persistence.User;
import com.makeandbuild.persistence.UserDao;
import com.makeandbuild.validation.exception.BeanValidationException;
import com.makeandbuild.validation.validators.NonDataValidator;
import com.makeandbuild.validation.validators.SecurityValidator;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class ValidatedFixture_IT extends AbstractTestNGSpringContextTests {
    @Autowired 
    EventDao eventDao;
    
    @Autowired 
    UserDao userDao;

    @Autowired 
    Fixture validatedFixture;
    
    @Autowired
    NonDataValidator nonDataValidator;

    @Autowired
    SecurityValidator securityValidator;

    @Test(enabled=true)
    public void testAll() throws IOException{
        validatedFixture.purge();
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));
        
        assertTrue(!eventDao.exists("1231231231-222"));
        assertTrue(!eventDao.exists("1231231231-12312312-12-3123123"));

        validatedFixture.load();
        assertTrue(userDao.exists(1L));
        assertTrue(userDao.exists(2L));
        
        assertTrue(eventDao.exists("1231231231-222"));
        assertTrue(eventDao.exists("1231231231-12312312-12-3123123"));
        validatedFixture.purge();
    }
    @Test(enabled=true)
    public void testResourceSingularly() throws IOException, ClassNotFoundException{
        validatedFixture.purge(User.class, null);
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));

        nonDataValidator.setCount(0);
        securityValidator.setCount(0);
        validatedFixture.load(new ResourceEntityLoaderImpl("/fixtures/com.makeandbuild.persistence.User.json"));
        assertNotSame(0, nonDataValidator.getCount());
        assertEquals(0, securityValidator.getCount());
        assertTrue(userDao.exists(1L));
        assertTrue(userDao.exists(2L));
    }
    @Test(enabled=true)
    public void testResourceSingularlyInvalid() throws IOException, ClassNotFoundException{
        validatedFixture.purge(User.class, null);
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));

        try {
            validatedFixture.load(new ResourceEntityLoaderImpl("/fixtures/com.makeandbuild.persistence.Userinvalid.json", User.class, null));
            fail("should have been invalid");
        }catch (BeanValidationException e){
            assertNotNull(e);
        }
    }
    @Test(enabled=true)
    public void testEntityClassSingularly() throws IOException, ClassNotFoundException{
        validatedFixture.purge(User.class, null);
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));

        validatedFixture.load(User.class, null);
        assertTrue(userDao.exists(1L));
        assertTrue(userDao.exists(2L));
    }
}
