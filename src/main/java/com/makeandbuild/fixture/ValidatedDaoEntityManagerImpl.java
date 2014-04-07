package com.makeandbuild.fixture;

import org.springframework.beans.factory.annotation.Autowired;

import com.makeandbuild.persistence.Dao;
import com.makeandbuild.validation.ValidationProxyManager;

@SuppressWarnings("rawtypes")
public class ValidatedDaoEntityManagerImpl extends DaoEntityManagerImpl{
    @Autowired
    ValidationProxyManager validationProxyManager;
    
    private Dao validationDao;
    private String[] validationTypes;

    public Dao getDao() {
        if (validationDao == null)
            validationDao = (Dao)validationProxyManager.newBeanValidatorProxy(dao, validationTypes);
        return validationDao;
    }
    
    public ValidatedDaoEntityManagerImpl(Dao dao, String subtype, String... validationTypes) {
        super(dao, subtype);
        this.validationTypes = validationTypes;
    }

    public ValidatedDaoEntityManagerImpl(Dao dao) {
        super(dao);
    }
}