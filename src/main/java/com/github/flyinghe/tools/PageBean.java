package com.github.flyinghe.tools;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Flying on 2016/6/5.
 * <p>本类用于分页操作</p>
 */
public class PageBean<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer pageCode;// 当前页码
    private Integer pageRecord;// 当前页的最多记录数
    private Long totalRecord;// 总记录数
    private Integer indexNo;//索引数，如（索引数为5）：上一页 1 2 3 ... 4 5 下一页
    private Integer begin;// 开始页码,页面上显示的页码索引，如:上一页 1 2 3 ... 4 5 下一页(通过其他属性计算得出)
    private Integer end;// 结束页码(通过其他属性计算得出)

    private List<T> beanList = null;// 当前页的记录对象

    /**
     * @param pageCode    当前页码
     * @param pageRecord  设置的每页显示的记录数
     * @param totalRecord 总的记录数
     * @param indexNo     索引数
     */
    public PageBean(Integer pageCode, Integer pageRecord, Long totalRecord, Integer indexNo) {
        this.pageCode = pageCode;
        this.pageRecord = pageRecord;
        this.totalRecord = totalRecord;
        this.indexNo = indexNo;
    }

    /**
     * @param pageCode    当前页码
     * @param pageRecord  设置的每页显示的记录数
     * @param indexNo     索引数
     * @param totalRecord 总的记录数
     * @param beanList    记录对像集合
     */
    public PageBean(Integer pageCode, Integer pageRecord, Integer indexNo, Long totalRecord, List<T> beanList) {
        super();
        this.pageCode = pageCode;
        this.pageRecord = pageRecord;
        this.indexNo = indexNo;
        this.totalRecord = totalRecord;
        this.beanList = beanList;
    }

    /**
     * 获取当前页码
     *
     * @return 返回当前页码
     */
    public Integer getPageCode() {
        return pageCode;
    }

    /**
     * 设置当前页码
     *
     * @param pageCode 当前页码
     */
    public void setPageCode(Integer pageCode) {
        this.pageCode = pageCode;
    }

    /**
     * 获取设置的每页显示的记录数
     *
     * @return
     */
    public Integer getPageRecord() {
        return pageRecord;
    }

    /**
     * 设置每页显示的记录数
     *
     * @param pageRecord 每页记录数
     */
    public void setPageRecord(Integer pageRecord) {
        this.pageRecord = pageRecord;
    }

    /**
     * 获取总的记录数
     *
     * @return
     */
    public Long getTotalRecord() {
        return totalRecord;
    }

    /**
     * 设置总的记录数
     *
     * @param totalRecord 总记录数
     */
    public void setTotalRecord(Long totalRecord) {
        this.totalRecord = totalRecord;
    }

    /**
     * 获取索引数
     *
     * @return 返回索引数
     */
    public Integer getIndexNo() {
        return indexNo;
    }

    /**
     * 设置索引数
     *
     * @param indexNo 索引数
     */
    public void setIndexNo(Integer indexNo) {
        this.indexNo = indexNo;
    }

    /**
     * 获取开始页码
     *
     * @return 返回开始页码
     */
    public Integer getBegin() {
        this.setBeginAndEnd();
        return begin;
    }

    /**
     * 获取结束页码
     *
     * @return 返回结束页码
     */
    public Integer getEnd() {
        this.setBeginAndEnd();
        return end;
    }

    /**
     * 获取记录总页数
     *
     * @return 返回记录总页数
     */
    public Integer getTotalPage() {
        Number i = this.totalRecord / this.pageRecord;
        return this.totalRecord % this.pageRecord == 0 ? i.intValue() : i.intValue() + 1;
    }

    /**
     * 获取记录对象
     *
     * @return 返回记录对像
     */
    public List<T> getBeanList() {
        return beanList;
    }

    /**
     * 设置记录对象
     *
     * @param beanList 记录对像集合
     */
    public void setBeanList(List<T> beanList) {
        this.beanList = beanList;
    }

    /**
     * 根据其他成员参数设置该成员参数
     */
    private void setBeginAndEnd() {
        Integer totalPage = this.getTotalPage();
        if (totalPage <= this.indexNo) {
            this.begin = 1;
            this.end = totalPage;
            return;
        }

        int minus = this.indexNo / 2;
        int add = this.indexNo % 2 == 0 ? minus - 1 : minus;
        if (this.pageCode - minus < 1) {
            this.begin = 1;
            this.end = this.indexNo;
            return;
        }
        if (this.pageCode + add > totalPage) {
            this.begin = totalPage - (this.indexNo - 1);
            this.end = totalPage;
            return;
        }

        this.begin = this.pageCode - minus;
        this.end = this.pageCode + add;
    }

}
