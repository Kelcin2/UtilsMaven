package com.flying.dao.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Created by FlyingHe on 2018/2/3.
 */
public class Ognl {
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj instanceof String) {
            if (((String) obj).trim().length() == 0) {
                return true;
            }
        } else if (obj instanceof Collection) {
            if (((Collection<?>) obj).isEmpty()) {
                return true;
            }
        } else if (obj.getClass().isArray()) {
            if (Array.getLength(obj) == 0) {
                return true;
            }
        } else if (obj instanceof Map) {
            if (((Map<?, ?>) obj).isEmpty()) {
                return true;
            }
        } else {
            return false;
        }

        return false;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
