package com.github.flyinghe.tools.log;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by FlyingHe on 2019/10/28.
 */
public class LogUtils {
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static java.util.logging.Logger getLogger(String prefix, Class<?> clazz) {
        return org.apache.cxf.common.logging.LogUtils
                .getL7dLogger(clazz, null, String.format("%s,%s", prefix, clazz.getName()));
    }

    public static Logger getLogger(Class<?> clazz, String prefix) {
        return LoggerFactory.getLogger(String.format("%s,%s", prefix, clazz.getName()));
    }


    public static void debugRestHttp(Logger logger, HttpServletRequest request, Object payload,
                                     Object returnObj) {
        logRestHttp(Level.DEBUG, logger, request, payload, returnObj);
    }

    public static void infoRestHttp(Logger logger, HttpServletRequest request, Object payload,
                                    Object returnObj) {
        logRestHttp(Level.INFO, logger, request, payload, returnObj);
    }

    public static void warnRestHttp(Logger logger, HttpServletRequest request, Object payload,
                                    Object returnObj) {
        logRestHttp(Level.WARN, logger, request, payload, returnObj);
    }

    public static void errorRestHttp(Logger logger, HttpServletRequest request, Object payload,
                                     Object returnObj) {
        logRestHttp(Level.ERROR, logger, request, payload, returnObj);
    }

    /**
     * 用于记录HTTP REST 的接口日志
     *
     * @param level     日志级别
     * @param logger    日志记录器
     * @param request   请求对象
     * @param payload   payload数据(请求体的Json数据)
     * @param returnObj 接口返回的数据(一般也为Json数据)
     */
    public static void logRestHttp(Level level, Logger logger, HttpServletRequest request, Object payload,
                                   Object returnObj) {
        try {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            String contentType = request.getContentType();
            String queryParam = request.getQueryString();
            String payloadStr = payload == null ? "" : objectMapper.writeValueAsString(payload);
            String returnStr = returnObj == null ? "" : objectMapper.writeValueAsString(returnObj);
            StringBuilder msgSb = new StringBuilder();
            msgSb.append(String.format("uri:%s\r\n", uri))
                    .append(String.format("method:%s\r\n", method))
                    .append(String.format("contentType:%s\r\n", contentType))
                    .append(String.format("queryParam:%s\r\n", queryParam))
                    .append("Header:[\r\n");

            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (StringUtils.isNotBlank(request.getHeader(headerName))) {
                    msgSb.append(String.format("\t%s:%s\r\n", headerName, request.getHeader(headerName)));
                } else {
                    Enumeration<String> headers = request.getHeaders(headerName);
                    List<String> headersList = new ArrayList<>();
                    while (headers.hasMoreElements()) {
                        headersList.add(headers.nextElement());
                    }
                    msgSb.append(
                            String.format("\t%s:%s\r\n", headerName, StringUtils.join(headersList.toArray(), ",")));
                }
            }
            msgSb.append("]\r\n")
                    .append(String.format("payload:%s\r\n", payloadStr))
                    .append(String.format("response:%s\r\n", returnStr))
                    .append("========================================================================");
            if (Level.DEBUG.equals(level)) {
                logger.debug(msgSb.toString());
            } else if (Level.INFO.equals(level)) {
                logger.info(msgSb.toString());
            } else if (Level.WARN.equals(level)) {
                logger.warn(msgSb.toString());
            } else if (Level.ERROR.equals(level)) {
                logger.error(msgSb.toString());
            }
        } catch (Exception e) {
            //do nothing
        }
    }
}
