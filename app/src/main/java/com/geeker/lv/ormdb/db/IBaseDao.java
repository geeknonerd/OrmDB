package com.geeker.lv.ormdb.db;

import java.util.List;

/**
 * Created by lv on 17-1-13.
 */
public interface IBaseDao<T> {
    /**
     * insert data
     * @param entity
     * @return
     */
    Long insert(T entity);

    /**
     * update data
     * @param entity
     * @param where
     * @return
     */
    int update(T entity, T where);

    /**
     * delete data
     * @param where
     * @return
     */
    int delete(T where);

    /**
     * query
     * @param where
     * @return
     */
    List<T> query(T where);

    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);

    List<T> query(String sql);

}
