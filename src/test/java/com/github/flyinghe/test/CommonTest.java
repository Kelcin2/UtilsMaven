package com.github.flyinghe.test;

import com.github.flyinghe.domain.Person;
import com.github.flyinghe.domain.Pet;
import com.github.flyinghe.domain.TestObj;
import com.github.flyinghe.domain.User;
import com.github.flyinghe.exception.ReadExcelException;
import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.ExcelWriter;
import com.github.flyinghe.tools.XLSXReader;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by FlyingHe on 2016/11/21.
 */
public class CommonTest {
    private File file1 = new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xlsx");
    private File file2 = new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xls");

    @Test
    public void test1() throws WriteExcelException {
        User user = new User();
        user.setName("user");
        user.setSex("male");
        ExcelWriter<User> excelWriter = new ExcelWriter<>(-1, ExcelWriter.XLSX);

        XSSFCellStyle cellStyle = excelWriter.createXLSXCellStyle();
        cellStyle.setFillForegroundColor(excelWriter.createXLSXColor(202, 234, 206));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        excelWriter.getTitleCellStyleMapping().put("name", cellStyle);
        excelWriter.write(user).endWrite(this.file1);
    }

    private User producceUser() {
        User user = new User();
        user.setSex("male");
        user.setName("testUser");
        user.setBirth(new Date());
        Pet pet = new Pet();
        pet.setBirth(new Date());
        pet.setName("testPet");
        user.setPet(pet);
        return user;
    }

    @Test
    public void test2() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        TestObj testObj = new TestObj();
        PropertyUtils.setSimpleProperty(testObj, "stringType", "stringType");
        System.out.println(PropertyUtils.getSimpleProperty(testObj, "stringType"));
        PropertyUtils.setSimpleProperty(testObj, "nullType", null);
        System.out.println(PropertyUtils.getSimpleProperty(testObj, "nullType"));
        testObj.getListType().add(this.producceUser());
        PropertyUtils.setIndexedProperty(testObj, "listType[0]", this.producceUser());
        System.out.println(PropertyUtils.getIndexedProperty(testObj, "listType[0]"));
        System.out.println(PropertyUtils.getIndexedProperty(testObj, "listType", 0));


    }

    @Test
    public void test3() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Map<String, Object> data = new HashMap<>();
        data.put("stringType", "StringType");
        data.put("charType", 'ʒ');
        data.put("dateType", new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime((Date) data.get("dateType"));
        data.put("calendarType", calendar);
        data.put("booleanType", "是");
        data.put("byteType", "100");
        data.put("shortType", "100");
        data.put("integerType", "100");
        data.put("longType", "100");
        data.put("floatType", "5.66777");
        data.put("doubleType", 7.56733F);
        data.put("nullType", null);

        TestObj testObj = CommonUtils.toBean(data, TestObj.class);
        System.out.println(testObj);

    }

    @Test
    public void test4()
            throws ReadExcelException {
        XLSXReader xlsxReader = XLSXReader.readExcel(new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xlsx"));
        List<Map<String, Object>> datas = xlsxReader.getDatas();
        BigDecimal bd = new BigDecimal(6.45645756745645E+21);
        System.out.println(datas);
    }

    @Test
    public void test5() {
        Field age = ReflectionUtils.findField(Person.class, "age1");
        System.out.println(age);
    }

    @Test
    public void test6() throws Exception {
        Person person = CommonUtils.mapToBean(new HashMap<String, Object>() {{
            put("age", 111);
            put("address", "成都市天府广场");
            put("birthday", "2019-11-16 12:04:55.666");
            put("birthdaies", new ArrayList<String>() {{
                add("2019-11-16 12:04:55.666");
                add("2019-11-17 12:09:55");
            }});
        }}, Person.class);
        System.out.println(person);

        Person person1 = CommonUtils.mapToBean(new Person() {{
            this.setAge(111);
            this.setAddress("成都市天府广场");
            this.setBirthday(new Date());
            this.setBirthdaies(new ArrayList<Date>() {{
                add(new Date());
                add(new Date());
            }});
        }}, Person.class);
        System.out.println(person1);
    }

    @Test
    public void test7() {
        Map map = new HashMap();
        map.put("name", "name");
        Map pet = new HashMap();
        pet.put("name", "pet");
        pet.put("birth", new Date());
        map.put("pet", pet);
        User user = CommonUtils.toBean(map, User.class);
        System.out.println(user);
    }

    @Test
    public void test8() throws Exception {
        Person person = CommonUtils.mapToBean(new HashMap<String, Object>() {{
            this.put("birthdaies", "2020-02-04 19:09:55");
            this.put("ins", "1,2,3,4");
        }}, Person.class, new CommonUtils.MapToBeanCB() {
            @Override
            public Object[] getValue(String key, Object value) {
                if ("ins".equals(key)) {
                    if (null != value) {
                        List<Integer> result = new ArrayList<>();
                        for (String i : value.toString().split(",")) {
                            try {
                                result.add(Integer.valueOf(i));
                            } catch (Exception e) {

                            }
                        }
                        value = result;
                    }
                }
                return new Object[]{key, value};
            }
        });
        System.out.println(person);
    }
}
