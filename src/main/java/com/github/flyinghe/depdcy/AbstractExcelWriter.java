package com.github.flyinghe.depdcy;

import com.github.flyinghe.exception.WriteExcelException;
import com.github.flyinghe.tools.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by FlyingHe on 2018/12/20.
 */
public abstract class AbstractExcelWriter<T> {
    /**
     * xlsx文件中每个Sheet中最多row数,1-based
     */
    public static final int XLSX_ROW_MOST = 1048576;
    /**
     * xlsx文件中每个row最多cell数,1-based
     */
    public static final int XLSX_COLUMN_MOST = 16384;
    /**
     * xls文件中每个Sheet中最多row数,1-based
     */
    public static final int XLS_ROW_MOST = 65535;
    /**
     * xls文件中每个row最多cell数,1-based
     */
    public static final int XLS_COLUMN_MOST = 256;
    /**
     * 预留行数最大值
     *
     * @see #rowNumReserved
     */
    public static final int EXCEL_ROW_RESERVED_MOST = 256;
    /**
     * xlsx文件类型
     */
    public static final int XLSX = 1;
    /**
     * xls文件类型
     */
    public static final int XLS = 2;
    /**
     * @see #map
     */
    public static final String CELL_STYLE = "cellStyle";
    /**
     * @see #map
     */
    public static final String CELL_VALUE = "cellValue";
    /**
     * 此常量用于{@link #booleanMapping}
     */
    public static final String TRUE = "true";
    /**
     * 此常量用于{@link #booleanMapping}
     */
    public static final String FALSE = "false";
    /**
     * 默认写入日期格式
     */
    public static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";
    /******************************************************************************************************/
    /**
     * 记录当前sheet的当前行，0-based
     */
    private int currentRowInSheet = -1;
    /**
     * 从第几行开始写,该值通过{@link #rowNumReserved}计算得到，0-based,
     * 排除预留行
     */
    private int startRowIndex = 0;
    /**
     * 允许的最大行数,每个Sheet,该值通过计算得到,包括预留行以及标题行,1-based,
     */
    private int allowMaxRows = 0;
    /**
     * 记录当前正在写入的Sheet对象
     */
    protected Sheet currentSheet = null;
    /**
     * 当前Sheet的drawing patriarch
     */
    protected Drawing currentPatriarch = null;
    /**
     * 标识上一行是否为空行(当需要写入空行时永远为false)
     */
    protected boolean isBlankLastRow = false;
    /**
     * 记录上一行Row对象(当不需要写入空行时需要)
     */
    private Row lastRow = null;
    /**
     * 记录整个Excel文档的非空行(若指定了写入标题则会计算标题行),
     * 不会计算预留行,1-based
     */
    private int realRowInExcel = 0;
    /**
     * 记录整个Excel文档的sheet数,1-based
     */
    protected int allSheetInExcel = 0;
    /**
     * Excel WorkBook对象,由子类构造
     */
    protected Workbook workbook = null;
    /**
     * 自定义cellStyle池,专门用于自定义的一些可复用的cellStyle,
     * 一般配合回调函数使用。
     *
     * @see WriteExcelCallback#handleCellValue
     */
    private Map<String, CellStyle> cellStylePool = null;
    /**
     * 用于修改默认而采用自定的cellValue或者cellStyle。需配合{@link WriteExcelCallback#handleCellValue}使用。
     * 最多两个元素,Key命名参照:{@link #CELL_STYLE};{@link #CELL_VALUE}
     *
     * @see WriteExcelCallback#handleCellValue
     * @see #CELL_STYLE
     * @see #CELL_VALUE
     */
    private Map<String, Object> map = null;

    /**
     * 变量池,用于记录一些全局范围内的变量,一般用于向Excel中写入在{@link #datas}数据中不存在的属性值,
     * 一般配合{@link #map}使用
     */
    private Map<String, Object> varPool = null;
    /******************************************************************************************************/
    /**
     * 是否写入标题,注意,若为false则titles即便为不null也不会写入标题,
     * 若为true而titles为Null则titles将会被自动赋值为properties,
     * 若为true而titles不为null,则将会按照指定titles写入标题
     */
    private boolean isWriteTitle = true;
    /**
     * 是否跳过空行,即空行是否会被写入
     */
    protected boolean isSkipBlankRow = true;
    /**
     * 记录需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性会被写入)
     */
    private List<String> properties = null;
    /**
     * 记录不需要被写入Excel文档的属性名(根据此值判断datas里元素的哪些属性不会被写入),若为null表示不排除任何属性写入
     */
    private List<String> excludeProps = null;
    /**
     * 写入每一页第{@link #startRowIndex}行的标题,指定此属性需要开启标题写入总开关
     * {@link #isWriteTitle}
     */
    private List<String> titles = null;
    /**
     * 需要被写入Excel的数据
     */
    protected List<T> datas = null;
    /**
     * 指定一个Sheet里最多能写多少数据行,达到此限定值则换页,&lt;=0表示不限制
     * ,所有数据会写在一个Sheet中(注意,若此页已经达到Excel规定最大值,则会强制换页,
     * 最大值包括预留行,标题行以及数据行)
     *
     * @see AbstractExcelWriter#XLS_ROW_MOST
     * @see AbstractExcelWriter#XLSX_ROW_MOST
     */
    private int limit = -1;
    /**
     * 每一个Sheet顶端预留行数,&lt;=0表示不预留,1-based
     */
    private int rowNumReserved = 0;
    /**
     * Excel文件类型
     *
     * @see #XLSX
     * @see #XLS
     */
    protected int excelType = XLSX;

