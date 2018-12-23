package com.github.flyinghe.test;

import com.github.flyinghe.depdcy.AbstractExcelWriter;
import com.github.flyinghe.domain.TestObj;
import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.ExcelWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by FlyingHe on 2018/12/19.
 */
public class TestExcelWriter {
    private File file1 = new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xlsx");
    private File file2 = new File("C:\\Users\\FlyingHe\\Desktop", "datas2.xlsx");
    private File file3 = new File("C:\\Users\\FlyingHe\\Desktop", "datas3.xls");
    private File file4 = new File("C:\\Users\\FlyingHe\\Desktop", "datas4.xls");

    private List<Map<String, Object>> getMapDatas(int num) {
        List<Map<String, Object>> datas = new ArrayList<>(num);
        Random random = new Random();
        for (int i = 0; i < num; i++) {
            Map<String, Object> data = new HashMap<>();
            datas.add(data);
            data.put("stringType", "StringType" + i);
            data.put("charType", (char) i);
            data.put("dateType", new Date(new Date().getTime() + i * 24 * 3600000L));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date) data.get("dateType"));
            data.put("calendarType", calendar);
            data.put("booleanType", i % 5 == 0);
            data.put("byteType", (byte) i);
            data.put("shortType", (short) i);
            data.put("integerType", i);
            data.put("longType", (long) i);
            data.put("floatType", i * 100 * random.nextFloat());
            data.put("doubleType", i * 100 * random.nextDouble());
            data.put("nullType", i % 10 == 0 ? null : "notNull");
        }
        return datas;
    }

    private List<TestObj> getDomainDatas(int num, boolean hasBlank) {
        List<TestObj> datas = new ArrayList<>(num);
        Random random = new Random();
        for (int i = 0; i < num; i++) {
            TestObj data = new TestObj();
            datas.add(data);
            if (!hasBlank || ((i + 1) % 10 == 0)) {
                data.setStringType("StringType" + i);
                data.setCharType((char) i);
                data.setDateType(new Date(new Date().getTime() + i * 24 * 3600000L));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(data.getDateType());
                data.setCalendarType(calendar);
                data.setBooleanType(random.nextInt(100) > 50);
                data.setByteType((byte) i);
                data.setShortType((short) i);
                data.setIntegerType(i);
                data.setLongType((long) i);
                data.setFloatType(i * 100 * random.nextFloat());
                data.setDoubleType(i * 100 * random.nextDouble());
                data.setNullType(i % 10 == 0 ? null : "notNull");
            }
        }
        return datas;
    }

    /**
     * 基础测试:
     * 测试每种数据类型写入情况，两种文件类型，isWriteTitle参数
     */
    @Test
    public void test1() throws WriteExcelException {
        new ExcelWriter<Map<String, Object>>().write(this.getMapDatas(200)).endWrite(this.file1);
        new ExcelWriter<TestObj>(false, -1, 0, ExcelWriter.XLSX, null)
                .write(this.getDomainDatas(200, false))
                .endWrite(this.file2);
        new ExcelWriter<Map<String, Object>>(-1, ExcelWriter.XLS)
                .write(this.getMapDatas(200))
                .endWrite(this.file3);
        new ExcelWriter<TestObj>(false, -1, 0, ExcelWriter.XLS, null)
                .write(this.getDomainDatas(200, false))
                .endWrite(this.file4);
    }

    /**
     * 基础测试:
     * 测试每种数据类型写入情况，两种文件类型，isSkipBlankRow，limit，rowNumReserved，dateFormat参数
     */
    @Test
    public void test2() throws WriteExcelException {
        new ExcelWriter<TestObj>(true, false, 50, 10, ExcelWriter.XLSX, null)
                .write(this.getDomainDatas(200, true))
                .endWrite(this.file1);
        new ExcelWriter<TestObj>(true, true, 5, 10, ExcelWriter.XLSX, "【yyyy】【MM】【dd】")
                .write(this.getDomainDatas(200, true))
                .endWrite(this.file2);
        new ExcelWriter<TestObj>(true, false, 50, 0, ExcelWriter.XLS, null)
                .write(this.getDomainDatas(200, true))
                .endWrite(this.file3);
        new ExcelWriter<TestObj>(true, true, 50, 0, ExcelWriter.XLS, null)
                .write(this.getDomainDatas(200, true))
                .endWrite(this.file4);
    }

    private CellStyle produceCellStyle(ExcelWriter writer, Short ffColor, Short fontColor) {
        CellStyle cellStyle = writer.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillForegroundColor(ffColor);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = writer.createFont();
        font.setColor(fontColor);
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * 功能测试:
     * 测试dateFormatMapping，booleanMapping，cellStyleMapping，titleCellStyleMapping，columnWidthMapping参数
     */
    @Test
    public void test3() throws WriteExcelException {
        ExcelWriter<TestObj> excelWriter =
                new ExcelWriter<>(true, true, 5, 10, ExcelWriter.XLSX, null);
        excelWriter.getDateFormatMapping().put("dateType", "yyyy.MM.dd");
        excelWriter.getDateFormatMapping().put("calendarType", "yyyy-MM-dd HH:mm:ss");
        excelWriter.putBooleanMapping("booleanType", "是", "否");
        excelWriter.getColumnWidthMapping().put("dateType", 40);
        excelWriter.getColumnWidthMapping().put("calendarType", 40);
        excelWriter.getCellStyleMapping().put("booleanType",
                this.produceCellStyle(excelWriter, IndexedColors.SKY_BLUE.getIndex(), IndexedColors.BLACK.getIndex()));
        excelWriter.getCellStyleMapping().put("floatType",
                this.produceCellStyle(excelWriter, IndexedColors.BLACK.getIndex(), IndexedColors.WHITE.getIndex()));
        excelWriter.getTitleCellStyleMapping().put("nullType",
                this.produceCellStyle(excelWriter, IndexedColors.GREEN.getIndex(), IndexedColors.BLACK.getIndex()));
        excelWriter.getTitleCellStyleMapping().put("longType",
                this.produceCellStyle(excelWriter, IndexedColors.GOLD.getIndex(), IndexedColors.WHITE.getIndex()));
        excelWriter.write(this.getDomainDatas(200, true)).endWrite(this.file1);


        ExcelWriter<TestObj> excelWriter1 =
                new ExcelWriter<>(true, false, 50, 0, ExcelWriter.XLS, null);
        excelWriter1.getDateFormatMapping().put("dateType", "yyyy.MM.dd");
        excelWriter1.getDateFormatMapping().put("calendarType", "yyyy-MM-dd HH:mm:ss");
        excelWriter1.putBooleanMapping("booleanType", "是", "否");
        excelWriter1.getColumnWidthMapping().put("dateType", 40);
        excelWriter1.getColumnWidthMapping().put("calendarType", 40);
        excelWriter1.getCellStyleMapping().put("booleanType",
                this.produceCellStyle(excelWriter1, IndexedColors.SKY_BLUE.getIndex(), IndexedColors.BLACK.getIndex()));
        excelWriter1.getCellStyleMapping().put("floatType",
                this.produceCellStyle(excelWriter1, IndexedColors.BLACK.getIndex(), IndexedColors.WHITE.getIndex()));
        excelWriter1.getTitleCellStyleMapping().put("nullType",
                this.produceCellStyle(excelWriter1, IndexedColors.GREEN.getIndex(), IndexedColors.BLACK.getIndex()));
        excelWriter1.getTitleCellStyleMapping().put("longType",
                this.produceCellStyle(excelWriter1, IndexedColors.GOLD.getIndex(), IndexedColors.WHITE.getIndex()));
        excelWriter1.write(this.getDomainDatas(200, true)).endWrite(this.file3);
    }

    /**
     * 功能测试:
     * 测试defaultCellStyle，defaultTitleCellStyle，defaultHeight，defaultTitleHeight，defaultColumnWidth参数
     */
    @Test
    public void test4() throws WriteExcelException {
        ExcelWriter<TestObj> excelWriter =
                new ExcelWriter<>(true, true, 5, 0, ExcelWriter.XLSX, null);
        excelWriter.getDateFormatMapping().put("dateType", "yyyy.MM.dd");
        excelWriter.getDateFormatMapping().put("calendarType", "yyyy-MM-dd HH:mm:ss");
        excelWriter.putBooleanMapping("booleanType", "是", "否");
        excelWriter.getColumnWidthMapping().put("dateType", 40);
        excelWriter.getColumnWidthMapping().put("calendarType", 40);
        excelWriter.getCellStyleMapping().put("booleanType",
                this.produceCellStyle(excelWriter, IndexedColors.SKY_BLUE.getIndex(), IndexedColors.BLACK.getIndex()));
        excelWriter.getCellStyleMapping().put("floatType",
                this.produceCellStyle(excelWriter, IndexedColors.BLACK.getIndex(), IndexedColors.WHITE.getIndex()));
        excelWriter.getTitleCellStyleMapping().put("nullType",
                this.produceCellStyle(excelWriter, IndexedColors.GREEN.getIndex(), IndexedColors.BLACK.getIndex()));
        excelWriter.getTitleCellStyleMapping().put("longType",
                this.produceCellStyle(excelWriter, IndexedColors.GOLD.getIndex(), IndexedColors.WHITE.getIndex()));
        excelWriter.setDefaultCellStyle(this.produceCellStyle(excelWriter, IndexedColors.GREY_40_PERCENT.getIndex(),
                IndexedColors.WHITE.getIndex()));
        excelWriter
                .setDefaultTitleCellStyle(this.produceCellStyle(excelWriter, IndexedColors.GREY_80_PERCENT.getIndex(),
                        IndexedColors.WHITE.getIndex()));
        excelWriter.setDefaultHeight(30f);
        excelWriter.setDefaultTitleHeight(50f);
        excelWriter.setDefaultColumnWidth(35);
        excelWriter.write(this.getDomainDatas(200, true)).endWrite(this.file1);


        ExcelWriter<TestObj> excelWriter1 =
                new ExcelWriter<>(true, false, 50, 0, ExcelWriter.XLS, null);
        excelWriter1.getDateFormatMapping().put("dateType", "yyyy.MM.dd");
        excelWriter1.getDateFormatMapping().put("calendarType", "yyyy-MM-dd HH:mm:ss");
        excelWriter1.putBooleanMapping("booleanType", "是", "否");
        excelWriter1.getColumnWidthMapping().put("dateType", 40);
        excelWriter1.getColumnWidthMapping().put("calendarType", 40);
        excelWriter1.getCellStyleMapping().put("booleanType",
                this.produceCellStyle(excelWriter1, IndexedColors.SKY_BLUE.getIndex(), IndexedColors.BLACK.getIndex()));
        excelWriter1.getCellStyleMapping().put("floatType",
                this.produceCellStyle(excelWriter1, IndexedColors.BLACK.getIndex(), IndexedColors.WHITE.getIndex()));
        excelWriter1.getTitleCellStyleMapping().put("nullType",
                this.produceCellStyle(excelWriter1, IndexedColors.GREEN.getIndex(), IndexedColors.BLACK.getIndex()));
        excelWriter1.getTitleCellStyleMapping().put("longType",
                this.produceCellStyle(excelWriter1, IndexedColors.GOLD.getIndex(), IndexedColors.WHITE.getIndex()));
        excelWriter1.setDefaultHeight(30f);
        excelWriter1.setDefaultTitleHeight(20f);
        excelWriter1.setDefaultColumnWidth(35);
        excelWriter1.write(this.getDomainDatas(200, true)).endWrite(this.file3);
    }

    /**
     * 功能测试:
     * 测试properties，excludeProps，titles参数以及回调
     */
    @Test
    public void test5() throws WriteExcelException {
        ExcelWriter<TestObj> excelWriter =
                new ExcelWriter<>(true, true, 5, 3, ExcelWriter.XLSX, null);
        excelWriter.setProperties(CommonUtils
                .arrayToList(new String[]{"stringType", "charType", "calendarType", "booleanType", "nullType"}));
        excelWriter.setTitles(CommonUtils.arrayToList(new String[]{"字符串类型", "字符类型", "日期类型", "布尔类型", "空值类型"}));
        excelWriter.setHandleRowReserved(new AbstractExcelWriter.HandleRowReserved<TestObj>() {
            @Override
            public void callback(Sheet sheet, AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
                sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 4));
                Cell cell = sheet.createRow(0).createCell(0);
                CellStyle cellStyle = writer.createCellStyle();
                Font font = writer.createFont();
                font.setBold(true);
                font.setFontHeightInPoints((short) 20);
                font.setColor(IndexedColors.WHITE.getIndex());
                cellStyle.setFont(font);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                cellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("this is TEST!");
            }
        });
        excelWriter.setHandleCellValue(new AbstractExcelWriter.HandleCellValue<TestObj>() {
            @Override
            public void callback(String property, Object value, AbstractExcelWriter<TestObj> writer)
                    throws WriteExcelException {
                if (!writer.getCellStylePool().containsKey("booleanTypeTrue")) {
                    CellStyle cellStyle = writer.createCellStyle();
                    cellStyle.cloneStyleFrom(writer.getDefaultCellStyle());
                    cellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    writer.getCellStylePool().put("booleanTypeTrue", cellStyle);
                }
                if (!writer.getCellStylePool().containsKey("booleanTypeFalse")) {
                    CellStyle cellStyle = writer.createCellStyle();
                    cellStyle.cloneStyleFrom(writer.getDefaultCellStyle());
                    cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    writer.getCellStylePool().put("booleanTypeFalse", cellStyle);
                }
                if (null == value) {
                    return;
                }
                if ("booleanType".equals(property)) {
                    if ((boolean) value) {
                        writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeTrue"));
                        writer.putCellValueToMap("合格");
                    } else {
                        writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeFalse"));
                        writer.putCellValueToMap("不合格");
                    }
                }
                if ("calendarType".equals(property)) {
                    writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeTrue"));
                    writer.putCellValueToMap(
                            new SimpleDateFormat("[yyyy][MM][dd]").format(((Calendar) value).getTime()));
                }
                if ("nullType".equals(property)) {
                    writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeTrue"));
                }
            }
        });
        excelWriter.write(this.getDomainDatas(200, true)).endWrite(this.file1);
        System.out.println(excelWriter.getAllSheetInExcel());
        System.out.println(excelWriter.getRealDataInExcel());
        System.out.println(excelWriter.getRealRowInExcel());

        /*************************************************************************************************/

        ExcelWriter<TestObj> excelWriter1 =
                new ExcelWriter<>(true, true, 50, 3, ExcelWriter.XLS, null);
        excelWriter1.setProperties(CommonUtils
                .arrayToList(new String[]{"stringType", "charType", "calendarType", "booleanType", "nullType"}));
        excelWriter1.setExcludeProps(CommonUtils.arrayToList(
                new String[]{"dateType", "byteType", "shortType", "integerType", "longType", "floatType", "nullType",
                        "doubleType"}));
        excelWriter1.setHandleRowReserved(new AbstractExcelWriter.HandleRowReserved<TestObj>() {
            @Override
            public void callback(Sheet sheet, AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
                sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 4));
                Cell cell = sheet.createRow(0).createCell(0);
                CellStyle cellStyle = writer.createCellStyle();
                Font font = writer.createFont();
                font.setBold(true);
                font.setFontHeightInPoints((short) 20);
                font.setColor(IndexedColors.WHITE.getIndex());
                cellStyle.setFont(font);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                cellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(cellStyle);
                cell.setCellValue("this is TEST!");
            }
        });
        excelWriter1.setHandleCellValue(new AbstractExcelWriter.HandleCellValue<TestObj>() {
            @Override
            public void callback(String property, Object value, AbstractExcelWriter<TestObj> writer)
                    throws WriteExcelException {
                if (!writer.getCellStylePool().containsKey("booleanTypeTrue")) {
                    CellStyle cellStyle = writer.createCellStyle();
                    cellStyle.cloneStyleFrom(writer.getDefaultCellStyle());
                    cellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    writer.getCellStylePool().put("booleanTypeTrue", cellStyle);
                }
                if (!writer.getCellStylePool().containsKey("booleanTypeFalse")) {
                    CellStyle cellStyle = writer.createCellStyle();
                    cellStyle.cloneStyleFrom(writer.getDefaultCellStyle());
                    cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    writer.getCellStylePool().put("booleanTypeFalse", cellStyle);
                }
                if ("booleanType".equals(property)) {
                    if (null == value) {
                        return;
                    }
                    if ((boolean) value) {
                        writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeTrue"));
                        writer.putCellValueToMap("合格");
                    } else {
                        writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeFalse"));
                        writer.putCellValueToMap("不合格");
                    }
                }
                if ("calendarType".equals(property)) {
                    if (null == value) {
                        return;
                    }
                    writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeTrue"));
                    writer.putCellValueToMap(
                            new SimpleDateFormat("[yyyy][MM][dd]").format(((Calendar) value).getTime()));
                }
                if ("nullType".equals(property)) {
                    if (null == value) {
                        writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeFalse"));
                        writer.putCellValueToMap("空值");
                    } else {
                        writer.putCellStyleToMap(writer.getCellStylePool().get("booleanTypeTrue"));
                    }
                }
            }
        });
        excelWriter1.write(this.getDomainDatas(200, true)).endWrite(this.file3);
        System.out.println(excelWriter1.getAllSheetInExcel());
        System.out.println(excelWriter1.getRealDataInExcel());
        System.out.println(excelWriter1.getRealRowInExcel());

    }
}
