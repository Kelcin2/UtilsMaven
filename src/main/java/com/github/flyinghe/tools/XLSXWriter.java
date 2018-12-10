package com.github.flyinghe.tools;

import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.exception.WriteExcelRuntimeException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by FlyingHe on 2017/8/27.
 * <p>
 * 此类用于向Excel2007以上文档中写入数据(后缀为xlsx),支持写入大数据量的需求。
 * 注意数据类型只支持Map类型或者普通JavaBean
 * </p>
 */
public class XLSXWriter<T> {
    //每个Sheet中最多row数,1-based
    public static final int ROW_MOST = 1048576;
    //每个row最多cell数,1-based
    public static final int COLUMN_MOST = 16384;
    //默认写入日期格式
    public static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";


    //记录当前sheet的当前行，0-based
    private int currentRowInSheet = -1;
    //当前sheet，0-based
    private int currentSheetInExcel = -1;
    //记录当前正在写入的Sheet对象
    private SXSSFSheet currentSheetPO;
    //标识上一行是否为空行(当isSkipBlankRow=false需要)
    private boolean isBlankLastRow = false;
    //记录上一行Row对象(当isSkipBlankRow=false需要)
    private Row lastRow = null;
    //记录每次需要被写入的数据
    private List<T> datas = new ArrayList<T>();

    //记录写入当前sheet的非空行(若指定了写入标题则会计算标题行)
    private int realRowInSheet = 0;
    //记录整个Excel文档的非空行(若指定了写入标题则会计算标题行)
    private int realRowInExcel = 0;
    //记录整个Excel文档的sheet数
    private int allSheetInExcel = 0;

    //是否写入标题,注意,若为false则titles即便为不null也不会写入标题,
    //若为true而titles为Null则titles将会被自动赋值为properties,
    //若为true而titles不为null,则将会按照指定titles写入标题
    private boolean isWriteTitle = true;
    //是否跳过空行,即空行是否会被写入
    private boolean isSkipBlankRow = true;
    //记录需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性会被写入)
    private List<String> properties = null;
    //记录不需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性不会被写入),若为null表示不排除任何属性写入
    private List<String> excludeProps = null;
    //写入每一页第1行的标题(1-based),指定此属性需要开启标题写入总开关isWriteTitle
    private List<String> titles = null;
    //指定一个Sheet里最多能写多少行,达到此限定值则换页,<=0表示不限制
    //,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
    private int limit = -1;
    //Excel WorkBook对象
    private SXSSFWorkbook workbook = null;
    //Excel文档保存到此文件(当调用endWrite方法时)
    private File file = null;
    //默认转换日期格式
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
    //默认单元格样式,用户可以覆盖此值以达到在下次写入数据时使用新的单元格样式
    private CellStyle defaultCellStyle = null;
    //默认标题单元格样式,用户可以覆盖此值以达到在下次写入标题时使用新的标题单元格样式
    private CellStyle defaultTitleCellStyle = null;
    //默认当前写入Sheet的行高(即每一行的行高),用户可以覆盖此值以达到下次换页时新的一页所有行的
    //行高使用新的Sheet行高(若被写入行没有指定自己的行高的话;<0表示使用默认值)
    private short sheetHeight = -1;
    //默认当前写入Sheet的列宽(即每一列的列宽),用户可以覆盖此值以达到下次换页时新的一页使用新的Sheet列宽(<0表示使用默认值16)
    private int sheetWidth = 16;
    //默认当前写入行的行高(此值若指定则后面的行高会覆盖此前设定的Sheet行高,<0表示使用设定的Sheet行高)
    private short rowHeight = -1;


    /**
     * @param file 指定调用{@link #endWrite()}时,将缓存数据写入的文件
     */
    public XLSXWriter(File file) {
        this(file, true);
    }

    /**
     * @param file           指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param isSkipBlankRow 指定是否跳过空行,即空行是否会被写入，true表示不会写入空行,false表示会写入空行
     */
    public XLSXWriter(File file, boolean isSkipBlankRow) {
        this(file, -1, isSkipBlankRow);
    }

