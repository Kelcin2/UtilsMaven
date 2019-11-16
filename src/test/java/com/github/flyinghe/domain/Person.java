package com.github.flyinghe.domain;


import java.util.Date;
import java.util.List;

/**
 * Created by Flying on 2016/5/28.
 */
public class Person {
    private Integer age;
    private String address;
    private Date birthday;
    private List<Date> birthdaies;

    public Person(Integer age, String address) {
        this.age = age;
        this.address = address;
    }

    public Person() {
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public List<Date> getBirthdaies() {
        return birthdaies;
    }

    public void setBirthdaies(List<Date> birthdaies) {
        this.birthdaies = birthdaies;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
