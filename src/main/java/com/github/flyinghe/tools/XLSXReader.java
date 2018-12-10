package com.github.flyinghe.tools;

import com.github.flyinghe.depdcy.ExcelHandler;
import com.github.flyinghe.depdcy.XSSFSheetXMLHandlerPlus;
import com.github.flyinghe.depdcy.XSSFSheetXMLHandlerPlus.SheetContentsHandler;
import com.github.flyinghe.exception.ReadExcelException;
import com.github.flyinghe.exception.ReadExcelRuntimeException;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;

/**
 * Created by FlyingHe on 2017/8/9.
 * <p>
 * (POI 3.14及以上版本)此类采用流方式读取Excel（2007版本以上,文件后缀为xlsx）文件，
 * 一般用于读取数据量较大的文件,较小数据量可采用此类({@link ReadExcelUtils})读取。
 * 注意:读取的Excel文件格式为，第0行(0-based)为标题行(可作为列名行列名的解释说明)，
 * 第1行为列名行(存储在Map中时作为key),后面的行称为数据行(存储在Map时的value),
 * Excel文件中每一个Sheet的格式均是这样
 * </p>
 */
public class XLSXReader implements SheetContentsHandler {
    private static final String[] DATE_PATTERN = new String[]{"yyyy-MM-dd HH:mm:ss.S"};
    private OPCPackage xlsxPackage;
    //是否是新的一行
    private boolean isNewRow = false;
    //记录当前列，0-based
    private int currentColInRow = -1;
    //记录当前sheet的当前行，0-based
    private int currentRowInSheet = -1;
    //当前sheet，0-based
    private int currentSheetInExcel = -1;
    //记录该sheet的数据的非空行数（即不包括第0行和第一行等标题行以及所有空行）
    private int realRowInSheet = 0;
    //记录整个Excel文档的数据的非空行数（即不包括所有sheet的第0行和第一行等标题行以及所有空行）
    private int realRowInExcel = 0;
    //记录整个Excel文档的sheet数
    private int allSheetInExcel = 0;
    //记录标题,即第0行，0-based
    private List<String> titles = new ArrayList<String>();
    //记录列名,即第1行，0-based
    private List<String> columns = new ArrayList<String>();
    //记录每一个数据行(即不包括所有sheet的第0行和第一行等标题行以及所有空行,0-based)
    private List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
    //指定最多读取多少数据行，即datas里最多含有多少元素，<= 0表示不限制,若指定了limit则在达到限制后会清空datas再放入新的元素
    private int limit = -1;
    //在datas达到limit限制后执行的回调函数
    private ExcelHandler callback;
    //若有小数则指定保留几位小数,若为Null或者<=0则表示不四舍五入
    private Integer scale;

    /**
     * @param p The XLSX package to process
     */
    public XLSXReader(OPCPackage p) {
        this(p, null);
    }

    /**
     * @param p     The XLSX package to process
     * @param scale 指定若数值中含有小数则保留几位小数，四舍五入,null或者&lt;=0表示不四舍五入
     */
    public XLSXReader(OPCPackage p, Integer scale) {
        this(p, scale, -1, null);
    }

    /**
     * @param p        The XLSX package to process
     * @param limit    指定{@link #datas}最多含有多少元素,&lt;=0表示不限制
     * @param callback 指定{@link #datas}达到{@link #limit}限制时执行的回调函数,
     *                 若为null则表示不执行回调函数。
     *                 注意：在{@link #datas}未达到指定限制而文件数据已经完全读取完毕的情况下也会调用回调函数(若有回调函数),
     *                 回调函数在datas被清空之前调用(若需要回调则必须启用限制,即{@link #limit} &gt;0)。
     */
    public XLSXReader(OPCPackage p, int limit, ExcelHandler callback) {
        this(p, null, limit, callback);
    }

    /**
     * @param p        The XLSX package to process
     * @param scale    指定若数值中含有小数则保留几位小数，四舍五入,null或者&lt;=0表示不四舍五入
     * @param limit    指定{@link #datas}最多含有多少元素,&lt;=0表示不限制
     * @param callback 指定{@link #datas}达到{@link #limit}限制时执行的回调函数,
     *                 若为null则表示不执行回调函数。
     *                 注意：在{@link #datas}未达到指定限制而文件数据已经完全读取完毕的情况下也会调用回调函数(若有回调函数),
     *                 回调函数在datas被清空之前调用(若需要回调则必须启用限制,即{@link #limit} &gt;0)。
     */
    public XLSXReader(OPCPackage p, Integer scale, int limit, ExcelHandler callback) {
        this.xlsxPackage = p;
        this.limit = limit;
        this.callback = callback;
        this.scale = scale;
    }

