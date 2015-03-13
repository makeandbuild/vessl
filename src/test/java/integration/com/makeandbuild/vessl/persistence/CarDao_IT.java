package com.makeandbuild.vessl.persistence;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.makeandbuild.vessl.persistence.couch.CouchDao;
import com.makeandbuild.vessl.persistence.couch.CouchId;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class CarDao_IT extends AbstractTestNGSpringContextTests {
    @Autowired
    CouchDao carDao;

    @Test
    public void testCouchDao() throws IOException, ClassNotFoundException{
        carDao.delete(new Criteria("key", "toyota"),new Criteria("view", "_design/car/_view/byMake"));
    	ObjectNode obj = createCar(2015);
    	
    	String id = obj.get("_id").asText();
        ObjectNode car = carDao.find(new CouchId(id));
        assertNotNull(car);

        List<Criteria> criterias = new ArrayList<Criteria>();
        criterias.add(new Criteria("view", "_design/car/_view/byYear"));
        criterias.add(new Criteria("key", 2015));
        AbstractPagedResponse<ObjectNode, ArrayNode> response = carDao.find(new AbstractPagedRequest(), criterias);
        assertTrue(response.getItems().size() ==1);

    	createCar(2006);
    	createCar(2007);
    	createCar(2008);
    	createCar(2009);
    	createCar(2011);
    	createCar(2012);

        criterias = new ArrayList<Criteria>();
        criterias.add(new Criteria("view", "_design/car/_view/byMake"));
        criterias.add(new Criteria("key", "toyota"));
        response = carDao.find(new AbstractPagedRequest(0,2), criterias);
        assertTrue(response.getItems().size() >1);
        assertTrue(response.getTotalPages() > 1);
        response = carDao.find(new AbstractPagedRequest(1,2), criterias);
        assertTrue(response.getItems().size() >1);
        assertTrue(response.getTotalPages() > 1);
        
        carDao.delete(new Criteria("key", "toyota"),new Criteria("view", "_design/car/_view/byMake"));
    }

	private ObjectNode createCar(int year) {
		ObjectNode obj = new ObjectMapper().createObjectNode();
    	obj.put("make", "toyota");
    	obj.put("model", "camry");
    	obj.put("year", year);
    	obj = carDao.save(obj);
		return obj;
	}
}