    /**
     * @param file  指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param limit 指定每一页最多写入多少行数据(包括标题)，达到此限定值则换页,&lt;=0表示不限制
     *              ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
     */
    public XLSXWriter(File file, int limit) {
        this(file, limit, true);
    }

    /**
     * @param file           指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param limit          指定每一页最多写入多少行数据(包括标题)，达到此限定值则换页,&lt;=0表示不限制
     *                       ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
     * @param isSkipBlankRow 指定是否跳过空行,即空行是否会被写入，true表示不会写入空行,false表示会写入空行
     */
    public XLSXWriter(File file, int limit, boolean isSkipBlankRow) {
        this(file, limit, null, isSkipBlankRow);
    }

    /**
     * @param file           指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param limit          指定每一页最多写入多少行数据(包括标题)，达到此限定值则换页,&lt;=0表示不限制
     *                       ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
     * @param isSkipBlankRow 指定是否跳过空行,即空行是否会被写入，true表示不会写入空行,false表示会写入空行
     * @param dateFormat     指定要到日期类型是写入日期的格式,若为null表示使用默认值
     */
    public XLSXWriter(File file, int limit, boolean isSkipBlankRow, String dateFormat) {
        this(file, limit, null, isSkipBlankRow, null, dateFormat, true, null, true);
    }

    /**
     * @param file           指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param limit          指定每一页最多写入多少行数据(包括标题)，达到此限定值则换页,&lt;=0表示不限制
     *                       ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
     * @param properties     指定需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性会被写入),
     *                       指定为null则在写入数据时会默认写入每一个数据的所有属性
     * @param isSkipBlankRow 指定是否跳过空行,即空行是否会被写入，true表示不会写入空行,false表示会写入空行
     */
    public XLSXWriter(File file, int limit, List<String> properties, boolean isSkipBlankRow) {
        this(file, limit, properties, isSkipBlankRow, null, DATE_PATTERN, true, null, true);
    }

    /**
     * @param file           指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param limit          指定每一页最多写入多少行数据(包括标题)，达到此限定值则换页,&lt;=0表示不限制
     *                       ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
     * @param isSkipBlankRow 指定是否跳过空行,即空行是否会被写入，true表示不会写入空行,false表示会写入空行
     * @param excludeProps   指定不需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性不会被写入),若为null表示不排除任何属性写入
     * @param dateFormat     指定要到日期类型是写入日期的格式,若为null表示使用默认值
     */
    public XLSXWriter(File file, int limit, boolean isSkipBlankRow, List<String> excludeProps,
                      String dateFormat) {
        this(file, limit, null, isSkipBlankRow, excludeProps, dateFormat, true, null, true);
    }

    /**
     * @param file           指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param limit          指定每一页最多写入多少行数据(包括标题)，达到此限定值则换页,&lt;=0表示不限制
     *                       ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
     * @param properties     指定需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性会被写入),
     *                       指定为null则在写入数据时会默认写入每一个数据的所有属性
     * @param isSkipBlankRow 指定是否跳过空行,即空行是否会被写入，true表示不会写入空行,false表示会写入空行
     * @param titles         写入每一页第1行的标题(1-based),指定此属性需要开启标题写入总开关{@link #isWriteTitle}
     * @param isWriteTitle   是否写入标题,注意,若为false则{@link #titles}即便为不null也不会写入标题,
     *                       若为true而{@link #titles}为Null则{@link #titles}将会被自动赋值为properties,
     *                       若为true而{@link #titles}不为null,则将会按照指定{@link #titles}写入标题
     */
    public XLSXWriter(File file, int limit, List<String> properties, boolean isSkipBlankRow, List<String> titles,
                      boolean isWriteTitle) {
        this(file, limit, properties, isSkipBlankRow, null, DATE_PATTERN, true, titles, isWriteTitle);
    }