    /**
     * 默认转换日期格式
     */
    private SimpleDateFormat defaultDateFormat = new SimpleDateFormat(DATE_PATTERN);
    /**
     * 用于用户设置了{@link #dateFormatMapping}时,临时使用此日期格式对象
     */
    private SimpleDateFormat dateFormatTemp = new SimpleDateFormat(DATE_PATTERN);
    /**
     * 配置属性名到日期格式的映射,若写入的属性值是日期,并且该属性值的日期格式在此配置中
     * ,则该属性值的日期格式采用此映射值,否则采用默认日期格式
     *
     * @see #defaultDateFormat
     */
    private Map<String, String> dateFormatMapping = null;

    /**
     * 属性到boolean值的映射,若写入的属性值是boolean值,并且该属性值在此配置中,则该属性值采用此映射值,
     * 否则采用默认取值方式取值。
     * 注意:映射的值类型不能再是boolean
     *
     * @see #TRUE
     * @see #FALSE
     */
    private Map<String, Map<String, Object>> booleanMapping = null;
    /**
     * 默认单元格样式,用户可以覆盖此值
     */
    private CellStyle defaultCellStyle = null;

    /**
     * 配置属性名到单元格样式的映射,若匹配不到则使用默认单元格样式
     *
     * @see #defaultCellStyle
     */
    private Map<String, CellStyle> cellStyleMapping = null;
    /**
     * 默认标题单元格样式,用户可以覆盖此值
     */
    private CellStyle defaultTitleCellStyle = null;
    /**
     * 配置属性名到标题单元格样式的映射,若匹配不到则使用默认标题单元格样式
     *
     * @see #defaultTitleCellStyle
     */
    private Map<String, CellStyle> titleCellStyleMapping = null;
    /**
     * 默认行高(即每一行的行高,该行高只针对数据行的行高,不包括标题行的行高;&lt;=0表示使用默认值)
     */
    private float defaultHeight = -1;
    /**
     * 默认标题行行高(该行高只针对标题行的行高;&lt;=0表示使用默认值)
     */
    private float defaultTitleHeight = -1;
    /**
     * 默认列宽(即每一列的列宽),用户可以覆盖此值(&lt;=0表示使用默认值16)
     */
    private int defaultColumnWidth = 16;
    /**
     * 配置属性名到列宽的映射,若匹配不到则使用默认列宽
     *
     * @see #defaultColumnWidth
     */
    private Map<String, Integer> columnWidthMapping = null;

    /**
     * WriteExcelCallback
     *
     * @see WriteExcelCallback
     */
    private WriteExcelCallback<T> writeExcelCallback = null;


    /**
     * 获取当前workbook
     *
     * @return 获取当前workbook
     */
    public Workbook getWorkbook() {
        return this.workbook;
    }

    /**
     * 获取自定义cellStyle池
     *
     * @return 自定义cellStyle池
     */
    public Map<String, CellStyle> getCellStylePool() {
        if (null == this.cellStylePool) {
            this.cellStylePool = new HashMap<String, CellStyle>(16);
        }
        return this.cellStylePool;
    }

    /**
     * 获取整个Excel文档的非空行(若指定了写入标题则会计算标题行),
     * 不会计算预留行,1-based
     *
     * @return 整个Excel文档的非空行
     */
    public int getRealRowInExcel() {
        return realRowInExcel;
    }

    /**
     * 获取整个Excel文档的sheet数,1-based
     */
    public int getAllSheetInExcel() {
        return allSheetInExcel;
    }

    /**
     * 获取此Excel文档中写入的数据量,不包括标题行和预留行,一般在Excel文档写入完毕时调用
     *
     * @return 此Excel文档中写入的数据量(非空行)
     */
    public int getRealDataInExcel() {
        return !this.isWriteTitle ? this.realRowInExcel : (this.realRowInExcel - this.allSheetInExcel);
    }

    /**
     * 获取当前Sheet的drawing patriarch
     *
     * @return 当前Sheet的drawing patriarch
     */
    public Drawing getCurrentPatriarch() {
        if (null != this.currentSheet && null == this.currentPatriarch) {
            this.currentPatriarch = this.currentSheet.createDrawingPatriarch();
        }
        return this.currentPatriarch;
    }

    /**
     * 获取变量池
     *
     * @return 变量池
     * @see #varPool
     */
    public Map<String, Object> getVarPool() {
        if (null == this.varPool) {
            this.varPool = new HashMap<>();
        }
        return this.varPool;
    }

    /**
     * 从变量池中获取一个指定key的变量值
     *
     * @param key   指定key
     * @param clazz 该值类型
     * @param <V>   泛型
     * @return 返回从变量池中获取到的一个指定key的变量值
     * @see #varPool
     */
    public <V> V getVarPool(String key, Class<V> clazz) {
        if (null == this.varPool) {
            return null;
        }
        return (V) this.varPool.get(key);
    }

    /**
     * 获取上一行是否为空行
     *
     * @return 获取上一行是否为空行
     */
    public boolean isBlankLastRow() {
        return this.isBlankLastRow;
    }

