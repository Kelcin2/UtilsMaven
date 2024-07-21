package com.github.flyinghe.tools.log;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

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
            String uri = Optional.ofNullable(request.getRequestURI()).orElse("");
            String method = Optional.ofNullable(request.getMethod()).orElse("");
            String contentType = Optional.ofNullable(request.getContentType()).orElse("");
            String queryParam = Optional.ofNullable(request.getQueryString()).orElse("");
            String payloadStr = payload == null ? "" : objectMapper.writeValueAsString(payload);
            String returnStr = returnObj == null ? "" : objectMapper.writeValueAsString(returnObj);
            StringBuilder msgSb = new StringBuilder();
            msgSb.append(String.format("Uri:%s\r\n", uri))
                    .append(String.format("Method:%s\r\n", method))
                    .append(String.format("ContentType:%s\r\n", contentType))
                    .append(String.format("QueryParam:%s\r\n", queryParam))
                    .append("RequestHeader:[\r\n");

            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (StringUtils.isNotBlank(request.getHeader(headerName))) {
                    msgSb.append(String.format("\t%s:%s\r\n", headerName,
                            Optional.ofNullable(request.getHeader(headerName)).orElse("")));
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
                    .append(String.format("Payload:%s\r\n", payloadStr))
                    .append(String.format("Response:%s\r\n", returnStr))
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