    /**
     * @param file                指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param limit               指定每一页最多写入多少行数据(包括标题)，达到此限定值则换页,&lt;=0表示不限制
     *                            ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
     * @param properties          指定需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性会被写入),
     *                            指定为null则在写入数据时会默认写入每一个数据的所有属性
     * @param excludeProps        指定不需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性不会被写入),若为null表示不排除任何属性写入
     * @param isSkipBlankRow      指定是否跳过空行,即空行是否会被写入，true表示不会写入空行,false表示会写入空行
     * @param dateFormat          指定要到日期类型是写入日期的格式,若为null表示使用默认值
     * @param isCompressTempFiles 指定写入缓存时缓存文件是否需要压缩,若不压缩缓存文件可能占用大量硬盘容量
     *                            (23MB的CSV数据转换成Excel时缓存数据会超过1GB,故建议压缩)
     * @param titles              写入每一页第1行的标题(1-based),指定此属性需要开启标题写入总开关{@link #isWriteTitle}
     * @param isWriteTitle        是否写入标题,注意,若为false则{@link #titles}即便为不null也不会写入标题,
     *                            若为true而{@link #titles}为Null则{@link #titles}将会被自动赋值为properties,
     *                            若为true而{@link #titles}不为null,则将会按照指定{@link #titles}写入标题
     */
    public XLSXWriter(File file, int limit, List<String> properties, String[] excludeProps, boolean isSkipBlankRow,
                      String dateFormat, boolean isCompressTempFiles, List<String> titles, boolean isWriteTitle) {
        this(file, limit, properties, true, CommonUtils.arrayToList(excludeProps), dateFormat, isCompressTempFiles,
                titles,
                isWriteTitle);

    }


