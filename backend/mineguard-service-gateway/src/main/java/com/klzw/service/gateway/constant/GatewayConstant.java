package com.klzw.service.gateway.constant;

/**
 * 网关常量
 */
public class GatewayConstant {

    private GatewayConstant() {
    }

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_ROLES = "X-User-Roles";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final int FILTER_ORDER_TRACE = -100;
    public static final int FILTER_ORDER_LOG = -50;
    public static final int FILTER_ORDER_AUTH = 0;
    public static final int FILTER_ORDER_RATE_LIMIT = 100;
}
