package com.br.feedbackflow.model;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {

    private Integer statusCode;
    private String body;
    private Map<String,String> headers;

    public ApiResponse(){
        this.headers = new HashMap<>();
        this.headers.put("Content-Type", "application/json");
    }

    public ApiResponse(Integer statusCode, String body){
        this();
        this.statusCode = statusCode;
        this.body = body;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return String.format(
                "\n╔══════════════════════════════════════════╗\n" +
                        "║            API RESPONSE                  ║\n" +
                        "╠══════════════════════════════════════════╣\n" +
                        "║ Status Code : %d\n" +
                        "║ Body        : %s\n" +
                        "║ Headers     : %s\n" +
                        "╚══════════════════════════════════════════╝",
                statusCode, body, headers
        );
    }
}