    public Integer getScale() {
        return scale;
    }

    /**
     * 获取数据，一般用于没有添加限制且需要将Excel中所有非空数据行(即不包括所有sheet的第0行和第一行等标题行以及所有空行)转换成beanMap时使用
     *
     * @return
     */
    public List<Map<String, Object>> getDatas() {
        return datas;
    }

    /**
     * 获取整个Excel文档的数据的非空行数（即不包括所有sheet的第0行和第一行等标题行以及所有空行）
     *
     * @return
     */
    public int getRealRowInExcel() {
        return realRowInExcel;
    }

    /**
     * 获取整个Excel文档的sheet数，需要{@link #process()}执行完毕后才能正确获得
     *
     * @return
     */
    public int getAllSheetInExcel() {
        return allSheetInExcel;
    }

    /**
     * 返回标题,一般在解析完整个Excel文档之后获取
     *
     * @return
     */
    public List<String> getTitles() {
        return titles;
    }

    /**
     * 返回列名，一般在解析完整个Excel文档之后获取
     *
     * @return
     */
    public List<String> getColumns() {
        return columns;
    }

    public int getLimit() {
        return limit;
    }

    /**
     * 每开始一个新的Sheet则初始化一些数据
     */
    private void initDataPerSheet() {
        this.allSheetInExcel++;
        this.currentSheetInExcel++;
        this.currentRowInSheet = -1;
        this.realRowInSheet = 0;
    }

    /**
     * 开始解析指定Sheet里的内容
     *
     * @param styles
     * @param strings
     * @param sheetHandler
     * @param sheetInputStream 指定sheet流
     * @throws ReadExcelException
     */
    public void processSheet(
            StylesTable styles,
            ReadOnlySharedStringsTable strings,
            SheetContentsHandler sheetHandler,
            InputStream sheetInputStream) throws ReadExcelException {

        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = SAXHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandlerPlus(
                    styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            //开始解析
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException e) {
            throw new ReadExcelException("SAX parser appears to be broken - " + e.getMessage());
        } catch (Exception e) {
            throw new ReadExcelException(e.getMessage());
        }
    }

    /**
     * 开始解析Excel文档
     *
     * @throws ReadExcelException
     */
    public void process() throws ReadExcelException {
        try {
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
            XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (iter.hasNext()) {
                //初始化数据
                this.initDataPerSheet();
                InputStream stream = iter.next();
                this.processSheet(styles, strings, this, stream);
                stream.close();
            }
            //解析完后，判断用户是否设置了limit，若设置了执行以下操作
            if (this.limit > 0) {
                if (!this.datas.isEmpty()) {
                    if (this.callback != null) {
                        //若数据不为空且回调函数被设置，则调用回调函数
                        this.callback.callback(this.currentRowInSheet, this.currentSheetInExcel, this.realRowInSheet,
                                this.realRowInExcel, this.allSheetInExcel, this.titles, this.columns, this.datas);
                    }
                    this.datas.clear();
                }
            }
        } catch (Exception e) {
            throw new ReadExcelException(e.getMessage());
        }
    }


    /**
     * 判断data的类型，尝试解析成日期类型(返回{@link Date})，其次布尔类型(返回{@link Boolean})，
     * 然后数字类型(转变成字符串输出)，
     * 前三种均解析失败则直接返回字符串data
     *
     * @param data
     * @return 解析后的值
     */
    private Object getValue(String data) {
        Object value = null;
        try {
            //判断是否是日期类型
            Date date = DateUtils.parseDate(data, DATE_PATTERN);
            value = date;
        } catch (ParseException e) {
            //抛异常证明非日期类型
            if ("true".equalsIgnoreCase(data)) {
                value = Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(data)) {
                value = Boolean.FALSE;
            } else {
                //判断是否数字
                try {
                    BigDecimal bd = new BigDecimal(data);
                    if (this.scale == null || this.scale <= 0) {
                        //不四舍五入，直接输出
                        value = data;
                    } else {
                        value = bd.toString().contains(".") ? bd.setScale(this.scale, RoundingMode.HALF_UP).toString() :
                                data;
                    }
                } catch (NumberFormatException e1) {
                    //证明不是数字,直接输出
                    value = data;
                }
            }
        }

        return value;
    }

