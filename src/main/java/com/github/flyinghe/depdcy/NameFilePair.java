package com.github.flyinghe.depdcy;

import java.io.File;
import java.io.Serializable;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class NameFilePair implements Serializable {
    private String name;
    private File file;

    public NameFilePair() {
    }

    public NameFilePair(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
