package com.makeandbuild.vessl.persistence;


import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class AdminUserDao_IT extends AbstractTestNGSpringContextTests{

    @Autowired 
    UserDao userDao;
    
    @Autowired
    AdminUserDao adminUserDao;
    
    @AfterMethod
    public void cleanUp() {
        userDao.deleteAll();
    }
    
    @Test
    public void testFindByUserName(){
        
        AdminUser user= new AdminUser();
        user.setCreatedAt(new Date());
        user.setLatitude(33.801078);
        user.setLongitude(-84.436287);
        user.setLoginCount(1);
        user.setUsername("azuercher888");
        user.setUserType(UserType.simple);
        user.setApiKey("TestKeys");
        
        user = (AdminUser) userDao.create(user);
        Long createdUserId = user.getId(); 
        assertNotNull(createdUserId);

        user.setUserType(UserType.admin);
        user = (AdminUser) userDao.save(user);
        
        List<AdminUser> adminUserList = adminUserDao.findAdminUserByUserName("azuercher888");
        assertNotNull(adminUserList);
        assertTrue(adminUserList.size()>0);
        
        AdminUser foundUser = adminUserList.get(0);
        assertNotNull(foundUser.getApiKey());
        assertNotNull(foundUser.getUsername());
        assertNotNull(foundUser.getUserType());
    }
}
