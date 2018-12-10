package com.github.flyinghe.tools;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * 此类提供一些针对javax.servlet.Filter的工具，例如针对Http请求的编码过滤工具等等
 *
 * @author Flying
 * @see javax.servlet.Filter
 */
public class FilterUtils {
    /**
     * 获取一个指定编码后的request，但只对POST和GET请求编码，其他请求则直接返回原始请求
     *
     * @param request 指定原始请求
     * @return 返回已经编码的request
     * @throws UnsupportedEncodingException 若不支持指定编码则抛出此异常
     */
    public static HttpServletRequest getEncodingRequest(
            HttpServletRequest request)
            throws UnsupportedEncodingException {
        String method = request.getMethod();
        if (method.equalsIgnoreCase("post")) {
            return new EncodingPostRequest(request);
        } else if (method.equalsIgnoreCase("get")) {
            return new EncodingGetRequest(request);
        }
        return request;
    }

    /**
     * 获取一个指定编码后的request，但只对POST和GET请求编码，其他请求则直接返回原始请求
     *
     * @param request 指定原始请求
     * @param charset 指定编码
     * @return 返回已经编码的request
     * @throws UnsupportedEncodingException 若不支持指定编码则抛出此异常
     */
    public static HttpServletRequest getEncodingRequest(
            HttpServletRequest request, String charset)
            throws UnsupportedEncodingException {
        String method = request.getMethod();
        if (method.equalsIgnoreCase("post")) {
            return new EncodingPostRequest(request, charset);
        } else if (method.equalsIgnoreCase("get")) {
            return new EncodingGetRequest(request, charset);
        }
        return request;
    }

    /**
     * 获取一个指定编码后的request，但只对POST和GET请求编码，其他请求则直接返回原始请求
     *
     * @param request       指定原始请求
     * @param charset       指定编码
     * @param originCharset 指定原始编码
     * @return 返回已经编码的request
     * @throws UnsupportedEncodingException 若不支持指定编码则抛出此异常
     */
    public static HttpServletRequest getEncodingRequest(
            HttpServletRequest request, String charset, String originCharset)
            throws UnsupportedEncodingException {
        String method = request.getMethod();
        if (method.equalsIgnoreCase("post")) {
            return new EncodingPostRequest(request, charset);
        } else if (method.equalsIgnoreCase("get")) {
            return new EncodingGetRequest(request, charset, originCharset);
        }
        return request;
    }

    /**
     * 获取编码后的response
     *
     * @param response 指定response
     * @param charset  指定编码集
     * @return 返回编码后的response
     */
    public static HttpServletResponse getEncodingResponse(
            HttpServletResponse response, String charset) {
        response.setCharacterEncoding(charset);
        response.setContentType("text/html;charset=" + charset);
        return response;
    }

    /**
     * 在response中设置禁用浏览器缓存
     *
     * @param response 指定response
     * @return 返回禁用浏览器缓存后的response
     */
    public static HttpServletResponse noCache(HttpServletResponse response) {
        response.setHeader("cache-control", "no-cache");
        response.setHeader("pragma", "no-cache");
        response.setHeader("expires", "0");

        return response;
    }
}

/**
 * 已经编码的Post请求
 *
 * @author Flying
 */
class EncodingPostRequest extends HttpServletRequestWrapper {
    private String charset = "UTF-8";

    /**
     * @param request 指定原始请求
     * @throws UnsupportedEncodingException 若指定编码不受支持则抛出此异常
     */
    public EncodingPostRequest(HttpServletRequest request)
            throws UnsupportedEncodingException {
        super(request);
        request.setCharacterEncoding(this.charset);
    }

    /**
     * @param request 指定原始请求
     * @param charset 指定编码
     * @throws UnsupportedEncodingException 若指定编码不受支持则抛出此异常
     */
    public EncodingPostRequest(HttpServletRequest request, String charset)
            throws UnsupportedEncodingException {
        super(request);
        this.charset = charset;
        request.setCharacterEncoding(this.charset);
    }
}

/**
 * 已经编码的GET请求
 *
 * @author Flying
 */
class EncodingGetRequest extends HttpServletRequestWrapper {
    private String charset = "UTF-8";//即将被设置成的编码,默认UTF-8
    private String originCharset = "ISO-8859-1";//默认原始编码是ISO-8859-1

    /**
     * @param request 指定原始请求
     */
    public EncodingGetRequest(HttpServletRequest request) {
        super(request);
    }

    /**
     * @param request 指定原始请求
     * @param charset 指定编码
     */
    public EncodingGetRequest(HttpServletRequest request, String charset) {
        super(request);
        this.charset = charset;
    }

    /**
     * @param request       指定原始请求
     * @param charset       指定编码
     * @param originCharset 指定原始编码
     */
    public EncodingGetRequest(HttpServletRequest request, String charset, String originCharset) {
        super(request);
        this.charset = charset;
        this.originCharset = originCharset;
    }

    // 对参数值进行编码
    @Override
    public String getParameter(String name) {
        if (this.charset.equalsIgnoreCase(this.originCharset)) {
            return super.getParameter(name);
        }
        String value = super.getParameter(name);
        if (value == null) {
            return null;
        }
        try {
            value = new String(value.getBytes(this.originCharset), this.charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return value;
    }

    // 对参数值进行编码
    @Override
    public String[] getParameterValues(String name) {
        if (this.charset.equalsIgnoreCase(this.originCharset)) {
            return super.getParameterValues(name);
        }
        String[] v = super.getParameterValues(name);
        if (v == null) {
            return null;
        }
        String[] values = new String[v.length];
        try {
            int i = 0;
            for (String value : v) {
                values[i++] = new String(value.getBytes(this.originCharset),
                        this.charset);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return values;
    }

    // 对参数值进行编码
    @Override
    public Map<String, String[]> getParameterMap() {
        if (this.charset.equalsIgnoreCase(this.originCharset)) {
            return super.getParameterMap();
        }
        Map<String, String[]> v = super.getParameterMap();
        if (v.isEmpty()) {
            return v;
        }
        Map<String, String[]> map = new HashMap<String, String[]>();
        try {
            for (String key : v.keySet()) {
                String[] values = new String[v.get(key).length];
                int i = 0;
                for (String value : v.get(key)) {
                    values[i++] = new String(value.getBytes(this.originCharset),
                            this.charset);
                }
                map.put(key, values);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return map;
    }
}
