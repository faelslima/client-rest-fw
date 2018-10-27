/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.restclient.framework.exception;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Rafael Lima
 */
public class DataServiceException extends Exception {

    public DataServiceException() {
        super();
    }

    public DataServiceException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (message.contains("html")) {
            Document document = Jsoup.parse(message);
            Element body = document.body();
            Elements elements = body.getElementsByTag("pre");
            String erroDescription = "";
            erroDescription = elements.stream().map((element) -> element.html() + "\n").reduce(erroDescription, String::concat);
            return erroDescription;
        }
        return message;
    }
}
