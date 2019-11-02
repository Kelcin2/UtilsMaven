package com.github.flyinghe.tools;

/**
 * Created by FlyingHe on 2019/10/25.
 */
public class ValidateUtils {
    /**
     * 传入一个对象数组,若任意一个对象为空则返回true,否则返回false
     *
     * @param objs 校验空值的数组
     * @return
     */
    public static boolean isAnyObjBlank(Object... objs) {
        boolean result = false;
        if (Ognl.isEmpty(objs)) {
            result = true;
        } else {
            for (Object obj : objs) {
                if (Ognl.isEmpty(obj)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 传入一个对象数组,若任意一个对象不为空则返回true,否则返回false
     *
     * @param objs 校验空值的数组
     * @return
     */
    public static boolean isAnyObjNotBlank(Object... objs) {
        boolean result = false;
        if (Ognl.isNotEmpty(objs)) {
            for (Object obj : objs) {
                if (Ognl.isNotEmpty(obj)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
