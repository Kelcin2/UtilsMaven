package com.github.flyinghe.tools.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.flyinghe.depdcy.FlyingFilePart;
import com.github.flyinghe.depdcy.KeyValuePair;
import com.github.flyinghe.depdcy.NameFilePair;
import com.github.flyinghe.depdcy.NamePartSourcePair;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.Ognl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class HttpUtils {
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PUT = "PUT";
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;
    private HttpClient httpClient;
    private DefaultHttpMethodRetryHandler retryHandler;
    private Logger logger;

    static {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
    }

    public HttpUtils() {
        this(null, null, null, null, null);
    }

    /**
     * @param connectionTimeout 建立连接时间,0不限制
     */
    public HttpUtils(Integer connectionTimeout) {
        this(null, null, connectionTimeout, null, null);
    }

    /**
     * @param connectionTimeout 建立连接时间,0不限制
     * @param logger            日志记录器
     */
    public HttpUtils(Integer connectionTimeout, Logger logger) {
        this(null, null, connectionTimeout, null, logger);
    }

    /**
     * @param soTimeout         等待数据时间,0不限制
     * @param connectionTimeout 建立连接时间,0不限制
     */
    public HttpUtils(Integer soTimeout, Integer connectionTimeout) {
        this(null, soTimeout, connectionTimeout, null, null);
    }

    /**
     * @param soTimeout         等待数据时间,0不限制
     * @param connectionTimeout 建立连接时间,0不限制
     * @param logger            日志记录器
     */
    public HttpUtils(Integer soTimeout, Integer connectionTimeout, Logger logger) {
        this(null, soTimeout, connectionTimeout, null, logger);
    }

    /**
     * @param soTimeout         等待数据时间,0不限制
     * @param connectionTimeout 建立连接时间,0不限制
     * @param retryCount        失败重试次数
     * @param logger            日志记录器
     */
    public HttpUtils(Integer soTimeout, Integer connectionTimeout, Integer retryCount, Logger logger) {
        this(null, soTimeout, connectionTimeout, retryCount, logger);
    }

    /**
     * @param httpClient        httpClient
     * @param soTimeout         等待数据时间,0不限制
     * @param connectionTimeout 建立连接时间,0不限制
     * @param retryCount        失败重试次数
     * @param logger            日志记录器
     */
    public HttpUtils(HttpClient httpClient, Integer soTimeout, Integer connectionTimeout, Integer retryCount,
                     Logger logger) {
        if (null == httpClient) {
            this.httpClient = new HttpClient();
        } else {
            this.httpClient = httpClient;
        }
        if (null != soTimeout) {
            this.httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);
        }
        if (null != connectionTimeout) {
            this.httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);
        }

        if (null != retryCount) {
            this.retryHandler = new DefaultHttpMethodRetryHandler(retryCount, false);
        }
        this.logger = logger;
    }

    public static Header getAuthHeader(String username, String pwd) {
        if (null == username) {
            username = "";
        }
        if (null == pwd) {
            pwd = "";
        }
        return new Header("Authorization",
                String.format("Basic %s", CommonUtils.bytesToBase64Str(String.format("%s:%s", username, pwd).getBytes(
                        StandardCharsets.UTF_8))));
    }

    public static List<Header> getAuthHeaderList(String username, String pwd) {
        return new ArrayList<Header>() {{
            add(getAuthHeader(username, pwd));
        }};
    }

    /**
     * 执行一个Get请求，并将返回的数据解析成clazz对象(需要目标接口返回Json数据,并符合clazz对象数据结构)
     *
     * @param url         请求url
     * @param queryParams Get请求参数
     * @param clazz       将目标接口解析成对象的class对象
     * @param <RES>
     * @return 将返回的数据解析成的clazz对象, 若返回数据为空则返回null
     * @throws Exception
     */
    public <RES> RES execGetJson(String url, List<NameValuePair> queryParams, Class<RES> clazz)
            throws Exception {
        RES result = null;
        String responseBody = execGet(url, queryParams);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody, clazz);
        }
        return result;
    }

    /**
     * 执行一个Get请求，并将返回的数据解析成clazz对象(需要目标接口返回Json数据,并符合clazz对象数据结构)
     *
     * @param url        请求url
     * @param queryParam Get请求参数
     * @param clazz      将目标接口解析成对象的class对象
     * @param <RES>
     * @return 将返回的数据解析成的clazz对象, 若返回数据为空则返回null
     * @throws Exception
     */
    public <RES> RES execGetJson(String url, Map<String, String> queryParam, Class<RES> clazz)
            throws Exception {
        RES result = null;

        List<NameValuePair> nameValuePairList = new ArrayList<>();
        if (MapUtils.isNotEmpty(queryParam)) {
            queryParam.forEach((k, v) -> nameValuePairList.add(new NameValuePair(k, v)));
        }
        String responseBody = execGet(url, nameValuePairList);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody, clazz);
        }
        return result;
    }

    /**
     * 执行一个Get请求，并将返回的数据解析成Map数据(需要目标接口返回Json数据)
     *
     * @param url         请求url
     * @param queryParams Get请求参数
     * @return 返回解析后的Map, 若没有数据返回null
     * @throws Exception
     */
    public Map<String, Object> execGetJson(String url, List<NameValuePair> queryParams) throws Exception {
        Map<String, Object> result = null;
        String responseBody = execGet(url, queryParams);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody,
                    typeFactory.constructMapType(HashMap.class, String.class, Object.class));
        }
        return result;
    }

    /**
     * 执行一个Get请求，并将返回的数据解析成Map数据(需要目标接口返回Json数据)
     *
     * @param url         请求url
     * @param queryParams Get请求参数
     * @return 返回解析后的Map, 若没有数据返回null
     * @throws Exception
     */
    public Map<String, Object> execGetJson(String url, Map<String, String> queryParams) throws Exception {
        Map<String, Object> result = null;
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        if (MapUtils.isNotEmpty(queryParams)) {
            queryParams.forEach((k, v) -> nameValuePairList.add(new NameValuePair(k, v)));
        }
        String responseBody = execGet(url, nameValuePairList);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody,
                    typeFactory.constructMapType(HashMap.class, String.class, Object.class));
        }
        return result;
    }

    /**
     * 执行一个Get请求，并返回字符串数据
     *
     * @param url         请求url
     * @param queryParams Get请求参数
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execGet(String url, List<NameValuePair> queryParams) throws Exception {
        return execGet(url, null, queryParams);
    }

    /**
     * 执行一个Get请求，并返回字符串数据
     *
     * @param url            请求url
     * @param requestHeaders 请求头列表
     * @param queryParams    Get请求参数
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execGet(String url, List<Header> requestHeaders, List<NameValuePair> queryParams)
            throws Exception {
        return this.execGetOrDelete(url, METHOD_GET, requestHeaders, queryParams, "200");
    }

    /**
     * 执行一个Delete请求，并返回字符串数据
     *
     * @param url            请求url
     * @param requestHeaders 请求头列表
     * @param queryParams    Delete请求参数
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execDelete(String url, List<Header> requestHeaders, List<NameValuePair> queryParams)
            throws Exception {
        return this.execDelete(url, requestHeaders, queryParams, "200");
    }

    /**
     * 执行一个Delete请求，并返回字符串数据
     *
     * @param url              请求url
     * @param requestHeaders   请求头列表
     * @param queryParams      Delete请求参数
     * @param successCodeRegex 表示请求成功的正则表达式,若响应码不符合正则表达式则表示此请求失败并抛出异常
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execDelete(String url, List<Header> requestHeaders, List<NameValuePair> queryParams, String successCodeRegex)
            throws Exception {
        return this.execGetOrDelete(url, METHOD_DELETE, requestHeaders, queryParams, successCodeRegex);
    }

    /**
     * 执行一个Get或者Delete请求，并返回字符串数据
     *
     * @param url              请求url
     * @param method           请求方法,{@link HttpUtils#METHOD_GET}或者{@link HttpUtils#METHOD_DELETE}
     * @param requestHeaders   请求头列表
     * @param queryParams      请求参数
     * @param successCodeRegex 表示请求成功的正则表达式,若响应码不符合正则表达式则表示此请求失败并抛出异常
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execGetOrDelete(String url, String method, List<Header> requestHeaders, List<NameValuePair> queryParams , String successCodeRegex) throws Exception {
        String result = null;
        HttpMethod httpMethod = METHOD_GET.equals(method) ? new GetMethod(url) : new DeleteMethod(url);
        if (null != this.retryHandler) {
            httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, this.retryHandler);
        }
        if (CollectionUtils.isNotEmpty(queryParams)) {
            httpMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(httpMethod::setRequestHeader);
        }
        Exception _e = null;
        long startTime = System.currentTimeMillis();
        try {
            this.httpClient.executeMethod(httpMethod);
            if (!Pattern.matches(successCodeRegex, String.valueOf(httpMethod.getStatusCode()))) {
                throw new Exception(
                        String.format("请求url【%s】返回的响应码不符合表示请求成功的正则表达式【%s】,ERROR:【%s】【%s】", url, successCodeRegex,
                                httpMethod.getStatusLine().toString(), httpMethod.getResponseBodyAsString()));
            }
            result = httpMethod.getResponseBodyAsString();
        } catch (Exception e) {
            _e = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            if (null != this.logger) {
                getOrDeleteLog(httpMethod, endTime - startTime, this.logger, _e);
            }
            httpMethod.releaseConnection();
        }
        return result;
    }


    /**
     * 执行一个Post请求，并将返回的Json数据解析到clazz对象中
     *
     * @param url       请求url
     * @param paramBody queryParameter请求参数
     * @param clazz     将目标接口解析成对象的class对象
     * @param <REQ>
     * @param <RES>
     * @return 将返回的数据解析成的clazz对象, 若返回数据为空则返回null
     * @throws Exception
     */
    public <REQ, RES> RES execPostJson(String url, REQ paramBody, Class<RES> clazz) throws Exception {
        return execPostJson(url, null, paramBody, clazz);
    }

    /**
     * 执行一个Post请求，并将返回的Json数据解析到Map中
     *
     * @param url       请求url
     * @param paramBody Post请求体,会被解析成Json数据放入Post请求体中
     * @return 将返回的数据解析成的Map对象, 若返回数据为空则返回null
     * @throws Exception
     */
    public Map<String, Object> execPostJson(String url, Map<String, Object> paramBody) throws Exception {
        return execPostJson(url, null, paramBody);
    }

    /**
     * 执行一个Post请求，并将返回的Json数据解析到Map中
     *
     * @param url        请求url
     * @param queryParam queryParameter请求参数
     * @param paramBody  Post请求体,会被解析成Json数据放入Post请求体中
     * @return 将返回的数据解析成的Map对象, 若返回数据为空则返回null
     * @throws Exception
     */
    public Map<String, Object> execPostJson(String url, Map<String, String> queryParam,
                                            Map<String, Object> paramBody) throws Exception {

        Map<String, Object> result = null;
        String responseBody = execPost(url, queryParam, paramBody);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody,
                    typeFactory.constructMapType(HashMap.class, String.class, Object.class));
        }

        return result;
    }

    /**
     * 执行一个Post请求，并将返回的Json数据解析到clazz对象中
     *
     * @param url        请求url
     * @param queryParam queryParameter请求参数
     * @param paramBody  Post请求体,会被解析成Json数据放入Post请求体中
     * @param clazz      将目标接口解析成对象的class对象
     * @param <REQ>
     * @param <RES>
     * @return 将返回的数据解析成的clazz对象, 若返回数据为空则返回null
     * @throws Exception
     */
    public <REQ, RES> RES execPostJson(String url, Map<String, String> queryParam,
                                       REQ paramBody, Class<RES> clazz) throws Exception {
        RES result = null;
        String responseBody = execPost(url, queryParam, paramBody);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody, clazz);
        }
        return result;
    }

    /**
     * 执行一个Post请求，并将返回的Json数据解析到Map中
     *
     * @param url       请求url
     * @param paramBody Post请求体,会被解析成Json数据放入Post请求体中
     * @param username  用户名
     * @param pwd       密码
     * @return 将返回的数据解析成的Map对象, 若返回数据为空则返回null
     * @throws Exception
     */
    public Map<String, Object> execPostJson(String url, Map<String, Object> paramBody, String username,
                                            String pwd) throws Exception {
        String body = null == paramBody ? "" : objectMapper.writeValueAsString(paramBody);
        String responseBody =
                this.execPost(url, HttpUtils.getAuthHeaderList(username, pwd), HttpUtils.CONTENT_TYPE_JSON, null, body);
        if (StringUtils.isNotBlank(responseBody)) {
            return objectMapper.readValue(responseBody,
                    typeFactory.constructMapType(HashMap.class, String.class, Object.class));
        }
        return null;
    }

    /**
     * 执行一个Post请求，并返回字符串数据(一般用于请求体数据为Json)
     *
     * @param url        请求url
     * @param queryParam queryParameter请求参数
     * @param paramBody  Post请求体,会被解析成Json数据放入Post请求体中
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execPost(String url, Map<String, String> queryParam, Object paramBody)
            throws Exception {
        List<NameValuePair> nameValuePairList = null;
        if (MapUtils.isNotEmpty(queryParam)) {
            nameValuePairList = new ArrayList<>(queryParam.size());
            for (Map.Entry<String, String> entry : queryParam.entrySet()) {
                nameValuePairList.add(new NameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        return execPost(url, nameValuePairList, paramBody);
    }

    /**
     * 执行一个Post请求，并返回字符串数据(一般用于请求体数据为Json)
     *
     * @param url         请求url
     * @param queryParams queryParameter请求参数
     * @param paramBody   Post请求体,会被解析成Json数据放入Post请求体中
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execPost(String url, List<NameValuePair> queryParams, Object paramBody)
            throws Exception {
        return execPost(url, null, CONTENT_TYPE_JSON, queryParams,
                paramBody == null ? "" : paramBody instanceof String ? paramBody.toString() : objectMapper.writeValueAsString(paramBody));
    }

    /**
     * @param url            请求url
     * @param requestHeaders 请求头列表
     * @param contentType    contentType
     * @param queryParams    queryParameter请求参数
     * @param paramBody      Post请求体
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execPost(String url, List<Header> requestHeaders, String contentType,
                           List<NameValuePair> queryParams,
                           String paramBody)
            throws Exception {
        return this.execPostOrPut(url, METHOD_POST, requestHeaders, contentType, queryParams, paramBody, "200");
    }

    /**
     * @param url            请求url
     * @param requestHeaders 请求头列表
     * @param contentType    contentType
     * @param queryParams    queryParameter请求参数
     * @param paramBody      Put请求体
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execPut(String url, List<Header> requestHeaders, String contentType,
                           List<NameValuePair> queryParams,
                           String paramBody)
            throws Exception {
        return this.execPut(url, requestHeaders, contentType, queryParams, paramBody, "200");
    }

    /**
     * @param url              请求url
     * @param requestHeaders   请求头列表
     * @param contentType      contentType
     * @param queryParams      queryParameter请求参数
     * @param paramBody        Put请求体
     * @param successCodeRegex 表示请求成功的正则表达式,若响应码不符合正则表达式则表示此请求失败并抛出异常
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execPut(String url, List<Header> requestHeaders, String contentType,
                          List<NameValuePair> queryParams,
                          String paramBody, String successCodeRegex)
            throws Exception {
        return this.execPostOrPut(url, METHOD_PUT, requestHeaders, contentType, queryParams, paramBody, successCodeRegex);
    }

    /**
     * @param url            请求url
     * @param method         请求方法,{@link HttpUtils#METHOD_POST}或者{@link HttpUtils#METHOD_PUT}
     * @param requestHeaders 请求头列表
     * @param contentType    contentType
     * @param queryParams    queryParameter请求参数
     * @param paramBody      请求体
     * @param successCodeRegex 表示请求成功的正则表达式,若响应码不符合正则表达式则表示此请求失败并抛出异常
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execPostOrPut(String url,String method, List<Header> requestHeaders, String contentType,
                           List<NameValuePair> queryParams, String paramBody, String successCodeRegex)
            throws Exception {
        if (StringUtils.isBlank(contentType)) {
            contentType = CONTENT_TYPE_JSON;
        }
        if (null == paramBody) {
            paramBody = "";
        }

        String result = null;
        HttpMethod httpMethod = METHOD_POST.equals(method) ? new PostMethod(url) : new PutMethod(url);
        if (null != this.retryHandler) {
            httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, this.retryHandler);
        }
        if (CollectionUtils.isNotEmpty(queryParams)) {
            httpMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(httpMethod::setRequestHeader);
        }
        StringRequestEntity stringRequestEntity = null;
        Exception _e = null;
        long startTime = System.currentTimeMillis();
        try {
            stringRequestEntity = new StringRequestEntity(paramBody, contentType, "UTF-8");
            if (httpMethod instanceof PostMethod) {
                ((PostMethod) httpMethod).setRequestEntity(stringRequestEntity);
            } else {
                ((PutMethod) httpMethod).setRequestEntity(stringRequestEntity);
            }
            this.httpClient.executeMethod(httpMethod);
            if (!Pattern.matches(successCodeRegex, String.valueOf(httpMethod.getStatusCode()))) {
                throw new Exception(
                        String.format("请求url【%s】返回的响应码不符合表示请求成功的正则表达式【%s】,ERROR:【%s】【%s】", url, successCodeRegex,
                                httpMethod.getStatusLine().toString(), httpMethod.getResponseBodyAsString()));
            }
            result = httpMethod.getResponseBodyAsString();
        } catch (Exception e) {
            _e = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            if (null != this.logger) {
                postOrPutLog(httpMethod, stringRequestEntity, endTime - startTime, this.logger, _e);
            }
            httpMethod.releaseConnection();
        }
        return result;
    }

    /**
     * 执行一个Post请求，并返回字符串数据(一般用于返回数据为Json)
     *
     * @param url       请求url
     * @param fileParam 文件请求参数
     * @return 返回字符串数据(一般为Json)
     * @throws Exception
     */
    public <RES> RES execPostFile(String url, Map<String, File> fileParam, Class<RES> clazz)
            throws Exception {
        RES result = null;
        List<NameFilePair> filePairs = new ArrayList<>();
        if (MapUtils.isNotEmpty(fileParam)) {
            fileParam.forEach((k, v) -> filePairs.add(new NameFilePair(k, v)));
        }
        String responseBody = execPostFile(url, null, null, null, filePairs);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody, clazz);
        }
        return result;
    }

    /**
     * 执行一个Post请求，并返回字符串数据(一般用于返回数据为Json)
     *
     * @param url       请求url
     * @param fileParam 文件请求参数
     * @return 返回字符串数据(一般为Json)
     * @throws Exception
     */
    public <RES> RES execPostFilePS(String url, Map<String, PartSource> fileParam, Class<RES> clazz)
            throws Exception {
        RES result = null;
        List<NamePartSourcePair> namePartSourcePairs = new ArrayList<>();
        if (MapUtils.isNotEmpty(fileParam)) {
            fileParam.forEach((k, v) -> namePartSourcePairs.add(new NamePartSourcePair(k, v)));
        }
        String responseBody = execPostFile(url, null, null, null, namePartSourcePairs);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody, clazz);
        }
        return result;
    }

    /**
     * 执行一个Post请求，并返回字符串数据
     *
     * @param url            请求url
     * @param requestHeaders 请求头
     * @param queryParams    queryParameter请求参数
     * @param formdataParams form-data请求参数
     * @param fileParams     文件请求参数
     * @return 返回字符串数据
     * @throws Exception
     */
    public <T extends KeyValuePair> String execPostFile(String url, List<Header> requestHeaders,
                                                        List<NameValuePair> queryParams,
                                                        List<NameValuePair> formdataParams,
                                                        List<T> fileParams)
            throws Exception {
        return this.execPostOrPutFile(url, METHOD_POST, requestHeaders, queryParams, formdataParams, fileParams, "200");
    }

    /**
     * 执行一个Put请求，并返回字符串数据
     *
     * @param url            请求url
     * @param requestHeaders 请求头
     * @param queryParams    queryParameter请求参数
     * @param formdataParams form-data请求参数
     * @param fileParams     文件请求参数
     * @return 返回字符串数据
     * @throws Exception
     */
    public <T extends KeyValuePair> String execPutFile(String url, List<Header> requestHeaders,
                                                        List<NameValuePair> queryParams,
                                                        List<NameValuePair> formdataParams,
                                                        List<T> fileParams)
            throws Exception {
        return this.execPutFile(url, requestHeaders, queryParams, formdataParams, fileParams, "200");
    }

    /**
     * 执行一个Put请求，并返回字符串数据
     *
     * @param url              请求url
     * @param requestHeaders   请求头
     * @param queryParams      queryParameter请求参数
     * @param formdataParams   form-data请求参数
     * @param fileParams       文件请求参数
     * @param successCodeRegex 表示请求成功的正则表达式,若响应码不符合正则表达式则表示此请求失败并抛出异常
     * @return 返回字符串数据
     * @throws Exception
     */
    public <T extends KeyValuePair> String execPutFile(String url, List<Header> requestHeaders,
                                                       List<NameValuePair> queryParams,
                                                       List<NameValuePair> formdataParams,
                                                       List<T> fileParams, String successCodeRegex)
            throws Exception {
        return this.execPostOrPutFile(url, METHOD_PUT, requestHeaders, queryParams, formdataParams, fileParams, successCodeRegex);
    }

    /**
     * 执行一个Post或者Put请求，并返回字符串数据
     *
     * @param url            请求url
     * @param method         请求方法,{@link HttpUtils#METHOD_POST}或者{@link HttpUtils#METHOD_PUT}
     * @param requestHeaders 请求头
     * @param queryParams    queryParameter请求参数
     * @param formdataParams form-data请求参数
     * @param fileParams     文件请求参数
     * @param successCodeRegex 表示请求成功的正则表达式,若响应码不符合正则表达式则表示此请求失败并抛出异常
     * @return 返回字符串数据
     * @throws Exception
     */
    public <T extends KeyValuePair> String execPostOrPutFile(String url,String method, List<Header> requestHeaders,
                                                        List<NameValuePair> queryParams,
                                                        List<NameValuePair> formdataParams,
                                                        List<T> fileParams, String successCodeRegex)
            throws Exception {
        String result = null;
        HttpMethod httpMethod = METHOD_POST.equals(method) ? new PostMethod(url) : new PutMethod(url);
        if (null != this.retryHandler) {
            httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, this.retryHandler);
        }
        if (CollectionUtils.isNotEmpty(queryParams)) {
            httpMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(httpMethod::setRequestHeader);
        }
        MultipartRequestEntity multipartRequestEntity = null;
        Exception _e = null;
        long startTime = System.currentTimeMillis();
        try {
            List<Part> parts = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(formdataParams)) {
                formdataParams.forEach(v -> parts.add(new StringPart(v.getName(), v.getValue(), "UTF-8")));
            }
            if (CollectionUtils.isNotEmpty(fileParams)) {
                for (KeyValuePair keyValuePair : fileParams) {
                    FlyingFilePart part = null;
                    if (keyValuePair.getValue() instanceof File) {
                        part = new FlyingFilePart(keyValuePair.getKey(), (File) keyValuePair.getValue());
                    } else if (keyValuePair.getValue() instanceof PartSource) {
                        part = new FlyingFilePart(keyValuePair.getKey(), (PartSource) keyValuePair.getValue());
                    }
                    if (null != part) {
                        parts.add(part);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(parts)) {
                multipartRequestEntity = new MultipartRequestEntity(parts.toArray(new Part[]{}), new HttpMethodParams());
                if (httpMethod instanceof PostMethod) {
                    ((PostMethod) httpMethod).setRequestEntity(multipartRequestEntity);
                } else {
                    ((PutMethod) httpMethod).setRequestEntity(multipartRequestEntity);
                }
            }
            this.httpClient.executeMethod(httpMethod);
            if (!Pattern.matches(successCodeRegex, String.valueOf(httpMethod.getStatusCode()))) {
                throw new Exception(
                        String.format("请求url【%s】返回的响应码不符合表示请求成功的正则表达式【%s】,ERROR:【%s】【%s】", url, successCodeRegex,
                                httpMethod.getStatusLine().toString(), httpMethod.getResponseBodyAsString()));
            }
            result = httpMethod.getResponseBodyAsString();
        } catch (Exception e) {
            _e = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            if (null != this.logger) {
                postOrPutLog(httpMethod, multipartRequestEntity, formdataParams, fileParams, endTime - startTime,
                        this.logger, _e);
            }
            httpMethod.releaseConnection();
        }
        return result;
    }

    public static String getExceptionMsg(Exception _e) {
        String errorStack = "";
        if (null != _e && Ognl.isNotEmpty(_e.getStackTrace())) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            _e.printStackTrace(ps);
            try {
                errorStack = baos.toString("UTF-8");
            } catch (Exception e) {
                //do nothing
            } finally {
                CommonUtils.closeIOStream(null, baos);
                CommonUtils.closeIOStream(null, ps);
            }
        }
        return errorStack;
    }

    public static void printHeader(StringBuilder msgSb, Header[] headers) {
        if (Ognl.isNotEmpty(headers)) {
            for (int i = 0; i < headers.length; i++) {
                Header header = headers[i];
                msgSb.append(String.format("\t%s:%s\r\n", header.getName(), header.getValue()));
            }
        }
    }

    public static void getOrDeleteLog(HttpMethod httpMethod, Long exeTime, Logger logger, Exception _e) {
        if (!(httpMethod instanceof GetMethod) && !(httpMethod instanceof DeleteMethod)) {
            return;
        }
        String uri = httpMethod.getPath();
        String method = httpMethod instanceof GetMethod ? METHOD_GET : METHOD_DELETE;
        String queryParam = Optional.ofNullable(httpMethod.getQueryString()).orElse("");
        String respStatus =
                Optional.ofNullable(httpMethod.getStatusLine()).map(StatusLine::getStatusCode).map(Objects::toString)
                        .orElse("");
        String returnStr = "";
        try {
            returnStr = Optional.ofNullable(httpMethod.getResponseBodyAsString()).orElse("");
        } catch (Exception e) {
            //do nothing
        }
        String timeConsuming = null != exeTime ? String.valueOf(exeTime) : "";
        String errorStack = getExceptionMsg(_e);
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("Uri:%s\r\n", uri))
                .append(String.format("Method:%s\r\n", method))
                .append(String.format("QueryParam:%s\r\n", queryParam))
                .append("RequestHeader:[\r\n");
        printHeader(msgSb, httpMethod.getRequestHeaders());
        msgSb.append("]\r\n").append("ResponseHeader:[\r\n");
        printHeader(msgSb, httpMethod.getResponseHeaders());
        msgSb.append("]\r\n")
                .append(String.format("ResponseStatus:%s\r\n", respStatus))
                .append(String.format("Response:%s\r\n", returnStr))
                .append(String.format("TimeConsuming:%s\r\n", timeConsuming))
                .append(String.format("ErrorStack:%s\r\n", errorStack))
                .append("========================================================================");
        logger.debug(msgSb.toString());
    }

    public static void postOrPutLog(HttpMethod httpMethod, StringRequestEntity entity, Long exeTime, Logger logger,
                               Exception _e) {
        if (!(httpMethod instanceof PostMethod) && !(httpMethod instanceof PutMethod)) {
            return;
        }
        String uri = httpMethod.getPath();
        String method = httpMethod instanceof PostMethod ? METHOD_POST : METHOD_PUT;
        String contentType = "";
        String requestCharSet;
        String responseCharSet;
        if (httpMethod instanceof PostMethod) {
            requestCharSet = Optional.ofNullable(((PostMethod) httpMethod).getRequestCharSet()).orElse("");
            responseCharSet = Optional.ofNullable(((PostMethod) httpMethod).getResponseCharSet()).orElse("");
        } else {
            requestCharSet = Optional.ofNullable(((PutMethod) httpMethod).getRequestCharSet()).orElse("");
            responseCharSet = Optional.ofNullable(((PutMethod) httpMethod).getResponseCharSet()).orElse("");
        }
        String payloadStr = "";
        if (null != entity) {
            contentType = entity.getContentType();
            payloadStr = entity.getContent();
        }
        String queryParam = Optional.ofNullable(httpMethod.getQueryString()).orElse("");
        String respStatus =
                Optional.ofNullable(httpMethod.getStatusLine()).map(StatusLine::getStatusCode).map(Objects::toString)
                        .orElse("");
        String returnStr = "";
        try {
            returnStr = Optional.ofNullable(httpMethod.getResponseBodyAsString()).orElse("");
        } catch (Exception e) {
            //do nothing
        }
        String timeConsuming = null != exeTime ? String.valueOf(exeTime) : "";
        String errorStack = getExceptionMsg(_e);
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("Uri:%s\r\n", uri))
                .append(String.format("Method:%s\r\n", method))
                .append(String.format("ContentType:%s\r\n", contentType))
                .append(String.format("RequestCharSet:%s\r\n", requestCharSet))
                .append(String.format("QueryParam:%s\r\n", queryParam))
                .append("RequestHeader:[\r\n");
        printHeader(msgSb, httpMethod.getRequestHeaders());
        msgSb.append("]\r\n").append("ResponseHeader:[\r\n");
        printHeader(msgSb, httpMethod.getResponseHeaders());
        msgSb.append("]\r\n")
                .append(String.format("Payload:%s\r\n", payloadStr))
                .append(String.format("ResponseStatus:%s\r\n", respStatus))
                .append(String.format("ResponseCharSet:%s\r\n", responseCharSet))
                .append(String.format("Response:%s\r\n", returnStr))
                .append(String.format("TimeConsuming:%s\r\n", timeConsuming))
                .append(String.format("ErrorStack:%s\r\n", errorStack))
                .append("========================================================================");
        logger.debug(msgSb.toString());
    }

    public static <T extends KeyValuePair> void postOrPutLog(HttpMethod httpMethod, MultipartRequestEntity entity,
                                                        List<NameValuePair> formdataParams,
                                                        List<T> fileParams, Long exeTime, Logger logger, Exception _e) {
        String uri = httpMethod.getPath();
        String method = httpMethod instanceof PostMethod ? METHOD_POST : METHOD_PUT;
        String contentType = "";
        String contentLength = "";
        String responseCharSet;
        if (httpMethod instanceof PostMethod) {
            responseCharSet = Optional.ofNullable(((PostMethod) httpMethod).getResponseCharSet()).orElse("");
        } else {
            responseCharSet = Optional.ofNullable(((PutMethod) httpMethod).getResponseCharSet()).orElse("");
        }
        if (null != entity) {
            contentType = entity.getContentType();
            contentLength = Optional.ofNullable(entity.getContentLength()).map(Objects::toString).orElse("");
        }
        String queryParam = Optional.ofNullable(httpMethod.getQueryString()).orElse("");
        String respStatus =
                Optional.ofNullable(httpMethod.getStatusLine()).map(StatusLine::getStatusCode).map(Objects::toString)
                        .orElse("");
        String returnStr = "";
        try {
            returnStr = Optional.ofNullable(httpMethod.getResponseBodyAsString()).orElse("");
        } catch (Exception e) {
            //do nothing
        }
        String timeConsuming = null != exeTime ? String.valueOf(exeTime) : "";
        String errorStack = getExceptionMsg(_e);
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("Uri:%s\r\n", uri))
                .append(String.format("Method:%s\r\n", method))
                .append(String.format("ContentType:%s\r\n", contentType))
                .append(String.format("ContentLength:%s\r\n", contentLength))
                .append(String.format("QueryParam:%s\r\n", queryParam))
                .append("RequestHeader:[\r\n");
        printHeader(msgSb, httpMethod.getRequestHeaders());
        msgSb.append("]\r\n").append("ResponseHeader:[\r\n");
        printHeader(msgSb, httpMethod.getResponseHeaders());
        msgSb.append("]\r\n").append("FormdataParams:[\r\n");
        if (CollectionUtils.isNotEmpty(formdataParams)) {
            for (NameValuePair pair : formdataParams) {
                msgSb.append(String.format("\t%s:%s\r\n", pair.getName(), pair.getValue()));
            }
        }
        msgSb.append("]\r\n")
                .append("FileParams:[\r\n");
        if (CollectionUtils.isNotEmpty(fileParams)) {
            for (KeyValuePair pair : fileParams) {
                if (pair.getValue() instanceof File) {
                    File file = (File) pair.getValue();
                    msgSb.append(String.format("\t%s:%s(%d)\r\n", pair.getKey(), file.getName(), file.length()));
                } else if (pair.getValue() instanceof PartSource) {
                    PartSource partSource = (PartSource) pair.getValue();
                    msgSb.append(String.format("\t%s:%s(%d)\r\n", pair.getKey(), partSource.getFileName(),
                            partSource.getLength()));
                }
            }
        }
        msgSb.append("]\r\n")
                .append(String.format("ResponseStatus:%s\r\n", respStatus))
                .append(String.format("ResponseCharSet:%s\r\n", responseCharSet))
                .append(String.format("Response:%s\r\n", returnStr))
                .append(String.format("TimeConsuming:%s\r\n", timeConsuming))
                .append(String.format("ErrorStack:%s\r\n", errorStack))
                .append("========================================================================");
        logger.debug(msgSb.toString());
    }
}
