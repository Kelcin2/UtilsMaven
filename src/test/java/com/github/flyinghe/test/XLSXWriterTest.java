package com.github.flyinghe.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.flyinghe.depdcy.AbstractExcelWriter;
import com.github.flyinghe.domain.TestObj;
import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.XLSXWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by FlyingHe on 2018/12/23.
 */
public class XLSXWriterTest {
    private File file1 = new File("C:\\Users\\FlyingHe\\Desktop", "datas1.xlsx");
    private File file2 = new File("C:\\Users\\FlyingHe\\Desktop", "datas2.xlsx");
    private File file3 = new File("C:\\Users\\FlyingHe\\Desktop", "datas3.xlsx");
    private File file4 = new File("C:\\Users\\FlyingHe\\Desktop", "datas4.xlsx");

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
            if (!hasBlank || ((i + 1) % 10 != 0)) {
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
     * 测试空文件
     */
    @Test
    public void test0() throws Exception {
        XLSXWriter<Map<String, Object>> writer = new XLSXWriter<>();
        writer.setTitles(CommonUtils.arrayToList(new String[]{"姓名", "年龄"}));
        writer.setProperties(CommonUtils.arrayToList(new String[]{"1", "2"}));
        writer.write(new ArrayList<>());
        writer.endWrite(file1);
    }


    /**
     * 基础测试:
     * 测试每种数据类型写入情况，两种文件类型，isWriteTitle参数
     */
    @Test
    public void test1() throws WriteExcelException {
        new XLSXWriter<Map<String, Object>>().write(this.getMapDatas(200)).endWrite(this.file1);
        new XLSXWriter<TestObj>(false, 100, 0, false, null)
                .write(this.getDomainDatas(200, false))
                .endWrite(this.file2);
        new XLSXWriter<Map<String, Object>>(-1, null)
                .write(this.getMapDatas(200))
                .endWrite(this.file3);
        new XLSXWriter<TestObj>(false, -1, 0, true, null)
                .write(this.getDomainDatas(200, false))
                .endWrite(this.file4);
    }

    /**
     * 基础测试:
     * 测试每种数据类型写入情况，两种文件类型，isSkipBlankRow，limit，rowNumReserved，dateFormat参数
     */
    @Test
    public void test2() throws WriteExcelException {
        new XLSXWriter<TestObj>(true, false, 50, 10, true, null)
                .write(this.getDomainDatas(200, true))
                .endWrite(this.file1);
        new XLSXWriter<TestObj>(true, true, 5, 10, true, "【yyyy】【MM】【dd】")
                .write(this.getDomainDatas(200, true))
                .endWrite(this.file2);
        new XLSXWriter<TestObj>(true, false, 50, 0, true, null)
                .write(this.getDomainDatas(200, true))
                .endWrite(this.file3);
        new XLSXWriter<TestObj>(true, true, 50, 0, true, null)
                .write(this.getDomainDatas(200, true))
                .endWrite(this.file4);
    }

    private CellStyle produceCellStyle(XLSXWriter writer, Short ffColor, Short fontColor) {
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
        XLSXWriter<TestObj> xlsxWriter =
                new XLSXWriter<>(true, true, 5, 10, true, null);
        xlsxWriter.getDateFormatMapping().put("dateType", "yyyy.MM.dd");
        xlsxWriter.getDateFormatMapping().put("calendarType", "yyyy-MM-dd HH:mm:ss");
        xlsxWriter.putBooleanMapping("booleanType", "是", "否");
        xlsxWriter.getColumnWidthMapping().put("dateType", -40);
        xlsxWriter.getColumnWidthMapping().put("calendarType", 40);
        xlsxWriter.getCellStyleMapping().put("booleanType",
                this.produceCellStyle(xlsxWriter, IndexedColors.SKY_BLUE.getIndex(), IndexedColors.BLACK.getIndex()));
        xlsxWriter.getCellStyleMapping().put("floatType",
                this.produceCellStyle(xlsxWriter, IndexedColors.BLACK.getIndex(), IndexedColors.WHITE.getIndex()));
        xlsxWriter.getTitleCellStyleMapping().put("nullType",
                this.produceCellStyle(xlsxWriter, IndexedColors.GREEN.getIndex(), IndexedColors.BLACK.getIndex()));
        xlsxWriter.getTitleCellStyleMapping().put("longType",
                this.produceCellStyle(xlsxWriter, IndexedColors.GOLD.getIndex(), IndexedColors.WHITE.getIndex()));
        xlsxWriter.write(this.getDomainDatas(200, true)).endWrite(this.file1);


        XLSXWriter<TestObj> xlsxWriter1 =
                new XLSXWriter<>(true, false, 50, 0, true, null);
        xlsxWriter1.getDateFormatMapping().put("dateType", "yyyy.MM.dd");
        xlsxWriter1.getDateFormatMapping().put("calendarType", "yyyy-MM-dd HH:mm:ss");
        xlsxWriter1.putBooleanMapping("booleanType", "是", "否");
        xlsxWriter1.getColumnWidthMapping().put("dateType", 40);
        xlsxWriter1.getColumnWidthMapping().put("calendarType", 40);
        xlsxWriter1.getCellStyleMapping().put("booleanType",
                this.produceCellStyle(xlsxWriter1, IndexedColors.SKY_BLUE.getIndex(), IndexedColors.BLACK.getIndex()));
        xlsxWriter1.getCellStyleMapping().put("floatType",
                this.produceCellStyle(xlsxWriter1, IndexedColors.BLACK.getIndex(), IndexedColors.WHITE.getIndex()));
        xlsxWriter1.getTitleCellStyleMapping().put("nullType",
                this.produceCellStyle(xlsxWriter1, IndexedColors.GREEN.getIndex(), IndexedColors.BLACK.getIndex()));
        xlsxWriter1.getTitleCellStyleMapping().put("longType",
                this.produceCellStyle(xlsxWriter1, IndexedColors.GOLD.getIndex(), IndexedColors.WHITE.getIndex()));
        xlsxWriter1.write(this.getDomainDatas(200, true)).endWrite(this.file3);
    }

    /**
     * 功能测试:
     * 测试defaultCellStyle，defaultTitleCellStyle，defaultHeight，defaultTitleHeight，defaultColumnWidth参数
     */
    @Test
    public void test4() throws WriteExcelException {
        XLSXWriter<TestObj> xlsxWriter =
                new XLSXWriter<>(true, true, 5, 0, true, null);
        xlsxWriter.getDateFormatMapping().put("dateType", "yyyy.MM.dd");
        xlsxWriter.getDateFormatMapping().put("calendarType", "yyyy-MM-dd HH:mm:ss");
        xlsxWriter.putBooleanMapping("booleanType", "是", "否");
        xlsxWriter.getColumnWidthMapping().put("dateType", 40);
        xlsxWriter.getColumnWidthMapping().put("calendarType", 40);
        xlsxWriter.getCellStyleMapping().put("booleanType",
                this.produceCellStyle(xlsxWriter, xlsxWriter.createXLSXColor(2,3,3,51).getIndex(), IndexedColors.BLACK.getIndex()));
        xlsxWriter.getCellStyleMapping().put("floatType",
                this.produceCellStyle(xlsxWriter, IndexedColors.BLACK.getIndex(), IndexedColors.WHITE.getIndex()));
        xlsxWriter.getTitleCellStyleMapping().put("nullType",
                this.produceCellStyle(xlsxWriter, IndexedColors.GREEN.getIndex(), IndexedColors.BLACK.getIndex()));
        xlsxWriter.getTitleCellStyleMapping().put("longType",
                this.produceCellStyle(xlsxWriter, IndexedColors.GOLD.getIndex(), IndexedColors.WHITE.getIndex()));
        xlsxWriter.setDefaultCellStyle(this.produceCellStyle(xlsxWriter, IndexedColors.GREY_40_PERCENT.getIndex(),
                IndexedColors.WHITE.getIndex()));
        xlsxWriter
                .setDefaultTitleCellStyle(this.produceCellStyle(xlsxWriter, IndexedColors.GREY_80_PERCENT.getIndex(),
                        IndexedColors.WHITE.getIndex()));
        xlsxWriter.setDefaultHeight(30f);
        xlsxWriter.setDefaultTitleHeight(50f);
        xlsxWriter.setDefaultColumnWidth(35);
        xlsxWriter.write(this.getDomainDatas(200, true)).endWrite(this.file1);


        XLSXWriter<TestObj> xlsxWriter1 =
                new XLSXWriter<>(true, false, 50, 0, true, null);
        xlsxWriter1.getDateFormatMapping().put("dateType", "yyyy.MM.dd");
        xlsxWriter1.getDateFormatMapping().put("calendarType", "yyyy-MM-dd HH:mm:ss");
        xlsxWriter1.putBooleanMapping("booleanType", "是", "否");
        xlsxWriter1.getColumnWidthMapping().put("dateType", 40);
        xlsxWriter1.getColumnWidthMapping().put("calendarType", 40);
        xlsxWriter1.getCellStyleMapping().put("booleanType",
                this.produceCellStyle(xlsxWriter1, IndexedColors.SKY_BLUE.getIndex(), IndexedColors.BLACK.getIndex()));
        xlsxWriter1.getCellStyleMapping().put("floatType",
                this.produceCellStyle(xlsxWriter1, IndexedColors.BLACK.getIndex(), IndexedColors.WHITE.getIndex()));
        xlsxWriter1.getTitleCellStyleMapping().put("nullType",
                this.produceCellStyle(xlsxWriter1, IndexedColors.GREEN.getIndex(), IndexedColors.BLACK.getIndex()));
        xlsxWriter1.getTitleCellStyleMapping().put("longType",
                this.produceCellStyle(xlsxWriter1, IndexedColors.GOLD.getIndex(), IndexedColors.WHITE.getIndex()));
        xlsxWriter1.setDefaultHeight(30f);
        xlsxWriter1.setDefaultTitleHeight(20f);
        xlsxWriter1.setDefaultColumnWidth(35);
        xlsxWriter1.write(this.getDomainDatas(200, true)).endWrite(this.file3);
    }

    /**
     * 功能测试:
     * 测试properties，excludeProps，titles参数以及handleRowReserved,handleCellValue回调
     */
    @Test
    public void test5() throws WriteExcelException {
        XLSXWriter<TestObj> xlsxWriter =
                new XLSXWriter<>(true, true, 5, 3, true, null);
        xlsxWriter.setProperties(CommonUtils
                .arrayToList(new String[]{"stringType", "charType", "calendarType", "booleanType", "nullType"}));
        xlsxWriter.setTitles(CommonUtils.arrayToList(new String[]{"字符串类型", "字符类型", "日期类型", "布尔类型", "空值类型"}));
        xlsxWriter.setWriteExcelCallback(new AbstractExcelWriter.WriteExcelCallback<TestObj>() {
            @Override
            public void handleRowReserved(Sheet sheet, AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
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

            @Override
            public Boolean handleCellValue(String property, Object value, TestObj data,
                                           AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
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
                    return null;
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
                return null;
            }
        });
        xlsxWriter.write(this.getDomainDatas(200, true)).endWrite(this.file1);
        System.out.println(xlsxWriter.getAllSheetInExcel());
        System.out.println(xlsxWriter.getRealDataInExcel());
        System.out.println(xlsxWriter.getRealRowInExcel());

        /*************************************************************************************************/

        XLSXWriter<TestObj> xlsxWriter1 =
                new XLSXWriter<>(true, true, 50, 3, true, null);
        xlsxWriter1.setProperties(CommonUtils
                .arrayToList(new String[]{"stringType", "charType", "calendarType", "booleanType", "nullType"}));
        xlsxWriter1.setExcludeProps(CommonUtils.arrayToList(
                new String[]{"dateType", "byteType", "shortType", "integerType", "longType", "floatType", "nullType",
                        "doubleType"}));
        xlsxWriter1.setWriteExcelCallback(new AbstractExcelWriter.WriteExcelCallback<TestObj>() {
            @Override
            public void handleRowReserved(Sheet sheet, AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
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

            @Override
            public Boolean handleCellValue(String property, Object value, TestObj data,
                                           AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
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
                        return null;
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
                        return null;
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
                return null;
            }
        });

        xlsxWriter1.write(this.getDomainDatas(200, true)).endWrite(this.file3);
        System.out.println(xlsxWriter1.getAllSheetInExcel());
        System.out.println(xlsxWriter1.getRealDataInExcel());
        System.out.println(xlsxWriter1.getRealRowInExcel());

    }

    /**
     * 功能测试:
     * 测试properties，excludeProps，titles参数以及handleRowReserved,handleCellValue回调
     */
    @Test
    public void test6() throws Exception {
        XLSXWriter<TestObj> xlsxWriter =
                new XLSXWriter<>(true, true, 5, 3, true, null);
        xlsxWriter.setProperties(CommonUtils
                .arrayToList(new String[]{"stringType", "charType", "calendarType", "booleanType", "nullType"}));
        xlsxWriter.setTitles(CommonUtils.arrayToList(new String[]{"字符串类型", "字符类型", "日期类型", "布尔类型", "空值类型"}));
        xlsxWriter.setWriteExcelCallback(new AbstractExcelWriter.WriteExcelCallback<TestObj>() {
            @Override
            public void handleRowReserved(Sheet sheet, AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
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

            @Override
            public Boolean handleCellValue(String property, Object value, TestObj data,
                                           AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
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
                    return null;
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
                return null;
            }
        });
        xlsxWriter.write(this.getDomainDatas(200, true)).endWrite(this.file1);
        System.out.println(xlsxWriter.getAllSheetInExcel());
        System.out.println(xlsxWriter.getRealDataInExcel());
        System.out.println(xlsxWriter.getRealRowInExcel());

        /*************************************************************************************************/

        XLSXWriter<TestObj> xlsxWriter1 =
                new XLSXWriter<>(true, true, -1, 3, true, null);
        xlsxWriter1.setProperties(CommonUtils
                .arrayToList(new String[]{"no", "stringType", "charType", "calendarType", "booleanType", "nullType"}));
        xlsxWriter1.setExcludeProps(CommonUtils.arrayToList(
                new String[]{"dateType", "byteType", "shortType", "integerType", "longType", "floatType", "nullType",
                        "doubleType"}));
        xlsxWriter1.setWriteExcelCallback(new AbstractExcelWriter.WriteExcelCallback<TestObj>() {
            @Override
            public Boolean formFeedManually(int currentRowInSheet, Sheet currentSheet,
                                            AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
                return !writer.isBlankLastRow() && writer.getVarPool("no", Integer.class) >= 50;
            }

            @Override
            public void handleRowReserved(Sheet sheet, AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
                writer.getVarPool().put("no", 0);
                int sheetIndex = writer.getWorkbook().getSheetIndex(sheet);
                writer.getWorkbook().setSheetName(sheetIndex, String.format("工作簿%d", sheetIndex + 1));
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

            @Override
            public boolean beforeWritePerRow(TestObj data, int currentRowInSheet, Row lastRow, Sheet currentSheet,
                                             AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
                if (!writer.isBlankLastRow()) {
                    writer.getVarPool().put("no", writer.getVarPool("no", Integer.class) + 1);
                }
                if (null == data.getBooleanType() || data.getBooleanType()) {
                    return true;
                }
                currentRowInSheet++;
                if (!writer.getCellStylePool().containsKey("booleanTypeFalse")) {
                    CellStyle cellStyle = writer.createCellStyle();
                    cellStyle.cloneStyleFrom(writer.getDefaultCellStyle());
                    cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    writer.getCellStylePool().put("booleanTypeFalse", cellStyle);
                }

                if (writer.isBlankLastRow()) {
                    currentRowInSheet -= 1;
                }
                currentSheet.addMergedRegion(new CellRangeAddress(currentRowInSheet, currentRowInSheet + 1, 0, 0));
                currentSheet.addMergedRegion(new CellRangeAddress(currentRowInSheet, currentRowInSheet + 1, 3, 3));
                currentSheet.addMergedRegion(new CellRangeAddress(currentRowInSheet, currentRowInSheet + 1, 4, 4));
                Row row = writer.isBlankLastRow() ? lastRow : currentSheet.createRow(currentRowInSheet);


                writer.createCell(row, 0, writer.getDefaultCellStyle())
                        .setCellValue(writer.getVarPool("no", Integer.class));

                writer.createCell(row, 1, writer.getDefaultCellStyle()).setCellValue(data.getStringType());

                writer.createCell(row, 2, writer.getDefaultCellStyle()).setCellValue(data.getCharType());

                writer.createCell(row, 3, writer.getCellStylePool().get("booleanTypeFalse"))
                        .setCellValue(DateFormatUtils.format(data.getCalendarType(), AbstractExcelWriter.DATE_PATTERN));

                writer.createCell(row, 4, writer.getCellStylePool().get("booleanTypeFalse")).setCellValue("不合格");

                try {
                    ClientAnchor anchor =
                            writer.getCurrentPatriarch()
                                    .createAnchor(0, 0, 0, 0, 5, currentRowInSheet, 7, currentRowInSheet + 2);
                    anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    BufferedImage bi = ImageIO.read(new File("C:\\Users\\FlyingHe\\Desktop\\test.jpg"));
                    ImageIO.write(bi, "jpeg", bos);
                    writer.getCurrentPatriarch().createPicture(anchor,
                            writer.getWorkbook().addPicture(bos.toByteArray(), Workbook.PICTURE_TYPE_JPEG));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new WriteExcelException(e);
                }

                writer.next(writer.isBlankLastRow() ? 1 : 2);
                return false;
            }

            @Override
            public Boolean handleCellValue(String property, Object value, TestObj data,
                                           AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
                if ("no".equals(property)) {
                    writer.putCellValueToMap(writer.getVarPool().get("no"));
                    return true;
                }
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
                        return null;
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
                        return null;
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
                return null;
            }

            @Override
            public void afterWritePerRow(TestObj data, int currentRowInSheet, Row currentRow, Sheet currentSheet,
                                         AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
                if (writer.isBlankLastRow()) {
                    currentRow.getCell(0).setCellValue("");
                }
            }

        });
        String json =
                "[{\"stringType\":\"StringType0\",\"charType\":\"\\u0000\",\"dateType\":1577632698261,\"calendarType\":1577632698261,\"booleanType\":true,\"byteType\":0,\"shortType\":0,\"integerType\":0,\"longType\":0,\"floatType\":0.0,\"doubleType\":0.0,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType1\",\"charType\":\"\\u0001\",\"dateType\":1577719098261,\"calendarType\":1577719098261,\"booleanType\":false,\"byteType\":1,\"shortType\":1,\"integerType\":1,\"longType\":1,\"floatType\":98.48185,\"doubleType\":89.06491506185058,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType2\",\"charType\":\"\\u0002\",\"dateType\":1577805498261,\"calendarType\":1577805498261,\"booleanType\":true,\"byteType\":2,\"shortType\":2,\"integerType\":2,\"longType\":2,\"floatType\":115.596924,\"doubleType\":149.14665022801032,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType3\",\"charType\":\"\\u0003\",\"dateType\":1577891898261,\"calendarType\":1577891898261,\"booleanType\":false,\"byteType\":3,\"shortType\":3,\"integerType\":3,\"longType\":3,\"floatType\":146.1343,\"doubleType\":196.617897092616,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType4\",\"charType\":\"\\u0004\",\"dateType\":1577978298261,\"calendarType\":1577978298261,\"booleanType\":true,\"byteType\":4,\"shortType\":4,\"integerType\":4,\"longType\":4,\"floatType\":101.903625,\"doubleType\":196.34393784853913,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType5\",\"charType\":\"\\u0005\",\"dateType\":1578064698261,\"calendarType\":1578064698261,\"booleanType\":false,\"byteType\":5,\"shortType\":5,\"integerType\":5,\"longType\":5,\"floatType\":364.63293,\"doubleType\":105.71964220794567,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType6\",\"charType\":\"\\u0006\",\"dateType\":1578151098261,\"calendarType\":1578151098261,\"booleanType\":true,\"byteType\":6,\"shortType\":6,\"integerType\":6,\"longType\":6,\"floatType\":236.43683,\"doubleType\":451.27315917430207,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType7\",\"charType\":\"\\u0007\",\"dateType\":1578237498261,\"calendarType\":1578237498261,\"booleanType\":true,\"byteType\":7,\"shortType\":7,\"integerType\":7,\"longType\":7,\"floatType\":496.38943,\"doubleType\":210.52161909126417,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType8\",\"charType\":\"\\b\",\"dateType\":1578323898261,\"calendarType\":1578323898261,\"booleanType\":false,\"byteType\":8,\"shortType\":8,\"integerType\":8,\"longType\":8,\"floatType\":136.20491,\"doubleType\":513.5019668008987,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType10\",\"charType\":\"\\n\",\"dateType\":1578496698261,\"calendarType\":1578496698261,\"booleanType\":false,\"byteType\":10,\"shortType\":10,\"integerType\":10,\"longType\":10,\"floatType\":839.9895,\"doubleType\":88.56446704765818,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType11\",\"charType\":\"\\u000B\",\"dateType\":1578583098261,\"calendarType\":1578583098261,\"booleanType\":false,\"byteType\":11,\"shortType\":11,\"integerType\":11,\"longType\":11,\"floatType\":299.67505,\"doubleType\":174.4697525991819,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType12\",\"charType\":\"\\f\",\"dateType\":1578669498261,\"calendarType\":1578669498261,\"booleanType\":true,\"byteType\":12,\"shortType\":12,\"integerType\":12,\"longType\":12,\"floatType\":909.73883,\"doubleType\":124.28040453159919,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType13\",\"charType\":\"\\r\",\"dateType\":1578755898261,\"calendarType\":1578755898261,\"booleanType\":true,\"byteType\":13,\"shortType\":13,\"integerType\":13,\"longType\":13,\"floatType\":745.9876,\"doubleType\":430.52744861500634,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType14\",\"charType\":\"\\u000E\",\"dateType\":1578842298261,\"calendarType\":1578842298261,\"booleanType\":false,\"byteType\":14,\"shortType\":14,\"integerType\":14,\"longType\":14,\"floatType\":1130.9913,\"doubleType\":1023.5794020833591,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType15\",\"charType\":\"\\u000F\",\"dateType\":1578928698261,\"calendarType\":1578928698261,\"booleanType\":false,\"byteType\":15,\"shortType\":15,\"integerType\":15,\"longType\":15,\"floatType\":627.42706,\"doubleType\":924.4823000086355,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType16\",\"charType\":\"\\u0010\",\"dateType\":1579015098261,\"calendarType\":1579015098261,\"booleanType\":false,\"byteType\":16,\"shortType\":16,\"integerType\":16,\"longType\":16,\"floatType\":800.54083,\"doubleType\":21.96037908252162,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType17\",\"charType\":\"\\u0011\",\"dateType\":1579101498261,\"calendarType\":1579101498261,\"booleanType\":false,\"byteType\":17,\"shortType\":17,\"integerType\":17,\"longType\":17,\"floatType\":879.477,\"doubleType\":184.5572669085253,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType18\",\"charType\":\"\\u0012\",\"dateType\":1579187898261,\"calendarType\":1579187898261,\"booleanType\":false,\"byteType\":18,\"shortType\":18,\"integerType\":18,\"longType\":18,\"floatType\":317.65207,\"doubleType\":1584.9753904539473,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType20\",\"charType\":\"\\u0014\",\"dateType\":1579360698261,\"calendarType\":1579360698261,\"booleanType\":true,\"byteType\":20,\"shortType\":20,\"integerType\":20,\"longType\":20,\"floatType\":179.54301,\"doubleType\":260.6752737114231,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType21\",\"charType\":\"\\u0015\",\"dateType\":1579447098261,\"calendarType\":1579447098261,\"booleanType\":true,\"byteType\":21,\"shortType\":21,\"integerType\":21,\"longType\":21,\"floatType\":1460.6332,\"doubleType\":1738.3692189074673,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType22\",\"charType\":\"\\u0016\",\"dateType\":1579533498261,\"calendarType\":1579533498261,\"booleanType\":true,\"byteType\":22,\"shortType\":22,\"integerType\":22,\"longType\":22,\"floatType\":1471.2285,\"doubleType\":1782.8243686686017,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType23\",\"charType\":\"\\u0017\",\"dateType\":1579619898261,\"calendarType\":1579619898261,\"booleanType\":false,\"byteType\":23,\"shortType\":23,\"integerType\":23,\"longType\":23,\"floatType\":501.46332,\"doubleType\":1154.5146027820329,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType24\",\"charType\":\"\\u0018\",\"dateType\":1579706298261,\"calendarType\":1579706298261,\"booleanType\":true,\"byteType\":24,\"shortType\":24,\"integerType\":24,\"longType\":24,\"floatType\":2285.6064,\"doubleType\":790.8170233637699,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType25\",\"charType\":\"\\u0019\",\"dateType\":1579792698261,\"calendarType\":1579792698261,\"booleanType\":true,\"byteType\":25,\"shortType\":25,\"integerType\":25,\"longType\":25,\"floatType\":136.20198,\"doubleType\":1500.024212822809,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType26\",\"charType\":\"\\u001A\",\"dateType\":1579879098261,\"calendarType\":1579879098261,\"booleanType\":true,\"byteType\":26,\"shortType\":26,\"integerType\":26,\"longType\":26,\"floatType\":466.88904,\"doubleType\":1600.8050170908573,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType27\",\"charType\":\"\\u001B\",\"dateType\":1579965498261,\"calendarType\":1579965498261,\"booleanType\":false,\"byteType\":27,\"shortType\":27,\"integerType\":27,\"longType\":27,\"floatType\":301.76315,\"doubleType\":2683.4199320210496,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType28\",\"charType\":\"\\u001C\",\"dateType\":1580051898261,\"calendarType\":1580051898261,\"booleanType\":false,\"byteType\":28,\"shortType\":28,\"integerType\":28,\"longType\":28,\"floatType\":2174.5078,\"doubleType\":1130.6841680994908,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType30\",\"charType\":\"\\u001E\",\"dateType\":1580224698261,\"calendarType\":1580224698261,\"booleanType\":true,\"byteType\":30,\"shortType\":30,\"integerType\":30,\"longType\":30,\"floatType\":2051.9397,\"doubleType\":32.38076066434681,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType31\",\"charType\":\"\\u001F\",\"dateType\":1580311098261,\"calendarType\":1580311098261,\"booleanType\":true,\"byteType\":31,\"shortType\":31,\"integerType\":31,\"longType\":31,\"floatType\":1354.4893,\"doubleType\":2296.5790780742627,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType32\",\"charType\":\" \",\"dateType\":1580397498261,\"calendarType\":1580397498261,\"booleanType\":true,\"byteType\":32,\"shortType\":32,\"integerType\":32,\"longType\":32,\"floatType\":663.4384,\"doubleType\":1142.7482693129637,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType33\",\"charType\":\"!\",\"dateType\":1580483898261,\"calendarType\":1580483898261,\"booleanType\":true,\"byteType\":33,\"shortType\":33,\"integerType\":33,\"longType\":33,\"floatType\":1235.5437,\"doubleType\":703.5494537971156,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType34\",\"charType\":\"\\\"\",\"dateType\":1580570298261,\"calendarType\":1580570298261,\"booleanType\":true,\"byteType\":34,\"shortType\":34,\"integerType\":34,\"longType\":34,\"floatType\":430.0982,\"doubleType\":881.5472392443705,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType35\",\"charType\":\"#\",\"dateType\":1580656698261,\"calendarType\":1580656698261,\"booleanType\":false,\"byteType\":35,\"shortType\":35,\"integerType\":35,\"longType\":35,\"floatType\":768.57294,\"doubleType\":1403.3221628050198,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType36\",\"charType\":\"$\",\"dateType\":1580743098261,\"calendarType\":1580743098261,\"booleanType\":true,\"byteType\":36,\"shortType\":36,\"integerType\":36,\"longType\":36,\"floatType\":737.4965,\"doubleType\":54.13690833489944,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType37\",\"charType\":\"%\",\"dateType\":1580829498261,\"calendarType\":1580829498261,\"booleanType\":true,\"byteType\":37,\"shortType\":37,\"integerType\":37,\"longType\":37,\"floatType\":3394.186,\"doubleType\":1190.2750948676046,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType38\",\"charType\":\"&\",\"dateType\":1580915898261,\"calendarType\":1580915898261,\"booleanType\":true,\"byteType\":38,\"shortType\":38,\"integerType\":38,\"longType\":38,\"floatType\":1714.9662,\"doubleType\":1125.6207303614442,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType40\",\"charType\":\"(\",\"dateType\":1581088698261,\"calendarType\":1581088698261,\"booleanType\":false,\"byteType\":40,\"shortType\":40,\"integerType\":40,\"longType\":40,\"floatType\":111.076355,\"doubleType\":3670.9613515016126,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType41\",\"charType\":\")\",\"dateType\":1581175098261,\"calendarType\":1581175098261,\"booleanType\":true,\"byteType\":41,\"shortType\":41,\"integerType\":41,\"longType\":41,\"floatType\":1369.3687,\"doubleType\":925.8076327806582,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType42\",\"charType\":\"*\",\"dateType\":1581261498261,\"calendarType\":1581261498261,\"booleanType\":false,\"byteType\":42,\"shortType\":42,\"integerType\":42,\"longType\":42,\"floatType\":137.09218,\"doubleType\":4035.7690211417043,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType43\",\"charType\":\"+\",\"dateType\":1581347898261,\"calendarType\":1581347898261,\"booleanType\":true,\"byteType\":43,\"shortType\":43,\"integerType\":43,\"longType\":43,\"floatType\":448.2361,\"doubleType\":1993.8526834484967,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType44\",\"charType\":\",\",\"dateType\":1581434298261,\"calendarType\":1581434298261,\"booleanType\":false,\"byteType\":44,\"shortType\":44,\"integerType\":44,\"longType\":44,\"floatType\":2666.6567,\"doubleType\":449.1460572435601,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType45\",\"charType\":\"-\",\"dateType\":1581520698261,\"calendarType\":1581520698261,\"booleanType\":true,\"byteType\":45,\"shortType\":45,\"integerType\":45,\"longType\":45,\"floatType\":3886.3975,\"doubleType\":1253.455915804534,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType46\",\"charType\":\".\",\"dateType\":1581607098261,\"calendarType\":1581607098261,\"booleanType\":true,\"byteType\":46,\"shortType\":46,\"integerType\":46,\"longType\":46,\"floatType\":1474.5902,\"doubleType\":811.9715715648897,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType47\",\"charType\":\"/\",\"dateType\":1581693498261,\"calendarType\":1581693498261,\"booleanType\":true,\"byteType\":47,\"shortType\":47,\"integerType\":47,\"longType\":47,\"floatType\":2223.647,\"doubleType\":4032.680839274393,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType48\",\"charType\":\"0\",\"dateType\":1581779898261,\"calendarType\":1581779898261,\"booleanType\":false,\"byteType\":48,\"shortType\":48,\"integerType\":48,\"longType\":48,\"floatType\":4459.595,\"doubleType\":1394.550782749389,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType50\",\"charType\":\"2\",\"dateType\":1581952698261,\"calendarType\":1581952698261,\"booleanType\":true,\"byteType\":50,\"shortType\":50,\"integerType\":50,\"longType\":50,\"floatType\":2623.8647,\"doubleType\":4573.265125337373,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType51\",\"charType\":\"3\",\"dateType\":1582039098261,\"calendarType\":1582039098261,\"booleanType\":false,\"byteType\":51,\"shortType\":51,\"integerType\":51,\"longType\":51,\"floatType\":959.59015,\"doubleType\":3376.507964772061,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType52\",\"charType\":\"4\",\"dateType\":1582125498261,\"calendarType\":1582125498261,\"booleanType\":false,\"byteType\":52,\"shortType\":52,\"integerType\":52,\"longType\":52,\"floatType\":1954.1365,\"doubleType\":5107.999636212774,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType53\",\"charType\":\"5\",\"dateType\":1582211898261,\"calendarType\":1582211898261,\"booleanType\":true,\"byteType\":53,\"shortType\":53,\"integerType\":53,\"longType\":53,\"floatType\":897.6856,\"doubleType\":3522.6504354287536,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType54\",\"charType\":\"6\",\"dateType\":1582298298261,\"calendarType\":1582298298261,\"booleanType\":true,\"byteType\":54,\"shortType\":54,\"integerType\":54,\"longType\":54,\"floatType\":3661.426,\"doubleType\":3594.534756997711,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType55\",\"charType\":\"7\",\"dateType\":1582384698261,\"calendarType\":1582384698261,\"booleanType\":true,\"byteType\":55,\"shortType\":55,\"integerType\":55,\"longType\":55,\"floatType\":790.2205,\"doubleType\":184.14520528897683,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType56\",\"charType\":\"8\",\"dateType\":1582471098261,\"calendarType\":1582471098261,\"booleanType\":false,\"byteType\":56,\"shortType\":56,\"integerType\":56,\"longType\":56,\"floatType\":5196.5806,\"doubleType\":2827.8728282500365,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType57\",\"charType\":\"9\",\"dateType\":1582557498261,\"calendarType\":1582557498261,\"booleanType\":true,\"byteType\":57,\"shortType\":57,\"integerType\":57,\"longType\":57,\"floatType\":2377.1028,\"doubleType\":4172.105181208781,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType58\",\"charType\":\":\",\"dateType\":1582643898261,\"calendarType\":1582643898261,\"booleanType\":false,\"byteType\":58,\"shortType\":58,\"integerType\":58,\"longType\":58,\"floatType\":1789.0131,\"doubleType\":284.4135150467356,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType60\",\"charType\":\"<\",\"dateType\":1582816698261,\"calendarType\":1582816698261,\"booleanType\":false,\"byteType\":60,\"shortType\":60,\"integerType\":60,\"longType\":60,\"floatType\":5678.075,\"doubleType\":3292.168433066053,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType61\",\"charType\":\"=\",\"dateType\":1582903098261,\"calendarType\":1582903098261,\"booleanType\":true,\"byteType\":61,\"shortType\":61,\"integerType\":61,\"longType\":61,\"floatType\":3035.47,\"doubleType\":1163.4844158530307,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType62\",\"charType\":\">\",\"dateType\":1582989498261,\"calendarType\":1582989498261,\"booleanType\":false,\"byteType\":62,\"shortType\":62,\"integerType\":62,\"longType\":62,\"floatType\":1058.2662,\"doubleType\":604.2146871196935,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType63\",\"charType\":\"?\",\"dateType\":1583075898261,\"calendarType\":1583075898261,\"booleanType\":true,\"byteType\":63,\"shortType\":63,\"integerType\":63,\"longType\":63,\"floatType\":1127.4906,\"doubleType\":5124.774173316448,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType64\",\"charType\":\"@\",\"dateType\":1583162298261,\"calendarType\":1583162298261,\"booleanType\":false,\"byteType\":64,\"shortType\":64,\"integerType\":64,\"longType\":64,\"floatType\":1637.7312,\"doubleType\":3365.813122447047,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType65\",\"charType\":\"A\",\"dateType\":1583248698261,\"calendarType\":1583248698261,\"booleanType\":true,\"byteType\":65,\"shortType\":65,\"integerType\":65,\"longType\":65,\"floatType\":4571.846,\"doubleType\":2082.472293877771,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType66\",\"charType\":\"B\",\"dateType\":1583335098261,\"calendarType\":1583335098261,\"booleanType\":false,\"byteType\":66,\"shortType\":66,\"integerType\":66,\"longType\":66,\"floatType\":5737.161,\"doubleType\":5971.516512820773,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType67\",\"charType\":\"C\",\"dateType\":1583421498261,\"calendarType\":1583421498261,\"booleanType\":false,\"byteType\":67,\"shortType\":67,\"integerType\":67,\"longType\":67,\"floatType\":4858.7344,\"doubleType\":4520.7093607076,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType68\",\"charType\":\"D\",\"dateType\":1583507898261,\"calendarType\":1583507898261,\"booleanType\":true,\"byteType\":68,\"shortType\":68,\"integerType\":68,\"longType\":68,\"floatType\":4600.0396,\"doubleType\":6559.521913525385,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType70\",\"charType\":\"F\",\"dateType\":1583680698261,\"calendarType\":1583680698261,\"booleanType\":false,\"byteType\":70,\"shortType\":70,\"integerType\":70,\"longType\":70,\"floatType\":4644.016,\"doubleType\":1160.879851197947,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType71\",\"charType\":\"G\",\"dateType\":1583767098261,\"calendarType\":1583767098261,\"booleanType\":false,\"byteType\":71,\"shortType\":71,\"integerType\":71,\"longType\":71,\"floatType\":6987.6123,\"doubleType\":4086.4221988105446,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType72\",\"charType\":\"H\",\"dateType\":1583853498261,\"calendarType\":1583853498261,\"booleanType\":false,\"byteType\":72,\"shortType\":72,\"integerType\":72,\"longType\":72,\"floatType\":3765.8872,\"doubleType\":1685.0384419458037,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType73\",\"charType\":\"I\",\"dateType\":1583939898261,\"calendarType\":1583939898261,\"booleanType\":true,\"byteType\":73,\"shortType\":73,\"integerType\":73,\"longType\":73,\"floatType\":2477.9272,\"doubleType\":900.2391761619187,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType74\",\"charType\":\"J\",\"dateType\":1584026298261,\"calendarType\":1584026298261,\"booleanType\":true,\"byteType\":74,\"shortType\":74,\"integerType\":74,\"longType\":74,\"floatType\":7049.2344,\"doubleType\":2492.6868394188377,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType75\",\"charType\":\"K\",\"dateType\":1584112698261,\"calendarType\":1584112698261,\"booleanType\":true,\"byteType\":75,\"shortType\":75,\"integerType\":75,\"longType\":75,\"floatType\":3309.8281,\"doubleType\":5446.677718324912,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType76\",\"charType\":\"L\",\"dateType\":1584199098261,\"calendarType\":1584199098261,\"booleanType\":false,\"byteType\":76,\"shortType\":76,\"integerType\":76,\"longType\":76,\"floatType\":3128.3213,\"doubleType\":149.3427213359635,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType77\",\"charType\":\"M\",\"dateType\":1584285498261,\"calendarType\":1584285498261,\"booleanType\":true,\"byteType\":77,\"shortType\":77,\"integerType\":77,\"longType\":77,\"floatType\":3230.4773,\"doubleType\":442.51299031836913,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType78\",\"charType\":\"N\",\"dateType\":1584371898261,\"calendarType\":1584371898261,\"booleanType\":false,\"byteType\":78,\"shortType\":78,\"integerType\":78,\"longType\":78,\"floatType\":7596.4795,\"doubleType\":512.3286374490821,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType80\",\"charType\":\"P\",\"dateType\":1584544698261,\"calendarType\":1584544698261,\"booleanType\":false,\"byteType\":80,\"shortType\":80,\"integerType\":80,\"longType\":80,\"floatType\":3140.6575,\"doubleType\":3128.0195952313097,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType81\",\"charType\":\"Q\",\"dateType\":1584631098261,\"calendarType\":1584631098261,\"booleanType\":true,\"byteType\":81,\"shortType\":81,\"integerType\":81,\"longType\":81,\"floatType\":4885.074,\"doubleType\":6834.664830057242,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType82\",\"charType\":\"R\",\"dateType\":1584717498261,\"calendarType\":1584717498261,\"booleanType\":false,\"byteType\":82,\"shortType\":82,\"integerType\":82,\"longType\":82,\"floatType\":1878.2964,\"doubleType\":3052.04034838878,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType83\",\"charType\":\"S\",\"dateType\":1584803898261,\"calendarType\":1584803898261,\"booleanType\":true,\"byteType\":83,\"shortType\":83,\"integerType\":83,\"longType\":83,\"floatType\":5829.0405,\"doubleType\":6194.611689901902,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType84\",\"charType\":\"T\",\"dateType\":1584890298261,\"calendarType\":1584890298261,\"booleanType\":false,\"byteType\":84,\"shortType\":84,\"integerType\":84,\"longType\":84,\"floatType\":5092.918,\"doubleType\":7811.786835248482,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType85\",\"charType\":\"U\",\"dateType\":1584976698261,\"calendarType\":1584976698261,\"booleanType\":false,\"byteType\":85,\"shortType\":85,\"integerType\":85,\"longType\":85,\"floatType\":3075.2307,\"doubleType\":7783.786597723218,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType86\",\"charType\":\"V\",\"dateType\":1585063098261,\"calendarType\":1585063098261,\"booleanType\":false,\"byteType\":86,\"shortType\":86,\"integerType\":86,\"longType\":86,\"floatType\":6746.0254,\"doubleType\":5960.785408318837,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType87\",\"charType\":\"W\",\"dateType\":1585149498261,\"calendarType\":1585149498261,\"booleanType\":true,\"byteType\":87,\"shortType\":87,\"integerType\":87,\"longType\":87,\"floatType\":62.10124,\"doubleType\":2562.8955476363676,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType88\",\"charType\":\"X\",\"dateType\":1585235898261,\"calendarType\":1585235898261,\"booleanType\":true,\"byteType\":88,\"shortType\":88,\"integerType\":88,\"longType\":88,\"floatType\":8765.803,\"doubleType\":2210.7524279216145,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType90\",\"charType\":\"Z\",\"dateType\":1585408698261,\"calendarType\":1585408698261,\"booleanType\":true,\"byteType\":90,\"shortType\":90,\"integerType\":90,\"longType\":90,\"floatType\":760.1992,\"doubleType\":3626.2484665858815,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType91\",\"charType\":\"[\",\"dateType\":1585495098261,\"calendarType\":1585495098261,\"booleanType\":true,\"byteType\":91,\"shortType\":91,\"integerType\":91,\"longType\":91,\"floatType\":9007.241,\"doubleType\":5545.531863034056,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType92\",\"charType\":\"\\\\\",\"dateType\":1585581498261,\"calendarType\":1585581498261,\"booleanType\":false,\"byteType\":92,\"shortType\":92,\"integerType\":92,\"longType\":92,\"floatType\":98.95206,\"doubleType\":1336.5762363630124,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType93\",\"charType\":\"]\",\"dateType\":1585667898261,\"calendarType\":1585667898261,\"booleanType\":false,\"byteType\":93,\"shortType\":93,\"integerType\":93,\"longType\":93,\"floatType\":538.9618,\"doubleType\":2100.654571414474,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType94\",\"charType\":\"^\",\"dateType\":1585754298261,\"calendarType\":1585754298261,\"booleanType\":true,\"byteType\":94,\"shortType\":94,\"integerType\":94,\"longType\":94,\"floatType\":8119.5825,\"doubleType\":3581.89372320061,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType95\",\"charType\":\"_\",\"dateType\":1585840698261,\"calendarType\":1585840698261,\"booleanType\":false,\"byteType\":95,\"shortType\":95,\"integerType\":95,\"longType\":95,\"floatType\":7906.879,\"doubleType\":884.075770420293,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType96\",\"charType\":\"`\",\"dateType\":1585927098261,\"calendarType\":1585927098261,\"booleanType\":true,\"byteType\":96,\"shortType\":96,\"integerType\":96,\"longType\":96,\"floatType\":1822.7966,\"doubleType\":9152.201369883885,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType97\",\"charType\":\"a\",\"dateType\":1586013498261,\"calendarType\":1586013498261,\"booleanType\":false,\"byteType\":97,\"shortType\":97,\"integerType\":97,\"longType\":97,\"floatType\":2274.7864,\"doubleType\":4122.662295092675,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType98\",\"charType\":\"b\",\"dateType\":1586099898261,\"calendarType\":1586099898261,\"booleanType\":false,\"byteType\":98,\"shortType\":98,\"integerType\":98,\"longType\":98,\"floatType\":8497.097,\"doubleType\":3577.677570638035,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType100\",\"charType\":\"d\",\"dateType\":1586272698261,\"calendarType\":1586272698261,\"booleanType\":false,\"byteType\":100,\"shortType\":100,\"integerType\":100,\"longType\":100,\"floatType\":2331.1895,\"doubleType\":6482.045038967843,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType101\",\"charType\":\"e\",\"dateType\":1586359098261,\"calendarType\":1586359098261,\"booleanType\":false,\"byteType\":101,\"shortType\":101,\"integerType\":101,\"longType\":101,\"floatType\":1788.943,\"doubleType\":3180.366584462254,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType102\",\"charType\":\"f\",\"dateType\":1586445498261,\"calendarType\":1586445498261,\"booleanType\":false,\"byteType\":102,\"shortType\":102,\"integerType\":102,\"longType\":102,\"floatType\":4863.7437,\"doubleType\":5684.47808085574,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType103\",\"charType\":\"g\",\"dateType\":1586531898261,\"calendarType\":1586531898261,\"booleanType\":true,\"byteType\":103,\"shortType\":103,\"integerType\":103,\"longType\":103,\"floatType\":1155.7369,\"doubleType\":8878.202042673962,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType104\",\"charType\":\"h\",\"dateType\":1586618298261,\"calendarType\":1586618298261,\"booleanType\":true,\"byteType\":104,\"shortType\":104,\"integerType\":104,\"longType\":104,\"floatType\":4673.7524,\"doubleType\":7220.305067459013,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType105\",\"charType\":\"i\",\"dateType\":1586704698261,\"calendarType\":1586704698261,\"booleanType\":false,\"byteType\":105,\"shortType\":105,\"integerType\":105,\"longType\":105,\"floatType\":4930.3804,\"doubleType\":783.9677191490415,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType106\",\"charType\":\"j\",\"dateType\":1586791098261,\"calendarType\":1586791098261,\"booleanType\":true,\"byteType\":106,\"shortType\":106,\"integerType\":106,\"longType\":106,\"floatType\":5538.441,\"doubleType\":2504.40049330198,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType107\",\"charType\":\"k\",\"dateType\":1586877498261,\"calendarType\":1586877498261,\"booleanType\":false,\"byteType\":107,\"shortType\":107,\"integerType\":107,\"longType\":107,\"floatType\":5062.7944,\"doubleType\":6817.241577344445,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType108\",\"charType\":\"l\",\"dateType\":1586963898261,\"calendarType\":1586963898261,\"booleanType\":true,\"byteType\":108,\"shortType\":108,\"integerType\":108,\"longType\":108,\"floatType\":1848.6256,\"doubleType\":10120.248076253987,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType110\",\"charType\":\"n\",\"dateType\":1587136698261,\"calendarType\":1587136698261,\"booleanType\":false,\"byteType\":110,\"shortType\":110,\"integerType\":110,\"longType\":110,\"floatType\":5514.1045,\"doubleType\":402.80763596892564,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType111\",\"charType\":\"o\",\"dateType\":1587223098261,\"calendarType\":1587223098261,\"booleanType\":false,\"byteType\":111,\"shortType\":111,\"integerType\":111,\"longType\":111,\"floatType\":7325.1006,\"doubleType\":10613.315880556373,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType112\",\"charType\":\"p\",\"dateType\":1587309498269,\"calendarType\":1587309498269,\"booleanType\":false,\"byteType\":112,\"shortType\":112,\"integerType\":112,\"longType\":112,\"floatType\":11159.888,\"doubleType\":7117.967405985852,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType113\",\"charType\":\"q\",\"dateType\":1587395898269,\"calendarType\":1587395898269,\"booleanType\":false,\"byteType\":113,\"shortType\":113,\"integerType\":113,\"longType\":113,\"floatType\":7999.698,\"doubleType\":8267.982133354348,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType114\",\"charType\":\"r\",\"dateType\":1587482298269,\"calendarType\":1587482298269,\"booleanType\":false,\"byteType\":114,\"shortType\":114,\"integerType\":114,\"longType\":114,\"floatType\":8973.348,\"doubleType\":1636.9750825657238,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType115\",\"charType\":\"s\",\"dateType\":1587568698269,\"calendarType\":1587568698269,\"booleanType\":true,\"byteType\":115,\"shortType\":115,\"integerType\":115,\"longType\":115,\"floatType\":11481.807,\"doubleType\":4660.393589904851,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType116\",\"charType\":\"t\",\"dateType\":1587655098269,\"calendarType\":1587655098269,\"booleanType\":true,\"byteType\":116,\"shortType\":116,\"integerType\":116,\"longType\":116,\"floatType\":9989.253,\"doubleType\":8690.221568026398,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType117\",\"charType\":\"u\",\"dateType\":1587741498269,\"calendarType\":1587741498269,\"booleanType\":true,\"byteType\":117,\"shortType\":117,\"integerType\":117,\"longType\":117,\"floatType\":10641.106,\"doubleType\":8084.049706020367,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType118\",\"charType\":\"v\",\"dateType\":1587827898269,\"calendarType\":1587827898269,\"booleanType\":false,\"byteType\":118,\"shortType\":118,\"integerType\":118,\"longType\":118,\"floatType\":1115.8738,\"doubleType\":5224.911284895932,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType120\",\"charType\":\"x\",\"dateType\":1588000698269,\"calendarType\":1588000698269,\"booleanType\":true,\"byteType\":120,\"shortType\":120,\"integerType\":120,\"longType\":120,\"floatType\":6160.6494,\"doubleType\":5946.49510923016,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType121\",\"charType\":\"y\",\"dateType\":1588087098269,\"calendarType\":1588087098269,\"booleanType\":true,\"byteType\":121,\"shortType\":121,\"integerType\":121,\"longType\":121,\"floatType\":6831.1,\"doubleType\":10551.907795907506,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType122\",\"charType\":\"z\",\"dateType\":1588173498269,\"calendarType\":1588173498269,\"booleanType\":false,\"byteType\":122,\"shortType\":122,\"integerType\":122,\"longType\":122,\"floatType\":8728.313,\"doubleType\":530.7828171612589,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType123\",\"charType\":\"{\",\"dateType\":1588259898269,\"calendarType\":1588259898269,\"booleanType\":true,\"byteType\":123,\"shortType\":123,\"integerType\":123,\"longType\":123,\"floatType\":10462.783,\"doubleType\":4960.933485404779,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType124\",\"charType\":\"|\",\"dateType\":1588346298269,\"calendarType\":1588346298269,\"booleanType\":true,\"byteType\":124,\"shortType\":124,\"integerType\":124,\"longType\":124,\"floatType\":4789.719,\"doubleType\":9147.132683083857,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType125\",\"charType\":\"}\",\"dateType\":1588432698269,\"calendarType\":1588432698269,\"booleanType\":false,\"byteType\":125,\"shortType\":125,\"integerType\":125,\"longType\":125,\"floatType\":2060.1035,\"doubleType\":11681.684273150953,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType126\",\"charType\":\"~\",\"dateType\":1588519098269,\"calendarType\":1588519098269,\"booleanType\":false,\"byteType\":126,\"shortType\":126,\"integerType\":126,\"longType\":126,\"floatType\":6021.8076,\"doubleType\":6242.1164504119215,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType127\",\"charType\":\"\u007F\",\"dateType\":1588605498269,\"calendarType\":1588605498269,\"booleanType\":true,\"byteType\":127,\"shortType\":127,\"integerType\":127,\"longType\":127,\"floatType\":3063.6545,\"doubleType\":5109.792240706848,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType128\",\"charType\":\"\u0080\",\"dateType\":1588691898269,\"calendarType\":1588691898269,\"booleanType\":true,\"byteType\":-128,\"shortType\":128,\"integerType\":128,\"longType\":128,\"floatType\":5232.2397,\"doubleType\":10753.179267517762,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType130\",\"charType\":\"\u0082\",\"dateType\":1588864698269,\"calendarType\":1588864698269,\"booleanType\":true,\"byteType\":-126,\"shortType\":130,\"integerType\":130,\"longType\":130,\"floatType\":8623.209,\"doubleType\":11150.642883561573,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType131\",\"charType\":\"\u0083\",\"dateType\":1588951098269,\"calendarType\":1588951098269,\"booleanType\":false,\"byteType\":-125,\"shortType\":131,\"integerType\":131,\"longType\":131,\"floatType\":5924.5156,\"doubleType\":7847.04075872877,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType132\",\"charType\":\"\u0084\",\"dateType\":1589037498269,\"calendarType\":1589037498269,\"booleanType\":false,\"byteType\":-124,\"shortType\":132,\"integerType\":132,\"longType\":132,\"floatType\":9365.959,\"doubleType\":5887.167590754695,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType133\",\"charType\":\"\u0085\",\"dateType\":1589123898269,\"calendarType\":1589123898269,\"booleanType\":false,\"byteType\":-123,\"shortType\":133,\"integerType\":133,\"longType\":133,\"floatType\":6262.93,\"doubleType\":8794.963979425947,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType134\",\"charType\":\"\u0086\",\"dateType\":1589210298269,\"calendarType\":1589210298269,\"booleanType\":true,\"byteType\":-122,\"shortType\":134,\"integerType\":134,\"longType\":134,\"floatType\":11505.307,\"doubleType\":8310.583803624033,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType135\",\"charType\":\"\u0087\",\"dateType\":1589296698269,\"calendarType\":1589296698269,\"booleanType\":false,\"byteType\":-121,\"shortType\":135,\"integerType\":135,\"longType\":135,\"floatType\":1346.5073,\"doubleType\":11923.920184563367,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType136\",\"charType\":\"\u0088\",\"dateType\":1589383098269,\"calendarType\":1589383098269,\"booleanType\":true,\"byteType\":-120,\"shortType\":136,\"integerType\":136,\"longType\":136,\"floatType\":11025.955,\"doubleType\":6008.441333414649,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType137\",\"charType\":\"\u0089\",\"dateType\":1589469498269,\"calendarType\":1589469498269,\"booleanType\":false,\"byteType\":-119,\"shortType\":137,\"integerType\":137,\"longType\":137,\"floatType\":5511.7075,\"doubleType\":12247.000977302125,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType138\",\"charType\":\"\u008A\",\"dateType\":1589555898269,\"calendarType\":1589555898269,\"booleanType\":true,\"byteType\":-118,\"shortType\":138,\"integerType\":138,\"longType\":138,\"floatType\":3499.642,\"doubleType\":12110.06229663936,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType140\",\"charType\":\"\u008C\",\"dateType\":1589728698269,\"calendarType\":1589728698269,\"booleanType\":true,\"byteType\":-116,\"shortType\":140,\"integerType\":140,\"longType\":140,\"floatType\":1364.0374,\"doubleType\":3486.8817743956815,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType141\",\"charType\":\"\u008D\",\"dateType\":1589815098269,\"calendarType\":1589815098269,\"booleanType\":false,\"byteType\":-115,\"shortType\":141,\"integerType\":141,\"longType\":141,\"floatType\":8882.558,\"doubleType\":8300.598016115815,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType142\",\"charType\":\"\u008E\",\"dateType\":1589901498269,\"calendarType\":1589901498269,\"booleanType\":false,\"byteType\":-114,\"shortType\":142,\"integerType\":142,\"longType\":142,\"floatType\":9749.171,\"doubleType\":1564.5181850638817,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType143\",\"charType\":\"\u008F\",\"dateType\":1589987898269,\"calendarType\":1589987898269,\"booleanType\":false,\"byteType\":-113,\"shortType\":143,\"integerType\":143,\"longType\":143,\"floatType\":12134.964,\"doubleType\":11826.821618254206,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType144\",\"charType\":\"\u0090\",\"dateType\":1590074298269,\"calendarType\":1590074298269,\"booleanType\":true,\"byteType\":-112,\"shortType\":144,\"integerType\":144,\"longType\":144,\"floatType\":2868.588,\"doubleType\":5528.184969527553,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType145\",\"charType\":\"\u0091\",\"dateType\":1590160698269,\"calendarType\":1590160698269,\"booleanType\":false,\"byteType\":-111,\"shortType\":145,\"integerType\":145,\"longType\":145,\"floatType\":3200.7986,\"doubleType\":10168.80360329019,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType146\",\"charType\":\"\u0092\",\"dateType\":1590247098269,\"calendarType\":1590247098269,\"booleanType\":true,\"byteType\":-110,\"shortType\":146,\"integerType\":146,\"longType\":146,\"floatType\":11938.046,\"doubleType\":676.9207769135544,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType147\",\"charType\":\"\u0093\",\"dateType\":1590333498269,\"calendarType\":1590333498269,\"booleanType\":false,\"byteType\":-109,\"shortType\":147,\"integerType\":147,\"longType\":147,\"floatType\":13255.934,\"doubleType\":3795.0623548311005,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType148\",\"charType\":\"\u0094\",\"dateType\":1590419898269,\"calendarType\":1590419898269,\"booleanType\":false,\"byteType\":-108,\"shortType\":148,\"integerType\":148,\"longType\":148,\"floatType\":5073.5654,\"doubleType\":2949.8097011699133,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType150\",\"charType\":\"\u0096\",\"dateType\":1590592698269,\"calendarType\":1590592698269,\"booleanType\":false,\"byteType\":-106,\"shortType\":150,\"integerType\":150,\"longType\":150,\"floatType\":11558.445,\"doubleType\":8019.505469136916,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType151\",\"charType\":\"\u0097\",\"dateType\":1590679098269,\"calendarType\":1590679098269,\"booleanType\":false,\"byteType\":-105,\"shortType\":151,\"integerType\":151,\"longType\":151,\"floatType\":4812.8345,\"doubleType\":3878.0115577975535,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType152\",\"charType\":\"\u0098\",\"dateType\":1590765498269,\"calendarType\":1590765498269,\"booleanType\":true,\"byteType\":-104,\"shortType\":152,\"integerType\":152,\"longType\":152,\"floatType\":15036.261,\"doubleType\":14904.953994262898,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType153\",\"charType\":\"\u0099\",\"dateType\":1590851898269,\"calendarType\":1590851898269,\"booleanType\":false,\"byteType\":-103,\"shortType\":153,\"integerType\":153,\"longType\":153,\"floatType\":9903.429,\"doubleType\":8344.267918585334,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType154\",\"charType\":\"\u009A\",\"dateType\":1590938298269,\"calendarType\":1590938298269,\"booleanType\":true,\"byteType\":-102,\"shortType\":154,\"integerType\":154,\"longType\":154,\"floatType\":6945.53,\"doubleType\":6558.642061138794,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType155\",\"charType\":\"\u009B\",\"dateType\":1591024698269,\"calendarType\":1591024698269,\"booleanType\":true,\"byteType\":-101,\"shortType\":155,\"integerType\":155,\"longType\":155,\"floatType\":10723.673,\"doubleType\":7983.542320110577,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType156\",\"charType\":\"\u009C\",\"dateType\":1591111098269,\"calendarType\":1591111098269,\"booleanType\":true,\"byteType\":-100,\"shortType\":156,\"integerType\":156,\"longType\":156,\"floatType\":14771.345,\"doubleType\":1111.401821129719,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType157\",\"charType\":\"\u009D\",\"dateType\":1591197498269,\"calendarType\":1591197498269,\"booleanType\":true,\"byteType\":-99,\"shortType\":157,\"integerType\":157,\"longType\":157,\"floatType\":3068.087,\"doubleType\":11084.389897109188,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType158\",\"charType\":\"\u009E\",\"dateType\":1591283898269,\"calendarType\":1591283898269,\"booleanType\":true,\"byteType\":-98,\"shortType\":158,\"integerType\":158,\"longType\":158,\"floatType\":2515.9695,\"doubleType\":12947.614325365119,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType160\",\"charType\":\" \",\"dateType\":1591456698269,\"calendarType\":1591456698269,\"booleanType\":true,\"byteType\":-96,\"shortType\":160,\"integerType\":160,\"longType\":160,\"floatType\":13106.941,\"doubleType\":9465.65201001309,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType161\",\"charType\":\"¡\",\"dateType\":1591543098269,\"calendarType\":1591543098269,\"booleanType\":false,\"byteType\":-95,\"shortType\":161,\"integerType\":161,\"longType\":161,\"floatType\":8465.947,\"doubleType\":10168.533982118222,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType162\",\"charType\":\"¢\",\"dateType\":1591629498269,\"calendarType\":1591629498269,\"booleanType\":false,\"byteType\":-94,\"shortType\":162,\"integerType\":162,\"longType\":162,\"floatType\":12080.04,\"doubleType\":6949.386802975735,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType163\",\"charType\":\"£\",\"dateType\":1591715898269,\"calendarType\":1591715898269,\"booleanType\":true,\"byteType\":-93,\"shortType\":163,\"integerType\":163,\"longType\":163,\"floatType\":9909.152,\"doubleType\":8017.811583233079,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType164\",\"charType\":\"¤\",\"dateType\":1591802298269,\"calendarType\":1591802298269,\"booleanType\":true,\"byteType\":-92,\"shortType\":164,\"integerType\":164,\"longType\":164,\"floatType\":9584.621,\"doubleType\":4709.05990446658,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType165\",\"charType\":\"¥\",\"dateType\":1591888698269,\"calendarType\":1591888698269,\"booleanType\":true,\"byteType\":-91,\"shortType\":165,\"integerType\":165,\"longType\":165,\"floatType\":13919.338,\"doubleType\":16444.102346744712,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType166\",\"charType\":\"¦\",\"dateType\":1591975098269,\"calendarType\":1591975098269,\"booleanType\":false,\"byteType\":-90,\"shortType\":166,\"integerType\":166,\"longType\":166,\"floatType\":12751.712,\"doubleType\":8208.96830925776,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType167\",\"charType\":\"§\",\"dateType\":1592061498269,\"calendarType\":1592061498269,\"booleanType\":false,\"byteType\":-89,\"shortType\":167,\"integerType\":167,\"longType\":167,\"floatType\":7860.137,\"doubleType\":9064.329915370028,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType168\",\"charType\":\"¨\",\"dateType\":1592147898269,\"calendarType\":1592147898269,\"booleanType\":false,\"byteType\":-88,\"shortType\":168,\"integerType\":168,\"longType\":168,\"floatType\":12337.637,\"doubleType\":14282.374431608963,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType170\",\"charType\":\"ª\",\"dateType\":1592320698269,\"calendarType\":1592320698269,\"booleanType\":true,\"byteType\":-86,\"shortType\":170,\"integerType\":170,\"longType\":170,\"floatType\":16370.8125,\"doubleType\":9979.070808616432,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType171\",\"charType\":\"«\",\"dateType\":1592407098269,\"calendarType\":1592407098269,\"booleanType\":false,\"byteType\":-85,\"shortType\":171,\"integerType\":171,\"longType\":171,\"floatType\":7681.129,\"doubleType\":1309.4317645429794,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType172\",\"charType\":\"¬\",\"dateType\":1592493498269,\"calendarType\":1592493498269,\"booleanType\":false,\"byteType\":-84,\"shortType\":172,\"integerType\":172,\"longType\":172,\"floatType\":9286.958,\"doubleType\":8057.76979645549,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType173\",\"charType\":\"\u00AD\",\"dateType\":1592579898269,\"calendarType\":1592579898269,\"booleanType\":false,\"byteType\":-83,\"shortType\":173,\"integerType\":173,\"longType\":173,\"floatType\":4326.7065,\"doubleType\":2727.3330360618866,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType174\",\"charType\":\"®\",\"dateType\":1592666298269,\"calendarType\":1592666298269,\"booleanType\":true,\"byteType\":-82,\"shortType\":174,\"integerType\":174,\"longType\":174,\"floatType\":3146.9263,\"doubleType\":14717.013674758186,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType175\",\"charType\":\"¯\",\"dateType\":1592752698269,\"calendarType\":1592752698269,\"booleanType\":true,\"byteType\":-81,\"shortType\":175,\"integerType\":175,\"longType\":175,\"floatType\":9779.427,\"doubleType\":7007.24981956129,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType176\",\"charType\":\"°\",\"dateType\":1592839098269,\"calendarType\":1592839098269,\"booleanType\":false,\"byteType\":-80,\"shortType\":176,\"integerType\":176,\"longType\":176,\"floatType\":1840.1735,\"doubleType\":15570.206961871785,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType177\",\"charType\":\"±\",\"dateType\":1592925498269,\"calendarType\":1592925498269,\"booleanType\":false,\"byteType\":-79,\"shortType\":177,\"integerType\":177,\"longType\":177,\"floatType\":13224.231,\"doubleType\":5101.41088162177,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType178\",\"charType\":\"²\",\"dateType\":1593011898269,\"calendarType\":1593011898269,\"booleanType\":true,\"byteType\":-78,\"shortType\":178,\"integerType\":178,\"longType\":178,\"floatType\":15282.675,\"doubleType\":16928.48795658637,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType180\",\"charType\":\"´\",\"dateType\":1593184698269,\"calendarType\":1593184698269,\"booleanType\":true,\"byteType\":-76,\"shortType\":180,\"integerType\":180,\"longType\":180,\"floatType\":5872.7544,\"doubleType\":8111.095230502014,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType181\",\"charType\":\"µ\",\"dateType\":1593271098269,\"calendarType\":1593271098269,\"booleanType\":false,\"byteType\":-75,\"shortType\":181,\"integerType\":181,\"longType\":181,\"floatType\":67.399704,\"doubleType\":12929.807642461885,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType182\",\"charType\":\"¶\",\"dateType\":1593357498269,\"calendarType\":1593357498269,\"booleanType\":false,\"byteType\":-74,\"shortType\":182,\"integerType\":182,\"longType\":182,\"floatType\":12274.986,\"doubleType\":6659.771333721179,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType183\",\"charType\":\"·\",\"dateType\":1593443898269,\"calendarType\":1593443898269,\"booleanType\":true,\"byteType\":-73,\"shortType\":183,\"integerType\":183,\"longType\":183,\"floatType\":16038.156,\"doubleType\":4495.740428530836,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType184\",\"charType\":\"¸\",\"dateType\":1593530298269,\"calendarType\":1593530298269,\"booleanType\":true,\"byteType\":-72,\"shortType\":184,\"integerType\":184,\"longType\":184,\"floatType\":13196.38,\"doubleType\":13493.38953624924,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType185\",\"charType\":\"¹\",\"dateType\":1593616698269,\"calendarType\":1593616698269,\"booleanType\":true,\"byteType\":-71,\"shortType\":185,\"integerType\":185,\"longType\":185,\"floatType\":1197.0626,\"doubleType\":16790.95193390073,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType186\",\"charType\":\"º\",\"dateType\":1593703098269,\"calendarType\":1593703098269,\"booleanType\":true,\"byteType\":-70,\"shortType\":186,\"integerType\":186,\"longType\":186,\"floatType\":6166.093,\"doubleType\":14960.953549896536,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType187\",\"charType\":\"»\",\"dateType\":1593789498269,\"calendarType\":1593789498269,\"booleanType\":true,\"byteType\":-69,\"shortType\":187,\"integerType\":187,\"longType\":187,\"floatType\":816.9823,\"doubleType\":10777.15973383878,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType188\",\"charType\":\"¼\",\"dateType\":1593875898269,\"calendarType\":1593875898269,\"booleanType\":false,\"byteType\":-68,\"shortType\":188,\"integerType\":188,\"longType\":188,\"floatType\":14248.951,\"doubleType\":6518.377866107448,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType190\",\"charType\":\"¾\",\"dateType\":1594048698269,\"calendarType\":1594048698269,\"booleanType\":false,\"byteType\":-66,\"shortType\":190,\"integerType\":190,\"longType\":190,\"floatType\":16741.105,\"doubleType\":183.48157356021565,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType191\",\"charType\":\"¿\",\"dateType\":1594135098269,\"calendarType\":1594135098269,\"booleanType\":false,\"byteType\":-65,\"shortType\":191,\"integerType\":191,\"longType\":191,\"floatType\":18585.207,\"doubleType\":6292.263919220567,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType192\",\"charType\":\"À\",\"dateType\":1594221498269,\"calendarType\":1594221498269,\"booleanType\":false,\"byteType\":-64,\"shortType\":192,\"integerType\":192,\"longType\":192,\"floatType\":902.8313,\"doubleType\":18857.139340927304,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType193\",\"charType\":\"Á\",\"dateType\":1594307898269,\"calendarType\":1594307898269,\"booleanType\":true,\"byteType\":-63,\"shortType\":193,\"integerType\":193,\"longType\":193,\"floatType\":15453.85,\"doubleType\":16663.77398476745,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType194\",\"charType\":\"Â\",\"dateType\":1594394298269,\"calendarType\":1594394298269,\"booleanType\":false,\"byteType\":-62,\"shortType\":194,\"integerType\":194,\"longType\":194,\"floatType\":2693.8086,\"doubleType\":2781.2062423540247,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType195\",\"charType\":\"Ã\",\"dateType\":1594480698269,\"calendarType\":1594480698269,\"booleanType\":false,\"byteType\":-61,\"shortType\":195,\"integerType\":195,\"longType\":195,\"floatType\":10161.401,\"doubleType\":7996.426104072614,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType196\",\"charType\":\"Ä\",\"dateType\":1594567098269,\"calendarType\":1594567098269,\"booleanType\":false,\"byteType\":-60,\"shortType\":196,\"integerType\":196,\"longType\":196,\"floatType\":15058.154,\"doubleType\":8335.22423249435,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType197\",\"charType\":\"Å\",\"dateType\":1594653498269,\"calendarType\":1594653498269,\"booleanType\":false,\"byteType\":-59,\"shortType\":197,\"integerType\":197,\"longType\":197,\"floatType\":17235.564,\"doubleType\":15346.018527930328,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":\"StringType198\",\"charType\":\"Æ\",\"dateType\":1594739898269,\"calendarType\":1594739898269,\"booleanType\":false,\"byteType\":-58,\"shortType\":198,\"integerType\":198,\"longType\":198,\"floatType\":18747.293,\"doubleType\":4414.441269514296,\"nullType\":\"notNull\",\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null},{\"stringType\":null,\"charType\":null,\"dateType\":null,\"calendarType\":null,\"booleanType\":null,\"byteType\":null,\"shortType\":null,\"integerType\":null,\"longType\":null,\"floatType\":null,\"doubleType\":null,\"nullType\":null,\"listType\":[],\"arrayType\":null,\"mapType\":null,\"userType\":null}]";
        ObjectMapper objectMapper = new ObjectMapper();
        List<TestObj> domainDatas = objectMapper
                .readValue(json, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, TestObj.class));

//        List<TestObj> domainDatas = this.getDomainDatas(200, true);
        xlsxWriter1.write(domainDatas).endWrite(this.file3);
        System.out.println(xlsxWriter1.getAllSheetInExcel());
        System.out.println(xlsxWriter1.getRealDataInExcel());
        System.out.println(xlsxWriter1.getRealRowInExcel());
        System.out.println(new ObjectMapper().writeValueAsString(domainDatas));
    }

    /**
     * 大数据测试
     */
    @Test
    public void test7() throws WriteExcelException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(String.format("========================开始写入(%s)========================",
                dateFormat.format(new Date())));
        long s = System.currentTimeMillis();
        XLSXWriter<TestObj> xlsxWriter =
                new XLSXWriter<>(true, true, -1, 3, true, "yyyy|MM|dd HH|mm|ss");
        xlsxWriter.setProperties(CommonUtils
                .arrayToList(new String[]{"stringType", "charType", "calendarType", "booleanType", "nullType"}));
        xlsxWriter.setTitles(CommonUtils.arrayToList(new String[]{"字符串类型", "字符类型", "日期类型", "布尔类型", "空值类型"}));
        xlsxWriter.setWriteExcelCallback(new AbstractExcelWriter.WriteExcelCallback<TestObj>() {
            @Override
            public void handleRowReserved(Sheet sheet, AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
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

            @Override
            public Boolean handleCellValue(String property, Object value, TestObj data,
                                           AbstractExcelWriter<TestObj> writer) throws WriteExcelException {
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
                    return null;
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
                return null;
            }
        });

        for (int i = 0; i < 10; i++) {
            xlsxWriter.write(this.getDomainDatas(1000000, true));
            System.out.println("======================================================================");
            System.out.println(String.format("当前写入Sheet总数:%d", xlsxWriter.getAllSheetInExcel()));
            System.out.println(String.format("当前写入数据量(包括标题):%d", xlsxWriter.getRealRowInExcel()));
            System.out.println(String.format("当前写入数据量(不包括标题):%d", xlsxWriter.getRealDataInExcel()));
        }

        System.out.println(String.format("========================结束写入临时文件(%s)========================",
                dateFormat.format(new Date())));
        System.out.println(String.format("耗时:%d ms", System.currentTimeMillis() - s));

        System.out.println(String.format("========================开始写入目标文件(%s)========================",
                dateFormat.format(new Date())));

        xlsxWriter.endWrite(this.file1);

        System.out.println(String.format("========================结束写入目标文件(%s)========================",
                dateFormat.format(new Date())));
        System.out.println(String.format("共写入Sheet数:%d", xlsxWriter.getAllSheetInExcel()));
        System.out.println(String.format("共写入数据量(包括标题):%d", xlsxWriter.getRealRowInExcel()));
        System.out.println(String.format("共写入数据量(不包括标题):%d", xlsxWriter.getRealDataInExcel()));
        System.out.println(String.format("共耗时:%d ms", System.currentTimeMillis() - s));
    }

}
