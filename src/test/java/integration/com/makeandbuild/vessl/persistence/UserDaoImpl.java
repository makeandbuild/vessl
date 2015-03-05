package com.makeandbuild.vessl.persistence;

import com.makeandbuild.vessl.persistence.jdbc.BaseDaoImpl;
import com.makeandbuild.vessl.persistence.jdbc.ReflectionBasedJdbcMapper;

public class UserDaoImpl extends BaseDaoImpl<User, Long> implements UserDao {
    public UserDaoImpl() {
        super(ReflectionBasedJdbcMapper.proxy(User.class), User.class, Long.class);
    }

}
