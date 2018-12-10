package com.github.flyinghe.utlils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by FlyingHe on 2017/8/26.
 */
public class Example1 {
    public static void main(String[] args) throws Throwable {
        SXSSFWorkbook wb = new SXSSFWorkbook(-1); // turn off auto-flushing and accumulate all rows in memory
        Sheet sh = wb.createSheet();
        for (int rownum = 0; rownum < 104857; rownum++) {
            Row row = sh.createRow(rownum);
            for (int cellnum = 0; cellnum < 10; cellnum++) {
                Cell cell = row.createCell(cellnum);
                String address = new CellReference(cell).formatAsString();
                cell.setCellValue(address);
            }

            // manually control how rows are flushed to disk
            if ((rownum + 1) % 1000 == 0) {
                ((SXSSFSheet) sh).flushRows(); // retain 100 last rows and flush all others
                System.out.println("Flus:" + (rownum + 1));
                // ((SXSSFSheet)sh).flushRows() is a shortcut for ((SXSSFSheet)sh).flushRows(0),
                // this method flushes all rows
            }

        }

        File file = new File("C:\\Users\\FlyingHe\\Desktop", "datas.xlsx");
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();

        // dispose of temporary files backing this workbook on disk
        wb.dispose();
        System.out.println("Done--104857");
    }
}
