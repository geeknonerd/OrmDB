package com.geeker.lv.ormdb.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * Created by lv on 17-1-13.
 */
public class BaseDaoFactory {
    private String sqliteDatabasePath;
    private SQLiteDatabase sqLiteDatabase;
    private static BaseDaoFactory instance = new BaseDaoFactory();

    public BaseDaoFactory() {
        sqliteDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.db";
        openDatabase();
    }

    private void openDatabase() {
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sqliteDatabasePath, null);
    }

    public static BaseDaoFactory getInstance() {
        return instance;
    }

    public synchronized <T extends BaseDao<M>, M> T getDataHelper(Class<T> clazz, Class<M> entityClass) {
        BaseDao baseDao = null;
        try {
            baseDao = clazz.newInstance();
            baseDao.init(entityClass, sqLiteDatabase);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) baseDao;
    }

}
