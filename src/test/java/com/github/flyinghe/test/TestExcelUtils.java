package com.github.flyinghe.test;

import com.github.flyinghe.domain.Student;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.WriteExcelUtils;
import com.github.flyinghe.tools.XLSXWriter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by FlyingHe on 2017/8/26.
 */
public class TestExcelUtils {
    @Test
    public void testBigData() throws Exception {
        List<String> props = CommonUtils
                .arrayToList(new String[]{"name", "age", "price", "prices", "_byte", "_short", "email", "tel", "date"});
        List<String> titles =
                CommonUtils.arrayToList(new String[]{"姓名", "年龄", "价格", "价格们", "字节", "短整型", "电子邮箱", "手机号", "日期"});
        File file = new File("C:\\Users\\FlyingHe\\Desktop", "students.xlsx");
        XLSXWriter<Student> xlsxWriter =
                new XLSXWriter<Student>(file, 500000, props, null, true, "yyyy-MM-dd HH:mm:ss", true, titles, true);
        List<Student> studentList = new ArrayList<>();
        System.out.println("============================Start=========================");
        long s = System.currentTimeMillis();
        for (int i = 0; i < 2000000; i++) {
            Student student = new Student();
            student.setName("name" + i);
            student.set_byte((byte) (i % 127));
            student.set_short((short) (i % 32767));
            student.setAge(i);
            student.setPrice((float) i);
            student.setPrices((double) i);
            student.setEmail("Email:" + i);
            student.setTel("Tel:" + i);
            student.setDate(new Date(new Date().getTime() + i * 1000L));
            studentList.add(student);
            if ((i + 1) % 30000 == 0) {
                xlsxWriter.write(studentList);
                studentList.clear();
                System.out.println("props:" + xlsxWriter.getProperties().size());
                System.out
                        .println("titls:" + (xlsxWriter.getTitles() == null ? "null" : xlsxWriter.getTitles().size()));
                System.out.println(
                        "exclude:" + (xlsxWriter.getExcludeProps() == null ? "null" :
                                xlsxWriter.getExcludeProps().size()));
                System.out.println("realRowInSheet:" + xlsxWriter.getRealRowInSheet());
                System.out.println("realDataInSheet:" + xlsxWriter.getRealDataInSheet());
                System.out.println("realRowInExcel:" + xlsxWriter.getRealRowInExcel());
                System.out.println("realDataInExcel:" + xlsxWriter.getRealDataInExcel());
                System.out.println("allSheetInExcel:" + xlsxWriter.getAllSheetInExcel());
                System.out.println("===================================");
            }
        }
        if (!studentList.isEmpty()) {
            xlsxWriter.write(studentList);
            studentList.clear();
            System.out.println("props:" + xlsxWriter.getProperties().size());
            System.out
                    .println("titls:" + (xlsxWriter.getTitles() == null ? "null" : xlsxWriter.getTitles().size()));
            System.out.println(
                    "exclude:" + (xlsxWriter.getExcludeProps() == null ? "null" :
                            xlsxWriter.getExcludeProps().size()));
            System.out.println("realRowInSheet:" + xlsxWriter.getRealRowInSheet());
            System.out.println("realDataInSheet:" + xlsxWriter.getRealDataInSheet());
            System.out.println("realRowInExcel:" + xlsxWriter.getRealRowInExcel());
            System.out.println("realDataInExcel:" + xlsxWriter.getRealDataInExcel());
            System.out.println("allSheetInExcel:" + xlsxWriter.getAllSheetInExcel());
            System.out.println("===================================");
        }
        xlsxWriter.endWrite();
        long e = System.currentTimeMillis();
        System.out.println("============================Done=========================");
        System.out.println("一共写入数据量(包含标题):" + xlsxWriter.getRealRowInExcel());
        System.out.println("一共写入数据量(不包含标题):" + xlsxWriter.getRealDataInExcel());
        System.out.println("一共写入页数:" + xlsxWriter.getAllSheetInExcel());
        System.out.println("一共耗时:" + (e - s) + "ms");
    }

    @Test
    public void test5() throws Exception {
        XLSXWriter<Student> xlsxWriter =
                new XLSXWriter<>(new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xlsx"),
                        3, null, true, null, null, true, null, true);
        Student student = new Student();
        student.setName("name");
        student.set_byte((byte) 127);
        student.set_short((short) 32767);
        student.setAge(3);
        student.setPrice(0.99F);
        student.setPrices(1.99D);
        student.setEmail("Email:");
        student.setTel("Tel:");
        student.setDate(new Date());
        xlsxWriter.write(student);
        xlsxWriter.write(new Student());
        xlsxWriter.write(new Student());//
        xlsxWriter.write(student);
//        xlsxWriter.write(student);
        xlsxWriter.write(new Student());
        xlsxWriter.endWrite();
        System.out.println("RelRow:" + xlsxWriter.getRealRowInExcel());
        System.out.println("RelData:" + xlsxWriter.getRealDataInExcel());
        System.out.println("AllSheetInExcel:" + xlsxWriter.getAllSheetInExcel());
    }

