package com.github.flyinghe.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.flyinghe.tools.date.DateUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * @author Flying
 */
public class CommonUtils {
    private CommonUtils() {}

    /**
     * 返回一个随机UUID字符串，由数字和大写字母组成的32位字符串
     *
     * @return 返回一个随机UUID字符串
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * 将一个Map对象转换成一个Bean对象
     *
     * @param property 一个Map对象，封装了相应的Bean中的属性
     * @param clazz    需要转换成的Bean
     * @return 返回一个封装好的Bean类实例，封装失败返回null
     */
    public static <T> T toBean(Map<String, ?> property, Class<T> clazz) {
        T bean = null;
        try {
            bean = clazz.newInstance();
            BeanUtils.populate(bean, property);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    /**
     * 将一个JavaBean对象的所有属性封装在Map中并返回，
     * 注意只能将拥有Getter方法的属性才能取出并封装到Map中
     *
     * @param bean 需要转换的Bean对象
     * @return 返回一个Map , 失败返回null
     */
    public static <T> Map<String, Object> toMap(T bean) {
        Map<String, Object> map = null;
        try {
            map = PropertyUtils.describe(bean);
            map.remove("class");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * 将一个Bean转化成一个Map(所有属性均为Map或者List)
     *
     * @param bean 待转化的Bean
     * @return 返回转化成的Map
     * @throws Exception
     */
    public static Map<String, Object> beanToMap(Object bean) throws Exception {
        if (Ognl.isEmpty(bean)) {
            return new HashMap<>();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(objectMapper.writeValueAsString(bean),
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
    }

    /**
     * 将一个Map转化成一个Bean
     *
     * @param obj   Map对象
     * @param clazz 被转换成的Bean的Class对象
     * @param <T>
     * @return 被转化成的Bean
     * @throws Exception
     */
    public static <T> T mapToBean(Object obj, Class<T> clazz) throws Exception {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof Map)) {
            throw new Exception("obj必须为java.util.Map类型");
        }
        T bean = null;
        bean = clazz.newInstance();
        Set<String> properties = PropertyUtils.describe(bean).keySet();
        properties.remove("class");
        Map<String, Object> map = (Map<String, Object>) obj;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (!properties.contains(key) || null == ReflectionUtils.findField(clazz, key) || null == value) {
                    continue;
                }

                if (ReflectionUtils.findField(clazz, key).getGenericType() instanceof TypeVariable) {
                    //若该属性是一个泛型类型,则直接赋值
                    PropertyUtils.setSimpleProperty(bean, key, value);
                } else if (value instanceof Map) {
                    PropertyUtils.setSimpleProperty(bean, key,
                            mapToBean(value, ReflectionUtils.findField(clazz, key).getType()));
                } else if (value instanceof List) {
                    Class<?> propertyClazz = ReflectionUtils.findField(clazz, key).getType();
                    if (!propertyClazz.getName().equalsIgnoreCase("java.util.List")) {
                        continue;
                    }
                    List<?> values = (List<?>) value;
                    if (CollectionUtils.isEmpty(values)) {
                        continue;
                    }
                    Object valueNested = values.get(0);
                    //判断这个集合元素的类型
                    Type actualTypeArgument =
                            ((ParameterizedType) ReflectionUtils.findField(clazz, key).getGenericType())
                                    .getActualTypeArguments()[0];
                    List propertyValue = new ArrayList<>();
                    if (actualTypeArgument instanceof TypeVariable) {
                        //若该List的泛型未指定具体类型,则直接赋值
                        propertyValue.addAll(values);
                        PropertyUtils.setSimpleProperty(bean, key, propertyValue);
                    } else if (valueNested instanceof Map) {
                        Class propertyNestedClazz = (Class) actualTypeArgument;
                        for (int i = 0; i < values.size(); i++) {
                            propertyValue.add(mapToBean(values.get(i), propertyNestedClazz));
                        }
                        PropertyUtils.setSimpleProperty(bean, key, propertyValue);
                    } else if (((Class) actualTypeArgument).getName().equalsIgnoreCase("java.util.Date") &&
                            (valueNested instanceof String || StringUtils.isNumeric(valueNested.toString()))) {
                        for (int i = 0; i < values.size(); i++) {
                            propertyValue.add(DateUtils.strToDate(values.get(i).toString()));
                        }
                        PropertyUtils.setSimpleProperty(bean, key, propertyValue);
                    } else {
                        PropertyUtils.setSimpleProperty(bean, key, values);
                    }
                } else if (
                        ReflectionUtils.findField(clazz, key).getType().getName().equalsIgnoreCase("java.util.Date") &&
                                (value instanceof String || StringUtils.isNumeric(value.toString()))) {
                    PropertyUtils.setSimpleProperty(bean, key, DateUtils.strToDate(value.toString()));
                } else {
                    BeanUtils.setProperty(bean, key, value);
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        return bean;
    }

    /**
     * 将一个List转化成包含具体Bean的List并返回
     *
     * @param obj   List对象
     * @param clazz 最终返回List的元素Class对象
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> listToBean(Object obj, Class<T> clazz) throws Exception {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof List)) {
            throw new Exception("obj必须为java.util.List类型");
        }
        List<T> result = new ArrayList<>();
        List<Map<String, Object>> objList = (List<Map<String, Object>>) obj;
        if (CollectionUtils.isNotEmpty(objList)) {
            for (Map<String, Object> map : objList) {
                T bean = mapToBean(map, clazz);
                if (null != bean) {
                    result.add(bean);
                }
            }
        }
        return result;
    }

    /**
     * 将Bean的属性值修改为Map中对应的值
     *
     * @param property 一个Map对象，封装了相应的Bean类中的属性
     * @param bean     需要修改的Bean类
     * @return 返回一个属性已修改的Bean类实例
     */
    public static <T> T modifyBean(Map<String, ?> property, T bean) {
        try {
            BeanUtils.populate(bean, property);
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 复制一个对象并返回，属于浅度克隆，对象所属类必须符合JavaBean规范
     *
     * @param bean 被复制的对象
     * @return 返回一个被复制对象的一个副本
     */
    public static <T> T cloneBean(T bean) {
        try {
            T newBean = (T) BeanUtils.cloneBean(bean);
            return newBean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭输入输出流
     *
     * @param in  输入流
     * @param out 输出流
     */
    public static void closeIOStream(InputStream in, OutputStream out) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据HTTP请求获取客户端IP地址
     *
     * @param request
     * @return IP
     */
    public static String getIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 将一个[]转换成List并输出,顺序与原[]一致
     *
     * @param array []数组
     * @param <T>   任意对象类型(非原始类型)
     * @return 转换后的List, 若array==null,返回null
     */
    public static <T> List<T> arrayToList(T[] array) {
        List<T> list = null;
        if (array != null) {
            list = new ArrayList<>();
            for (T e : array) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * 将一个List数组转换成[]
     *
     * @param list  List数组
     * @param array List数组元素存储在此[]中
     * @param <T>
     * @return 转换后的[], 若list==null或者array==null返回Null
     * @see List#toArray(Object[])
     */
    public static <T> T[] listToArray(List<T> list, T[] array) {
        if (list == null || array == null) {
            return null;
        }

        return list.toArray(array);
    }

    /**
     * 将一张图片转换成指定格式的Base64字符串编码
     *
     * @param image      指定一张图片
     * @param formatName 图片格式名,如gif,png,jpg,jpeg等,默认为jpeg
     * @return 返回编码好的字符串, 失败返回null
     */
    public static String imgToBase64Str(BufferedImage image, String formatName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String base64Str = null;
        try {
            ImageIO.write(image, formatName == null ? "jpeg" : formatName, baos);
            byte[] bytes = baos.toByteArray();
            base64Str = CommonUtils.bytesToBase64Str(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            CommonUtils.closeIOStream(null, baos);
        }
        return base64Str;
    }


    /**
     * 将指定的字节数组编码成Base64字符串
     *
     * @param bytes 指定字节数组
     * @return 返回由指定字节数组编码成的Base64字符串
     */
    public static String bytesToBase64Str(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return null;
        }
        Base64.Encoder encoder = Base64.getEncoder();
        String base64Str = encoder.encodeToString(bytes);
        return base64Str;
    }

    /**
     * 将一张图片转换成指定格式的Base64字符串编码,带类似于"data:image/png;base64,"前缀,
     * 返回的字符串可直接放入HTML img标签src属性中显示图片
     *
     * @param image      指定一张图片
     * @param formatName 图片格式名,如gif,png,jpg,jpeg等,默认为jpeg
     * @return 返回编码好的字符串, 失败返回null
     */
    public static String imgToBase64StrWithPrefix(BufferedImage image, String formatName) {
        formatName = formatName != null ? formatName : "jpeg";
        return String.format("data:image/%s;base64,%s", formatName, imgToBase64Str(image, formatName));
    }

    /**
     * 将指定的图片字节数组进行Base64编码并返回
     *
     * @param imageBytes 指定的图片字节数组
     * @param formatName 图片格式名,如gif,png,jpg,jpeg等,默认为jpeg
     * @return 编码后的Base64字符串
     */
    public static String imgToBase64StrWithPrefix(byte[] imageBytes, String formatName) {
        formatName = formatName != null ? formatName : "jpeg";
        return String.format("data:image/%s;base64,%s", formatName, CommonUtils.bytesToBase64Str(imageBytes));
    }

    /**
     * 将Base64字符串转化成Image对象
     *
     * @param imgStr 指定Base64编码的图片字符串
     * @return 返回转化后的Image对象, 失败返回Null
     */
    public static BufferedImage base64StrToImg(String imgStr) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(imgStr);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] < 0) {
                bytes[i] += 256;
            }
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage image = null;
        try {
            image = ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            CommonUtils.closeIOStream(bais, null);
        }
        return image;
    }

    /**
     * 判断一个请求是否是Ajax请求
     *
     * @param request Http请求
     * @return 是Ajax请求返回true, 否则返回false
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String ajaxHeader = request.getHeader("X-Requested-With");
        return StringUtils.isNotBlank(ajaxHeader) && "XMLHttpRequest".equalsIgnoreCase(ajaxHeader);
    }

    /**
     * 将一个对象序列化成字符串
     *
     * @param obj 被序列化的对象,该对象必须实现{@link java.io.Serializable}接口
     * @param <T>
     * @return 返回对象被序列化后的字符串
     */
    public static <T> String serialize(T obj) {
        if (obj == null) {
            return "";
        }
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return CommonUtils.bytesToBase64Str(bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("serialize session error", e);
        } finally {
            CommonUtils.closeIOStream(null, bos);
            CommonUtils.closeIOStream(null, oos);
        }
    }

    /**
     * 将一个序列化字符串反序列化成对象
     *
     * @param str 序列化字符串
     * @param <T>
     * @return 返回被反序列化后的对象
     */
    public static <T> T deserialize(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            bis = new ByteArrayInputStream(
                    decoder.decode(str));
            ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("deserialize session error", e);
        } finally {
            CommonUtils.closeIOStream(bis, null);
            CommonUtils.closeIOStream(ois, null);
        }
    }


}
