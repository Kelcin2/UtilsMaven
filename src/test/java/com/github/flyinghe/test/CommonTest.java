package com.github.flyinghe.test;

import com.github.flyinghe.domain.User;
import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.tools.ExcelWriter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;

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

        XSSFCellStyle cellStyle = excelWriter.createXSSFCellStyle();
        cellStyle.setFillForegroundColor(excelWriter.createXSSFColor(202, 234, 206));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        excelWriter.getTitleCellStyleMapping().put("name", cellStyle);
        excelWriter.write(user).endWrite(this.file1);
    }
    @Test
    public void test2() throws WriteExcelException {

    }
}
