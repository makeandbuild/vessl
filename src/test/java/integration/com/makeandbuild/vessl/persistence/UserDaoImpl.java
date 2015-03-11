package com.makeandbuild.vessl.persistence;

import org.springframework.beans.factory.annotation.Autowired;

import com.makeandbuild.vessl.persistence.jdbc.BaseDaoImpl;
import com.makeandbuild.vessl.persistence.jdbc.CascadeDelete;
import com.makeandbuild.vessl.persistence.jdbc.ReflectionBasedJdbcMapper;

public class UserDaoImpl extends BaseDaoImpl<User, Long> implements UserDao {
	@Autowired
	@CascadeDelete (joinAttributeName = "userId")
	EventDao eventDao;

	public UserDaoImpl() {
        super(ReflectionBasedJdbcMapper.proxy(User.class), User.class, Long.class);
    }

}
