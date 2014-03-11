package com.makeandbuild.persistence;

import java.util.List;

import com.makeandbuild.persistence.jdbc.BaseDao;

public interface AdminUserDao extends BaseDao<AdminUser, Long>{

    List<AdminUser> findAdminUserByUserName(String userName);
}
