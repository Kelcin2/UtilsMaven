package com.github.flyinghe.test;

import com.github.flyinghe.tools.http.HttpUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

/**
 * Created by FlyingHe on 2019/11/7.
 */
public class HttpUtilsTest {

    @Test
    public void test1() throws Exception {
        String url =
                "http://10.0.6.11:7003/ApprovalSB/Qualification/Gxp/ProxyServices/ApGxpQualifyCheckSyncSoapProxy?wsdl";
        String paramBody = FileUtils.readFileToString(new File("C:\\Users\\FlyingHe\\Desktop\\资质证校验.xml"));
        String result = new HttpUtils(null, LoggerFactory.getLogger(HttpUtilsTest.class))
                .execPost(url, HttpUtils.getAuthHeaderList("esb_ebs01", "gkht_7890"), HttpUtils.CONTENT_TYPE_XML, null,
                        paramBody);
        System.out.println(result);
    }

    @Test
    public void test2() throws Exception {
        String url = "http://127.0.0.1:11434/v1/chat/completions";
        String payload = "{\"model\":\"llama3\",\"messages\":[{\"role\":\"user\",\"content\":\"你很优秀哦?\"}],\"stream\":true}";
        String result = new HttpUtils(null, LoggerFactory.getLogger(HttpUtilsTest.class)).execPost(url, new HashMap<>(),
                payload);
        System.out.println(result);
    }
}
