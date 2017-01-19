package com.geeker.lv.ormdb;

import com.geeker.lv.ormdb.db.BaseDao;

/**
 * Created by lv on 17-1-13.
 */
public class UserDao extends BaseDao {
    @Override
    protected String createTable() {
        return "create table if not exists tb_user(name varchar(20),password varchar(20))";
    }
}
