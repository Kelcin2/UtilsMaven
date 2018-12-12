package com.github.flyinghe.domain;


import java.util.Date;

/**
 * Created by Flying on 2016/5/28.
 */
public class User extends Person {
    private String name;
    private String sex;
    private Date birth;
    private Pet pet;

    public User(String name, String sex, Date birth) {
        super();
        this.name = name;
        this.sex = sex;
        this.birth = birth;
    }

    public User() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirth() {
        return birth;
    }

    public void setBirth(Date birth) {
        this.birth = birth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", birth=" + birth +
                '}';
    }
}
