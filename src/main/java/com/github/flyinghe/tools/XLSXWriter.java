package com.github.flyinghe.tools;

import com.github.flyinghe.depdcy.AbstractExcelWriter;
import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.exception.WriteExcelRuntimeException;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by FlyingHe on 2018/12/18.
 * <p>
 * 支持写入大数据量的需求,但仅支持xlsx类型的Excel文件。
 * </p>
 */
public class XLSXWriter<T> extends AbstractExcelWriter<T> {

    /**
     * @throws WriteExcelException 异常
     */
    public XLSXWriter() throws WriteExcelException {
        this(-1);
    }

    /**
     * @param limit {@link #limit}
     * @throws WriteExcelException 异常
     */
    public XLSXWriter(int limit) throws WriteExcelException {
        this(limit, DATE_PATTERN);
    }


    /**
     * @param limit      {@link #limit}
     * @param dateFormat 指定默认日期格式,若为空则使用默认日期格式
     * @throws WriteExcelException 异常
     * @see #DATE_PATTERN
     */
    public XLSXWriter(int limit, String dateFormat) throws WriteExcelException {
        this(true, limit, 0, true, dateFormat);
    }

    /**
     * @param isWriteTitle        {@link #isWriteTitle}
     * @param limit               {@link #limit}
     * @param rowNumReserved      {@link #rowNumReserved}
     * @param isCompressTempFiles 指定写入缓存时缓存文件是否需要压缩,若不压缩缓存文件可能占用大量硬盘容量
     * @param dateFormat          指定默认日期格式,若为空则使用默认日期格式
     * @throws WriteExcelException 异常
     * @see #DATE_PATTERN
     */
    public XLSXWriter(boolean isWriteTitle, int limit, int rowNumReserved, boolean isCompressTempFiles,
                      String dateFormat)
            throws WriteExcelException {
        this(isWriteTitle, true, limit, rowNumReserved, isCompressTempFiles, dateFormat);
    }

    /**
     * @param isWriteTitle        {@link #isWriteTitle}
     * @param isSkipBlankRow      {@link #isSkipBlankRow}
     * @param limit               {@link #limit}
     * @param rowNumReserved      {@link #rowNumReserved}
     * @param isCompressTempFiles 指定写入缓存时缓存文件是否需要压缩,若不压缩缓存文件可能占用大量硬盘容量
     * @param dateFormat          指定默认日期格式,若为空则使用默认日期格式
     * @throws WriteExcelException 异常
     * @see #DATE_PATTERN
     */
    public XLSXWriter(boolean isWriteTitle, boolean isSkipBlankRow, int limit, int rowNumReserved,
                      boolean isCompressTempFiles, String dateFormat)
            throws WriteExcelException {
        super(isWriteTitle, isSkipBlankRow, limit, rowNumReserved, XLSX, dateFormat);
        this.workbook = new SXSSFWorkbook(-1);
        ((SXSSFWorkbook) this.workbook).setCompressTempFiles(isCompressTempFiles);
        this.validateDataWhenConstruct();
    }

    /**
     * 将内存中的Excel数据刷入缓存
     */
    private void flush() {
        try {
            if (this.isSkipBlankRow && this.isBlankLastRow) {
                //若不需要写入空行,且上一行为空行的话则保留上一行不被刷入缓存
                ((SXSSFSheet) this.currentSheet).flushRows(1);
            } else {
                ((SXSSFSheet) this.currentSheet).flushRows();
            }
        } catch (Exception e) {
            throw new WriteExcelRuntimeException("Flush Error");
        }
    }

    @Override
    protected void initSheet() throws WriteExcelException {
        //在开始新的一页之前(第一页除外)会把上一页的数据刷入缓存,以防止出现刷入不及时而导致内存溢出的情况
        if (this.allSheetInExcel > 0) {
            this.flush();
        }
        super.initSheet();
    }

    @Override
    public AbstractExcelWriter<T> write(List<T> datas) throws WriteExcelException {
        super.write(datas);
        this.flush();
        return this;
    }

    /**
     * 结束写入并将workbook输出到指定流,
     * 该方法仅可调用一次
     *
     * @param os 该流需要手动关闭
     * @return 缓存全部写入指定的输出流后会尝试删除缓存文件, 删除成功返回true, 失败返回false
     */
    @Override
    public boolean endWrite(OutputStream os) {
        boolean flag = true;
        try {
            this.workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CommonUtils.closeIOStream(null, os);
            //删除用于缓存的临时文件
            flag = ((SXSSFWorkbook) this.workbook).dispose();
        }
        return flag;
    }
}
