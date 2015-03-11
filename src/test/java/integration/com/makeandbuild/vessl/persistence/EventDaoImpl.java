package com.makeandbuild.vessl.persistence;

import com.makeandbuild.vessl.persistence.jdbc.BaseDaoImpl;
import com.makeandbuild.vessl.persistence.jdbc.ReflectionBasedJdbcMapper;
import com.makeandbuild.vessl.persistence.User;

public class EventDaoImpl extends BaseDaoImpl<Event,String> implements EventDao {
    public EventDaoImpl() {
        super(ReflectionBasedJdbcMapper.proxy(Event.class), Event.class, String.class);
		this.addQueryJoinSupport("user", "INNER JOIN user ON (user.user_id = event.user_id)", ReflectionBasedJdbcMapper.proxy(User.class));
    }

}
