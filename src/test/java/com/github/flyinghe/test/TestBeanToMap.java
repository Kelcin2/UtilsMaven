package com.github.flyinghe.test;

import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.WriteExcelUtils;
import com.github.flyinghe.domain.Pet;
import com.github.flyinghe.domain.User;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.*;
import java.util.*;

/**
 * Created by Flying on 2016/5/28.
 */
public class TestBeanToMap {

    @Test
    public void test4() throws Exception {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        Integer[] ints = CommonUtils.listToArray(list, new Integer[list.size()]);
        System.out.println(ints);
    }

    @Test
    public void test3() throws Exception {
        Map<String, Object> map = new HashedMap();
        map.put("name", "nn");
        map.put("sex", 12);
        map.put("error", "error");
        map.put("birth", new Date());
        User user = CommonUtils.toBean(map, User.class);
        System.out.println(user);
    }

    @Test
    public void test1() throws Exception {
        User user = new User("小红", "女", new Date());
        //        user.setPet(new Pet("小猫", new Date()));
        user.setAge(90);
        user.setAddress("uiijji");
        Map<String, Object> map = CommonUtils.toMap(user);
        for (String s : map.keySet()) {
            System.out.println(s + ":" + map.get(s));
        }
    }

    @Test
    public void test2() throws IOException, WriteExcelException {
        User user = new User("小红", "女", new Date());
        user.setPet(new Pet("小猫", new Date()));
        user.setAge(90);
        user.setAddress("uiijji");
        List<User> list = new ArrayList<User>();
        list.add(user);
        Workbook workbook = new HSSFWorkbook();
        //        workbook
        File file = new File("C:\\Users\\h_kx1\\Desktop\\test.xls");
        OutputStream os = new FileOutputStream(file);

        WriteExcelUtils.writeWorkBook(workbook, list);
        WriteExcelUtils.writeWorkBookToExcel(workbook, os);
        CommonUtils.closeIOStream(null, os);
    }

}
