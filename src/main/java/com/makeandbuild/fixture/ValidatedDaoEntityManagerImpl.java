package com.makeandbuild.fixture;

import org.springframework.beans.factory.annotation.Autowired;

import com.makeandbuild.persistence.Dao;
import com.makeandbuild.validation.ValidationProxyManager;

public class ValidatedDaoEntityManagerImpl extends DaoEntityManagerImpl{
    
    @Autowired
    ValidationProxyManager validationProxyManager;
    
    private Dao validationDao;

    public Dao getDao() {
        if (validationDao == null)
            validationDao = (Dao)validationProxyManager.newBeanValidatorProxy(dao, "data");
        return validationDao;
    }

    
    
    public ValidatedDaoEntityManagerImpl(Dao dao, String subtype) {
        super(dao, subtype);
        // TODO Auto-generated constructor stub
    }

    public ValidatedDaoEntityManagerImpl(Dao dao) {
        super(dao);
        // TODO Auto-generated constructor stub
    }

}
