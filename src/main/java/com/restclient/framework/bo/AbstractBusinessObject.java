/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restclient.framework.bo;

import com.restclient.framework.dao.BaseRestDAO;
import com.restclient.framework.exception.DataServiceException;

/**
 * @author Rafael Lima
 * @param <T>
 */
public abstract class AbstractBusinessObject<T> {

    public abstract BaseRestDAO<T> getDAO();

    public T save(T entity) throws DataServiceException {
        return getDAO().save(entity);
    }

    public T find(Object id) {
        return getDAO().find(id);
    }
}
