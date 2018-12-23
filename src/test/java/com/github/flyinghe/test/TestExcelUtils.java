package com.github.flyinghe.test;

import com.github.flyinghe.depdcy.ExcelHandler;
import com.github.flyinghe.domain.Student;
import com.github.flyinghe.exception.ReadExcelException;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.WriteExcelUtils;
import com.github.flyinghe.tools.XLSXReader;
import com.github.flyinghe.tools.XLSXWriter1;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
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
        XLSXWriter1<Student> xlsxWriter1 =
                new XLSXWriter1<Student>(file, 500000, props, null, true, "yyyy-MM-dd HH:mm:ss", true, titles, true);
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
                xlsxWriter1.write(studentList);
                studentList.clear();
                System.out.println("props:" + xlsxWriter1.getProperties().size());
                System.out
                        .println("titls:" + (xlsxWriter1.getTitles() == null ? "null" : xlsxWriter1.getTitles().size()));
                System.out.println(
                        "exclude:" + (xlsxWriter1.getExcludeProps() == null ? "null" :
                                xlsxWriter1.getExcludeProps().size()));
                System.out.println("realRowInSheet:" + xlsxWriter1.getRealRowInSheet());
                System.out.println("realDataInSheet:" + xlsxWriter1.getRealDataInSheet());
                System.out.println("realRowInExcel:" + xlsxWriter1.getRealRowInExcel());
                System.out.println("realDataInExcel:" + xlsxWriter1.getRealDataInExcel());
                System.out.println("allSheetInExcel:" + xlsxWriter1.getAllSheetInExcel());
                System.out.println("===================================");
            }
        }
        if (!studentList.isEmpty()) {
            xlsxWriter1.write(studentList);
            studentList.clear();
            System.out.println("props:" + xlsxWriter1.getProperties().size());
            System.out
                    .println("titls:" + (xlsxWriter1.getTitles() == null ? "null" : xlsxWriter1.getTitles().size()));
            System.out.println(
                    "exclude:" + (xlsxWriter1.getExcludeProps() == null ? "null" :
                            xlsxWriter1.getExcludeProps().size()));
            System.out.println("realRowInSheet:" + xlsxWriter1.getRealRowInSheet());
            System.out.println("realDataInSheet:" + xlsxWriter1.getRealDataInSheet());
            System.out.println("realRowInExcel:" + xlsxWriter1.getRealRowInExcel());
            System.out.println("realDataInExcel:" + xlsxWriter1.getRealDataInExcel());
            System.out.println("allSheetInExcel:" + xlsxWriter1.getAllSheetInExcel());
            System.out.println("===================================");
        }
        xlsxWriter1.endWrite();
        long e = System.currentTimeMillis();
        System.out.println("============================Done=========================");
        System.out.println("一共写入数据量(包含标题):" + xlsxWriter1.getRealRowInExcel());
        System.out.println("一共写入数据量(不包含标题):" + xlsxWriter1.getRealDataInExcel());
        System.out.println("一共写入页数:" + xlsxWriter1.getAllSheetInExcel());
        System.out.println("一共耗时:" + (e - s) + "ms");
    }

    @Test
    public void test5() throws Exception {
        XLSXWriter1<Student> xlsxWriter1 =
                new XLSXWriter1<>(new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xlsx"),
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
        xlsxWriter1.write(student);
        xlsxWriter1.write(new Student());
        xlsxWriter1.write(new Student());//
        xlsxWriter1.write(student);
//        xlsxWriter1.write(student);
        xlsxWriter1.write(new Student());
        xlsxWriter1.endWrite();
        System.out.println("RelRow:" + xlsxWriter1.getRealRowInExcel());
        System.out.println("RelData:" + xlsxWriter1.getRealDataInExcel());
        System.out.println("AllSheetInExcel:" + xlsxWriter1.getAllSheetInExcel());
    }

    @Test
    public void test4() throws Exception {
        List<String> excludeProps = new ArrayList<>();
        excludeProps.add("Price");
        XLSXWriter1<Map<String, Object>> xlsxWriter1 =
                new XLSXWriter1<>(new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xlsx"),
                        101, null, true, excludeProps, null, true, null, false);
        Font font1 = xlsxWriter1.createFont();
        CellStyle dataCellStyle = xlsxWriter1.createCellStyle();
        dataCellStyle.setFont(font1);
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xlsxWriter1.setDefaultCellStyle(dataCellStyle);

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
                xlsxWriter1.setDefaultCellStyle(dataCellStyle);
                xlsxWriter1.write(list);
                list.clear();
                System.out.println("已经写入数据量:" + xlsxWriter1.getRealRowInExcel());
            }
        }
        xlsxWriter1.endWrite();
        System.out.println("================Done================");
        long e = System.currentTimeMillis();
        System.out.println("一共写入数据量:" + xlsxWriter1.getRealRowInExcel());
        System.out.println("Sheet数:" + (xlsxWriter1.getAllSheetInExcel()));
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
        XLSXWriter1<Student> xlsxWriter1 =
                new XLSXWriter1<>(new File("C:\\Users\\FlyingHe\\Desktop", "datas2.xlsx")
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
                xlsxWriter1.write(list);
                list.clear();
                System.out.println("已经写入数据量:" + (i + 1));
            }
        }
        xlsxWriter1.endWrite();
        System.out.println("================Done================");
        long e = System.currentTimeMillis();
        System.out.println("一共写入数据量:" + xlsxWriter1.getRealRowInExcel());
        System.out.println("一共耗时:" + (e - s) + "ms");
    }

    @Test
    public void test1() throws Exception {
        List<Map<String, Object>> datas = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "name" + i);
            data.put("date", new Date(new Date().getTime() + i * 24 * 3600000L));
            data.put("price", 0.1D);
            data.put("boolean", true);
            datas.add(data);
        }
        File file = new File("C:\\Users\\FlyingHe\\Desktop", "datas.xlsx");
        WriteExcelUtils.writeWorkBook(file, WriteExcelUtils.XLSX, datas, 5000);
    }

    @Test
    public void test2() throws Exception {

        OPCPackage p = OPCPackage.open(new File("C:\\Users\\FlyingHe\\Desktop", "datas.xlsx"), PackageAccess.READ);
        XLSXReader reader = null;
        reader = new XLSXReader(p, 2, 500, new ExcelHandler() {
            @Override
            public void callback(int currentRowInSheet, int currentSheetInExcel, int realRowInSheet, int realRowInExcel,
                                 int allSheetInExcel, List<String> titles, List<String> columns,
                                 List<Map<String, Object>> datas) throws ReadExcelException {
                try {
                    p.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
