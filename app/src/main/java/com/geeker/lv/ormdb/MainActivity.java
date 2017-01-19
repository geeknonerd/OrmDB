package com.geeker.lv.ormdb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.geeker.lv.db.R;
import com.geeker.lv.ormdb.db.BaseDaoFactory;
import com.geeker.lv.ormdb.db.IBaseDao;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    IBaseDao<User> userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userDao = BaseDaoFactory.getInstance().getDataHelper(UserDao.class, User.class);
    }

    public void insert(View v) {
        User user = new User("Liming", "123456");
        Log.e(TAG, user.toString());
        userDao.insert(user);
    }

    public void update(View v) {
        User user = new User("XiaoXiao", "123");
        User where = new User();
        where.setPassword("123");
        userDao.update(user, where);
    }

    public void delete(View v) {
        User where = new User();
        where.setName("XiaoXiao");
        userDao.delete(where);
    }

    public void query(View v) {
        User where = new User();
        where.setName("Liming");
        List<User> result = userDao.query(where);
        for (User u : result) {
            Log.e(TAG, u.toString());
        }
    }
}
