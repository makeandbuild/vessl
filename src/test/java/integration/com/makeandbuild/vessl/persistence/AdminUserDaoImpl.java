package com.makeandbuild.vessl.persistence;

import java.util.List;

import com.makeandbuild.vessl.persistence.jdbc.BaseDaoImpl;
import com.makeandbuild.vessl.persistence.jdbc.ReflectionBasedJdbcMapper;

public class AdminUserDaoImpl extends BaseDaoImpl<AdminUser, Long> implements AdminUserDao{

    public AdminUserDaoImpl(){
        super(ReflectionBasedJdbcMapper.proxy(AdminUser.class), AdminUser.class, Long.class);
    }
    
    @Override
    public List<AdminUser> findAdminUserByUserName(String userName){
        String sql = "SELECT * FROM user WHERE username like ?";
        return getJdbcTemplate().query(sql, new Object[]{userName}, getDomainMapper());
    }
}
