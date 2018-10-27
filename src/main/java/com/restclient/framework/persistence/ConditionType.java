/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restclient.framework.persistence;

/**
 * @author Rafael Lima
 */
public enum ConditionType {
    EQUALS("equals"),
    NOT_EQUALS("not_equals"),
    STARTS_WITH("starts_with"),
    ENDS_WITH("ends_with"),
    GREATER_THAN("greater_than"),
    GREATER_EQUALS_THAN("greater_equals_than"),
    LESS_THAN("less_than"),
    LESS_EQUALS_THAN("less_equals_than"),
    LIKE("like"),
    NOT_LIKE("not_like"),
    NULL("is_null"),
    NOT_NULL("is_not_null"),
    QUERY_STRING("query_string");

    private String description;

    ConditionType(String description) {
        if (description != null) {
            this.description = description.trim().toLowerCase();
        }
    }

    public String getDescription() {
        return " " + description + " ";
    }
}
