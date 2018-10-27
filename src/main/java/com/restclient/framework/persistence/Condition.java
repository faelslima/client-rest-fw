package com.restclient.framework.persistence;

import java.io.Serializable;

/**
 * @author Rafael Lima
 */
public class Condition implements Serializable {

    private String field;
    private Object value;
    private String queryString;
    private ConditionType conditionType;

    public Condition(String field, Object value) {
        this.field = field;
        this.value = value;
        this.conditionType = ConditionType.EQUALS;
    }

    public Condition(String field, Object value, ConditionType conditionType) {
        this.field = field;
        this.value = value;
        this.conditionType = conditionType;
    }
    
    public Condition(String field, ConditionType conditionType) {
        this.field = field;
        this.value = null;
        this.conditionType = conditionType;
    }

    public Condition(String queryString) {
        this.queryString = queryString;
        this.conditionType = ConditionType.QUERY_STRING;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

}
