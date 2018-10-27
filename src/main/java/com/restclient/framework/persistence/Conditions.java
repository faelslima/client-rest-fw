package com.restclient.framework.persistence;

import java.util.ArrayList;

/**
 * @author Rafael Lima
 */
public class Conditions extends ArrayList<Condition> {

    public void add(String field, Object value) {
        this.add(new Condition(field, value));
    }

    public void equals(String field, Object value) {
        this.add(field, value);
    }

    public void notEquals(String field, Object value) {
        this.add(new Condition(field, value, ConditionType.NOT_EQUALS));
    }

    public void like(String field, Object value) {
        this.add(new Condition(field, value, ConditionType.LIKE));
    }

    public void notLike(String field, Object value) {
        this.add(new Condition(field, value, ConditionType.NOT_LIKE));
    }
    
    public void isNull(String field) {
        this.add(new Condition(field, ConditionType.NULL));
    }
    
    public void isNotNull(String field) {
        this.add(new Condition(field, ConditionType.NOT_NULL));
    }

}
