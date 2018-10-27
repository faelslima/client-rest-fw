/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restclient.framework.utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;

/**
 * @author Rafael Lima
 */
public class Utils {

    private Utils() {
    }

    public static Class getFieldType(Class clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return field.getType();
            }
        }
        return null;
    }

    public static boolean entityIsPersisted(Object entity, Class clazz) {
        try {
            AccessibleObject accessibleObject = getIdAccessibleObject(clazz);
            if (accessibleObject instanceof Field) {
                Field field = (Field) accessibleObject;
                field.setAccessible(true);
                return field.get(entity) != null;
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static Class getIdType(Class clazz) {
        AccessibleObject accessibleObject = getIdAccessibleObject(clazz);
        if (accessibleObject instanceof Field) {
            Class type = ((Field) accessibleObject).getType();
            return type;
        }
        return null;
    }
    
    public static Object getId(Object object, Class clazz) {

        try {
            if (object == null) {
                return null;
            }

            AccessibleObject accessibleObject = getIdAccessibleObject(clazz);
            if (accessibleObject instanceof Field) {
                Field field = (Field) accessibleObject;
                field.setAccessible(true);
                return field.get(object);
            }

            if (accessibleObject instanceof Method) {
                Method method = (Method) accessibleObject;
                return method.invoke(object);
            }

        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static AccessibleObject getIdAccessibleObject(Class clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                return field;
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            return getIdAccessibleObject(clazz.getSuperclass());
        }
        return null;
    }
}
