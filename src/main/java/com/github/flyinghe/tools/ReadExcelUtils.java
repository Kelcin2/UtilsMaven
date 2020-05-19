package com.github.flyinghe.tools;

import com.github.flyinghe.exception.ReadExcelException;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本类用于读取Excel文档。
 *
 * @author Flying
 */
public class ReadExcelUtils {
    private static String[] PATTERN =
            new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S", "yyyy.MM.dd",
                    "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm:ss.S", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss",
                    "yyyy/MM/dd HH:mm:ss.S",};

    private ReadExcelUtils() {}

    /**
     * 获取指定单元格的内容。只能是日期（返回java.util.Date），整数值（返回的均是String类型），小数值（返回的均是String类型，并
     * 保留两位小数），字符串， 布尔类型。 其他类型返回null。
     *
     * @param type 指定类型（Cell中定义的类型）
     * @param cell 指定单元格
     * @return 返回Cell里的内容
     */
    public static Object getCellValue(int type, Cell cell) {
        switch (type) {
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DateUtil.getJavaDate(cell.getNumericCellValue());
                } else {
                    BigDecimal bd = new BigDecimal(cell.getNumericCellValue());
                    return bd.toString().contains(".") ? bd.setScale(2, RoundingMode.HALF_UP).toString() :
                            bd.toString();
                }
            case Cell.CELL_TYPE_STRING:
                try {
                    return DateUtils.parseDate(cell.getStringCellValue(), ReadExcelUtils.PATTERN);
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case Cell.CELL_TYPE_FORMULA:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * 获取指定Sheet页的列名，列名必须在第1列，0-based
     *
     * @param sheet 指定Sheet页
     * @return 返回List
     * @throws ReadExcelException
     */
    public static List<String> getColumnOfSheet(Sheet sheet) throws ReadExcelException {
        // 获取列名
        Row row = sheet.getRow(1);
        if (row == null) {
            throw new ReadExcelException("The Column name is invalid！");
        }
        List<String> columnList = new ArrayList<String>();
        // 遍历列名并存入List中
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                columnList.add(cell.getStringCellValue());
            }
        }
        return columnList;
    }

    /**
     * 将指定Sheet页的数据转化成beanMap存入List中，列名对应值从第2行开始，0-based
     *
     * @param sheet 指定Sheet页
     * @return 无结果返回空List
     * @throws ReadExcelException
     */
    public static List<Map<String, Object>> handleSheetToMapList(Sheet sheet) throws ReadExcelException {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        // 得到Sheet里的列名
        List<String> columnList = ReadExcelUtils.getColumnOfSheet(sheet);
        for (int i = 2; i < sheet.getLastRowNum() + 1; i++) {
            Map<String, Object> beanMap = new HashMap<String, Object>();
            // 得到行
            Row row = sheet.getRow(i);
            // 遍历列
            for (int j = 0; j < row.getLastCellNum(); j++) {
                // 获取单元格
                Cell cell = row.getCell(j);
                if (cell != null) {
                    // 单元格对应列名
                    String key = columnList.get(j);
                    // 单元格对应值
                    Object value = ReadExcelUtils.getCellValue(cell.getCellType(), cell);
                    // key-value pairs 放入map
                    if (value != null) {
                        beanMap.put(key, value);
                    }
                }
            }
            // 将每个beanMap放入List中
            if (!beanMap.isEmpty()) {
                listMap.add(beanMap);
            }
        }
        return listMap;
    }

    /**
     * 将WorkBook里的所有Sheet里的记录封装成beanMap放入List中。
     *
     * @param workbook 指定WorkBook
     * @return 无结果返回空List
     * @throws ReadExcelException
     */
    public static List<Map<String, Object>> handleWorkBookToMapList(Workbook workbook) throws ReadExcelException {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        // 遍历WorkBook所有Sheet
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            // 获取Sheet
            Sheet sheet = workbook.getSheetAt(i);
            // 将每个Sheet页获取到的beanMap均放入listMap中
            listMap.addAll(ReadExcelUtils.handleSheetToMapList(sheet));
        }

        return listMap;
    }

    /**
     * 指定Excel文件里的记录封装成beanMap返回
     *
     * @param file Excel文件
     * @return 无结果返回空List
     * @throws ReadExcelException
     */
    public static List<Map<String, Object>> handleWorkBookToMapList(File file) throws ReadExcelException {
        List<Map<String, Object>> listMap = null;
        try {
            Workbook workbook = WorkbookFactory.create(file);
            listMap = ReadExcelUtils.handleWorkBookToMapList(workbook);
            if (workbook != null) {
                workbook.close();
            }
        } catch (Exception e) {
            throw new ReadExcelException(e.getMessage());
        }
        return listMap;
    }

    /**
     * 将指定WorkBook里的记录封装成指定类型放入List中返回
     *
     * @param clazz    指定封装类型
     * @param workbook
     * @return 无结果返回空List
     * @throws ReadExcelException
     */
    public static <T> List<T> handleWorkBookToBeans(Class<T> clazz, Workbook workbook) throws ReadExcelException {
        List<Map<String, Object>> listMap = ReadExcelUtils.handleWorkBookToMapList(workbook);
        List<T> beans = new ArrayList<T>();
        for (Map<String, Object> map : listMap) {
            T bean = CommonUtils.toBean(map, clazz);
            if (bean != null) {
                beans.add(bean);
            }
        }

        return beans;
    }

    /**
     * 将指定Excel文件里的记录封装成指定类型放入List中返回
     *
     * @param clazz 指定封装类型
     * @param file  指定Excel文件
     * @return 无结果返回空List
     * @throws ReadExcelException
     */
    public static <T> List<T> handleWorkBookToBeans(Class<T> clazz, File file) throws ReadExcelException {
        List<T> beans = null;
        try {
            Workbook workbook = WorkbookFactory.create(file);
            beans = ReadExcelUtils.handleWorkBookToBeans(clazz, workbook);
            if (workbook != null) {
                workbook.close();
            }
        } catch (Exception e) {
            throw new ReadExcelException(e.getMessage());
        }
        return beans;
    }
}
