/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restclient.framework.dao.impl;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.restclient.framework.exception.DataServiceException;
import com.restclient.framework.persistence.Condition;
import com.restclient.framework.persistence.ConditionType;
import com.restclient.framework.persistence.Conditions;
import com.restclient.framework.persistence.LazyLoadingType;
import com.restclient.framework.utils.Utils;
import com.restclient.framework.dao.BaseRestDAO;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author Rafael Lima
 * @param <T>
 */
public abstract class BaseRestDAOImpl<T> implements BaseRestDAO<T> {

    private WebTarget baseWebTarget;
    private WebTarget webTarget;
    private static final MediaType JSON_TYPE = MediaType.APPLICATION_JSON_TYPE;

    public abstract String getBaseUrl();

    public abstract String getToken();

    @PostConstruct
    protected void init() {
        this.baseWebTarget = ClientBuilder.newClient()
                .target(getBaseUrl())
                .register(JacksonJsonProvider.class);

        this.webTarget = this.baseWebTarget.path(getEntityPath());
    }

    @Override
    public WebTarget getBaseWebTarget() {
        return this.baseWebTarget;
    }

    @Override
    public WebTarget getWebTarget() {
        return this.webTarget;
    }

    public WebTarget getWebTarget(Condition condition) {
        String $filter = buildFilter(condition);
        String $queryString = buildQueryString(condition);
        WebTarget target = getWebTarget();
        if ($filter != null) {
            target = target.queryParam("$filter", $filter);
        }
        if ($queryString != null) {
            target = target.queryParam("$queryString", $queryString);
        }
        return target;
    }

    public WebTarget getWebTarget(Conditions conditions) {
        String $filter = buildFilter(conditions);
        String $queryString = buildQueryString(conditions);
        WebTarget target = getWebTarget();
        if ($filter != null) {
            target = target.queryParam("$filter", $filter);
        }
        if ($queryString != null) {
            target = target.queryParam("$queryString", $queryString);
        }
        return target;
    }

    @Override
    public T find(Object id) {
        return find(id, false);
    }

    @Override
    public T find(Object id, boolean eager) {
        if (id != null) {
            if (!eager) {
                Response response = getBuilder(String.valueOf(id)).get();
                if (response.getStatus() == 200 && response.hasEntity()) {
                    return (T) response.readEntity(getGenericType());
                }
            } else {
                Invocation.Builder builder = getWebTarget()
                        .path(id.toString())
                        .queryParam("$lazyloadingtype", LazyLoadingType.EAGER)
                        .request(JSON_TYPE)
                        .header("Authorization", getToken());

                Response response = builder.get();
                if (response.getStatus() == 200 && response.hasEntity()) {
                    return (T) response.readEntity(getGenericType());
                }
            }
        }
        return null;
    }

    @Override
    public T save(T entity) throws DataServiceException {
        if (entity != null) {
            boolean persisted = Utils.entityIsPersisted(entity, getEntityClass());
            if (persisted) {
                return update(entity);
            } else {
                Object id = insertReturningId(entity);
                if (id != null) {
                    return find(id);
                }
            }
        }
        return null;
    }

    @Override
    public boolean insert(T entity) throws DataServiceException {
        if (entity != null) {
            Response response = getBuilder().post(getEntity(entity));
            int status = response.getStatus();
            if (status == 200 || status == 201) {
                return true;
            }
            if (status == 500) {
                String message = response.readEntity(String.class);
                throw new DataServiceException(message);
            }
        }
        return false;
    }

    public Object insertReturningId(T entity) throws DataServiceException {
        if (entity != null) {
            Response response = getBuilder().post(getEntity(entity));
            int status = response.getStatus();
            if (status == 200 && response.hasEntity()) {
                T obj = (T) response.readEntity(getGenericType());
                return Utils.getId(obj, getEntityClass());
            }
            if (status == 500) {
                String message = response.readEntity(String.class);
                throw new DataServiceException(message);
            }
        }
        return null;
    }

    @Override
    public T update(T entity) throws DataServiceException {
        if (entity != null) {
            Response response = getBuilder().put(getEntity(entity));
            int status = response.getStatus();
            if (status == 200 && response.hasEntity()) {
                return (T) response.readEntity(getGenericType());
            }
            if (status == 500) {
                String message = response.readEntity(String.class);
                throw new DataServiceException(message);
            }
        }
        return null;
    }

    @Override
    public boolean delete(Object id) throws DataServiceException {
        if (id != null) {
            Invocation.Builder builder = getWebTarget()
                    .path(String.valueOf(id))
                    .request()
                    .accept("text/plain")
                    .header("Authorization", getToken());

            Response response = builder.delete();
            int status = response.getStatus();
            if (status == 202) {
                return true;
            }
            if (status == 500) {
                String message = response.readEntity(String.class);
                throw new DataServiceException(message);
            }
        }
        return false;
    }

