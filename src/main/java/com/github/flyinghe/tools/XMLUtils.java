package com.github.flyinghe.tools;

import org.apache.commons.collections.MapUtils;
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

    /**
     * 将Map数据转化成XML并返回(不带根节点)
     *
     * @param datas Map数据
     * @return 转化成的XML
     * @throws Exception
     */
    public static String mapToXMLString(Map<String, Object> datas) throws Exception {
        Document document = mapToXML(null, datas);
        Element root = document.getRootElement();
        StringBuilder sb = new StringBuilder();
        Iterator iterator = root.elementIterator();
        while (iterator.hasNext()) {
            Element nextEle = (Element) iterator.next();
            sb.append(nextEle.asXML());
        }
        return sb.toString();
    }

    /**
     * 将Map数据转化成XML并返回(带根节点)
     *
     * @param rootStr 根节点,若为空或者不合法则默认为root
     * @param datas   Map数据
     * @return 转化成的XML
     * @throws Exception
     */
    public static String mapToXMLString(String rootStr, Map<String, Object> datas) throws Exception {
        Document document = mapToXML(rootStr, datas);
        return document.getRootElement().asXML();
    }

    /**
     * 把Map数据追加到XML根节点rootStr中
     *
     * @param rootStr 根节点,若为空或者不合法则默认为root
     * @param datas   需要追加的数据
     * @return 生成的XML文档
     * @throws Exception
     */
    public static Document mapToXML(String rootStr, Map<String, Object> datas) throws Exception {
        rootStr = StringUtils.isNotBlank(rootStr) ? rootStr.trim() : "<root></root>";
        Object obj = null;
        Reader reader = new StringReader(rootStr);
        Document document = null;
        try {
            document = saxReader.read(reader);
        } catch (DocumentException de) {
            reader.close();
            reader = new StringReader("<root></root>");
            document = saxReader.read(reader);
        }
        document.setXMLEncoding("UTF-8");
        Element root = document.getRootElement();
        mapToElement(root, datas);
        reader.close();
        return document;
    }

    /**
     * 把Map数据追加到XML节点element中
     *
     * @param element 被追加的节点
     * @param datas   需要追加的数据
     */
    public static void mapToElement(Element element, Map<String, Object> datas) {
        if (element == null || MapUtils.isEmpty(datas)) {
            return;
        }
        for (Map.Entry<String, Object> entry : datas.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (Ognl.isEmpty(value)) {
                element.addElement(key);
            } else if (value instanceof Map) {
                mapToElement(element.addElement(key), (Map<String, Object>) value);
            } else if (value instanceof List) {
                for (Object listObj : (List) value) {
                    if (Ognl.isEmpty(listObj)) {
                        element.addElement(key);
                    } else if (listObj instanceof Map) {
                        mapToElement(element.addElement(key), (Map<String, Object>) listObj);
                    } else {
                        element.addElement(key).addText(listObj.toString());
                    }
                }
            } else {
                element.addElement(key).addText(value.toString());
            }
        }
    }
}
