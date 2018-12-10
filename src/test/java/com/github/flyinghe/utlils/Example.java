package com.github.flyinghe.utlils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Assert;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by FlyingHe on 2017/8/26.
 */
public class Example {
    public static void main(String[] args) throws Throwable {
        SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
        Sheet sh = wb.createSheet();
        for (int rownum = 0; rownum < 1000; rownum++) {
            Row row = sh.createRow(rownum);
            Row row1 = sh.createRow(rownum);
            for (int cellnum = 0; cellnum < 10; cellnum++) {
                Cell cell = row.createCell(cellnum);
                String address = new CellReference(cell).formatAsString();
                cell.setCellValue(address);
            }

        }

        // Rows with rownum < 900 are flushed and not accessible
//        for (int rownum = 0; rownum < 103857; rownum++) {
//            Assert.assertNull(sh.getRow(rownum));
//        }
//
//        // ther last 100 rows are still in memory
//        for (int rownum = 103857; rownum < 104857; rownum++) {
//            Assert.assertNotNull(sh.getRow(rownum));
//        }

        File file = new File("C:\\Users\\FlyingHe\\Desktop", "datas.xlsx");
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();

        // dispose of temporary files backing this workbook on disk
        wb.dispose();
    }

}
