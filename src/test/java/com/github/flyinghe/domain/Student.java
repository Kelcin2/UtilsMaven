package com.github.flyinghe.domain;

import java.util.Date;

/**
 * Created by FlyingHe on 2017/8/27.
 */
public class Student {
    private String name;
    private Integer age;
    private Float price;
    private Double prices;
    private Byte _byte;
    private Short _short;
    private String email;
    private String tel;
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Double getPrices() {
        return prices;
    }

    public void setPrices(Double prices) {
        this.prices = prices;
    }

    public Byte get_byte() {
        return _byte;
    }

    public void set_byte(Byte _byte) {
        this._byte = _byte;
    }

    public Short get_short() {
        return _short;
    }

    public void set_short(Short _short) {
        this._short = _short;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