    /**
     * 手动将行指针下移1行
     *
     * @return 下移后的指针
     * @see #next(int)
     */
    public int next() {
        return this.next(1);
    }

    /**
     * 手动将行指针下移n行,一般在Excel回调方法中会使用(例如在回调方法中自行向Excel插入数据时),
     * 注意:调用此方法会重置{@link #isBlankLastRow},{@link #lastRow}属性
     *
     * @param n 将行指针下移的行数
     * @return 下移后的指针
     */
    public int next(int n) {
        this.currentRowInSheet += n;
        this.isBlankLastRow = false;
        this.lastRow = null;
        return this.currentRowInSheet;
    }

    /**
     * 创建一个新的CellStyle
     *
     * @return 创建一个新的样式对象, 用户可以通过此对象重定义默认单元格样式
     * @see #createXLSXCellStyle()
     */
    public CellStyle createCellStyle() {
        return this.workbook.createCellStyle();
    }

    /**
     * 创建一个新的适用于XLSX类型Excel文件的CellStyle
     *
     * @return
     * @throws WriteExcelException 异常
     * @see #createCellStyle()
     */
    public XSSFCellStyle createXLSXCellStyle() throws WriteExcelException {
        if (XLSX != this.excelType) {
            throw new WriteExcelException("仅适用于XLSX类型Excel文件使用");
        }
        return (XSSFCellStyle) this.createCellStyle();
    }

    /**
     * 创建一个新的Font用于改变默认单元格字体样式
     *
     * @return 返回一个新的Font对象
     */
    public Font createFont() {
        return this.workbook.createFont();
    }

    /**
     * 创建一个新的适用于XLSX类型Excel文件的Font
     *
     * @return 一个新的适用于XLSX类型Excel文件的Font
     * @throws WriteExcelException 异常
     */
    public XSSFFont createXLSXFont() throws WriteExcelException {
        if (XLSX != this.excelType) {
            throw new WriteExcelException("仅适用于XLSX类型Excel文件使用");
        }
        return (XSSFFont) this.createFont();
    }

