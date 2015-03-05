package com.makeandbuild.vessl.persistence;

import java.util.List;

import com.makeandbuild.vessl.persistence.jdbc.BaseDao;

public interface AdminUserDao extends BaseDao<AdminUser, Long>{

    List<AdminUser> findAdminUserByUserName(String userName);
}
