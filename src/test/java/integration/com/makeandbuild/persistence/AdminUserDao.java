package com.makeandbuild.persistence;

import java.util.List;

public interface AdminUserDao extends BaseDao<AdminUser, Long>{

    List<AdminUser> findAdminUserByUserName(String userName);
}
