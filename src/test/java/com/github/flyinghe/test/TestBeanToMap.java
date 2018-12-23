package com.github.flyinghe.test;

import com.github.flyinghe.domain.Pet;
import com.github.flyinghe.domain.User;
import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.WriteExcelUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Flying on 2016/5/28.
 */
public class TestBeanToMap {

    @Test
    public void test4() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        cell0.setCellValue("test");
        cell0.setCellStyle(cellStyle);

        Cell cell1 = row.createCell(1);
        cellStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setBold(false);
        font.setColor(IndexedColors.WHITE.getIndex());
        cellStyle.setFont(font);
        cell1.setCellValue("test1");
        cell1.setCellStyle(cellStyle);

        WriteExcelUtils.writeWorkBookToExcel(workbook,
                new FileOutputStream(new File("C:\\Users\\FlyingHe\\Desktop", "test.xlsx")));


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
        System.out.println(null instanceof CellStyle);
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
        File file = new File("C:\\Users\\FlyingHe\\Desktop\\test.xls");
        OutputStream os = new FileOutputStream(file);

        WriteExcelUtils.writeWorkBook(workbook, list);
        WriteExcelUtils.writeWorkBookToExcel(workbook, os);
        CommonUtils.closeIOStream(null, os);
    }

}
