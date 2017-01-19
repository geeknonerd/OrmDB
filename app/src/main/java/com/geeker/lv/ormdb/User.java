package com.geeker.lv.ormdb;

import com.geeker.lv.ormdb.db.annotion.DbField;
import com.geeker.lv.ormdb.db.annotion.DbTable;

/**
 * Created by lv on 17-1-13.
 */
@DbTable("tb_user")
public class User {
    @DbField("name")
    private String name;
    @DbField("password")
    private String password;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "name:"+name+",password:"+password;
    }
}
