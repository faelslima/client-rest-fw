/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restclient.framework.dao;

import com.restclient.framework.exception.DataServiceException;
import com.restclient.framework.persistence.Condition;
import com.restclient.framework.persistence.ConditionType;
import com.restclient.framework.persistence.Conditions;
import com.restclient.framework.persistence.LazyLoadingType;

import java.util.List;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

/**
 * @author Rafael Lima
 * @param <T>
 */
public interface BaseRestDAO<T> {

    public WebTarget getBaseWebTarget();

    public WebTarget getWebTarget();

    public Invocation.Builder getBuilder();

    public Invocation.Builder getBuilder(String... path);

    public Class getEntityClass();

    public String getEntityPath();

    public T find(Object id);

    public T find(Object id, boolean eager);

    public T unique(Condition condition);

    public T unique(Condition condition, LazyLoadingType lazyLoadingType);

    public T unique(Conditions conditions);

    public T unique(Conditions conditions, LazyLoadingType lazyLoadingType);

    public T unique(String field, Object value);

    public T unique(String field, Object value, LazyLoadingType lazyLoadingType);

    public List<T> listAll();

    public List<T> listAll(String orderBy);

    public List<T> list(Integer limit);

    public List<T> list(String field, Object value);

    public List<T> list(String field, Object value, Integer limit);

    public List<T> list(String field, Object value, ConditionType condition);

    public List<T> list(String field, Object value, ConditionType condition, Integer limit);

    public List<T> list(Condition condition, String orderBy);
    
    public List<T> list(Condition condition, Integer limit, String orderBy);

    public List<T> list(Conditions conditions, Integer limit, String orderBy);
    
    public List<T> list(Conditions conditions, String orderBy);

    public List<T> list(Integer limit, String orderBy);

    public List<T> list(Integer limit, Integer offset);

    public List<T> list(Integer limit, Integer offset, String orderBy);

    public List<T> list(Condition condition, Integer limit, Integer offset, String orderBy);

    public List<T> list(Conditions conditions, Integer limit, Integer offset, String orderBy);

    public T save(T entity) throws DataServiceException;

    public boolean insert(T entity) throws DataServiceException;

    public T update(T entity) throws DataServiceException;

    public boolean delete(Object id) throws DataServiceException;

}
