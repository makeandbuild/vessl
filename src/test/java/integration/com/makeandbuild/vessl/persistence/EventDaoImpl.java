package com.makeandbuild.vessl.persistence;

import com.makeandbuild.vessl.persistence.jdbc.BaseDaoImpl;
import com.makeandbuild.vessl.persistence.jdbc.ReflectionBasedJdbcMapper;

public class EventDaoImpl extends BaseDaoImpl<Event,String> implements EventDao {
    public EventDaoImpl() {
        super(ReflectionBasedJdbcMapper.proxy(Event.class), Event.class, String.class);
    }

}
