package com.github.flyinghe.test;

import com.github.flyinghe.depdcy.AbstractExcelWriter;
import com.github.flyinghe.domain.TestObj;
import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.tools.CommonUtils;
import com.github.flyinghe.tools.XLSXWriter;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
                this.produceCellStyle(xlsxWriter, IndexedColors.SKY_BLUE.getIndex(), IndexedColors.BLACK.getIndex()));
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
            public void handleCellValue(String property, Object value, TestObj data,
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
            public void handleCellValue(String property, Object value, TestObj data,
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
    public void test6() throws WriteExcelException {
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
            public void handleCellValue(String property, Object value, TestObj data,
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
            public void handleCellValue(String property, Object value, TestObj data,
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

            @Override
            public boolean beforeWritePerRow(TestObj data, int currentRowInSheet, Sheet currentSheet,
                                             AbstractExcelWriter<TestObj> writer) {
                if (null == data.getBooleanType() || data.getBooleanType()) {
                    return true;
                }
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
                currentSheet.addMergedRegion(new CellRangeAddress(currentRowInSheet + 1, currentRowInSheet + 2, 3, 3));
                currentSheet.addMergedRegion(new CellRangeAddress(currentRowInSheet + 1, currentRowInSheet + 2, 2, 2));
                Row row = currentSheet.createRow(currentRowInSheet + 1);

                writer.createCell(row, 0, writer.getDefaultCellStyle()).setCellValue(data.getStringType());

                writer.createCell(row, 1, writer.getDefaultCellStyle()).setCellValue(data.getCharType());

                writer.createCell(row, 2, writer.getCellStylePool().get("booleanTypeFalse"))
                        .setCellValue(DateFormatUtils.format(data.getCalendarType(), AbstractExcelWriter.DATE_PATTERN));

                writer.createCell(row, 3, writer.getCellStylePool().get("booleanTypeFalse")).setCellValue("不合格");

                try {
                    ClientAnchor anchor =
                            writer.getCurrentPatriarch()
                                    .createAnchor(0, 0, 0, 0, 4, currentRowInSheet + 1, 5, currentRowInSheet + 3);
                    anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    BufferedImage bi = ImageIO.read(new File("C:\\Users\\FlyingHe\\Desktop\\test.jpg"));
                    ImageIO.write(bi, "jpeg", bos);
                    writer.getCurrentPatriarch().createPicture(anchor,
                            writer.getWorkbook().addPicture(bos.toByteArray(), Workbook.PICTURE_TYPE_JPEG));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                writer.next(writer.isBlankLastRow() ? 1 : 2);
                return false;
            }
        });

        xlsxWriter1.write(this.getDomainDatas(200, true)).endWrite(this.file3);
        System.out.println(xlsxWriter1.getAllSheetInExcel());
        System.out.println(xlsxWriter1.getRealDataInExcel());
        System.out.println(xlsxWriter1.getRealRowInExcel());

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
            public void handleCellValue(String property, Object value, TestObj data,
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
