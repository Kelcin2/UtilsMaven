package com.github.flyinghe.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.http.HttpUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Created by FlyingHe on 2019/11/7.
 */
public class HttpUtilsTest {
    @Test
    public void testHttpClient() throws Exception {
        HttpClient httpClient = new HttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        String url =
                "http://10.0.6.11:7003/ApprovalSB/Qualification/Gxp/ProxyServices/ApGxpQualifyCheckSyncSoapProxy?wsdl";
        String paramBody = FileUtils.readFileToString(new File("C:\\Users\\FlyingHe\\Desktop\\资质证校验.xml"));


        String result = null;
        PostMethod postMethod = new PostMethod(url);
        try {
            StringRequestEntity stringRequestEntity =
                    new StringRequestEntity(paramBody != null ? paramBody : "", "application/xml", "UTF-8");
            postMethod.setRequestEntity(stringRequestEntity);
            postMethod.setRequestHeader("Authorization", "Basic " +
                    CommonUtils.bytesToBase64Str("esb_ebs01:gkht_7890".getBytes(StandardCharsets.UTF_8)));
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
        System.out.println(result);

    }

    @Test
    public void test1() throws Exception {
        String url =
                "http://10.0.6.11:7003/ApprovalSB/Qualification/Gxp/ProxyServices/ApGxpQualifyCheckSyncSoapProxy?wsdl";
        String paramBody = FileUtils.readFileToString(new File("C:\\Users\\FlyingHe\\Desktop\\资质证校验.xml"));
        String result = new HttpUtils()
                .execPost(url, HttpUtils.getAuthHeaderList("esb_ebs01", "gkht_7890"), HttpUtils.CONTENT_TYPE_XML, null,
                        paramBody);
        System.out.println(result);
    }
}
