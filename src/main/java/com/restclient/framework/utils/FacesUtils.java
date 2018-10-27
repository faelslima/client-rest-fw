package com.restclient.framework.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.ResponseWriter;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Collection of utility methods for the JSF API
 *
 * @author ayslan
 */
public class FacesUtils {

    public static List<FacesMessage> getMessageList() {
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        if (currentInstance != null) {
            return currentInstance.getMessageList();
        }
        return null;
    }

    public static ExternalContext getExternalContext() {
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        if (currentInstance != null) {
            return currentInstance.getExternalContext();
        }
        return null;
    }

    public static Cookie[] getCookies() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getCookies();
        }
        return null;
    }

    public static Map<String, String> getHeaders() {
        Map<String, String> listaConteudosHeaders = new HashMap();
        HttpServletRequest request = getRequest();
        if (request != null) {
            Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
                String str = headers.nextElement();
                listaConteudosHeaders.put(str, request.getHeader(str));
            }
            listaConteudosHeaders.put("authType", request.getAuthType());
            listaConteudosHeaders.put("characterEncoding", request.getCharacterEncoding());
            listaConteudosHeaders.put("contentType", request.getContentType());
            listaConteudosHeaders.put("contextPath", request.getContextPath());
            listaConteudosHeaders.put("localAddr", request.getLocalAddr());
            listaConteudosHeaders.put("localName", request.getLocalName());
            listaConteudosHeaders.put("method", request.getMethod());
            listaConteudosHeaders.put("pathInfo", request.getPathInfo());
            listaConteudosHeaders.put("pathTranslated", request.getPathTranslated());
            listaConteudosHeaders.put("protocol", request.getProtocol());
            listaConteudosHeaders.put("queryString", request.getQueryString());
            listaConteudosHeaders.put("remoteAddr", request.getRemoteAddr());
            listaConteudosHeaders.put("remoteHost", request.getRemoteHost());
            listaConteudosHeaders.put("remoteUser", request.getRemoteUser());
            listaConteudosHeaders.put("requestUri", request.getRequestURI());
            listaConteudosHeaders.put("scheme", request.getScheme());
            listaConteudosHeaders.put("serverName", request.getServerName());
            listaConteudosHeaders.put("servletPath", request.getServletPath());
        }
        return listaConteudosHeaders;
    }

    /**
     * Verify if FacesContext has message using getMessageList(): {@code FacesContext.getCurrentInstance().isEmpty()
     * }
     *
     * @return "true" if messages is not empty
     */
    public static boolean hasMessage() {
        List<FacesMessage> messages = getMessageList();
        return messages != null && !messages.isEmpty();
    }

    /**
     * Verify if FacesContext has message for a specific Severity using
     * getMessageList(): {@code FacesContext.getCurrentInstance().isEmpty() }
     *
     * @param severity
     * @return "true" if messages is not empty
     */
    public static boolean hasMessage(FacesMessage.Severity severity) {
        List<FacesMessage> messages = getMessageList();
        if (messages != null) {
            for (FacesMessage message : messages) {
                if (message.getSeverity() != null && message.getSeverity().equals(severity)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return generated HTML from component.
     *
     * @param component
     * @return HTML from component
     */
    public static String getHtml(UIComponent component) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        }
        ResponseWriter originalWriter = context.getResponseWriter();
        StringWriter writer = new StringWriter();
        try {
            context.setResponseWriter(context.getRenderKit().createResponseWriter(writer, "text/html", "UTF-8"));
            try {
                component.encodeAll(context);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (originalWriter != null) {
                context.setResponseWriter(originalWriter);
            }
        }
        return writer.toString();
    }

    /**
     * Get a object from Http Session
     *
     * @param attributeName Object name in session
     * @return
     */
    public static Object getFromSession(String attributeName) {
        ExternalContext externalContext = getExternalContext();
        if (externalContext != null) {
            HttpSession session = (HttpSession) externalContext.getSession(true);
            return session.getAttribute(attributeName);
        }
        return null;
    }

    /**
     * Get the value of the parameter in request callin
     * HttpServletRequest.getParameter.
     *
     * @param parameterName
     * @return
     */
    public static String getParameter(String parameterName) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getParameter(parameterName);
        }
        return null;
    }

    /**
     * Add the object in Http session map
     *
     * @param attributeName
     * @param value
     */
    public static void addToSession(String attributeName, Object value) {
        ExternalContext externalContext = getExternalContext();
        if (externalContext != null) {
            HttpSession session = (HttpSession) externalContext.getSession(false);
            session.setAttribute(attributeName, value);
        }
    }

    /**
     * Remove a object from session
     *
     * @param attributeName
     */
    public static void removeFromSession(String attributeName) {
        ExternalContext externalContext = getExternalContext();
        if (externalContext != null) {
            HttpSession session = (HttpSession) externalContext.getSession(false);
            session.removeAttribute(attributeName);
        }
    }

    /**
     * Invalidate the current Http session using HttpSession.invalidate()
     */
    public static void invalidateSession() {
        ExternalContext externalContext = getExternalContext();
        if (externalContext != null) {
            ((HttpSession) externalContext.getSession(true)).invalidate();
        }
    }

    /**
     * Get the current Http request from JSF "ExternalContext"
     *
     * @return HttpServletRequest get from JSF
     */
    public static HttpServletRequest getRequest() {
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        if (currentInstance != null) {
            ExternalContext externalContext = currentInstance.getExternalContext();
            if (externalContext != null) {
                return (HttpServletRequest) externalContext.getRequest();
            }
        }
        return null;
    }

    /**
     * Get the current Http response from JSF "ExternalContext"
     *
     * @return HttpServletResponse get from JSF
     */
    public static HttpServletResponse getResponse() {
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        if (currentInstance != null) {
            ExternalContext externalContext = currentInstance.getExternalContext();
            if (externalContext != null) {
                HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
                return response;
            }
        }
        return null;
    }

    /**
     * Get the ServletContext from JSF "ExternalContext
     *
     * @return
     */
    public static ServletContext getServletContext() {
        ExternalContext externalContext = getExternalContext();
        if (externalContext != null) {
            return (ServletContext) externalContext.getContext();
        }
        return null;
    }

    public static void redirect(String url) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            externalContext.redirect(externalContext.getRequestContextPath() + url);
            context.responseComplete();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get a bean form EL context. Same as "evaluateExpressionGet"
     *
     * @param <T>
     * @param beanName
     * @return
     */
    public static <T> T getBeanByEl(String beanName) {
        FacesContext context = FacesContext.getCurrentInstance();
        return (T) context.getApplication().evaluateExpressionGet(context, beanName, Object.class);
    }

    /**
     * Get a bean form EL context
     *
     * @param <T>
     * @param expression
     * @return
     */
    public static <T> T evaluateExpressionGet(String expression) {
        FacesContext context = FacesContext.getCurrentInstance();
        return (T) context.getApplication().evaluateExpressionGet(context, expression, Object.class);
    }

    public static void setValueEl(String expression, Object newValue) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);
        Class bindClass = valueExp.getType(elContext);
        if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
            valueExp.setValue(elContext, newValue);
        }
    }

    public static void setNewValueBean(String beanName, String attribute, Object newValue) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getApplication().getELResolver().setValue(context.getELContext(), beanName, attribute, newValue);
    }

    /**
     * Método responsável por limpar os valores de um determinado componente Ex:
     * ao passar um form o método vai limpar seus componentes "filhos"
     *
     * @param pComponent
     */
    public static void clearComponent(UIComponent pComponent) {
        if (pComponent instanceof EditableValueHolder) {
            EditableValueHolder editableValueHolder = (EditableValueHolder) pComponent;
            editableValueHolder.setSubmittedValue(null);
            editableValueHolder.setValue(null);
            editableValueHolder.setValid(true);
        }
        for (UIComponent child : pComponent.getChildren()) {
            clearComponent(child);
        }
    }

    public static String getRealPath(String caminho) {
        ServletContext scontext = getServletContext();
        if (scontext != null) {
            return scontext.getRealPath(caminho);
        }
        return null;
    }

    /**
     * Add Cookie no HttpServletResponse
     *
     * @param name
     * @param value
     */
    public static void addCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(2592000);
        // NOTE: If cookie version is set to 1, cookie values will be quoted.
        cookie.setVersion(0);
        HttpServletResponse response = getResponse();
        if (response != null) {
            response.addCookie(cookie);
        }
    }

    public static String getBrowser() {
        HttpServletRequest httpServletRequest = getRequest();
        if (httpServletRequest != null) {
            return httpServletRequest.getHeader("User-Agent");
        }
        return null;
    }

    public static Cookie getCookie(String cookieName) {
        Cookie[] cookies = getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static String getCookieValue(String cookieName) {
        Cookie cookie = getCookie(cookieName);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    /**
     * Removes Cookie
     *
     * @param nomeCookie
     */
    public static void removeCookie(String nomeCookie) {
        Cookie cookie = getCookie(nomeCookie);
        if (cookie != null) {
            cookie.setMaxAge(0);
            getResponse().addCookie(cookie);
        }
    }

    /**
     *
     * Method to find a component in ViewRoot
     *
     * @param id
     * @return
     */
    public static UIComponent findComponentInRoot(String id) {
        UIComponent component = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            UIComponent root = facesContext.getViewRoot();
            component = findComponent(root, id);
        }
        return component;
    }

    public static UIComponent findComponent(UIComponent base, String id) {
        if (id.equals(base.getId())) {
            return base;
        }

        UIComponent kid = null;
        UIComponent result = null;
        Iterator kids = base.getFacetsAndChildren();
        while (kids.hasNext() && (result == null)) {
            kid = (UIComponent) kids.next();
            if (id.equals(kid.getId())) {
                result = kid;
                break;
            }
            result = findComponent(kid, id);
            if (result != null) {
                break;
            }
        }
        return result;
    }
    public static final String FILE_DOWNLOAD_TOKEN = "xpert.download";

    /**
     * Generates a file to download.
     *
     * This method put a cookie named 'fileDownloadToken', this cookie can be
     * used to control the downloaded file by the "javax.faces.ViewState" (wich
     * is a param submited in JSF forms)
     *
     * @param bytes
     * @param contentType
     * @param fileName
     */
    public static void download(byte[] bytes, String contentType, String fileName) {
        download(bytes, contentType, fileName, true);
    }

    /**
     *
     * Generates a file to download.
     *
     * This method put a cookie named 'fileDownloadToken', this cookie can be
     * used to control the downloaded file by the "javax.faces.ViewState" (wich
     * is a param submited in JSF forms)
     *
     * @param bytes
     * @param contentType
     * @param fileName
     * @param attachment indicates attachment in header
     */
    public static void download(byte[] bytes, String contentType, String fileName, boolean attachment) {

        FacesContext.getCurrentInstance().responseComplete();
        HttpServletResponse response = getResponse();
        HttpServletRequest request = getRequest();
        if (response != null && request != null) {
            try {
                addCookie(FILE_DOWNLOAD_TOKEN, request.getParameter("javax.faces.ViewState"));
                response.setContentType(contentType);
                if (attachment) {
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                }
                response.setContentLength(bytes.length);
                response.getOutputStream().write(bytes);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Generates a file to download.
     *
     * This method put a cookie named 'fileDownloadToken', this cookie can be
     * used to control the downloaded file by the "javax.faces.ViewState" (wich
     * is a param submited in JSF forms)
     *
     * @param inputStream
     * @param contentType
     * @param fileName
     */
    public static void download(InputStream inputStream, String contentType, String fileName) {
        download(inputStream, contentType, fileName, true);
    }

    /**
     * Generates a file to download.
     *
     * This method put a cookie named 'fileDownloadToken', this cookie can be
     * used to control the downloaded file by the "javax.faces.ViewState" (wich
     * is a param submited in JSF forms)
     *
     * @param inputStream
     * @param contentType
     * @param fileName
     * @param attachment
     */
    public static void download(InputStream inputStream, String contentType, String fileName, boolean attachment) {
        download(inputStreamToByte(inputStream), contentType, fileName, attachment);
    }

    private static byte[] inputStreamToByte(InputStream inputStream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return buffer.toByteArray();
    }

    public static String getIP() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
            return ipAddress;
        }
        return null;
    }

    /**
     *
     * Create the FacesContext. This method is useful to Contexts where
     * FacesContext is not created, like a Filter.
     *
     * @param request
     * @param response
     * @return
     */
    public static FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {

            FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

            facesContext = contextFactory.getFacesContext(request.getSession().getServletContext(), request, response, lifecycle);
            // Set using our inner class
            InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

            // set a new viewRoot, otherwise context.getViewRoot returns null
            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "");
            facesContext.setViewRoot(view);
        }
        return facesContext;
    }

    private abstract static class InnerFacesContext extends FacesContext {

        protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }
}
