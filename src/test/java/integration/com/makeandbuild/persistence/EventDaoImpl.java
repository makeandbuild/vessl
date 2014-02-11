package com.makeandbuild.persistence;

import com.makeandbuild.persistence.jdbc.BaseDaoImpl;
import com.makeandbuild.persistence.jdbc.ReflectionBasedJdbcMapper;

public class EventDaoImpl extends BaseDaoImpl<Event,String> implements EventDao {
    public EventDaoImpl() {
        super(ReflectionBasedJdbcMapper.proxy(Event.class));
    }

}
