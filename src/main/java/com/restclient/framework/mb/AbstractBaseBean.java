/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restclient.framework.mb;

import com.restclient.framework.bo.AbstractBusinessObject;
import com.restclient.framework.dao.BaseRestDAO;
import com.restclient.framework.exception.DataServiceException;
import com.restclient.framework.persistence.Conditions;
import com.restclient.framework.utils.FacesUtils;
import com.restclient.framework.utils.Utils;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

/**
 * @author Rafael Lima
 * @param <T>
 */
public abstract class AbstractBaseBean<T> implements Serializable {

    public abstract AbstractBusinessObject<T> getBO();
    private List<T> dataModel;
    private List<T> listSelected;
    private T entity;
    private Class entityClass;
    private String listName;
    private String formName;
    private String currentPageReportTemplate = "{totalRecords} Registro(s) (Página {currentPage} de {totalPages})";
    private String paginatorTemplate = "{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink} {RowsPerPageDropdown} {CurrentPageReport}";

    public AbstractBaseBean() {
        if (getClass().getGenericSuperclass() != null && !getClass().getGenericSuperclass().equals(Object.class)) {
            if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
                if (parameterizedType != null && parameterizedType.getActualTypeArguments() != null && parameterizedType.getActualTypeArguments().length > 0) {
                    entityClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
                    listName = "list" + entityClass.getSimpleName();
                    formName = "form" + entityClass.getSimpleName();
                }
            }
        }
    }

    @PostConstruct
    public void postConstruct() {
        loadEntityFromParameter();
        if (entity == null) {
            entity = getEntityNewInstance();
        }
        init();
        createDataModel();
    }

    public void loadEntityFromParameter() {
        Object entityId = getIdFromParameter();
        if (entityId != null) {
            entity = getDAO().find(entityId, true);
        }
    }

    /**
     * @return A instance of id passed in parameter
     */
    private Object getIdFromParameter() {
        String parameter = FacesUtils.getParameter("guid");
        if (parameter == null || parameter.isEmpty()) {
            return null;
        }
        Class idType = Utils.getIdType(entityClass);
        if (idType == Long.class) {
            return Long.valueOf(parameter);
        }
        if (idType == Integer.class) {
            return Integer.valueOf(parameter);
        }
        return parameter;
    }

    public BaseRestDAO<T> getDAO() {
        return getBO().getDAO();
    }

    public void init() {
    }

    public abstract String getDataModelOrder();

    public Conditions getDataModelConditions() {
        return null;
    }

    public void createDataModel() {
        dataModel = getDAO().list(getDataModelConditions(), getDataModelOrder());
    }

    public T getEntityNewInstance() {
        try {
            return (T) getEntityClass().newInstance();
        } catch (Exception ex) {
            Logger.getLogger(AbstractBaseBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public boolean save() {
        try {
            getBO().save(entity);
            return true;
        } catch (DataServiceException ex) {
            Logger.getLogger(AbstractBaseBean.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void delete() throws DataServiceException {
        if (entity != null && Utils.entityIsPersisted(entity, getEntityClass())) {
            getDAO().delete(Utils.getId(entity, getEntityClass()));
        }
        if (dataModel != null && dataModel.contains(entity)) {
            dataModel.remove(entity);
        }
        setEntity(null);
    }

    public String edit() {
        Object id = Utils.getId(entity, entityClass);
        if (entity != null && id != null) {
            return getFormName() + "?guid=" + id.toString() + "&faces-redirect=true";
        }
        return null;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public List<T> getDataModel() {
        return dataModel;
    }

    public void setDataModel(List<T> dataModel) {
        this.dataModel = dataModel;
    }

    public String getListName() {
        return listName;
    }

    public String getFormName() {
        return formName;
    }

    public List<T> getListSelected() {
        return listSelected;
    }

    public void setListSelected(List<T> listSelected) {
        this.listSelected = listSelected;
    }

    public String getCurrentPageReportTemplate() {
        return currentPageReportTemplate;
    }

    public void setCurrentPageReportTemplate(String currentPageReportTemplate) {
        this.currentPageReportTemplate = currentPageReportTemplate;
    }

    public String getPaginatorTemplate() {
        return paginatorTemplate;
    }

    public void setPaginatorTemplate(String paginatorTemplate) {
        this.paginatorTemplate = paginatorTemplate;
    }

}