    @Test
    public void test4() throws Exception {
        List<String> excludeProps = new ArrayList<>();
        excludeProps.add("Price");
        XLSXWriter<Map<String, Object>> xlsxWriter =
                new XLSXWriter<>(new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xlsx"),
                        101, null, true, excludeProps, null, true, null, false);
        Font font1 = xlsxWriter.createFont();
        CellStyle dataCellStyle = xlsxWriter.createCellStyle();
        dataCellStyle.setFont(font1);
        dataCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        dataCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        xlsxWriter.setDefaultCellStyle(dataCellStyle);

        List<Map<String, Object>> list = new ArrayList<>();
        long s = System.currentTimeMillis();
        System.out.println("================Start================");
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> student = new HashMap<>();
            if ((i + 1) % 51 != 0) {
                student.put("name", "name:" + i);
                student.put("_byte", (byte) (i % 127));
                student.put("_short", (short) (i % 32767));
                student.put("age", i);
                student.put("Price", i + 0.99F);
                student.put("Prices", i + 1.99D);
                student.put("date", new Date(new Date().getTime() + i * 24 * 3600000L));
                if ((i + 1) % 30 != 0) {
                    student.put("Email", "Email:" + i);
                    student.put("Tel", "Tel:" + i);
                }

            }
            list.add(student);
            if ((i + 1) % 100 == 0) {
                xlsxWriter.setDefaultCellStyle(dataCellStyle);
                xlsxWriter.write(list);
                list.clear();
                System.out.println("已经写入数据量:" + xlsxWriter.getRealRowInExcel());
            }
        }
        xlsxWriter.endWrite();
        System.out.println("================Done================");
        long e = System.currentTimeMillis();
        System.out.println("一共写入数据量:" + xlsxWriter.getRealRowInExcel());
        System.out.println("Sheet数:" + (xlsxWriter.getAllSheetInExcel()));
        System.out.println("一共耗时:" + (e - s) + "ms");
    }

    @Test
    public void test3() throws Exception {
        List<String> excludeProps = new ArrayList<>();
//        excludeProps.add("date");
        excludeProps.add("email");
        List<String> props = new ArrayList<>();
        props.add("email");
        props.add("date");
        XLSXWriter<Student> xlsxWriter =
                new XLSXWriter<>(new File("C:\\Users\\FlyingHe\\Desktop", "datas2.xlsx")
                        , -1, props, true, excludeProps, null, true, null, false);

        List<Student> list = new ArrayList<>();
        long s = System.currentTimeMillis();
        System.out.println("================Start================");
        for (int i = 0; i < 1000; i++) {
            Student student = new Student();
            student.setName("name" + i);
            student.set_byte((byte) (i % 127));
            student.set_short((short) (i % 32767));
            student.setAge(i);
            student.setPrice(i + 0.99F);
            student.setPrices(i + 1.99D);
            student.setEmail("Email:" + i);
            student.setTel("Tel:" + i);
            student.setDate(new Date(new Date().getTime() + i * 1000L));
            list.add(student);
            if ((i + 1) % 100 == 0) {
                xlsxWriter.write(list);
                list.clear();
                System.out.println("已经写入数据量:" + (i + 1));
            }
        }
        xlsxWriter.endWrite();
        System.out.println("================Done================");
        long e = System.currentTimeMillis();
        System.out.println("一共写入数据量:" + xlsxWriter.getRealRowInExcel());
        System.out.println("一共耗时:" + (e - s) + "ms");
    }

    @Test
    public void test1() throws Exception {
        List<Map<String, Object>> datas = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "name" + i);
            data.put("age", "age" + i);
            data.put("nickname", "nickname" + i);
            data.put("sex", "sex" + i);
            data.put("look", "look" + i);
            data.put("date", new Date(new Date().getTime() + i * 24 * 3600000L));
            datas.add(data);
        }
        File file = new File("C:\\Users\\FlyingHe\\Desktop", "datas.xlsx");
        WriteExcelUtils.writeWorkBook(file, WriteExcelUtils.XLSX, datas, 5000);
    }

    @Test
    public void test2() throws Exception {
        Object value = true;
//        boolean _value = true;
//        value = _value;
        if (value instanceof Boolean) {
            System.out.println(true);
        } else {
            System.out.println(false);
        }
    }
}
