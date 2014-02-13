package com.makeandbuild.fixture;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.makeandbuild.persistence.EventDao;
import com.makeandbuild.persistence.User;
import com.makeandbuild.persistence.UserDao;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class Fixture_IT extends AbstractTestNGSpringContextTests {
    @Autowired 
    EventDao eventDao;
    
    @Autowired 
    UserDao userDao;

    @Autowired 
    Fixture fixture;

    @Test
    public void testAll() throws IOException{
        fixture.purge();
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));
        
        assertTrue(!eventDao.exists("1231231231-222"));
        assertTrue(!eventDao.exists("1231231231-12312312-12-3123123"));

        fixture.load();
        assertTrue(userDao.exists(1L));
        assertTrue(userDao.exists(2L));
        
        assertTrue(eventDao.exists("1231231231-222"));
        assertTrue(eventDao.exists("1231231231-12312312-12-3123123"));
    }
    @Test
    public void testResourceSingularly() throws IOException, ClassNotFoundException{
        fixture.purge(User.class);
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));

        fixture.load(new ResourceEntityLoaderImpl("/fixtures/com.makeandbuild.persistence.User.json"));
        assertTrue(userDao.exists(1L));
        assertTrue(userDao.exists(2L));
    }
    @Test
    public void testEntityClassSingularly() throws IOException, ClassNotFoundException{
        fixture.purge(User.class);
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));

        fixture.load(User.class);
        assertTrue(userDao.exists(1L));
        assertTrue(userDao.exists(2L));
    }
}
