package com.github.flyinghe.tools;

import com.github.flyinghe.exception.WriteExcelException;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Flying on 2016/5/28.
 * <p>
 * 本类用于将JavaBean写入Excel文档中，采用POI(3.14及以上版本)技术。
 * </p>
 */
public class WriteExcelUtils {
    public final static int XLSX = 1;
    public final static int XLS = 2;
    //默认日期格式
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";

    private WriteExcelUtils() {}

    /**
     * 给一个Cell赋值,若为空则视为"",若不为基本类型及其包装类(日期类型除外),则其值通过toString()获取
     *
     * @param cell       一个单元格
     * @param value      值
     * @param dateFormat 日期格式
     * @return 返回传入的Cell
     */
    public static Cell setCellValue(Cell cell, Object value, String dateFormat) {
        SimpleDateFormat format =
                new SimpleDateFormat(StringUtils.isNotBlank(dateFormat) ? dateFormat : DATE_PATTERN);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Date) {
            cell.setCellValue(format.format(value));
        } else if (value instanceof Calendar) {
            cell.setCellValue(format.format(value));
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Byte) {
            cell.setCellValue((Byte) value);
        } else if (value instanceof Short) {
            cell.setCellValue((Short) value);
        } else {
            cell.setCellValue(value.toString());
        }
        return cell;
    }


    /**
     * 将一个Bean写入某一行中，bean的属性由properties决定
     *
     * @param row        指定行
     * @param bean       指定Bean
     * @param properties 指定写入的属性
     * @param dateFormat 日期格式
     * @param cellStyle  每一个单元格的样式
     * @return 返回传入的Row
     */
    public static <T> Row writePerRow(Row row, T bean, List<String> properties, String dateFormat,
                                      CellStyle cellStyle) {
        Map<String, Object> mapBean = null;
        if (bean instanceof Map) {
            mapBean = (Map<String, Object>) bean;
        } else {
            mapBean = CommonUtils.toMap(bean);
        }
        if (mapBean == null) {
            return row;
        }
        for (int i = 0; i < properties.size(); i++) {
            if (mapBean.containsKey(properties.get(i))) {
                Cell cell = row.createCell(i);
                cell.setCellStyle(cellStyle);
                WriteExcelUtils.setCellValue(cell, mapBean.get(properties.get(i)), dateFormat);
            }

        }
        return row;
    }

    /**
     * 向某一Sheet中写入多个Bean
     *
     * @param sheet      指定Sheet
     * @param _titleRow  指定写入的标题在第几行，0-based
     * @param begin      指定写入的Bean从第几个开始，包含该下标,0-based
     * @param end        指定写入的Bean从第几个结束，不包含该下标，0-based
     * @param beans      指定写入的Beans(或者泛型为Map)
     * @param properties 指定写入的bean的属性
     * @param titles     指定写入的标题
     * @param dateFormat 日期格式
     * @return 返回传入的Sheet
     */
    public static <T> Sheet writePerSheet(Sheet sheet, int _titleRow, int begin, int end, List<T> beans,
                                          List<String> properties, List<String> titles, String dateFormat) {
        Row titleRow = sheet.createRow(_titleRow);
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        cellStyle.setFont(font);
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        sheet.setDefaultColumnWidth(16);
        for (int i = 0; i < titles.size(); i++) {
            Cell cell = titleRow.createCell(i);
            cell.setCellValue(titles.get(i));
            cell.setCellStyle(cellStyle);
        }
        //单元格样式
        CellStyle _cellStyle = sheet.getWorkbook().createCellStyle();
        _cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        _cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        int counter = _titleRow + 1;
        for (int i = begin; i < (end < beans.size() ? end : beans.size()); i++) {
            Row row = sheet.createRow(counter++);
            WriteExcelUtils.writePerRow(row, beans.get(i), properties, dateFormat, _cellStyle);
        }
        return sheet;
    }

    /**
     * 向某一WorkBook中写入多个Bean
     *
     * @param workbook   指定工作簿
     * @param _titleRow  指定写入的标题在第几行，0-based
     * @param count      指定每个Sheet写入几个bean
     * @param beans      指定写入的Beans(或者泛型为Map)
     * @param properties 指定写入的bean的属性
     * @param titles     指定写入的标题
     * @param dateFormat 日期格式
     * @return 返回传入的WorkBook
     */
    public static <T> Workbook writeWorkBook(Workbook workbook, int _titleRow, int count, List<T> beans,
                                             List<String> properties, List<String> titles, String dateFormat) {
        int sheetNum = beans.size() % count == 0 ? beans.size() / count : (beans.size() / count + 1);
        for (int i = 0; i < sheetNum; i++) {
            WriteExcelUtils
                    .writePerSheet(workbook.createSheet(), _titleRow, i * count, i * count + count, beans, properties,
                            titles, dateFormat);
        }
        return workbook;
    }

    /**
     * 向工作簿中写入beans，标题默认从第0行开始，0-based
     *
     * @param workbook   指定工作簿
     * @param count      指定每一个Sheet写入几个Bean
     * @param beans      指定写入的Beans(或者泛型为Map)
     * @param properties 指定写入的bean的属性
     * @param titles     指定写入的标题
     * @param dateFormat 日期格式
     * @return 返回传入的WorkBook
     */
    public static <T> Workbook writeWorkBook(Workbook workbook, int count, List<T> beans, List<String> properties,
                                             List<String> titles, String dateFormat) {
        return WriteExcelUtils.writeWorkBook(workbook, 0, count, beans, properties, titles, dateFormat);
    }

    /**
     * 向工作簿中写入beans，所有bean写在一个Sheet中,标题写在第0行,0-based
     *
     * @param workbook   指定工作簿
     * @param beans      指定写入的Beans(或者泛型为Map)
     * @param properties 指定写入的bean的属性
     * @param titles     指定写入的标题
     * @param dateFormat 日期格式
     * @return 返回传入的WorkBook
     */
    public static <T> Workbook writeWorkBook(Workbook workbook, List<T> beans, List<String> properties,
                                             List<String> titles, String dateFormat) {
        return WriteExcelUtils.writeWorkBook(workbook, beans.size(), beans, properties, titles, dateFormat);
    }

    /**
     * 向工作簿中写入beans，所有bean写在一个Sheet中,标题写在第0行,0-based。并输出到指定file中
     *
     * @param file       指定Excel输出文件
     * @param excelType  输出Excel文件类型{@link #XLSX}或者{@link #XLS},此类型必须与file文件名后缀匹配
     * @param beans      指定写入的Beans(或者泛型为Map)
     * @param properties 指定写入的bean的属性
     * @param titles     指定写入的标题
     * @param dateFormat 日期格式
     * @throws WriteExcelException
     */
    public static <T> void writeWorkBook(File file, int excelType, List<T> beans, List<String> properties,
                                         List<String> titles, String dateFormat) throws WriteExcelException {
        Workbook workbook = null;
        if (XLSX == excelType) {
            workbook = new XSSFWorkbook();
        } else if (XLS == excelType) {
            workbook = new HSSFWorkbook();
        } else {
            throw new WriteExcelException("excelType参数错误");
        }
        WriteExcelUtils.writeWorkBook(workbook, beans.size(), beans, properties, titles, dateFormat);
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            WriteExcelUtils.writeWorkBookToExcel(workbook, os);
        } catch (Exception e) {
            throw new WriteExcelException(e.getMessage());
        } finally {
            CommonUtils.closeIOStream(null, os);
        }
    }

    /**
     * 向工作簿中写入beans，所有bean写在一个Sheet中,默认以bean中的所有属性作为标题且写入第0行，0-based。并输出到指定file中
     *
     * @param file       指定Excel输出文件
     * @param excelType  输出Excel文件类型{@link #XLSX}或者{@link #XLS},此类型必须与file文件名后缀匹配
     * @param beans      指定写入的Beans(或者泛型为Map)
     * @param dateFormat 日期格式
     * @throws WriteExcelException
     */
    public static <T> void writeWorkBook(File file, int excelType, List<T> beans, String dateFormat)
            throws WriteExcelException {
        if (beans == null || beans.isEmpty()) {
            throw new WriteExcelException("beans参数不能为空");
        }
        Map<String, Object> map = null;
        if (beans.get(0) instanceof Map) {
            map = (Map<String, Object>) beans.get(0);
        } else {
            map = CommonUtils.toMap(beans.get(0));
        }
        if (map == null) {
            throw new WriteExcelException("获取bean属性失败");
        }
        List<String> properties = new ArrayList<String>();
        properties.addAll(map.keySet());
        WriteExcelUtils.writeWorkBook(file, excelType, beans, properties, properties, dateFormat);
    }

    /**
     * 向工作簿中写入beans，所有bean写在一个Sheet中,默认以bean中的所有属性作为标题且写入第0行，0-based。
     * 日期格式采用默认类型。并输出到指定file中
     *
     * @param file      指定Excel输出文件
     * @param excelType 输出Excel文件类型{@link #XLSX}或者{@link #XLS},此类型必须与file文件名后缀匹配
     * @param beans     指定写入的Beans(或者泛型为Map)
     * @throws WriteExcelException
     */
    public static <T> void writeWorkBook(File file, int excelType, List<T> beans) throws WriteExcelException {
        WriteExcelUtils.writeWorkBook(file, excelType, beans, null);
    }

    /**
     * 向工作簿中写入beans，所有bean写在一个Sheet中,默认以bean中的所有属性作为标题且写入第0行，0-based。
     * 日期格式采用默认类型。并输出到指定file中
     *
     * @param file      指定Excel输出文件
     * @param excelType 输出Excel文件类型{@link #XLSX}或者{@link #XLS},此类型必须与file文件名后缀匹配
     * @param beans     指定写入的Beans(或者泛型为Map)
     * @param count     指定每一个Sheet写入几个Bean
     * @throws WriteExcelException
     */
    public static <T> void writeWorkBook(File file, int excelType, List<T> beans, int count)
            throws WriteExcelException {
        Workbook workbook = null;
        if (XLSX == excelType) {
            workbook = new XSSFWorkbook();
        } else if (XLS == excelType) {
            workbook = new HSSFWorkbook();
        } else {
            throw new WriteExcelException("excelType参数错误");
        }

        if (beans == null || beans.isEmpty()) {
            throw new WriteExcelException("beans参数不能为空");
        }
        Map<String, Object> map = null;
        if (beans.get(0) instanceof Map) {
            map = (Map<String, Object>) beans.get(0);
        } else {
            map = CommonUtils.toMap(beans.get(0));
        }
        if (map == null) {
            throw new WriteExcelException("获取bean属性失败");
        }
        List<String> properties = new ArrayList<String>();
        properties.addAll(map.keySet());
        WriteExcelUtils.writeWorkBook(workbook, count, beans, properties, properties, null);
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            WriteExcelUtils.writeWorkBookToExcel(workbook, os);
        } catch (Exception e) {
            throw new WriteExcelException(e.getMessage());
        } finally {
            CommonUtils.closeIOStream(null, os);
        }
    }

    /**
     * 将Beans写入WorkBook中,默认以bean中的所有属性作为标题且写入第0行，0-based
     *
     * @param workbook   指定工作簿
     * @param beans      指定写入的Beans(或者泛型为Map)
     * @param dateFormat 日期格式
     * @return 返回传入的WorkBook
     */
    public static <T> Workbook writeWorkBook(Workbook workbook, List<T> beans, String dateFormat) {
        if (beans == null || beans.isEmpty()) {
            return workbook;
        }
        Map<String, Object> map = null;
        if (beans.get(0) instanceof Map) {
            map = (Map<String, Object>) beans.get(0);
        } else {
            map = CommonUtils.toMap(beans.get(0));
        }
        if (map == null) {
            return workbook;
        }
        List<String> properties = new ArrayList<String>();
        properties.addAll(map.keySet());
        return WriteExcelUtils.writeWorkBook(workbook, beans, properties, properties, dateFormat);
    }

    /**
     * 将Beans写入WorkBook中,默认以bean中的所有属性作为标题且写入第0行，0-based
     *
     * @param workbook 指定工作簿
     * @param beans    指定写入的Beans(或者泛型为Map)
     * @return 返回传入的WorkBook
     */
    public static <T> Workbook writeWorkBook(Workbook workbook, List<T> beans) {
        return WriteExcelUtils.writeWorkBook(workbook, beans, null);
    }

    /**
     * 将指定WorkBook写入指定文件中
     *
     * @param workbook 指定工作簿
     * @param os       指定输出流,需要手动关闭
     * @throws WriteExcelException 出现I/O异常抛出
     */
    public static void writeWorkBookToExcel(Workbook workbook, OutputStream os) throws WriteExcelException {
        try {
            workbook.write(os);
        } catch (IOException e) {
            throw new WriteExcelException(e.getMessage());
        }
    }

}
