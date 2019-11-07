package com.github.flyinghe.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.flyinghe.tools.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by FlyingHe on 2019/11/7.
 */
public class XMLUtilsTest {
    @Test
    public void test1() throws Exception {
        StringReader reader =
                new StringReader("<wsQualifyCheck xmlns:ser = \"http://service.send.ws.core.hgxp/\"></wsQualifyCheck>");
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(reader);


    }

    @Test
    public void test2() throws Exception {
        String json = FileUtils.readFileToString(new File("C:\\Users\\FlyingHe\\Desktop\\billjson.json"), "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));

        String xml = XMLUtils.mapToXMLString(map);
        System.out.println(xml);
    }


}
