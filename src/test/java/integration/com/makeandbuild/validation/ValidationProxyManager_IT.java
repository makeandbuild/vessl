package com.makeandbuild.validation;

import com.makeandbuild.persistence.AdminUser;
import com.makeandbuild.persistence.User;
import com.makeandbuild.persistence.UserDao;
import com.makeandbuild.persistence.UserType;
import com.makeandbuild.validation.exception.BeanValidationException;
import junit.framework.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.testng.AssertJUnit.assertNotNull;

/**
 * User: Jeremy Dyer
 * Date: 3/6/14
 * Time: 5:07 PM
 */
@Test(groups = {"function"})
@ContextConfiguration(locations={"classpath*:spring.xml"})
public class ValidationProxyManager_IT
        extends AbstractTestNGSpringContextTests {

    @Autowired
    ValidationProxyManager validationProxyManager;

    @Autowired
    UserDao userDao;

    @Test
    public void testAdminUserFailValidationWithNoAPIKey() {
        AdminUser user = new AdminUser();
        user.setCreatedAt(new Date());
        user.setLatitude(33.801078);
        user.setLongitude(-84.436287);
        user.setLoginCount(1);
        user.setUsername("jdyer");
        user.setUserType(UserType.simple);
        //user.setApiKey("TestKeys");       //Omit APIKey to fail validation

        //Get a Validation Proxy instance.
        UserDao validationUserDao = (UserDao) validationProxyManager.newBeanValidatorProxy(userDao);

        try {
            validationUserDao.save(user);   //Will throw a validation exception
            Assert.fail(BeanValidationException.class.getName() + " exception was expected");
        } catch (BeanValidationException bve) {
            Assert.assertTrue(true);
        }
    }


    @Test
    public void testUserCreatedTooEarly() throws ParseException {
        User user = new User();
        user.setCreatedAt(new Date());
        user.setLatitude(33.801078);
        user.setLongitude(-84.436287);
        user.setLoginCount(1);
        user.setUsername("jdyer");
        user.setUserType(UserType.simple);

        //Get a Validation Proxy instance.
        UserDao validationUserDao = (UserDao) validationProxyManager.newBeanValidatorProxy(userDao);
        try {
            user = validationUserDao.save(user);    //Should work.
            validationUserDao.deleteById(user.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Set the date to before 1900
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date d = sdf.parse("01/01/1899");
        user.setCreatedAt(d);

        try {
            validationUserDao.save(user);   //Should throw validation exception now
            Assert.fail(BeanValidationException.class.getName() + " exception was expected");
        } catch (BeanValidationException bve) {
            Assert.assertTrue(true);
        }
    }


    @Test
    public void testValidateSecurity() {
        User user = new User();
        user.setCreatedAt(new Date());
        user.setLatitude(33.801078);
        user.setLongitude(-84.436287);
        user.setLoginCount(1);
        user.setUsername("dummy");
        user.setUserType(UserType.simple);

        user = userDao.save(user);

        UserDao secureUserDao = (UserDao) validationProxyManager.newSecurityValidatorProxy(userDao);

        User findUser = null;
        try {
            findUser = secureUserDao.find(user.getId());
            Assert.fail("Expected Security exception");
        } catch (SecurityException se) {
            Assert.assertTrue(true);
        }

        //Now create a dummy Authentication and try again.
        Authentication auth = new UsernamePasswordAuthenticationToken("dummy", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            findUser = secureUserDao.find(user.getId());
        } catch (SecurityException se) {
            Assert.fail("Security Exception should not have been thrown");
        }
    }
}
