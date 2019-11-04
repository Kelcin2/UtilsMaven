package com.github.flyinghe.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.flyinghe.depdcy.FlyingFilePart;
import com.github.flyinghe.depdcy.KeyValuePair;
import com.github.flyinghe.depdcy.NameFilePair;
import com.github.flyinghe.depdcy.NamePartSourcePair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class HttpUtils {
    private static HttpClient httpClient;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;

    static {
        httpClient = new HttpClient();
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
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
    public static <RES> RES execGetJson(String url, List<NameValuePair> queryParams, Class<RES> clazz)
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
    public static <RES> RES execGetJson(String url, Map<String, String> queryParam, Class<RES> clazz)
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
    public static Map<String, Object> execGetJson(String url, List<NameValuePair> queryParams) throws Exception {
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
    public static Map<String, Object> execGetJson(String url, Map<String, String> queryParams) throws Exception {
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
    public static String execGet(String url, List<NameValuePair> queryParams) throws Exception {
        String result = null;
        GetMethod getMethod =
                new GetMethod(url);
        if (CollectionUtils.isNotEmpty(queryParams)) {
            getMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        try {
            httpClient.executeMethod(getMethod);
            if (200 != getMethod.getStatusCode()) {
                throw new Exception(
                        String.format("请求url【%s】未返回200【%d】,ERROR:【%s】【%s】", url, getMethod.getStatusCode(),
                                getMethod.getStatusLine().toString(), getMethod.getResponseBodyAsString()));
            }
            result = getMethod.getResponseBodyAsString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
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
    public static <REQ, RES> RES execPostJson(String url, REQ paramBody, Class<RES> clazz) throws Exception {
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
    public static Map<String, Object> execPostJson(String url, Map<String, Object> paramBody) throws Exception {
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
    public static Map<String, Object> execPostJson(String url, Map<String, String> queryParam,
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
    public static <REQ, RES> RES execPostJson(String url, Map<String, String> queryParam,
                                              REQ paramBody, Class<RES> clazz) throws Exception {
        RES result = null;
        String responseBody = execPost(url, queryParam, paramBody);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody, clazz);
        }
        return result;
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
    public static String execPost(String url, Map<String, String> queryParam, Object paramBody)
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
    public static String execPost(String url, List<NameValuePair> queryParams, Object paramBody)
            throws Exception {
        String result = null;
        PostMethod postMethod = new PostMethod(url);
        if (CollectionUtils.isNotEmpty(queryParams)) {
            postMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        try {
            StringRequestEntity stringRequestEntity =
                    new StringRequestEntity(
                            paramBody != null ? objectMapper.writeValueAsString(paramBody) : "",
                            "application/json", "UTF-8");
            postMethod.setRequestEntity(stringRequestEntity);
            httpClient.executeMethod(postMethod);
            if (200 != postMethod.getStatusCode()) {
                throw new Exception(
                        String.format("请求url【%s】未返回200【%d】,ERROR:【%s】【%s】", url, postMethod.getStatusCode(),
                                postMethod.getStatusLine().toString(), postMethod.getResponseBodyAsString()));
            }
            result = postMethod.getResponseBodyAsString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
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
    public static <RES> RES execPostFile(String url, Map<String, File> fileParam, Class<RES> clazz)
            throws Exception {
        RES result = null;
        List<NameFilePair> filePairs = new ArrayList<>();
        if (MapUtils.isNotEmpty(fileParam)) {
            fileParam.forEach((k, v) -> filePairs.add(new NameFilePair(k, v)));
        }
        String responseBody = execPostFile(url, null, null, filePairs);
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
    public static <RES> RES execPostFilePS(String url, Map<String, PartSource> fileParam, Class<RES> clazz)
            throws Exception {
        RES result = null;
        List<NamePartSourcePair> namePartSourcePairs = new ArrayList<>();
        if (MapUtils.isNotEmpty(fileParam)) {
            fileParam.forEach((k, v) -> namePartSourcePairs.add(new NamePartSourcePair(k, v)));
        }
        String responseBody = execPostFile(url, null, null, namePartSourcePairs);
        if (StringUtils.isNotBlank(responseBody)) {
            result = objectMapper.readValue(responseBody, clazz);
        }
        return result;
    }

    /**
     * 执行一个Post请求，并返回字符串数据
     *
     * @param url            请求url
     * @param queryParams    queryParameter请求参数
     * @param formdataParams form-data请求参数
     * @param fileParams     文件请求参数
     * @return 返回字符串数据
     * @throws Exception
     */
    public static <T extends KeyValuePair> String execPostFile(String url, List<NameValuePair> queryParams,
                                                               List<NameValuePair> formdataParams,
                                                               List<T> fileParams)
            throws Exception {
        String result = null;
        PostMethod postMethod = new PostMethod(url);
        if (CollectionUtils.isNotEmpty(queryParams)) {
            postMethod.setQueryString(queryParams.toArray(new NameValuePair[]{}));
        }
        try {
            List<Part> parts = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(formdataParams)) {
                formdataParams.forEach(v -> parts.add(new StringPart(v.getName(), v.getValue(), "UTF-8")));
            }
            if (CollectionUtils.isNotEmpty(fileParams)) {
                for (KeyValuePair nameFilePair : fileParams) {
                    FlyingFilePart part = null;
                    if (nameFilePair.getValue() instanceof File) {
                        part = new FlyingFilePart(nameFilePair.getKey(), (File) nameFilePair.getValue());
                    } else if (nameFilePair.getValue() instanceof PartSource) {
                        part = new FlyingFilePart(nameFilePair.getKey(), (PartSource) nameFilePair.getValue());
                    }
                    if (null != part) {
                        parts.add(part);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(parts)) {
                postMethod.setRequestEntity(
                        new MultipartRequestEntity(parts.toArray(new Part[]{}), new HttpMethodParams()));
            }
            httpClient.executeMethod(postMethod);
            if (200 != postMethod.getStatusCode()) {
                throw new Exception(
                        String.format("请求url【%s】未返回200【%d】,ERROR:【%s】【%s】", url, postMethod.getStatusCode(),
                                postMethod.getStatusLine().toString(), postMethod.getResponseBodyAsString()));
            }
            result = postMethod.getResponseBodyAsString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            postMethod.releaseConnection();
        }
        return result;
    }
}
