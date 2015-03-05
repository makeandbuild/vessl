package com.makeandbuild.vessl.fixture;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.makeandbuild.vessl.fixture.Fixture;
import com.makeandbuild.vessl.persistence.EventDao;
import com.makeandbuild.vessl.persistence.UserDao;
import com.makeandbuild.vessl.persistence.couch.CouchDao;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class FixtureDumper_IT extends AbstractTestNGSpringContextTests {
    @Autowired 
    EventDao eventDao;
    
    @Autowired 
    UserDao userDao;

    @Autowired 
    CouchDao carDao;

    @Autowired 
    Fixture fixture;

    @Test(enabled=true)
    public void testDump() throws IOException{
        fixture.dump(new File("dumped-fixtures"));
        
    }
}