    @Override
    public T unique(String field, Object value) {
        return unique(field, value, LazyLoadingType.LAZY);
    }

    @Override
    public T unique(String field, Object value, LazyLoadingType type) {
        if (field != null && value != null) {
            Invocation.Builder builder = getWebTarget()
                    .queryParam("$filter", field + ConditionType.EQUALS.getDescription() + value)
                    .queryParam("$limit", 1)
                    .queryParam("$lazyloadingtype", type)
                    .request(JSON_TYPE)
                    .header("Authorization", getToken());

            Response response = builder.get();
            if (response.getStatus() == 200 && response.hasEntity()) {
                return (T) response.readEntity(getGenericType());
            }
        }
        return null;
    }

    @Override
    public T unique(Condition condition) {
        return unique(condition, LazyLoadingType.LAZY);
    }

    @Override
    public T unique(Condition condition, LazyLoadingType type) {
        if (condition != null) {
            Invocation.Builder builder = getWebTarget(condition)
                    .queryParam("$limit", 1)
                    .queryParam("$lazyloadingtype", type)
                    .request(JSON_TYPE)
                    .header("Authorization", getToken());

            Response response = builder.get();
            if (response.getStatus() == 200 && response.hasEntity()) {
                return (T) response.readEntity(getGenericType());
            }
        }
        return null;
    }

    @Override
    public T unique(Conditions conditions) {
        return unique(conditions, LazyLoadingType.LAZY);
    }

    @Override
    public T unique(Conditions conditions, LazyLoadingType type) {
        if (conditions != null) {
            Invocation.Builder builder = getWebTarget(conditions)
                    .queryParam("$limit", 1)
                    .queryParam("$lazyloadingtype", type)
                    .request(JSON_TYPE)
                    .header("Authorization", getToken());

            Response response = builder.get();
            if (response.getStatus() == 200 && response.hasEntity()) {
                return (T) response.readEntity(getGenericType());
            }
        }
        return null;
    }

