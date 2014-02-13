package com.makeandbuild.persistence;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.makeandbuild.fixture.Fixture;

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

}
