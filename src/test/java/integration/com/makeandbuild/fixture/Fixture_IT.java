package com.makeandbuild.fixture;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.makeandbuild.persistence.AbstractPagedRequest;
import com.makeandbuild.persistence.AbstractPagedResponse;
import com.makeandbuild.persistence.Criteria;
import com.makeandbuild.persistence.EventDao;
import com.makeandbuild.persistence.User;
import com.makeandbuild.persistence.UserDao;
import com.makeandbuild.persistence.couch.CouchDao;
import com.makeandbuild.persistence.couch.CouchId;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class Fixture_IT extends AbstractTestNGSpringContextTests {
    @Autowired 
    EventDao eventDao;
    
    @Autowired 
    UserDao userDao;

    @Autowired 
    CouchDao carDao;

    @Autowired 
    Fixture fixture;

    @Test(enabled=true)
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
        fixture.purge();
    }
    @Test(enabled=true)
    public void testResourceSingularly() throws IOException, ClassNotFoundException{
        fixture.purge(User.class, null);
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));

        fixture.load(new ResourceEntityLoaderImpl("/fixtures/com.makeandbuild.persistence.User.json"));
        assertTrue(userDao.exists(1L));
        assertTrue(userDao.exists(2L));
    }
    @Test(enabled=true)
    public void testEntityClassSingularly() throws IOException, ClassNotFoundException{
        fixture.purge(User.class, null);
        assertTrue(!userDao.exists(1L));
        assertTrue(!userDao.exists(2L));

        fixture.load(User.class, null);
        assertTrue(userDao.exists(1L));
        assertTrue(userDao.exists(2L));
    }
    @Test
    public void tesDao() throws IOException, ClassNotFoundException{
        fixture.purge(ObjectNode.class, "car");
        fixture.load(ObjectNode.class, "car");
        ObjectNode car = carDao.find(new CouchId("123123123"));
        assertNotNull(car);
        
        List<Criteria> criterias = new ArrayList<Criteria>();
        criterias.add(new Criteria("view", "_design/car/_view/byMake"));
        criterias.add(new Criteria("key", "BMW"));
        AbstractPagedResponse<ObjectNode, ArrayNode> response = carDao.find(new AbstractPagedRequest(), criterias);
        assertTrue(response.getItems().size() ==2);
        
        criterias = new ArrayList<Criteria>();
        criterias.add(new Criteria("view", "_design/car/_view/byYear"));
        criterias.add(new Criteria("key", 2003));
        response = carDao.find(new AbstractPagedRequest(), criterias);
        assertTrue(response.getItems().size() ==1);
        
    }
}
