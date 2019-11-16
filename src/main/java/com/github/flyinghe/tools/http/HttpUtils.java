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
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class HttpUtils {
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";
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
        String result = null;
        GetMethod getMethod =
                new GetMethod(url);
        if (null != this.retryHandler) {
            getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, this.retryHandler);
        }
        if (CollectionUtils.isNotEmpty(queryParams)) {
            getMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(getMethod::setRequestHeader);
        }
        try {
            this.httpClient.executeMethod(getMethod);
            if (200 != getMethod.getStatusCode()) {
                throw new Exception(
                        String.format("请求url【%s】未返回200【%d】,ERROR:【%s】【%s】", url, getMethod.getStatusCode(),
                                getMethod.getStatusLine().toString(), getMethod.getResponseBodyAsString()));
            }
            result = getMethod.getResponseBodyAsString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            if (null != this.logger) {
                getLog(getMethod, this.logger);
            }
            getMethod.releaseConnection();
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
                paramBody != null ? objectMapper.writeValueAsString(paramBody) : "");
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
        if (StringUtils.isBlank(contentType)) {
            contentType = CONTENT_TYPE_JSON;
        }
        if (null == paramBody) {
            paramBody = "";
        }

        String result = null;
        PostMethod postMethod = new PostMethod(url);
        if (null != this.retryHandler) {
            postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, this.retryHandler);
        }
        if (CollectionUtils.isNotEmpty(queryParams)) {
            postMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(postMethod::setRequestHeader);
        }
        StringRequestEntity stringRequestEntity = null;
        try {
            stringRequestEntity = new StringRequestEntity(paramBody, contentType, "UTF-8");
            postMethod.setRequestEntity(stringRequestEntity);
            this.httpClient.executeMethod(postMethod);
            if (200 != postMethod.getStatusCode()) {
                throw new Exception(
                        String.format("请求url【%s】未返回200【%d】,ERROR:【%s】【%s】", url, postMethod.getStatusCode(),
                                postMethod.getStatusLine().toString(), postMethod.getResponseBodyAsString()));
            }
            result = postMethod.getResponseBodyAsString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            if (null != this.logger) {
                postLog(postMethod, stringRequestEntity, this.logger);
            }
            postMethod.releaseConnection();
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
        String result = null;
        PostMethod postMethod = new PostMethod(url);
        if (null != this.retryHandler) {
            postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, this.retryHandler);
        }
        if (CollectionUtils.isNotEmpty(queryParams)) {
            postMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(postMethod::setRequestHeader);
        }
        MultipartRequestEntity multipartRequestEntity = null;
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
                multipartRequestEntity =
                        new MultipartRequestEntity(parts.toArray(new Part[]{}), new HttpMethodParams());
                postMethod.setRequestEntity(multipartRequestEntity);
            }
            this.httpClient.executeMethod(postMethod);
            if (200 != postMethod.getStatusCode()) {
                throw new Exception(
                        String.format("请求url【%s】未返回200【%d】,ERROR:【%s】【%s】", url, postMethod.getStatusCode(),
                                postMethod.getStatusLine().toString(), postMethod.getResponseBodyAsString()));
            }
            result = postMethod.getResponseBodyAsString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            if (null != this.logger) {
                postLog(postMethod, multipartRequestEntity, formdataParams, fileParams, this.logger);
            }
            postMethod.releaseConnection();
        }
        return result;
    }

    public static void getLog(GetMethod getMethod, Logger logger) {
        String url = getMethod.getPath();
        String method = "GET";
        String queryParam = getMethod.getQueryString();
        String respStatus = String.valueOf(getMethod.getStatusCode());
        String returnStr = "";
        try {
            returnStr = getMethod.getResponseBodyAsString();
        } catch (Exception e) {
            //do nothing
        }
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("url:%s\r\n", url))
                .append(String.format("method:%s\r\n", method))
                .append(String.format("queryParam:%s\r\n", queryParam))
                .append("Header:[\r\n");
        Header[] requestHeaders = getMethod.getRequestHeaders();
        if (Ognl.isNotEmpty(requestHeaders)) {
            for (int i = 0; i < requestHeaders.length; i++) {
                Header header = requestHeaders[i];
                msgSb.append(String.format("\t%s:%s\r\n", header.getName(), header.getValue()));
            }
        }
        msgSb.append("]\r\n")
                .append(String.format("responseStatus:%s\r\n", respStatus))
                .append(String.format("response:%s\r\n", returnStr))
                .append("========================================================================");
        logger.debug(msgSb.toString());
    }

    public static void postLog(PostMethod postMethod, StringRequestEntity entity, Logger logger) {
        String url = postMethod.getPath();
        String method = "POST";
        String contentType = "";
        String charSet = "";
        String payloadStr = "";
        if (null != entity) {
            contentType = entity.getContentType();
            charSet = entity.getCharset();
            payloadStr = entity.getContent();
        }
        String queryParam = postMethod.getQueryString();
        String respStatus = String.valueOf(postMethod.getStatusCode());
        String returnStr = "";
        try {
            returnStr = postMethod.getResponseBodyAsString();
        } catch (Exception e) {
            //do nothing
        }
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("url:%s\r\n", url))
                .append(String.format("method:%s\r\n", method))
                .append(String.format("contentType:%s\r\n", contentType))
                .append(String.format("charSet:%s\r\n", charSet))
                .append(String.format("queryParam:%s\r\n", queryParam))
                .append("Header:[\r\n");
        Header[] requestHeaders = postMethod.getRequestHeaders();
        if (Ognl.isNotEmpty(requestHeaders)) {
            for (int i = 0; i < requestHeaders.length; i++) {
                Header header = requestHeaders[i];
                msgSb.append(String.format("\t%s:%s\r\n", header.getName(), header.getValue()));
            }
        }
        msgSb.append("]\r\n")
                .append(String.format("payload:%s\r\n", payloadStr))
                .append(String.format("responseStatus:%s\r\n", respStatus))
                .append(String.format("response:%s\r\n", returnStr))
                .append("========================================================================");
        logger.debug(msgSb.toString());
    }

    public static <T extends KeyValuePair> void postLog(PostMethod postMethod, MultipartRequestEntity entity,
                                                        List<NameValuePair> formdataParams,
                                                        List<T> fileParams, Logger logger) {
        String url = postMethod.getPath();
        String method = "POST";
        String contentType = "";
        String contentLength = "";
        if (null != entity) {
            contentType = entity.getContentType();
            contentLength = String.valueOf(entity.getContentLength());
        }
        String queryParam = postMethod.getQueryString();
        String respStatus = String.valueOf(postMethod.getStatusCode());
        String returnStr = "";
        try {
            returnStr = postMethod.getResponseBodyAsString();
        } catch (Exception e) {
            //do nothing
        }
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("url:%s\r\n", url))
                .append(String.format("method:%s\r\n", method))
                .append(String.format("contentType:%s\r\n", contentType))
                .append(String.format("contentLength:%s\r\n", contentLength))
                .append(String.format("queryParam:%s\r\n", queryParam))
                .append("Header:[\r\n");
        Header[] requestHeaders = postMethod.getRequestHeaders();
        if (Ognl.isNotEmpty(requestHeaders)) {
            for (int i = 0; i < requestHeaders.length; i++) {
                Header header = requestHeaders[i];
                msgSb.append(String.format("\t%s:%s\r\n", header.getName(), header.getValue()));
            }
        }
        msgSb.append("]\r\n")
                .append("FormdataParams:[\r\n");
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
                .append(String.format("responseStatus:%s\r\n", respStatus))
                .append(String.format("response:%s\r\n", returnStr))
                .append("========================================================================");
        logger.debug(msgSb.toString());
    }
}