    @Override
    public List<T> listAll() {
        Response response = getBuilder().get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> listAll(String orderBy) {
        Invocation.Builder builder = getWebTarget()
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(String field, Object value) {
        return list(field, value, ConditionType.EQUALS);
    }

    @Override
    public List<T> list(String field, Object value, Integer limit) {
        return list(field, value, ConditionType.EQUALS, limit);
    }

    @Override
    public List<T> list(String field, Object value, ConditionType condition) {
        return list(field, value, condition, null);
    }

    @Override
    public List<T> list(String field, Object value, ConditionType condition, Integer limit) {

        String condicao = ConditionType.EQUALS.getDescription();
        if (condition != null) {
            if (condition.equals(ConditionType.LIKE)) {
                condicao = " like ";
            }
        }

        WebTarget wt = getWebTarget().queryParam("$filter", field + condicao + value.toString());
        if (limit != null) {
            wt.queryParam("$limit", limit);
        }
        Invocation.Builder builder = wt.request(JSON_TYPE).header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Integer limit) {
        Invocation.Builder builder = getWebTarget()
                .queryParam("$limit", limit)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Integer limit, String orderBy) {
        Invocation.Builder builder = getWebTarget()
                .queryParam("$limit", limit)
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Integer limit, Integer offset) {
        Invocation.Builder builder = getWebTarget()
                .queryParam("$limit", limit)
                .queryParam("$offset", offset)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Integer limit, Integer offset, String orderBy) {
        Invocation.Builder builder = getWebTarget()
                .queryParam("$limit", limit)
                .queryParam("$offset", offset)
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Condition condition, String orderBy) {
        Invocation.Builder builder = getWebTarget(condition)
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Condition condition, Integer limit, String orderBy) {
        Invocation.Builder builder = getWebTarget(condition)
                .queryParam("$limit", limit)
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Conditions conditions, String orderBy) {
        Invocation.Builder builder = getWebTarget(conditions)
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Conditions conditions, Integer limit, String orderBy) {
        Invocation.Builder builder = getWebTarget(conditions)
                .queryParam("$limit", limit)
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Condition condition, Integer limit, Integer offset, String orderBy) {
        Invocation.Builder builder = getWebTarget(condition)
                .queryParam("$limit", limit)
                .queryParam("$offset", offset)
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    @Override
    public List<T> list(Conditions conditions, Integer limit, Integer offset, String orderBy) {
        Invocation.Builder builder = getWebTarget(conditions)
                .queryParam("$limit", limit)
                .queryParam("$offset", offset)
                .queryParam("$orderBy", orderBy)
                .request(JSON_TYPE)
                .header("Authorization", getToken());

        Response response = builder.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            return (List<T>) response.readEntity(getGenericTypeList());
        }
        return null;
    }

    private String buildFilter(Condition condition) {
        String $filter = null;
        if (condition != null) {
            ConditionType conditionType = condition.getConditionType();
            if (!conditionType.equals(ConditionType.QUERY_STRING)) {
                $filter = condition.getField() + conditionType.getDescription();
                if (condition.getValue() != null) {
                    Class entityClass = condition.getValue().getClass();
                    if (!entityClass.equals(String.class) && !entityClass.equals(Number.class)) {
                        $filter += Utils.getId(condition.getValue(), entityClass);
                        return $filter;
                    }
                    $filter += condition.getValue().toString();
                }
            }
        }
        return $filter;
    }

    private String buildFilter(Conditions conditions) {
        String $filter = null;
        if (conditions != null && conditions.size() > 0) {
            for (Condition condition : conditions) {
                if ($filter == null) {
                    $filter = buildFilter(condition);
                } else {
                    $filter += " and " + buildFilter(condition);
                }
            }
        }
        return $filter;
    }

    private String buildQueryString(Condition condition) {
        String $queryString = null;
        if (condition != null) {
            ConditionType conditionType = condition.getConditionType();
            if (conditionType.equals(ConditionType.QUERY_STRING)) {
                $queryString = condition.getQueryString();
            }
        }
        return $queryString;
    }

    private String buildQueryString(Conditions conditions) {
        String $queryString = null;
        if (conditions != null) {
            for (Condition condition : conditions) {
                if (condition.getConditionType().equals(ConditionType.QUERY_STRING)) {
                    if ($queryString == null) {
                        $queryString = "(" + buildQueryString(condition) + ")";
                    } else {
                        $queryString += " and (" + buildQueryString(condition) + ")";
                    }
                }
            }
        }
        return $queryString;
    }

    private Entity getEntity(T entity) {
        return Entity.entity(getJsonFromEntity(entity), JSON_TYPE);
    }

    @Override
    public Invocation.Builder getBuilder() {
        String[] str = null;
        return getBuilder(str);
    }

    @Override
    public Invocation.Builder getBuilder(String... paths) {
        if (paths == null) {
            return this.webTarget
                    .request(JSON_TYPE)
                    .accept("application/json", "text/plain")
                    .header("Authorization", getToken());
        }

        WebTarget target = getWebTarget();
        for (String path : paths) {
            target = target.path(path);
        }

        return target
                .request(JSON_TYPE)
                .accept("application/json", "text/plain")
                .header("Authorization", getToken());
    }

    public Type getEntityType() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return type;
    }

    public Type getEntityTypeList() {
        Type type = com.google.gson.reflect.TypeToken.getParameterized(List.class, getEntityType()).getType();
        return type;
    }

    public GenericType getGenericType() {
        return new GenericType(getEntityType());
    }

    public GenericType getGenericTypeList() {
        return new GenericType(getEntityTypeList());
    }

    //<editor-fold defaultstate="collapsed" desc="Gson">
    private Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        return builder.create();
    }

    public String getJsonFromEntity(T entity) {
        return getGson().toJson(entity, getEntityType());
    }

    public T getEntityFromJson(String json) {
        return (T) getGson().fromJson(json, getEntityType());
    }

    public List<T> getListFromJson(String json) {
        return getGson().fromJson(json, getEntityTypeList());
    }

    public String getJsonFromList(List<T> list) {
        return getGson().toJson(list, getEntityTypeList());
    }

    /**
     * Serializer byteArray.
     */
    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

        @Override
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(org.apache.commons.codec.binary.Base64.encodeBase64String(src));
        }

        @Override
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return org.apache.commons.codec.binary.Base64.decodeBase64(json.getAsString());
        }
    }

    /**
     * Serializer de Date.
     */
    private static class DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

        @Override
        public JsonElement serialize(Date t, Type type, JsonSerializationContext jsc) {
            return new JsonPrimitive(dateToJson(t));
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return jsonToDate(json.getAsJsonPrimitive().getAsString());
        }

        /**
         * Converte uma Date em um valor ISO 8601.
         *
         * @param data Date a ser convertido em String.
         * @return String com a data. Ex: "2013-01-17T00:00:00.124-02:00"
         */
        private String dateToJson(Date data) {
            if (data == null) {
                return null;
            }
            // O toString do Joda DateTime por padrão retorna um String de data em ISO 8601.
            DateTime dt = new DateTime(data, DateTimeZone.forID("-03:00"));
            return dt.toString().replace("-03:00", "Z");
        }

        /**
         * Coverte uma data ISO 8601 em DateTime.
         *
         * @param strDt
         * @return Date Ex: "2013-01-17T00:00:00.124-02:00"
         */
        private Date jsonToDate(String strDt) {
            if (strDt == null) {
                return null;
            }
            DateTime dt = new DateTime(strDt);
            return dt.toDate();
        }
    }
//</editor-fold>

}
