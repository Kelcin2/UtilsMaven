package com.github.flyinghe.domain;


/**
 * Created by Flying on 2016/5/28.
 */
public class Person {
    private Integer age;
    private String address;

    public Person(Integer age, String address) {
        this.age = age;
        this.address = address;
    }

    public Person() {
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
