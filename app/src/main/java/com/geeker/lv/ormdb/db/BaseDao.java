package com.geeker.lv.ormdb.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.geeker.lv.ormdb.db.annotion.DbField;
import com.geeker.lv.ormdb.db.annotion.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lv on 17-1-13.
 */
public abstract class BaseDao<T> implements IBaseDao<T> {

    private SQLiteDatabase database;
    private volatile boolean isInit = false;
    private Class<T> entityClass;
    private HashMap<String,Field> cacheMap;
    private String tableName;

    protected synchronized boolean init(Class<T> entity, SQLiteDatabase sqLiteDatabase) {
        if (!isInit) {
            entityClass = entity;
            database = sqLiteDatabase;
            if (entity.getAnnotation(DbTable.class) == null) {
                tableName = entity.getClass().getSimpleName();
            }else {
                tableName = entity.getAnnotation(DbTable.class).value();
            }
            if (!database.isOpen()) {
                return false;
            }
            if (!TextUtils.isEmpty(createTable())) {
                database.execSQL(createTable());
            }
            cacheMap = new HashMap<>();
            initCacheMap();
            isInit = true;
        }
        return isInit;
    }

    private void initCacheMap() {
        String sql = "select * from " + tableName + " limit 1,0";
        Cursor cursor = null;
        cursor = database.rawQuery(sql, null);
        String[] columnNames = cursor.getColumnNames();
        Field[] columnFields = entityClass.getDeclaredFields();
        for (Field field : columnFields) {
            field.setAccessible(true);
        }
        for (String columnName : columnNames) {
            Field columnField = null;
            for (Field field : columnFields) {
                String fieldName=null;
                if (field.getAnnotation(DbField.class) != null) {
                    fieldName = field.getAnnotation(DbField.class).value();
                }else {
                    fieldName = field.getName();
                }
                if (columnName.equals(fieldName)) {
                    columnField = field;
                    break;
                }
            }
            if (columnField != null) {
                cacheMap.put(columnName, columnField);
            }
        }
        cursor.close();
    }

    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues = new ContentValues();
        Set keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = map.get(key);
            if (value != null) {
                contentValues.put(key,value);
            }
        }
        return contentValues;
    }

    private Map<String, String> getValues(T entity) {
        HashMap<String, String> result = new HashMap<>();
        Iterator<Field> fieldsIterator = cacheMap.values().iterator();
        while (fieldsIterator.hasNext()) {
            Field columnToField = fieldsIterator.next();
            String cacheKey = null;
            String cacheValue = null;
            if (columnToField.getAnnotation(DbField.class) != null) {
                cacheKey = columnToField.getAnnotation(DbField.class).value();
            }else {
                cacheKey = columnToField.getName();
            }
            try {
                if (null == columnToField.get(entity)) {
                    continue;
                }
                cacheValue = columnToField.get(entity).toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            result.put(cacheKey, cacheValue);
        }
        return result;
    }

    @Override
    public Long insert(T entity) {
        Map<String, String> map = getValues(entity);
        ContentValues values = getContentValues(map);
        Long result = database.insert(tableName, null, values);
        return result;
    }

    @Override
    public int update(T entity, T where) {
        int result = -1;
        Map value = getValues(entity);
        Map whereClause = getValues(where);
        Condition condition = new Condition(whereClause);
        ContentValues contentValues = getContentValues(value);
        result = database.update(tableName, contentValues, condition.getWhereClause(), condition.getWhereArgs());
        return result;
    }

    @Override
    public int delete(T where) {
        Map map = getValues(where);
        Condition condititon = new Condition(map);
        int result = database.delete(tableName, condititon.whereClause, condititon.whereArgs);
        return result;
    }

    @Override
    public List<T> query(T where) {
        return query(where,null,null,null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
        Map map = getValues(where);
        String limitString = null;
        if (startIndex != null && limit != null) {
            limitString = startIndex + " , " + limit;
        }
        Condition condition = new Condition(map);
        Cursor cursor = database.query(tableName, null, condition.getWhereClause(),
                condition.getWhereArgs(), null, null, orderBy, limitString);
        List<T> result = getResult(cursor, where);
        return result;
    }

    @Override
    public List<T> query(String sql) {
        return null;
    }

    private List<T> getResult(Cursor cursor, T where) {
        ArrayList<T> list = new ArrayList();
        T item = null;
        while (cursor.moveToNext()) {
            try {
                item = (T) where.getClass().newInstance();
                Iterator iterator = cacheMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entity = (Map.Entry) iterator.next();
                    String columnName = (String) entity.getKey();
                    Integer columnIndex = cursor.getColumnIndex(columnName);
                    Field field = (Field) entity.getValue();
                    Class type = field.getType();
                    if (columnIndex != -1) {
                        if (type == String.class) {
                            field.set(item,cursor.getString(columnIndex));
                        } else if (type == Double.class) {
                            field.set(item, cursor.getDouble(columnIndex));
                        } else if (type == Integer.class) {
                            field.set(item,cursor.getInt(columnIndex));
                        } else if (type == Long.class) {
                            field.set(item,cursor.getLong(columnIndex));
                        }else if (type == byte[].class){
                            field.set(item,cursor.getBlob(columnIndex));
                        }else {
                            continue;
                        }
                    }
                }
                list.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private class Condition{
        private String whereClause;
        private String[] whereArgs;

        public Condition(Map<String,String> whereClause) {
            ArrayList<String> list = new ArrayList();
            StringBuilder sb = new StringBuilder();
            sb.append(" 1=1 ");
            Set<String> keys = whereClause.keySet();
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = whereClause.get(key);
                if (value != null) {
                    sb.append(" and " + key + "=?");
                    list.add(value);
                }
            }
            this.whereClause = sb.toString();
            this.whereArgs = list.toArray(new String[list.size()]);
        }

        public String getWhereClause() {
            return whereClause;
        }

        public String[] getWhereArgs() {
            return whereArgs;
        }
    }

    protected abstract String createTable();
}
