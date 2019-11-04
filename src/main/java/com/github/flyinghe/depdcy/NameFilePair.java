package com.github.flyinghe.depdcy;

import java.io.File;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class NameFilePair extends KeyValuePair<File> {

    public NameFilePair() {
    }

    public NameFilePair(String key, File value) {
        super(key, value);
    }
}