    @Override
    public void startRow(int rowNum) {
        this.isNewRow = true;
        this.currentRowInSheet = rowNum;
        this.currentColInRow = -1;
        if (this.currentRowInSheet > 1) {
            //证明此行是数据行
            this.data = new HashMap<String, Object>();
        }
    }

    @Override
    public void endRow(int rowNum) {
        if (this.currentRowInSheet > 1 && !this.data.isEmpty()) {
            //证明此行是数据行,且不为空行
            this.datas.add(this.data);
            this.realRowInSheet++;
            this.realRowInExcel++;
        }
        //判断用户是否开启限制
        if (this.limit > 0) {
            //开启限制
            if (this.datas.size() >= this.limit) {
                if (this.callback != null) {
                    //若设置了回调函数则调用
                    try {
                        this.callback.callback(this.currentRowInSheet, this.currentSheetInExcel, this.realRowInSheet,
                                this.realRowInExcel, this.allSheetInExcel, this.titles, this.columns, this.datas);
                    } catch (Exception e) {
                        throw new ReadExcelRuntimeException("SAX parser appears to be broken - " + e.getMessage());
                    }
                }
                //超过限制则清空
                this.datas.clear();
            }
        }
    }

    //用于记录数据行里的数据（只能记录一行数据行的数据）
    private Map<String, Object> data;

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
        if (this.isNewRow) {
            this.isNewRow = false;
        }
        // gracefully handle missing CellRef here in a similar way as XSSFCell does
        if (cellReference == null) {
            cellReference = new CellAddress(this.currentRowInSheet, this.currentColInRow).formatAsString();
        }
        // 获取cellReference指向的列数,0-based
        int thisCol = (new CellReference(cellReference)).getCol();
        this.currentColInRow = thisCol;
        if (formattedValue == null || formattedValue.isEmpty()) {
            return;
        }
        if (this.currentRowInSheet == 0) {
            //标题行
            if (this.currentSheetInExcel == 0) {
                //若是第0个Sheet则添加标题，0-based
                this.titles.add(formattedValue);
            }
            return;
        }
        if (this.currentRowInSheet == 1) {
            //列名行
            if (this.currentSheetInExcel == 0) {
                //若是第0个Sheet则添加列名，0-based
                this.columns.add(formattedValue);
            }
            return;
        }
        //数据行
        String key = this.columns.get(this.currentColInRow);
        Object value = this.getValue(formattedValue);
        this.data.put(key, value);
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
        //do nothing
    }

    /**
     * 读取整个Excel文件,并把读取的数据放入beanMap中,全部返回
     *
     * @param file Excel文件
     * @return 返回所有封装好的beanMap数据
     * @throws ReadExcelException
     */
    public static List<Map<String, Object>> readExcelToMapList(File file) throws ReadExcelException {
        return XLSXReader.readExcelToMapList(file, null);
    }

    /**
     * 读取整个Excel文件,并把读取的数据放入beanMap中,全部返回
     *
     * @param file  Excel文件
     * @param scale 指定若数值中含有小数则保留几位小数，四舍五入,null或者&lt;=0表示不四舍五入
     * @return 返回所有封装好的beanMap数据
     * @throws ReadExcelException
     */
    public static List<Map<String, Object>> readExcelToMapList(File file, Integer scale) throws ReadExcelException {
        return XLSXReader.readExcel(file, scale).getDatas();
    }

    /**
     * 读取整个Excel文件,并把读取的数据放入Class的实例中,全部返回
     *
     * @param file  Excel文件
     * @param clazz 指定封装类型
     * @return 返回所有封装好的javabean数据, 没有则返回空List
     * @throws ReadExcelException
     */
    public static <T> List<T> readExcelToBeans(File file, Class<T> clazz) throws ReadExcelException {
        return XLSXReader.readExcelToBeans(file, null, clazz);
    }

    /**
     * 读取整个Excel文件,并把读取的数据放入Class的实例中,全部返回
     *
     * @param file  Excel文件
     * @param scale 指定若数值中含有小数则保留几位小数，四舍五入,null或者&lt;=0表示不四舍五入
     * @param clazz 指定封装类型
     * @return 返回所有封装好的javabean数据, 没有则返回空List
     * @throws ReadExcelException
     */
    public static <T> List<T> readExcelToBeans(File file, Integer scale, Class<T> clazz) throws ReadExcelException {
        List<Map<String, Object>> mapList = XLSXReader.readExcelToMapList(file, scale);
        List<T> beans = new ArrayList<T>();
        for (Map<String, Object> map : mapList) {
            T bean = CommonUtils.toBean(map, clazz);
            if (bean != null) {
                beans.add(bean);
            }
        }
        return beans;
    }

    /**
     * 读取整个Excel文件,并把读取的数据放入beanMap中,
     * 可以通过返回的{@link XLSXReader}对象调用{@link XLSXReader#getDatas()}方法拿到数据
     *
     * @param file Excel文件
     * @return 返回 {@link XLSXReader}对象，你可以通过此对象获取你需要的数据
     * @throws ReadExcelException
     */
    public static XLSXReader readExcel(File file) throws ReadExcelException {
        return XLSXReader.readExcel(file, null);
    }

    /**
     * 读取整个Excel文件,并把读取的数据放入beanMap中,
     * 可以通过返回的{@link XLSXReader}对象调用{@link XLSXReader#getDatas()}方法拿到数据
     *
     * @param file  Excel文件
     * @param scale 指定若数值中含有小数则保留几位小数，四舍五入,null或者&lt;=0表示不四舍五入
     * @return 返回 {@link XLSXReader}对象，你可以通过此对象获取你需要的数据
     * @throws ReadExcelException
     */
    public static XLSXReader readExcel(File file, Integer scale) throws ReadExcelException {
        OPCPackage p = null;
        XLSXReader reader = null;
        try {
            p = OPCPackage.open(file, PackageAccess.READ);
            reader = new XLSXReader(p, scale);
            reader.process();
        } catch (Exception e) {
            throw new ReadExcelException(e.getMessage());
        } finally {
            if (p != null) {
                try {
                    p.close();
                } catch (IOException e) {
                    throw new ReadExcelException(e.getMessage());
                }
            }
        }
        return reader;
    }

    /**
     * 用于读取limit行之后处理读取的数据(通过回调函数处理)
     *
     * @param file     Excel文件
     * @param limit    指定最多读取多少数据行，&lt;= 0表示不限制,若指定了limit则在达到限制后会移除旧元素再放入新的元素
     * @param callback 指定{@link #datas}达到{@link #limit}限制时执行的回调函数,
     *                 若为null则表示不执行回调函数。
     *                 注意：在{@link #datas}未达到指定限制而文件数据已经完全读取完毕的情况下也会调用回调函数(若有回调函数),
     *                 回调函数在datas被清空之前调用(若需要回调则必须启用限制,即{@link #limit} &gt;0)。
     * @throws ReadExcelException
     */
    public static void readExcel(File file, int limit, ExcelHandler callback)
            throws ReadExcelException {
        XLSXReader.readExcel(file, null, limit, callback);
    }

    /**
     * 用于读取limit行之后处理读取的数据(通过回调函数处理)
     *
     * @param file     Excel文件
     * @param scale    指定若数值中含有小数则保留几位小数，四舍五入,null或者&lt;=0表示不四舍五入
     * @param limit    指定最多读取多少数据行，&lt;= 0表示不限制,若指定了limit则在达到限制后会移除旧元素再放入新的元素
     * @param callback 指定{@link #datas}达到{@link #limit}限制时执行的回调函数,
     *                 若为null则表示不执行回调函数。
     *                 注意：在{@link #datas}未达到指定限制而文件数据已经完全读取完毕的情况下也会调用回调函数(若有回调函数),
     *                 回调函数在datas被清空之前调用(若需要回调则必须启用限制,即{@link #limit} &gt;0)。
     * @throws ReadExcelException
     */
    public static void readExcel(File file, Integer scale, int limit, ExcelHandler callback)
            throws ReadExcelException {
        OPCPackage p = null;
        XLSXReader reader = null;
        try {
            p = OPCPackage.open(file, PackageAccess.READ);
            reader = new XLSXReader(p, scale, limit, callback);
            reader.process();
        } catch (Exception e) {
            throw new ReadExcelException(e.getMessage());
        } finally {
            if (p != null) {
                try {
                    p.close();
                } catch (IOException e) {
                    throw new ReadExcelException(e.getMessage());
                }
            }
        }
    }
}
