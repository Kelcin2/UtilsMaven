package com.github.flyinghe.tools;

import com.github.flyinghe.depdcy.AbstractExcelWriter;
import com.github.flyinghe.exception.WriteExcelException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by FlyingHe on 2018/12/18.
 * <p>
 * 支持xlsx和xls类型的Excel文件。
 * </p>
 */
public class ExcelWriter<T> extends AbstractExcelWriter<T> {

    /**
     * @throws WriteExcelException 异常
     */
    public ExcelWriter() throws WriteExcelException {
        this(-1);
    }

    /**
     * @param limit {@link #limit}
     * @throws WriteExcelException 异常
     */
    public ExcelWriter(int limit) throws WriteExcelException {
        this(limit, XLSX, DATE_PATTERN);
    }

    /**
     * @param limit     {@link #limit}
     * @param excelType {@link #excelType}
     * @throws WriteExcelException 异常
     */
    public ExcelWriter(int limit, int excelType) throws WriteExcelException {
        this(limit, excelType, DATE_PATTERN);
    }

    /**
     * @param limit      {@link #limit}
     * @param excelType  {@link #excelType}
     * @param dateFormat 指定默认日期格式,若为空则使用默认日期格式
     * @throws WriteExcelException 异常
     * @see #DATE_PATTERN
     */
    public ExcelWriter(int limit, int excelType, String dateFormat) throws WriteExcelException {
        this(true, limit, 0, excelType, dateFormat);
    }

    /**
     * @param isWriteTitle   {@link #isWriteTitle}
     * @param limit          {@link #limit}
     * @param rowNumReserved {@link #rowNumReserved}
     * @param excelType      {@link #excelType}
     * @param dateFormat     指定默认日期格式,若为空则使用默认日期格式
     * @throws WriteExcelException 异常
     * @see #DATE_PATTERN
     */
    public ExcelWriter(boolean isWriteTitle, int limit, int rowNumReserved, int excelType,
                       String dateFormat) throws WriteExcelException {
        this(isWriteTitle, true, limit, rowNumReserved, excelType, dateFormat);
    }

    /**
     * @param isWriteTitle   {@link #isWriteTitle}
     * @param isSkipBlankRow {@link #isSkipBlankRow}
     * @param limit          {@link #limit}
     * @param rowNumReserved {@link #rowNumReserved}
     * @param excelType      {@link #excelType}
     * @param dateFormat     指定默认日期格式,若为空则使用默认日期格式
     * @throws WriteExcelException 异常
     * @see #DATE_PATTERN
     */
    public ExcelWriter(boolean isWriteTitle, boolean isSkipBlankRow, int limit, int rowNumReserved, int excelType,
                       String dateFormat) throws WriteExcelException {
        super(isWriteTitle, isSkipBlankRow, limit, rowNumReserved, excelType, dateFormat);
        if (XLS == this.excelType) {
            this.workbook = new HSSFWorkbook();
        } else if (XLSX == this.excelType) {
            this.workbook = new XSSFWorkbook();
        }
        this.validateDataWhenConstruct();
    }

    @Override
    protected void validateDataWhenConstruct() throws WriteExcelException {
        if (null == this.workbook) {
            throw new WriteExcelException("Excel文件类型仅支持xls,xlsx类型,请检查您传入的文件类型参数");
        }
        super.validateDataWhenConstruct();
    }

    /**
     * 结束写入并将workbook输出到指定流,
     * 该方法可以多次调用
     *
     * @param os 该流需要手动关闭
     * @return 写入成功返回true, 否则返回false
     */
    @Override
    public boolean endWrite(OutputStream os) {
        boolean endSuccess = false;
        try {
            this.workbook.write(os);
            endSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return endSuccess;
    }
}
