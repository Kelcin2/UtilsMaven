package com.github.flyinghe.tools;

/**
 * Created by FlyingHe on 2018/2/7.
 */
public class Sort {
    public static final String ORDER_TYPE_DESC = "DESC";
    public static final String ORDER_TYPE_ASC = "ASC";
    private String orderBy;
    private String orderType = ORDER_TYPE_ASC;

    public Sort(String orderBy) {
        super();
        this.orderBy = orderBy;
    }

    public Sort(String orderBy, String orderType) {
        super();
        this.orderBy = orderBy;
        this.orderType = orderType;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
}
