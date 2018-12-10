package com.github.flyinghe.depdcy;

import com.github.flyinghe.exception.ReadExcelException;
import com.github.flyinghe.tools.XLSReader;
import com.github.flyinghe.tools.XLSXReader;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by FlyingHe on 2017/8/9.
 * 此接口为{@link XLSReader}和{@link XLSXReader}提供回调函数
 */
public interface ExcelHandler {
    /**
     * 此函数作为{@link XLSReader}和{@link XLSXReader}读取Excel文件工具类的回调函数
     *
     * @param currentRowInSheet   调用此函数时的当前sheet的当前行坐标，0-based
     * @param currentSheetInExcel 调用此函数时的当前sheet坐标，0-based
     * @param realRowInSheet      调用此函数时的当前sheet已经解析的数据的非空行数（即不包括第0行和第一行等标题行以及所有空行）
     * @param realRowInExcel      调用此函数时整个Excel文档已经解析的数据的非空行数（即不包括所有sheet的第0行和第一行等标题行以及所有空行）
     * @param allSheetInExcel     调用此函数时整个Excel文档已经解析的sheet数(包含正在解析的Sheet)
     * @param titles              标题,即第0行数据，0-based
     * @param columns             列名,即第1行数据，0-based
     * @param datas               datas被清空前调用此函数时的数据行数据，
     *                            在limit &gt;0的情况下datas.size()一般等于limit
     *                            (在datas未达到指定限制而文件数据已经完全读取完毕的情况下datas.size()会小于limit),
     *                            注意：若没有设置limit(即limit &lt;=0的情况下)，不会调用回调函数，
     *                            此时你应该使用类似于{@link XLSXReader#readExcelToMapList(File)}等等
     *                            不需要提供回调函数的静态函数来做处理
     * @throws ReadExcelException
     * @see XLSReader#readExcelToMapList(File)
     * @see XLSReader#readExcelToMapList(File, Integer)
     * @see XLSReader#readExcel(File)
     * @see XLSReader#readExcel(File, Integer)
     */
    public void callback(int currentRowInSheet, int currentSheetInExcel, int realRowInSheet, int realRowInExcel,
                         int allSheetInExcel, List<String> titles, List<String> columns,
                         List<Map<String, Object>> datas) throws ReadExcelException;
}