    /**
     * 创建一个Cell
     *
     * @param row       行对象
     * @param column    列坐标
     * @param cellStyle 单元格样式对象
     * @return 创建后的Cell
     */
    public Cell createCell(Row row, int column, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    /**
     * 获取属性名到日期格式的映射
     *
     * @return 返回属性名到日期格式的映射
     */
    public Map<String, String> getDateFormatMapping() {
        if (null == this.dateFormatMapping) {
            this.dateFormatMapping = new HashMap<String, String>(16);
        }
        return dateFormatMapping;
    }

    /**
     * 获取属性到boolean值的映射
     *
     * @return 返回属性到boolean值的映射
     */
    public Map<String, Map<String, Object>> getBooleanMapping() {
        if (null == this.booleanMapping) {
            this.booleanMapping = new HashMap<String, Map<String, Object>>(16);
        }
        return booleanMapping;
    }

    /**
     * 用于向boolean映射集合中添加映射值
     *
     * @param property   属性名
     * @param trueValue  当此属性为true时的值
     * @param falseValue 当此属性为false时的值
     * @see #booleanMapping
     */
    public void putBooleanMapping(String property, Object trueValue, Object falseValue) {
        Map<String, Object> mapping = new HashMap<>(2);
        mapping.put(TRUE, trueValue);
        mapping.put(FALSE, falseValue);
        this.getBooleanMapping().put(property, mapping);
    }

    /**
     * 将value放入到map中,
     * 此方法在{@link WriteExcelCallback#handleCellValue}
     * 回调中使用
     *
     * @param value 需要被放入的值
     */
    public void putCellValueToMap(Object value) {
        if (null != this.writeExcelCallback) {
            this.map.put(CELL_VALUE, value);
        }
    }

    /**
     * 将cellStyle放入到map中,
     * 此方法在{@link WriteExcelCallback#handleCellValue}
     * 回调中使用
     *
     * @param cellStyle 需要被放入的cellStyle
     */
    public void putCellStyleToMap(CellStyle cellStyle) {
        if (null != this.writeExcelCallback) {
            this.map.put(CELL_STYLE, cellStyle);
        }
    }

    /**
     * 获取默认单元格样式
     *
     * @return 返回默认单元格样式
     */
    public CellStyle getDefaultCellStyle() {
        return defaultCellStyle;
    }

    /**
     * 设置默认单元格样式
     *
     * @param defaultCellStyle 默认单元格样式
     */

    public void setDefaultCellStyle(CellStyle defaultCellStyle) {
        this.defaultCellStyle = defaultCellStyle;
    }

    /**
     * 获取属性名到单元格样式的映射
     *
     * @return 返回属性名到单元格样式的映射
     */
    public Map<String, CellStyle> getCellStyleMapping() {
        if (this.cellStyleMapping == null) {
            this.cellStyleMapping = new HashMap<String, CellStyle>(16);
        }
        return cellStyleMapping;
    }

    /**
     * 获取标题单元格样式
     *
     * @return 返回标题单元格样式
     */
    public CellStyle getDefaultTitleCellStyle() {
        return defaultTitleCellStyle;
    }

    /**
     * 设置标题单元格样式
     *
     * @param defaultTitleCellStyle 标题单元格样式
     */
    public void setDefaultTitleCellStyle(CellStyle defaultTitleCellStyle) {
        this.defaultTitleCellStyle = defaultTitleCellStyle;
    }

    /**
     * 获取属性名到标题单元格样式的映射
     *
     * @return 返回属性名到标题单元格样式的映射
     */
    public Map<String, CellStyle> getTitleCellStyleMapping() {
        if (null == this.titleCellStyleMapping) {
            this.titleCellStyleMapping = new HashMap<String, CellStyle>(16);
        }
        return titleCellStyleMapping;
    }

    /**
     * 获取默认行高
     *
     * @return 返回默认行高
     */
    public float getDefaultHeight() {
        return defaultHeight;
    }

    /**
     * 设置默认行高
     *
     * @param defaultHeight 默认行高
     */
    public void setDefaultHeight(float defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    /**
     * 获取默认标题行行高
     *
     * @return 返回默认标题行行高
     */
    public float getDefaultTitleHeight() {
        return defaultTitleHeight;
    }

    /**
     * 设置默认标题行行高
     *
     * @param defaultTitleHeight 默认标题行行高
     */
    public void setDefaultTitleHeight(float defaultTitleHeight) {
        this.defaultTitleHeight = defaultTitleHeight;
    }

    /**
     * 获取默认列宽
     *
     * @return 返回默认列宽
     */
    public int getDefaultColumnWidth() {
        return defaultColumnWidth;
    }

    /**
     * 设置默认列宽
     *
     * @param defaultColumnWidth 默认列宽
     * @see #defaultColumnWidth
     */
    public void setDefaultColumnWidth(int defaultColumnWidth) {
        if (defaultColumnWidth > 0) {
            this.defaultColumnWidth = defaultColumnWidth;
        } else {
            this.defaultColumnWidth = 16;
        }
    }

    /**
     * 获取属性名到列宽的映射(&lt;=0表示使用默认值)
     *
     * @return 返回属性名到列宽的映射
     * @see #defaultColumnWidth
     */
    public Map<String, Integer> getColumnWidthMapping() {
        if (null == this.columnWidthMapping) {
            this.columnWidthMapping = new HashMap<>(16);
        }
        return columnWidthMapping;
    }


    /**
     * 设置Excel回调方法
     *
     * @param writeExcelCallback Excel回调方法
     */
    public void setWriteExcelCallback(WriteExcelCallback<T> writeExcelCallback) {
        this.writeExcelCallback = writeExcelCallback;
    }

    /**
     * 设置properties
     *
     * @param properties properties
     * @see #properties
     */
    public void setProperties(Collection<String> properties) {
        if (null == this.properties) {
            this.properties = new ArrayList<>();
        }
        this.properties.clear();
        this.properties.addAll(properties);
    }

    /**
     * 设置excludeProps
     *
     * @param excludeProps excludeProps
     * @see #excludeProps
     */
    public void setExcludeProps(Collection<String> excludeProps) {
        if (null == this.excludeProps) {
            this.excludeProps = new ArrayList<>();
        }
        this.excludeProps.clear();
        this.excludeProps.addAll(excludeProps);
    }

    /**
     * 设置titles
     *
     * @param titles titles
     * @see #titles
     */
    public void setTitles(Collection<String> titles) {
        if (null == this.titles) {
            this.titles = new ArrayList<>();
        }
        this.titles.clear();
        this.titles.addAll(titles);
    }

    /**
     * 获取XLS类型Excel文件的自定义颜色板,可使用此颜色板使用RGB自定义颜色,
     * 注意自定义的颜色会覆盖已有的颜色,你应该覆盖没有被使用过的颜色
     *
     * @return XLS类型Excel文件的自定义颜色板
     * @throws WriteExcelException 异常
     * @see #createXLSXCellStyle()
     * @see #createXLSXColor(int, int, int, int)
     */
    public HSSFPalette getXLSPalette() throws WriteExcelException {
        if (XLS != this.excelType) {
            throw new WriteExcelException("此颜色板仅适用于XLS类型Excel文件使用");
        }
        return ((HSSFWorkbook) this.workbook).getCustomPalette();
    }

    /**
     * 创建一个自定义颜色,仅适用于XLSX类型Excel文件。
     * 你可以使用{@link XSSFCellStyle#setFillForegroundColor(XSSFColor)}
     * 来使用自定义颜色
     *
     * @param r red
     * @param g green
     * @param b blue
     * @return 一个新的自定义颜色
     * @throws WriteExcelException 异常
     * @see #createXLSXCellStyle()
     * @see #createXLSXColor(int, int, int, int)
     * @see #getXLSPalette()
     */
    public XSSFColor createXLSXColor(int r, int g, int b) throws WriteExcelException {
        return this.createXLSXColor(r, g, b, 255);
    }

    /**
     * 创建一个自定义颜色,仅适用于XLSX类型Excel文件。
     * 你可以使用{@link XSSFCellStyle#setFillForegroundColor(XSSFColor)}
     * 来使用自定义颜色
     *
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     * @return 一个新的自定义颜色
     * @throws WriteExcelException 异常
     * @see #createXLSXCellStyle()
     * @see #createXLSXColor(int, int, int)
     * @see #getXLSPalette()
     */
    public XSSFColor createXLSXColor(int r, int g, int b, int a) throws WriteExcelException {
        if (XLSX != this.excelType) {
            throw new WriteExcelException("此颜色仅适用于XLSX类型Excel文件使用");
        }
        return new XSSFColor(new java.awt.Color(r, g, b, a));
    }

    public AbstractExcelWriter() throws WriteExcelException {
        this(-1);
    }

    /**
     * @param limit {@link #limit}
     */
    public AbstractExcelWriter(int limit) throws WriteExcelException {
        this(limit, XLSX, DATE_PATTERN);
    }

    /**
     * @param limit     {@link #limit}
     * @param excelType {@link #excelType}
     * @see #DATE_PATTERN
     */
    public AbstractExcelWriter(int limit, int excelType) throws WriteExcelException {
        this(limit, excelType, DATE_PATTERN);
    }

    /**
     * @param limit      {@link #limit}
     * @param excelType  {@link #excelType}
     * @param dateFormat 指定默认日期格式,若为空则使用默认日期格式
     * @see #DATE_PATTERN
     */
    public AbstractExcelWriter(int limit, int excelType, String dateFormat) throws WriteExcelException {
        this(true, limit, 0, excelType, dateFormat);
    }

    /**
     * @param isWriteTitle   {@link #isWriteTitle}
     * @param limit          {@link #limit}
     * @param rowNumReserved {@link #rowNumReserved}
     * @param excelType      {@link #excelType}
     * @param dateFormat     指定默认日期格式,若为空则使用默认日期格式
     * @see #DATE_PATTERN
     */
    public AbstractExcelWriter(boolean isWriteTitle, int limit, int rowNumReserved, int excelType,
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
     * @see #DATE_PATTERN
     */
    public AbstractExcelWriter(boolean isWriteTitle, boolean isSkipBlankRow,
                               int limit, int rowNumReserved, int excelType, String dateFormat)
            throws WriteExcelException {
        this.isWriteTitle = isWriteTitle;
        this.isSkipBlankRow = isSkipBlankRow;
        this.limit = limit;
        this.rowNumReserved = rowNumReserved;
        this.excelType = excelType;

        //todo workbook由子类构造方法去初始化

        if (StringUtils.isNotBlank(dateFormat)) {
            this.defaultDateFormat.applyPattern(dateFormat);
        }
        if (this.rowNumReserved > 0) {
            //每一个Sheet从哪行开始写
            this.startRowIndex = this.rowNumReserved;
        }
        if (this.limit > 0) {
            if (this.rowNumReserved > 0) {
                this.allowMaxRows += this.rowNumReserved;
            }
            if (this.isWriteTitle) {
                this.allowMaxRows++;
            }
            this.allowMaxRows += this.limit;
        } else if (XLS == this.excelType) {
            this.allowMaxRows = XLS_ROW_MOST;
        } else if (XLSX == this.excelType) {
            this.allowMaxRows = XLSX_ROW_MOST;
        }
        //todo 由子类去调用validateDataWhenConstruct
    }

    /**
     * 构造时校验数据,此种校验只会进行一次
     *
     * @throws WriteExcelException 校验错误异常
     */
    protected void validateDataWhenConstruct() throws WriteExcelException {
        if (this.rowNumReserved > EXCEL_ROW_RESERVED_MOST) {
            throw new WriteExcelException(String.format("预留行数不得超过最大值%d", EXCEL_ROW_RESERVED_MOST));
        }
        if (this.limit > 0) {
            if (XLSX == this.excelType && this.allowMaxRows > XLSX_ROW_MOST) {
                throw new WriteExcelException(String.format("每页行数不得超过最大值%d", XLSX_ROW_MOST));
            }
            if (XLS == this.excelType && this.allowMaxRows > XLS_ROW_MOST) {
                throw new WriteExcelException(String.format("每页行数不得超过最大值%d", XLS_ROW_MOST));
            }
        }
    }

    /**
     * 修正数据并校验,此方法在每次写入数据前都需要校验,因为用户可能修改参数而导致参数非法
     *
     * @throws WriteExcelException 校验错误异常
     */
    private void validateDataPerWrite() throws WriteExcelException {
        //修正数据,并校验
        if (CollectionUtils.isEmpty(this.properties)) {
            if (CollectionUtils.isEmpty(this.datas)) {
                //此种情况下被写入数据不能为空,因为默认会根据datas去自动获取需要被写入的属性
                throw new WriteExcelException("datas为空,请检查数据");
            }
            T data = this.datas.get(0);
            this.setProperties(data instanceof Map ? ((Map) data).keySet() : CommonUtils.toMap(data).keySet());
        }
        if (CollectionUtils.isNotEmpty(this.excludeProps)) {
            //若设置了排除元素,先将元素排除
            this.properties.removeAll(this.excludeProps);
        }
        if (CollectionUtils.isEmpty(this.properties)) {
            throw new WriteExcelException("properties为空!");
        }
        if (this.isWriteTitle && CollectionUtils.isEmpty(this.titles)) {
            this.setTitles(this.properties);
        }
        if (this.isWriteTitle && this.properties.size() != this.titles.size()) {
            throw new WriteExcelException("属性与标题列数不一致");
        }
        if (XLSX == this.excelType && this.properties.size() > XLSX_COLUMN_MOST) {
            throw new WriteExcelException(String.format("列数不得超过最大值%d", XLSX_COLUMN_MOST));
        }
        if (XLS == this.excelType && this.properties.size() > XLS_COLUMN_MOST) {
            throw new WriteExcelException(String.format("列数不得超过最大值%d", XLS_COLUMN_MOST));
        }
        if (this.defaultCellStyle == null) {
            this.defaultCellStyle = this.workbook.createCellStyle();
            this.defaultCellStyle.setFont(this.workbook.createFont());
            this.defaultCellStyle.setAlignment(HorizontalAlignment.CENTER);
            this.defaultCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        if (this.isWriteTitle && this.defaultTitleCellStyle == null) {
            this.defaultTitleCellStyle = this.workbook.createCellStyle();
            Font font = this.workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 16);
            this.defaultTitleCellStyle.setFont(font);
            this.defaultTitleCellStyle.setAlignment(HorizontalAlignment.CENTER);
            this.defaultTitleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        if (this.writeExcelCallback != null && null == this.map) {
            this.map = new HashMap<>(2);
        }
    }

    /**
     * 给一个Cell赋值,若为null则视为"",若不为基本类型及其包装类(日期类型除外),则其值通过toString()获取
     *
     * @param cell       一个单元格
     * @param value      值
     * @param property   属性名,即列名
     * @param blankFixed 标志此单元格是否被固定认定为空单元格,true表示该单元格固定为空单元格,false表示该单元格固定为非空单元格,
     *                   若该参数为null,则使用默认的空单元格判定方法来判定该单元格是否为空单元格
     * @return 若此单元格写入空数据则返回true, 否则返回false
     */
    private boolean setCellValue(Cell cell, Object value, String property, Boolean blankFixed)
            throws WriteExcelException {
        boolean isBlankCell = false;
        if (value == null) {
            cell.setCellValue("");
            isBlankCell = true;
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Date || value instanceof Calendar) {
            if (MapUtils.isNotEmpty(this.dateFormatMapping) && this.dateFormatMapping.containsKey(property)) {
                //证明设置了该列名到日期格式的映射,日期格式采用映射格式
                this.dateFormatTemp.applyPattern(this.dateFormatMapping.get(property));
                cell.setCellValue(
                        this.dateFormatTemp.format(value instanceof Date ? value : ((Calendar) value).getTime()));
            } else {
                //采用默认的日期格式
                cell.setCellValue(
                        this.defaultDateFormat.format(value instanceof Date ? value : ((Calendar) value).getTime()));
            }
        } else if (value instanceof Boolean) {
            if (MapUtils.isNotEmpty(this.booleanMapping) && this.booleanMapping.containsKey(property)) {
                //证明设置了该列名到boolean值的映射,写入cell的值采用映射值
                Object valueMapping = this.booleanMapping.get(property).get((boolean) value ? TRUE : FALSE);
                if (valueMapping instanceof Boolean) {
                    throw new WriteExcelException(String.format("[%s]属性的boolean映射值类型不能再是boolean", property));
                }
                isBlankCell = this.setCellValue(cell, valueMapping, property, blankFixed);
            } else {
                cell.setCellValue((Boolean) value);
            }
        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Byte) {
            cell.setCellValue((Byte) value);
        } else if (value instanceof Short) {
            cell.setCellValue((Short) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Character) {
            cell.setCellValue(((Character) value).toString());
        } else {
            cell.setCellValue(value.toString());
        }
        return null == blankFixed ? isBlankCell : blankFixed;
    }

    /**
     * 将数据写入某一行中，data中需要写入的数据由properties决定
     *
     * @param row  指定行
     * @param data 指定写入数据
     * @return 若此行至少有一个非空单元格则返回true, 换言之, 此行写入完毕后仍为空行的话返回false, 非空行返回true
     */
    private boolean writePerRow(Row row, Map<String, Object> data, T originData) throws WriteExcelException {
        boolean isNotBlankRow = false;
        //标识当前写入的列
        int currenCol = -1;
        for (String property : this.properties) {
            currenCol++;
            Cell cell = row.createCell(currenCol);
            Boolean blankFixed = null;
            if (this.writeExcelCallback != null) {
                this.map.clear();
                blankFixed = this.writeExcelCallback.handleCellValue(property, data.get(property), originData, this);
            }
            CellStyle cellStyleTemp = this.defaultCellStyle;
            Object value = data.get(property);
            if (MapUtils.isNotEmpty(this.cellStyleMapping) && this.cellStyleMapping.containsKey(property)) {
                cellStyleTemp = this.cellStyleMapping.get(property);
            }
            if (MapUtils.isNotEmpty(this.map)) {
                cellStyleTemp = (CellStyle) this.map.getOrDefault(CELL_STYLE, cellStyleTemp);
                value = this.map.getOrDefault(CELL_VALUE, value);
            }
            if (null != cellStyleTemp) {
                cell.setCellStyle(cellStyleTemp);
            }
            if (!this.setCellValue(cell, value, property, blankFixed)) {
                isNotBlankRow = true;
            }
        }
        return isNotBlankRow;
    }

    /**
     * 判断是否需要初始化新的一页
     *
     * @return 若需要初始化则返回true, 反之false
     */
    private boolean needInitSheet() throws WriteExcelException {
        if (null == this.currentSheet) {
            //证明是第一页,肯定需要初始化
            return true;
        }
        //标识即将写入的下一行的行标
        int nextRowIndex = this.currentRowInSheet;
        if (!this.isBlankLastRow || !this.isSkipBlankRow) {
            nextRowIndex++;
        }
        boolean needInitSheet = nextRowIndex >= this.allowMaxRows;

        if (0 >= this.limit && !needInitSheet && null != this.writeExcelCallback) {
            //此种情况表示未达到强制换页,若配置了手动换页则调用手动换页回调
            Boolean formFeedManually =
                    this.writeExcelCallback.formFeedManually(this.currentRowInSheet, this.currentSheet, this);
            if (null != formFeedManually) {
                needInitSheet = formFeedManually;
            }
        }

        return needInitSheet;
    }

    /**
     * 初始化新的一页
     */
    protected void initSheet() throws WriteExcelException {
        this.currentSheet = this.workbook.createSheet();
        for (int i = 0; i < this.properties.size(); i++) {
            if (MapUtils.isNotEmpty(this.columnWidthMapping) &&
                    null != this.columnWidthMapping.get(this.properties.get(i))
                    && 0 > Integer.valueOf(0).compareTo(this.columnWidthMapping.get(this.properties.get(i)))) {
                //若此属性存在列宽的映射则采用此映射
                this.currentSheet.setColumnWidth(i, this.columnWidthMapping.get(this.properties.get(i)) * 256);
            } else {
                //若此属性不存在列宽的映射则采用默认值
                this.currentSheet.setColumnWidth(i, this.defaultColumnWidth * 256);
            }
        }
        this.allSheetInExcel++;
        this.currentRowInSheet = this.startRowIndex - 1;
        this.currentPatriarch = null;
        this.isBlankLastRow = false;
        this.lastRow = null;
        //预留行回调
        if (null != this.writeExcelCallback && this.rowNumReserved >= 0) {
            this.writeExcelCallback.handleRowReserved(this.currentSheet, this);
        }
        this.writeTitle();
    }

    /**
     * 向当前Sheet写入标题(紧接着预留行的下一行),若用户没有开启写入标题总开关(即{@link #isWriteTitle}为false),
     * 则不会做任何操作
     */
    private void writeTitle() {
        if (!this.isWriteTitle || CollectionUtils.isEmpty(this.titles)) {
            return;
        }
        this.currentRowInSheet++;
        Row row = this.currentSheet.createRow(this.currentRowInSheet);
        if (this.defaultTitleHeight > 0) {
            row.setHeightInPoints(this.defaultTitleHeight);
        }
        for (int i = 0; i < this.titles.size(); i++) {
            Cell cell = row.createCell(i);
            CellStyle cellStyleTemp = this.defaultTitleCellStyle;
            if (MapUtils.isNotEmpty(this.titleCellStyleMapping)) {
                cellStyleTemp = this.titleCellStyleMapping.getOrDefault(this.properties.get(i), cellStyleTemp);
            }
            cell.setCellStyle(cellStyleTemp);
            cell.setCellValue(this.titles.get(i));
        }
        this.realRowInExcel++;
    }


    /**
     * 写入指定数据
     *
     * @param data 指定被写入的数据
     */
    private void writePerData(T data) throws WriteExcelException {
        if (this.needInitSheet()) {
            this.initSheet();
        }
        //写入行前置回调
        if (null != this.writeExcelCallback &&
                !this.writeExcelCallback
                        .beforeWritePerRow(data, this.currentRowInSheet, this.lastRow, this.currentSheet, this)) {
            return;
        }
        //将data转换成Map数据结构
        Map<String, Object> mapBean = null;
        if (data instanceof Map) {
            mapBean = (Map<String, Object>) data;
        } else {
            mapBean = CommonUtils.toMap(data);
        }
        Row rowTemp = null;
        if (!this.isBlankLastRow || !this.isSkipBlankRow) {
            //下移一行
            this.currentRowInSheet++;
            rowTemp = this.currentSheet.createRow(this.currentRowInSheet);
            if (this.defaultHeight > 0) {
                rowTemp.setHeightInPoints(this.defaultHeight);
            }
        } else {
            //上一行是空行,并且配置了需要跳过空行,即不需要写入空行
            rowTemp = this.lastRow;
        }
        boolean isBlankRow = !this.writePerRow(rowTemp, mapBean, data);
        if (!isBlankRow) {
            //非空行
            this.realRowInExcel++;
            this.isBlankLastRow = false;
        } else if (this.isSkipBlankRow) {
            //是空行并且不需要写入空行
            this.isBlankLastRow = true;
            this.lastRow = rowTemp;
        }
        //写入行后置回调
        if (null != this.writeExcelCallback) {
            this.writeExcelCallback.afterWritePerRow(data, this.currentRowInSheet, rowTemp, this.currentSheet, this);
        }
    }

    /**
     * 初始化写入数据
     *
     * @param datas 被写入的数据
     */
    private void initWrite(List<T> datas) {
        if (null == this.datas) {
            this.datas = new ArrayList<T>();
        }
        if (null != datas) {
            this.datas.addAll(datas);
        }
    }

    /**
     * 写多个数据
     *
     * @param datas 被写入的数据
     * @return this
     * @throws WriteExcelException 校验异常
     */
    public AbstractExcelWriter<T> write(List<T> datas) throws WriteExcelException {
        this.initWrite(datas);
        this.validateDataPerWrite();
        for (T data : this.datas) {
            this.writePerData(data);
        }
        this.datas.clear();
        return this;
    }

    /**
     * 写单个数据
     *
     * @param data 被写入的数据
     * @return this
     * @throws WriteExcelException 校验异常
     * @see #write(List)
     */
    public AbstractExcelWriter<T> write(T data) throws WriteExcelException {
        List<T> list = new ArrayList<>(1);
        if (null != data) {
            list.add(data);
        }
        return this.write(list);
    }

    /**
     * 结束写入并将workbook写入到指定文件
     *
     * @param file 目标文件
     * @return 参考 {@link #endWrite(OutputStream)}
     */
    public boolean endWrite(File file) {
        FileOutputStream fos = null;
        boolean endSuccess = false;
        try {
            fos = new FileOutputStream(file);
            endSuccess = this.endWrite(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            CommonUtils.closeIOStream(null, fos);
        }
        return endSuccess;
    }

    /**
     * 结束写入并将workbook输出到指定流
     *
     * @param os 该流需要手动关闭
     * @return 参考实现类
     */
    public boolean endWrite(OutputStream os) {
        if (0 >= this.workbook.getNumberOfSheets()) {
            //若workbook是空的则至少含有一个Sheet,否则生成的文件无法打开
            this.currentSheet = this.workbook.createSheet();
            this.writeTitle();
        }
        return true;
    }



    /**
     * 写入Excel时的一些回调方法
     */
    public interface WriteExcelCallback<T> {
        /**
         * 手动换页回调,只有当{@link #limit}&lt;=0并且没有超出{@link #allowMaxRows}限制时有效,
         * 即第一页或者开启了{@link #limit}限制或者数据达到Excel规定最大值时,此方法不会生效,因为这三种情况会被强制换页
         *
         * @param currentRowInSheet 当前sheet行坐标
         * @param currentSheet      当前sheet(换页前的Sheet)
         * @param writer            当前{@link AbstractExcelWriter}实现类对象
         * @return 若为null表示使用默认判断换页流程, 若为true表示换页, false表示不换页
         */
        default public Boolean formFeedManually(int currentRowInSheet, Sheet currentSheet,
                                                AbstractExcelWriter<T> writer)
                throws WriteExcelException { return null; }

        /**
         * 当写入每个Sheet的预留行时调用,你可以使用此回调对预留行的内容进行自定义
         *
         * @param sheet  当前正在写入的Sheet
         * @param writer 当前{@link AbstractExcelWriter}实现类对象,可以使用此对象获取{@link CellStyle}和{@link org.apache.poi.ss.usermodel.Font}对象
         */
        default public void handleRowReserved(Sheet sheet, AbstractExcelWriter<T> writer) throws WriteExcelException {}

        /**
         * 向Excel里写入一行前的回调(写入行前置回调)
         *
         * @param data              该行数据
         * @param currentRowInSheet 当前sheet行坐标(实际上是上一行的行坐标)
         * @param lastRow           当{@link #isBlankLastRow}返回true时才会有值,表示上一行的行对象
         * @param currentSheet      当前sheet
         * @param writer            当前{@link AbstractExcelWriter}实现类对象
         * @return true表示在调用完该回调之后还会自动在 {@link #currentRowInSheet} 行坐标下写入该行数据,false则不会再自动写入该行数据
         */
        default public boolean beforeWritePerRow(T data, int currentRowInSheet, Row lastRow, Sheet currentSheet,
                                                 AbstractExcelWriter<T> writer)
                throws WriteExcelException {return true;}

        /**
         * 此回调需要在 {@link #beforeWritePerRow} 返回true时才会有效,当在设置数据单元格的值时调用,你可以使用此回调来修改即将写入单元格的值,
         * 同时你也可以通过传入的cellStyle来修改此单元格的样式。
         * 需要注意的是:若你需要修改即将写入单元格的值或者样式,你需要将修改后的值或者样式cellStyle对象通过
         * {@link #putCellValueToMap(Object)}和
         * {@link #putCellStyleToMap(CellStyle)}放入
         * map中。若你不想修改默认的值或者样式,不需要调用以上两个方法即可
         *
         * @param property 该数据单元格对应的属性名
         * @param value    该数据单元格对应的数据值
         * @param data     该行数据
         * @param writer   当前{@link AbstractExcelWriter}实现类对象,可以使用此对象获取{@link CellStyle}和{@link org.apache.poi.ss.usermodel.Font}对象,
         *                 自定义的cellStyle需要放入{@link #cellStylePool}中以便复用,因为POI对cellStyle对象的创建数量有限制
         * @return 若为null则表示该单元格空值判定方法采用默认方式,
         * 反之使用指定的布尔值来判断该单元格是否为空单元格(true表示该单元格固定为空单元格,false表示该单元格固定为非空单元格)
         * @see #CELL_VALUE
         * @see #CELL_STYLE
         * @see #cellStylePool
         * @see #map
         */
        default public Boolean handleCellValue(String property, Object value, T data, AbstractExcelWriter<T> writer)
                throws WriteExcelException {return null;}


        /**
         * 向Excel里写入一行后的回调(写入行后置回调),此回调需要在 {@link #beforeWritePerRow} 返回true时才会有效
         *
         * @param data              该行数据
         * @param currentRowInSheet 当前sheet行坐标
         * @param currentRow        当前行对象
         * @param currentSheet      当前sheet
         * @param writer            当前{@link AbstractExcelWriter}实现类对象
         */
        default public void afterWritePerRow(T data, int currentRowInSheet, Row currentRow, Sheet currentSheet,
                                             AbstractExcelWriter<T> writer) throws WriteExcelException {}

    }
}
