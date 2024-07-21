package com.github.flyinghe.tools.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.flyinghe.depdcy.KeyValuePair;
import com.github.flyinghe.depdcy.NameContentBodyPair;
import com.github.flyinghe.depdcy.NameFilePair;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.Ognl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class HttpUtils {
    public static final ContentType CONTENT_TYPE_JSON = ContentType.APPLICATION_JSON;
    public static final ContentType CONTENT_TYPE_XML = ContentType.APPLICATION_XML;
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PUT = "PUT";
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;
    private HttpRequestRetryStrategy retryStrategy;
    private Long soTimeout;
    private Long connectTimeout;
    private Logger logger;

    static {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
    }

    public HttpUtils() {
        this(null, null, null, null);
    }

    /**
     * @param connectionTimeout 建立连接时间,0不限制
     */
    public HttpUtils(Long connectionTimeout) {
        this(null, connectionTimeout, null, null);
    }

    /**
     * @param connectionTimeout 建立连接时间,0不限制
     * @param logger            日志记录器
     */
    public HttpUtils(Long connectionTimeout, Logger logger) {
        this(null, connectionTimeout, null, logger);
    }

    /**
     * @param soTimeout         等待数据时间,0不限制
     * @param connectionTimeout 建立连接时间,0不限制
     */
    public HttpUtils(Long soTimeout, Long connectionTimeout) {
        this(soTimeout, connectionTimeout, null, null);
    }

    /**
     * @param soTimeout         等待数据时间,0不限制
     * @param connectionTimeout 建立连接时间,0不限制
     * @param logger            日志记录器
     */
    public HttpUtils(Long soTimeout, Long connectionTimeout, Logger logger) {
        this(soTimeout, connectionTimeout, null, logger);
    }

    /**
     * @param soTimeout         等待数据时间,0不限制
     * @param connectionTimeout 建立连接时间,0不限制
     * @param retryCount        失败重试次数
     * @param logger            日志记录器
     */
    public HttpUtils(Long soTimeout, Long connectionTimeout, Integer retryCount, Logger logger) {
        this.soTimeout = soTimeout;
        this.connectTimeout = connectionTimeout;
        if (null != retryCount) {
            this.retryStrategy = new DefaultHttpRequestRetryStrategy(retryCount, TimeValue.ofSeconds(1L));
        }
        this.logger = logger;
    }


    private CloseableHttpClient getHttpClient(CloseableHttpClient httpClient) {
        if(null != httpClient) {
            return httpClient;
        }
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        RequestConfig.Builder requestConfigBuiler = RequestConfig.custom();
        if (null != this.soTimeout) {
            requestConfigBuiler.setResponseTimeout(this.soTimeout, TimeUnit.MILLISECONDS);
        }
        if (null != this.connectTimeout) {
            httpClientBuilder.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setConnectTimeout(this.connectTimeout, TimeUnit.MILLISECONDS)
                            .build())
                    .build());
        }
        if (null != this.retryStrategy) {
            httpClientBuilder.setRetryStrategy(this.retryStrategy);
        }
        httpClientBuilder.setDefaultRequestConfig(requestConfigBuiler.build());
        return httpClientBuilder.build();
    }

    public static Header getAuthHeader(String username, String pwd) {
        if (null == username) {
            username = "";
        }
        if (null == pwd) {
            pwd = "";
        }
        return new BasicHeader("Authorization",
                String.format("Basic %s", CommonUtils.bytesToBase64Str(String.format("%s:%s", username, pwd).getBytes(
                        StandardCharsets.UTF_8))),true);
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
            queryParam.forEach((k, v) -> nameValuePairList.add(new BasicNameValuePair(k, v)));
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
            queryParams.forEach((k, v) -> nameValuePairList.add(new BasicNameValuePair(k, v)));
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
        return this.execGetOrDelete(null, url, METHOD_GET, requestHeaders, queryParams, "200");
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
    public String execDelete(String url, List<Header> requestHeaders, List<NameValuePair> queryParams,
                             String successCodeRegex)
            throws Exception {
        return this.execGetOrDelete(null, url, METHOD_DELETE, requestHeaders, queryParams, successCodeRegex);
    }



    /**
     * 执行一个Get或者Delete请求，并返回字符串数据
     *
     * @param _httpClient      HttpClient
     * @param url              请求url
     * @param method           请求方法,{@link HttpUtils#METHOD_GET}或者{@link HttpUtils#METHOD_DELETE}
     * @param requestHeaders   请求头列表
     * @param queryParams      请求参数
     * @param successCodeRegex 表示请求成功的正则表达式,若响应码不符合正则表达式则表示此请求失败并抛出异常
     * @return 返回字符串数据
     * @throws Exception
     */
    public String execGetOrDelete(CloseableHttpClient _httpClient, String url, String method, List<Header> requestHeaders, List<NameValuePair> queryParams , String successCodeRegex) throws Exception {
        final HttpRecord httpRecord = new HttpRecord(this.logger, url);
        URIBuilder uriBuilder = new URIBuilder(url);
        if (CollectionUtils.isNotEmpty(queryParams)) {
            uriBuilder.addParameters(queryParams);
        }
        httpRecord.queryParams = uriBuilder.getQueryParams();
        ClassicHttpRequest httpRequest = METHOD_GET.equals(method) ? new HttpGet(uriBuilder.build()) : new HttpDelete(uriBuilder.build());
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(httpRequest::addHeader);
        }
        httpRecord.httpRequest = httpRequest;
        httpRecord.successCodeRegex = successCodeRegex;
        long startTime = System.currentTimeMillis();
        try (CloseableHttpClient httpClient = this.getHttpClient(_httpClient)) {
            httpClient.execute(httpRequest, null, new FlyingHttpClientResponseHandler(httpRecord));
        } catch (Exception e) {
            httpRecord.e = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            httpRecord.execTime = endTime - startTime;
            if (null != this.logger) {
                getOrDeleteLog(httpRecord);
            }
        }
        return httpRecord.responseResult;
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
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
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
    public String execPost(String url, List<Header> requestHeaders, ContentType contentType,
                           List<NameValuePair> queryParams,
                           String paramBody)
            throws Exception {
        return this.execPostOrPut(null, url, METHOD_POST, requestHeaders, contentType, queryParams, paramBody, "200");
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
    public String execPut(String url, List<Header> requestHeaders, ContentType contentType,
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
    public String execPut(String url, List<Header> requestHeaders, ContentType contentType,
                          List<NameValuePair> queryParams,
                          String paramBody, String successCodeRegex)
            throws Exception {
        return this.execPostOrPut(null, url, METHOD_PUT, requestHeaders, contentType, queryParams, paramBody, successCodeRegex);
    }

    /**
     * @param _httpClient    httpClient
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
    public String execPostOrPut(CloseableHttpClient _httpClient, String url,String method, List<Header> requestHeaders, ContentType contentType,
                           List<NameValuePair> queryParams, String paramBody, String successCodeRegex)
            throws Exception {
        contentType = Optional.ofNullable(contentType).orElse(ContentType.APPLICATION_JSON);
        paramBody = Optional.ofNullable(paramBody).orElse("");
        final HttpRecord httpRecord = new HttpRecord(this.logger, url);
        URIBuilder uriBuilder = new URIBuilder(url);
        if (CollectionUtils.isNotEmpty(queryParams)) {
            uriBuilder.addParameters(queryParams);
        }
        httpRecord.queryParams = uriBuilder.getQueryParams();
        ClassicHttpRequest httpRequest = METHOD_POST.equals(method) ? new HttpPost(uriBuilder.build()) : new HttpPut(uriBuilder.build());
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(httpRequest::addHeader);
        }
        httpRecord.httpRequest = httpRequest;
        httpRecord.successCodeRegex = successCodeRegex;
        httpRequest.setEntity(new StringEntity(paramBody, contentType, StandardCharsets.UTF_8.name(), false));
        long startTime = System.currentTimeMillis();
        try (CloseableHttpClient httpClient = this.getHttpClient(_httpClient)) {
            httpClient.execute(httpRequest, null, new FlyingHttpClientResponseHandler(httpRecord));
        } catch (Exception e) {
            httpRecord.e = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            httpRecord.execTime = endTime - startTime;
            if (null != this.logger) {
                postOrPutLog(httpRecord);
            }
        }
        return httpRecord.responseResult;
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
    public <RES> RES execPostFileCB(String url, Map<String, ContentBody> fileParam, Class<RES> clazz)
            throws Exception {
        RES result = null;
        List<NameContentBodyPair> nameContentBodyPairs = new ArrayList<>();
        if (MapUtils.isNotEmpty(fileParam)) {
            fileParam.forEach((k, v) -> nameContentBodyPairs.add(new NameContentBodyPair(k, v)));
        }
        String responseBody = execPostFile(url, null, null, null, nameContentBodyPairs);
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
        return this.execPostOrPutFile(null, url, METHOD_POST, requestHeaders, queryParams, formdataParams, fileParams, "200");
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
        return this.execPostOrPutFile(null, url, METHOD_PUT, requestHeaders, queryParams, formdataParams, fileParams, successCodeRegex);
    }

    /**
     * 执行一个Post或者Put请求，并返回字符串数据
     *
     * @param _httpClient    httpClient
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
    public <T extends KeyValuePair> String execPostOrPutFile(CloseableHttpClient _httpClient, String url,String method, List<Header> requestHeaders,
                                                        List<NameValuePair> queryParams,
                                                        List<NameValuePair> formdataParams,
                                                        List<T> fileParams, String successCodeRegex)
            throws Exception {
        final HttpRecord httpRecord = new HttpRecord(this.logger, url);
        URIBuilder uriBuilder = new URIBuilder(url);
        if (CollectionUtils.isNotEmpty(queryParams)) {
            uriBuilder.addParameters(queryParams);
        }
        httpRecord.queryParams = uriBuilder.getQueryParams();
        ClassicHttpRequest httpRequest = METHOD_POST.equals(method) ? new HttpGet(uriBuilder.build()) : new HttpDelete(uriBuilder.build());
        if (CollectionUtils.isNotEmpty(requestHeaders)) {
            requestHeaders.forEach(httpRequest::addHeader);
        }
        httpRecord.httpRequest = httpRequest;
        httpRecord.successCodeRegex = successCodeRegex;
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        long startTime = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(formdataParams)) {
            formdataParams.forEach(v -> builder.addTextBody(v.getName(), v.getValue(), CONTENT_TYPE_JSON));
        }
        if (CollectionUtils.isNotEmpty(fileParams)) {
            for (KeyValuePair keyValuePair : fileParams) {
                if (keyValuePair.getValue() instanceof File) {
                    builder.addBinaryBody(keyValuePair.getKey(), (File) keyValuePair.getValue());
                } else if (keyValuePair.getValue() instanceof ContentBody) {
                    builder.addPart(keyValuePair.getKey(), (ContentBody) keyValuePair.getValue());
                }
            }
        }
        httpRequest.setEntity(builder.build());
        try (CloseableHttpClient httpClient = this.getHttpClient(_httpClient)) {
            httpClient.execute(httpRequest, null, new FlyingHttpClientResponseHandler(httpRecord));
        } catch (Exception e) {
            httpRecord.e = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            httpRecord.execTime = endTime - startTime;
            if (null != this.logger) {
                postOrPutLog(httpRecord, formdataParams, fileParams);
            }
        }
        return httpRecord.responseResult;
    }

    public static String getExceptionMsg(Exception _e) {
        String errorStack = "";
        if (null != _e && Ognl.isNotEmpty(_e.getStackTrace())) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            _e.printStackTrace(ps);
            try {
                errorStack = baos.toString(StandardCharsets.UTF_8);
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

    static class FlyingHttpClientResponseHandler extends AbstractHttpClientResponseHandler<HttpRecord> {
        private final HttpRecord httpRecord;

        public FlyingHttpClientResponseHandler(HttpRecord httpRecord) {
            this.httpRecord = httpRecord;
        }

        @Override
        public HttpRecord handleResponse(ClassicHttpResponse response) throws IOException {
            this.httpRecord.responseCode = response.getCode();
            this.httpRecord.responseHeaders = response.getHeaders();
            final HttpEntity entity = response.getEntity();
            this.handleEntity(entity);
            if (!Pattern.matches(this.httpRecord.successCodeRegex, String.valueOf(response.getCode()))) {
                throw new HttpResponseException(response.getCode(),
                        String.format("请求url【%s】返回的响应码不符合表示请求成功的正则表达式【%s】,ERROR:【%d】【%s】【%s】", this.httpRecord.url, this.httpRecord.successCodeRegex,
                                response.getCode(),response.getReasonPhrase(), this.httpRecord.responseResult));
            }
            return this.httpRecord;
        }

        @Override
        public HttpRecord handleEntity(HttpEntity entity) throws IOException {
            try {
                this.httpRecord.responseResult = entity == null ? "" : EntityUtils.toString(entity);
                this.httpRecord.responseCharSet = Optional.ofNullable(entity).map(HttpEntity::getContentEncoding).orElse("");
                return this.httpRecord;
            } catch (final ParseException ex) {
                throw new ClientProtocolException(ex);
            }
        }
    }

    public static class HttpRecord {
        public Logger logger;
        public String url;
        public ClassicHttpRequest httpRequest;
        public List<NameValuePair> queryParams;
        public String successCodeRegex;
        public Long execTime;
        public Exception e;
        public String responseCharSet;
        public Integer responseCode;
        public String responseResult;
        public Header[] responseHeaders;

        public HttpRecord() {}

        public HttpRecord(Logger logger, String url) {
            this.logger = logger;
            this.url = url;
        }

        public String getQueryParamsString(){
            return CollectionUtils.isEmpty(this.queryParams) ? "" : StringUtils.join(queryParams, "&");
        }
    }

    public static void getOrDeleteLog(HttpRecord record) {
        ClassicHttpRequest request = record.httpRequest;
        if (!(request instanceof HttpGet) && !(request instanceof HttpDelete)) {
            return;
        }
        String uri = request.getPath();
        String method = request instanceof HttpGet ? METHOD_GET : METHOD_DELETE;
        String queryParam = record.getQueryParamsString();
        String respStatus = Optional.ofNullable(record.responseCode).map(String::valueOf).orElse("");
        String returnStr = Optional.ofNullable(record.responseResult).orElse("");
        String timeConsuming = Optional.ofNullable(record.execTime).map(String::valueOf).orElse("");
        String errorStack = getExceptionMsg(record.e);
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("Uri:%s\r\n", uri))
                .append(String.format("Method:%s\r\n", method))
                .append(String.format("QueryParam:%s\r\n", queryParam))
                .append("RequestHeader:[\r\n");
        printHeader(msgSb, request.getHeaders());
        msgSb.append("]\r\n").append("ResponseHeader:[\r\n");
        printHeader(msgSb, record.responseHeaders);
        msgSb.append("]\r\n")
                .append(String.format("ResponseStatus:%s\r\n", respStatus))
                .append(String.format("Response:%s\r\n", returnStr))
                .append(String.format("TimeConsuming:%s\r\n", timeConsuming))
                .append(String.format("ErrorStack:%s\r\n", errorStack))
                .append("========================================================================");
        record.logger.debug(msgSb.toString());
    }

    public static void postOrPutLog(HttpRecord record) {
        ClassicHttpRequest request = record.httpRequest;
        if (!(request instanceof HttpPost) && !(request instanceof HttpPut)) {
            return;
        }
        String uri = request.getPath();
        String method = request instanceof HttpPost ? METHOD_POST : METHOD_PUT;
        String contentType = Optional.ofNullable(request.getEntity()).map(HttpEntity::getContentType).orElse("");
        String requestCharSet = Optional.ofNullable(request.getEntity()).map(HttpEntity::getContentEncoding).orElse("");
        String responseCharSet = Optional.ofNullable(record.responseCharSet).orElse("");
        String payloadStr = "";
        String queryParam = record.getQueryParamsString();
        String respStatus = Optional.ofNullable(record.responseCode).map(String::valueOf).orElse("");
        String returnStr = Optional.ofNullable(record.responseResult).orElse("");
        try {
            if (null != request.getEntity()) {
                payloadStr = IOUtils.toString(request.getEntity().getContent(), requestCharSet);
            }
        } catch (Exception e) {
            //do nothing
        }
        String timeConsuming = Optional.ofNullable(record.execTime).map(String::valueOf).orElse("");
        String errorStack = getExceptionMsg(record.e);
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("Uri:%s\r\n", uri))
                .append(String.format("Method:%s\r\n", method))
                .append(String.format("ContentType:%s\r\n", contentType))
                .append(String.format("RequestCharSet:%s\r\n", requestCharSet))
                .append(String.format("QueryParam:%s\r\n", queryParam))
                .append("RequestHeader:[\r\n");
        printHeader(msgSb, request.getHeaders());
        msgSb.append("]\r\n").append("ResponseHeader:[\r\n");
        printHeader(msgSb, record.responseHeaders);
        msgSb.append("]\r\n")
                .append(String.format("Payload:%s\r\n", payloadStr))
                .append(String.format("ResponseStatus:%s\r\n", respStatus))
                .append(String.format("ResponseCharSet:%s\r\n", responseCharSet))
                .append(String.format("Response:%s\r\n", returnStr))
                .append(String.format("TimeConsuming:%s\r\n", timeConsuming))
                .append(String.format("ErrorStack:%s\r\n", errorStack))
                .append("========================================================================");
        record.logger.debug(msgSb.toString());
    }

    public static <T extends KeyValuePair> void postOrPutLog(HttpRecord record, List<NameValuePair> formdataParams,
                                                             List<T> fileParams) {
        ClassicHttpRequest request = record.httpRequest;
        if (!(request instanceof HttpPost) && !(request instanceof HttpPut)) {
            return;
        }
        String uri = request.getPath();
        String method = request instanceof HttpPost ? METHOD_POST : METHOD_PUT;
        String contentType = Optional.ofNullable(request.getEntity()).map(HttpEntity::getContentType).orElse("");
        String contentLength = Optional.ofNullable(request.getEntity()).map(HttpEntity::getContentLength).map(Objects::toString).orElse("");
        String responseCharSet = Optional.ofNullable(record.responseCharSet).orElse("");
        String queryParam = record.getQueryParamsString();
        String respStatus = Optional.ofNullable(record.responseCode).map(String::valueOf).orElse("");
        String returnStr = Optional.ofNullable(record.responseResult).orElse("");
        String timeConsuming = Optional.ofNullable(record.execTime).map(String::valueOf).orElse("");

        String errorStack = getExceptionMsg(record.e);
        StringBuilder msgSb = new StringBuilder();
        msgSb.append(String.format("Uri:%s\r\n", uri))
                .append(String.format("Method:%s\r\n", method))
                .append(String.format("ContentType:%s\r\n", contentType))
                .append(String.format("ContentLength:%s\r\n", contentLength))
                .append(String.format("QueryParam:%s\r\n", queryParam))
                .append("RequestHeader:[\r\n");
        printHeader(msgSb, request.getHeaders());
        msgSb.append("]\r\n").append("ResponseHeader:[\r\n");
        printHeader(msgSb, record.responseHeaders);
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
                } else if (pair.getValue() instanceof ContentBody) {
                    ContentBody contentBody = (ContentBody) pair.getValue();
                    msgSb.append(String.format("\t%s:%s(%d)\r\n", pair.getKey(), contentBody.getFilename(),
                            contentBody.getContentLength()));
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
        record.logger.debug(msgSb.toString());
    }
}
