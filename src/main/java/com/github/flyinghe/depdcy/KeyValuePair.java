package com.github.flyinghe.depdcy;

import java.io.Serializable;

/**
 * Created by FlyingHe on 2019/11/4.
 */
public class KeyValuePair<T> implements Serializable {
    protected String key;
    protected T value;

    public KeyValuePair() {
    }

    public KeyValuePair(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
