package com.github.flyinghe.tools;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class XMLUtils {
    private static SAXReader saxReader;

    static {
        saxReader = new SAXReader();
    }

    /**
     * 将一个xml文本转化成一个String并返回,若最终转化成的是其他类型则返回null,
     * 适用于"&lt;interface&gt;this is a test content&lt;/interface&gt;"这种简单的xml内容
     *
     * @param xml XML格式的文本字符串
     * @return
     * @throws Exception
     */
    public static String xmlToString(String xml) throws Exception {
        Object result = xmlToObj(xml);
        return result instanceof String ? (String) result : null;
    }


    /**
     * 将一个xml文本转化成一个Map&lt;String,Object&gt;对象并返回,若最终转化成的是其他类型则返回null,
     * 适用于"&lt;interface&gt;&lt;test&gt;this is a test content&lt;/test&gt;&lt;/interface&gt;"
     * 这种较复杂的嵌套xml内容
     *
     * @param xml XML格式的文本字符串
     * @return
     * @throws Exception
     */
    public static Map<String, Object> xmlToMap(String xml) throws Exception {
        Object result = xmlToObj(xml);
        return result instanceof Map ? (Map<String, Object>) result : null;
    }

    /**
     * 将一个XML格式的文本转化成一个Object对象返回
     *
     * @param xml XML格式的文本字符串
     * @return 返回对象类型可能是String, 或者Map&lt;String,Object&gt;对象
     * @throws Exception
     */
    public static Object xmlToObj(String xml) throws Exception {
        xml = StringUtils.isNotBlank(xml) ? xml.trim() : "";
        Object obj = null;
        Reader reader = new StringReader(xml);
        Document document = null;
        try {
            document = saxReader.read(reader);
        } catch (DocumentException de) {
            reader.close();
            reader = new StringReader(String.format("<root>%s</root>", xml));
            document = saxReader.read(reader);
        }
        Element root = document.getRootElement();
        obj = xmlElementToObj(root);
        reader.close();
        return obj;
    }

    /**
     * 将一个节点元素的内容转换成Object对象并返回
     *
     * @param element 一个XML节点元素
     * @return 若该节点元素没有子节点则返回该节点的文本内容(字符串), 若有直接子节点则返回一个Map&lt;String,Object&gt;对象,
     * 若有相同标签的子节点则会以该子节点名称为Key,value是一个List&lt;Object&gt;类型的值
     */
    public static Object xmlElementToObj(Element element) {
        Iterator eleIter = element.elementIterator();
        if (!eleIter.hasNext()) {
            return StringUtils.isBlank(element.getStringValue()) ? "" : element.getStringValue().trim();
        }
        Map<String, Object> result = new HashMap<>();
        while (eleIter.hasNext()) {
            Element nextEle = (Element) eleIter.next();
            if (result.containsKey(nextEle.getName())) {
                if (!(result.get(nextEle.getName()) instanceof List)) {
                    Object oldValue = result.put(nextEle.getName(), new ArrayList<Object>());
                    ((List<Object>) result.get(nextEle.getName())).add(oldValue);
                }
                ((List<Object>) result.get(nextEle.getName())).add(xmlElementToObj(nextEle));
            } else {
                result.put(nextEle.getName(), xmlElementToObj(nextEle));
            }
        }
        return result;
    }
}
