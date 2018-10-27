/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restclient.framework.persistence;

/**
 * @author Rafael Lima
 */
public enum LazyLoadingType {
    LAZY("lazy"),
    EAGER("eager");

    private String description;

    LazyLoadingType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
