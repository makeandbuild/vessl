package com.makeandbuild.vessl.persistence;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.makeandbuild.vessl.persistence.AbstractPagedRequest;
import com.makeandbuild.vessl.persistence.Criteria;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class EventDao_IT extends AbstractTestNGSpringContextTests {

    @Autowired 
    EventDao eventDao;
    
    @AfterMethod
    public void cleanUp() {
        eventDao.deleteAll();
    }

    @Test
    public void testAll(){
        Event event= new Event();
        String uuid = UUID.randomUUID().toString();
        event.setId(uuid);
        event.setType("user.loggedin");
        
        event = eventDao.create(event);

        event.setType("user.loggedOut");
        event = eventDao.save(event);
        
        event = eventDao.find(uuid);
        
        assertEquals("user.loggedOut", event.getType());
        assertTrue(eventDao.exists(uuid));
        
        List<Event> items = eventDao.find(new AbstractPagedRequest(), new Criteria("id", uuid)).getItems();
        event = items.get(0);
        assertEquals("user.loggedOut", event.getType());
        
    }
}
