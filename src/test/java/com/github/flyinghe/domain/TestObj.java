package com.github.flyinghe.domain;

import java.util.*;

/**
 * Created by FlyingHe on 2018/12/20.
 */
public class TestObj {
    private String stringType;
    private Character charType;
    private Date dateType;
    private Calendar calendarType;
    private Boolean booleanType;
    private Byte byteType;
    private Short shortType;
    private Integer integerType;
    private Long longType;
    private Float floatType;
    private Double doubleType;
    private Object nullType;
    private List<User> listType;
    private User[] arrayType;
    private Map<String, Object> mapType;
    private User userType;

    public String getStringType() {
        return stringType;
    }

    public void setStringType(String stringType) {
        this.stringType = stringType;
    }

    public Character getCharType() {
        return charType;
    }

    public void setCharType(Character charType) {
        this.charType = charType;
    }

    public Date getDateType() {
        return dateType;
    }

    public void setDateType(Date dateType) {
        this.dateType = dateType;
    }

    public Calendar getCalendarType() {
        return calendarType;
    }

    public void setCalendarType(Calendar calendarType) {
        this.calendarType = calendarType;
    }

    public Boolean getBooleanType() {
        return booleanType;
    }

    public void setBooleanType(Boolean booleanType) {
        this.booleanType = booleanType;
    }

    public Byte getByteType() {
        return byteType;
    }

    public void setByteType(Byte byteType) {
        this.byteType = byteType;
    }

    public Short getShortType() {
        return shortType;
    }

    public void setShortType(Short shortType) {
        this.shortType = shortType;
    }

    public Integer getIntegerType() {
        return integerType;
    }

    public void setIntegerType(Integer integerType) {
        this.integerType = integerType;
    }

    public Long getLongType() {
        return longType;
    }

    public void setLongType(Long longType) {
        this.longType = longType;
    }

    public Float getFloatType() {
        return floatType;
    }

    public void setFloatType(Float floatType) {
        this.floatType = floatType;
    }

    public Double getDoubleType() {
        return doubleType;
    }

    public void setDoubleType(Double doubleType) {
        this.doubleType = doubleType;
    }

    public Object getNullType() {
        return nullType;
    }

    public void setNullType(Object nullType) {
        this.nullType = nullType;
    }

    public List<User> getListType() {
        if (null == this.listType) {
            this.listType = new ArrayList<>(16);
        }
        return listType;
    }

    public void setListType(List<User> listType) {
        this.listType = listType;
    }

    public User[] getArrayType() {
        return arrayType;
    }

    public void setArrayType(User[] arrayType) {
        this.arrayType = arrayType;
    }

    public Map<String, Object> getMapType() {
        return mapType;
    }

    public void setMapType(Map<String, Object> mapType) {
        this.mapType = mapType;
    }

    public User getUserType() {
        return userType;
    }

    public void setUserType(User userType) {
        this.userType = userType;
    }
}