    /**
     * @param file                指定调用{@link #endWrite()}时,将缓存数据写入的文件
     * @param limit               指定每一页最多写入多少行数据(包括标题)，达到此限定值则换页,&lt;=0表示不限制
     *                            ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值1048576,则会强制换页)
     * @param properties          指定需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性会被写入),
     *                            指定为null则在写入数据时会默认写入每一个数据的所有属性
     * @param isSkipBlankRow      指定是否跳过空行,即空行是否会被写入，true表示不会写入空行,false表示会写入空行
     * @param excludeProps        指定不需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性不会被写入),若为null表示不排除任何属性写入
     * @param dateFormat          指定要到日期类型是写入日期的格式,若为null表示使用默认值
     * @param isCompressTempFiles 指定写入缓存时缓存文件是否需要压缩,若不压缩缓存文件可能占用大量硬盘容量
     *                            (23MB的CSV数据转换成Excel时缓存数据会超过1GB,故建议压缩)
     * @param titles              写入每一页第1行的标题(1-based),指定此属性需要开启标题写入总开关{@link #isWriteTitle}
     * @param isWriteTitle        是否写入标题,注意,若为false则{@link #titles}即便为不null也不会写入标题,
     *                            若为true而{@link #titles}为Null则{@link #titles}将会被自动赋值为properties,
     *                            若为true而{@link #titles}不为null,则将会按照指定{@link #titles}写入标题
     */
    public XLSXWriter(File file, int limit, List<String> properties, boolean isSkipBlankRow, List<String> excludeProps,
                      String dateFormat, boolean isCompressTempFiles, List<String> titles, boolean isWriteTitle) {
        if (limit > ROW_MOST) {
            //每一页行数不能超过最大值
            throw new WriteExcelRuntimeException("limit have to <= " + ROW_MOST);
        }
        if (properties != null && properties.size() > COLUMN_MOST) {
            //列数不能超过最大值
            throw new WriteExcelRuntimeException("properties.size() have to <= " + COLUMN_MOST);
        }
        if (isWriteTitle && titles != null && properties == null) {
            //若用户打开了标题写入总开关且指定了titles,则必须指定properties
            throw new WriteExcelRuntimeException("properties can't be null");
        }
        if (isWriteTitle && titles != null && titles.size() > COLUMN_MOST) {
            //列数不能超过最大值
            throw new WriteExcelRuntimeException("titles.size() have to <= " + COLUMN_MOST);
        }
        this.file = file;
        this.limit = limit;
        this.properties = properties;
        this.isSkipBlankRow = isSkipBlankRow;
        this.excludeProps = excludeProps;
        if (dateFormat != null) {
            this.dateFormat.applyPattern(dateFormat);
        }
        this.titles = titles;
        this.isWriteTitle = isWriteTitle;

        this.workbook = new SXSSFWorkbook(-1);
        this.workbook.setCompressTempFiles(isCompressTempFiles);
        this.defaultCellStyle = this.workbook.createCellStyle();
        this.defaultCellStyle.setFont(this.workbook.createFont());
        this.defaultCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        this.defaultCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        if (this.isWriteTitle) {
            this.defaultTitleCellStyle = this.workbook.createCellStyle();
            Font font = this.workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 16);
            this.defaultTitleCellStyle.setFont(font);
            this.defaultTitleCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            this.defaultTitleCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        }
    }

    /**
     * 指定默认单元格样式,下次写入数据时会应用此样式.
     *
     * @param defaultCellStyle 指定默认单元格样式
     */
    public void setDefaultCellStyle(CellStyle defaultCellStyle) {
        this.defaultCellStyle = defaultCellStyle;
    }

    /**
     * 指定默认标题单元格样式,下次写入标题时会应用此样式.
     *
     * @param defaultTitleCellStyle 指定默认标题单元格样式
     */
    public void setDefaultTitleCellStyle(CellStyle defaultTitleCellStyle) {
        this.defaultTitleCellStyle = defaultTitleCellStyle;
    }

    /**
     * @return 返回Sheet行高
     */
    public short getSheetHeight() {
        return sheetHeight;
    }

    /**
     * @param sheetHeight 指定下次换页时的每一行的行高
     */
    public void setSheetHeight(short sheetHeight) {
        this.sheetHeight = sheetHeight;
    }

    /**
     * @return 返回Sheet列宽
     */
    public int getSheetWidth() {
        return sheetWidth;
    }

    /**
     * @param sheetWidth 下次换页时的每一列列宽
     */
    public void setSheetWidth(int sheetWidth) {
        this.sheetWidth = sheetWidth;
    }

    /**
     * @return 返回当前行行高
     */
    public short getRowHeight() {
        return rowHeight;
    }

    /**
     * 设置当前写入行的行高(此值若指定则后面的行高会覆盖此前设定的Sheet行高,&lt;0表示使用设定的Sheet行高)
     *
     * @param rowHeight 指定当前行行高
     */
    public void setRowHeight(short rowHeight) {
        this.rowHeight = rowHeight;
    }

    /**
     * 创建一个新的CellStyle用于改变默认单元格样式,通过{@link #setDefaultCellStyle(CellStyle)}指定
     *
     * @return 创建一个新的样式对象, 用户可以通过此对象重定义默认单元格样式
     */
    public CellStyle createCellStyle() {
        return this.workbook.createCellStyle();
    }

    /**
     * 创建一个新的Font用于改变默认单元格字体样式,通过{@link CellStyle#setFont(Font)}来指定
     *
     * @return 返回一个新的Font对象
     */
    public Font createFont() {
        return this.workbook.createFont();
    }

    /**
     * 返回调用此方法时当前写入Sheet的非空行数量(若指定了写入标题则会计算标题行)
     *
     * @return 返回调用此方法时当前写入Sheet的非空行数量
     */
    public int getRealRowInSheet() {
        return realRowInSheet;
    }

    /**
     * 获取此Excel文档中非空行的数量,一般在Excel文档写入完毕时调用(若指定了写入标题则会计算标题行)
     *
     * @return 返回Excel文档中非空行的数量
     */
    public int getRealRowInExcel() {
        return realRowInExcel;
    }

    /**
     * 获取调用此方法时已经写入的Sheet数量,一般在Excel文档写入完毕时调用
     *
     * @return 返回Excel文档中Sheet数量
     */
    public int getAllSheetInExcel() {
        return allSheetInExcel;
    }

    /**
     * 获取此Excel文档中写入的数据量,不包括标题行,一般在Excel文档写入完毕时调用
     *
     * @return 此Excel文档中写入的数据量
     */
    public int getRealDataInExcel() {
        return !this.isWriteTitle ? this.realRowInExcel : (this.realRowInExcel - this.allSheetInExcel);
    }

    /**
     * 获取当前页中写入的数据量,不包括标题行
     *
     * @return 返回当前页中写入的数据量
     */
    public int getRealDataInSheet() {
        return !this.isWriteTitle ? this.realRowInSheet : (this.realRowInSheet - 1);
    }

    /**
     * 获取需要写入Excel文档的属性值
     *
     * @return 返回需要写入Excel文档的属性值
     */
    public List<String> getProperties() {
        return this.properties == null ? null : new ArrayList<>(this.properties);
    }

    /**
     * 获取需要排除的属性
     *
     * @return 返回需要排除的属性
     */
    public List<String> getExcludeProps() {
        return this.excludeProps == null ? null : new ArrayList<>(this.excludeProps);
    }

    /**
     * 获取需要写入的标题
     *
     * @return 返回需要写入的标题
     */
    public List<String> getTitles() {
        return this.titles == null ? null : new ArrayList<>(this.titles);
    }

    /**
     * 获取设定的限制(每一个Sheet最多写入多少行)
     *
     * @return 返回设定的限制值
     */
    public int getLimit() {
        return limit;
    }

    /**
     * 给一个Cell赋值,若为空则视为"",若不为基本类型及其包装类(日期类型除外),则其值通过toString()获取
     *
     * @param cell  一个单元格
     * @param value 值
     * @return 若此单元格写入空数据则返回true, 否则返回false
     */
    private boolean setCellValue(Cell cell, Object value) {
        boolean isBlankCell = false;
        if (value == null) {
            cell.setCellValue("");
            isBlankCell = true;
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Date) {
            cell.setCellValue(this.dateFormat.format(value));
        } else if (value instanceof Calendar) {
            cell.setCellValue(this.dateFormat.format(value));
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
        return isBlankCell;
    }

    /**
     * 将数据写入某一行中，data中需要写入的数据由properties决定
     *
     * @param row  指定行
     * @param data 指定写入数据
     * @return 若此行至少有一个非空单元格则返回true, 换言之, 此行写入完毕后仍为空行的话返回false, 非空行返回true
     */
    private boolean writePerRow(Row row, Map<String, Object> data) {
        boolean isNotBlankRow = false;
        int currenCol = -1;//标识当前写入的列
        for (int i = 0; i < this.properties.size(); i++) {
            if (this.excludeProps == null) {
                //没有设置需要排除的元素
                currenCol++;
                if (data.containsKey(this.properties.get(i))) {
                    Cell cell = row.createCell(currenCol);
                    cell.setCellStyle(this.defaultCellStyle);
                    if (!this.setCellValue(cell, data.get(this.properties.get(i)))) {
                        isNotBlankRow = true;
                    }
                }

            } else {
                //设置了需要排除的元素
                if (this.excludeProps.contains(this.properties.get(i))) {
                    //此元素在排除元素列表中
                    if (this.isWriteTitle) {
                        currenCol++;
                    }
                    continue;
                } else {
                    //此元素不在排除元素列表中
                    currenCol++;
                    if (data.containsKey(this.properties.get(i))) {
                        Cell cell = row.createCell(currenCol);
                        cell.setCellStyle(this.defaultCellStyle);
                        if (!this.setCellValue(cell, data.get(this.properties.get(i)))) {
                            isNotBlankRow = true;
                        }
                    }
                }
            }

        }
        return isNotBlankRow;
    }

    /**
     * 初始化新的一页
     */
    private void initSheet() {
        //在开始新的一页之前(第一页除外)会把上一页的数据刷入缓存,以防止出现刷入不及时而导致内存溢出的情况
        if (this.allSheetInExcel > 0) {
            this.flush();
        }
        this.currentSheetPO = this.workbook.createSheet();
        this.currentSheetPO.setDefaultColumnWidth(this.sheetWidth < 0 ? 16 : this.sheetWidth);
        if (this.sheetHeight > 0) {
            this.currentSheetPO.setDefaultRowHeight(this.sheetHeight);
        }
        this.allSheetInExcel++;
        this.currentSheetInExcel++;
        this.currentRowInSheet = -1;
        this.realRowInSheet = 0;
        this.writeTitle();
    }

    /**
     * 向当前Sheet第一行(1-based)写入标题,若用户没有开启写入标题总开关(即{@link #isWriteTitle}为false),
     * 或者{@link #titles}为空则不会做任何操作
     */
    private void writeTitle() {
        if (!this.isWriteTitle || this.titles == null || this.titles.isEmpty()) {
            return;
        }
        this.currentRowInSheet++;
        Row row = this.currentSheetPO.createRow(this.currentRowInSheet);
        row.setHeight(this.rowHeight < 0 ? -1 : this.rowHeight);
        for (int i = 0; i < this.titles.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(this.defaultTitleCellStyle);
            cell.setCellValue(this.titles.get(i));
        }
        this.realRowInSheet++;
        this.realRowInExcel++;
    }

    /**
     * 初始化{@link #properties}和{@link #titles},
     * 尤其适用于用户没有指定properties或者指定了写入标题但没有提供需要写入的标题的情况下
     */
    private void initPropertisAndTitles() {
        if ((this.properties == null || this.properties.isEmpty()) && this.datas != null && this.datas.size() > 0) {
            if (this.datas.get(0) instanceof Map) {
                this.properties = new ArrayList<>(((Map<String, Object>) this.datas.get(0)).keySet());
            } else {
                this.properties = new ArrayList<>(CommonUtils.toMap(this.datas.get(0)).keySet());
            }
            if (this.properties.size() > COLUMN_MOST) {
                //列数不能超过最大值
                throw new WriteExcelRuntimeException("properties.size() have to <= " + COLUMN_MOST);
            }
        }
        if (this.isWriteTitle && (this.titles == null || this.titles.isEmpty())) {
            this.titles = new ArrayList<>(this.properties);
        }
    }

    /**
     * 每次在执行写入数据之前都会初始化一些数据
     *
     * @param _datas
     * @return 初始化失败返回false, 反之返回true
     */
    private boolean initWrite(List<T> _datas) {
        if (_datas != null) {
            this.datas.addAll(_datas);
        } else {
            return false;
        }

        this.initPropertisAndTitles();
        if (this.currentSheetPO == null) {
            //证明开始写第一页
            this.initSheet();
        }
        return true;
    }

    /**
     * 写入指定数据
     *
     * @param data 指定被写入的数据
     */
    private void writePerData(T data) {
        if (this.limit > 0 && (this.currentRowInSheet + (isSkipBlankRow && isBlankLastRow ? 0 : 1)) >= this.limit) {
            //用户开启了限制,并且此Sheet已经写入了允许的最大行数则换页
            this.initSheet();
        } else if (this.limit <= 0 &&
                (this.currentRowInSheet + (isSkipBlankRow && isBlankLastRow ? 0 : 1)) >= ROW_MOST) {
            //若用户没有开启限制,但是此页写入已经超过了Excel规定的最大行数,强制换页
            this.initSheet();
        }
        //将data转换成Map数据结构
        Map<String, Object> mapBean = null;
        if (data instanceof Map) {
            mapBean = (Map<String, Object>) data;
        } else {
            mapBean = CommonUtils.toMap(data);
        }
        if (mapBean == null) {
            throw new WriteExcelRuntimeException("Bean转换为Map时发生错误");
        }
        if (this.properties == null || this.properties.isEmpty()) {
            throw new WriteExcelRuntimeException("properties属性不能为空");
        }
        if (!this.isSkipBlankRow) {
            //未开启跳过空行设置,即需要写入空行
            this.currentRowInSheet++;
            Row row = this.currentSheetPO.createRow(this.currentRowInSheet);
            row.setHeight(this.rowHeight < 0 ? -1 : this.rowHeight);
            if (this.writePerRow(row, mapBean)) {
                this.realRowInSheet++;
                this.realRowInExcel++;
            }
        } else {
            //开启跳过空行设置,即不写入空行
            if (!this.isBlankLastRow) {
                //若上一行不是空行则指针下移一行
                this.currentRowInSheet++;
                //创建一个新的Row
                Row thisRow = this.currentSheetPO.createRow(this.currentRowInSheet);
                thisRow.setHeight(this.rowHeight < 0 ? -1 : this.rowHeight);
                //开始将数据写入该行
                if (this.writePerRow(thisRow, mapBean)) {
                    //若写入了数据
                    this.realRowInSheet++;
                    this.realRowInExcel++;
                    //此行不是空行
                    this.isBlankLastRow = false;
                } else {
                    //若没有写入任何数据,此行标记为空行
                    this.isBlankLastRow = true;
                }
                this.lastRow = thisRow;
            } else {
                //若上一行为空行则直接在上一行写入数据
                if (this.writePerRow(this.lastRow, mapBean)) {
                    //若写入了数据
                    this.realRowInSheet++;
                    this.realRowInExcel++;
                    this.isBlankLastRow = false;
                } else {
                    //若没有写入任何数据,此行还是标记为空行
                    this.isBlankLastRow = true;
                }
            }

        }
    }

    /**
     * 将内存中数据刷入缓存并且清空内存中数据
     */
    private void flushAndClear() {
        //尝试将内存中的数据刷入硬盘缓存中
        this.flush();
        //清除数据,释放内存
        this.datas.clear();
    }

    /**
     * 将内存中的Excel数据刷入缓存
     */
    private void flush() {
        try {
            if (this.isSkipBlankRow && this.isBlankLastRow) {
                //若不需要写入空行,且上一行为空行的话则保留上一行不被刷入缓存
                this.currentSheetPO.flushRows(1);
            } else {
                this.currentSheetPO.flushRows();
            }
        } catch (Exception e) {
            throw new WriteExcelRuntimeException("Flush Error");
        }
    }

    /**
     * 写入数据,指定的数据会被写入Excel文档中,此数据写入内存完毕后会立即刷入硬盘缓存中以释放内存。
     * 你可以多次调用此方法以达到写入大量数据到Excel文档中的目的。注意:当你不需要再写入数据时
     * 需要调用{@link #endWrite()}方法把缓存数据全部写入您指定的{@link #file}文件中,
     * 此后你不可以再次调用此方法
     *
     * @param _datas 指定写入的数据(数据可以是JavaBean也可以是Map)
     */
    public void write(List<T> _datas) {
        if (!this.initWrite(_datas)) {
            return;
        }
        for (int i = 0; i < this.datas.size(); i++) {
            this.writePerData(this.datas.get(i));
        }
        this.flushAndClear();
    }

    /**
     * 写入数据,指定的数据会被写入Excel文档中,此数据写入内存完毕后会立即刷入硬盘缓存中以释放内存。
     * 你可以多次调用此方法以达到写入多条数据到Excel文档中的目的。注意:当你不需要再写入数据时
     * 需要调用{@link #endWrite()}方法把缓存数据全部写入您指定的{@link #file}文件中,
     * 此后你不可以再次调用此方法
     *
     * @param data 指定写入的数据(数据可以是JavaBean也可以是Map)
     */
    public void write(T data) {
        List<T> _datas = new ArrayList<T>(1);
        _datas.add(data);
        this.write(_datas);
    }

    /**
     * 结束Excel文档写入工作(同时将缓存数据全部写入指定的{@link #file}文件中)
     *
     * @return 缓存全部写入指定的 {@link #file}文件后会尝试删除缓存文件,删除成功返回true,失败返回false
     * @throws WriteExcelException 写入过程中发生IO异常
     */
    public boolean endWrite() throws WriteExcelException {
        FileOutputStream out = null;
        boolean flag = true;
        try {
            out = new FileOutputStream(this.file);
            this.workbook.write(out);
        } catch (IOException e) {
            throw new WriteExcelException(e.getMessage());
        } finally {
            CommonUtils.closeIOStream(null, out);
            //删除用于缓存的临时文件
            flag = this.workbook.dispose();
        }
        return flag;
    }
}
