package com.github.flyinghe.domain;

import java.util.Date;

/**
 * Created by Flying on 2016/5/28.
 */
public class Pet {
    private String name;
    private Date birth;

    public Pet() {
    }

    public Pet(String name, Date birth) {
        this.name = name;
        this.birth = birth;
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


}
