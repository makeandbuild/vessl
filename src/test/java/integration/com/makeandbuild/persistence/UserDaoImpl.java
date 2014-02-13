package com.makeandbuild.persistence;

import com.makeandbuild.persistence.jdbc.BaseDaoImpl;
import com.makeandbuild.persistence.jdbc.ReflectionBasedJdbcMapper;

public class UserDaoImpl extends BaseDaoImpl<User, Long> implements UserDao {
    public UserDaoImpl() {
        super(ReflectionBasedJdbcMapper.proxy(User.class), User.class, Long.class);
    }

}
