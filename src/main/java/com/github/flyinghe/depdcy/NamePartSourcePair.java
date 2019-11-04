package com.github.flyinghe.depdcy;

import org.apache.commons.httpclient.methods.multipart.PartSource;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class NamePartSourcePair extends KeyValuePair<PartSource> {

    public NamePartSourcePair() {
    }

    public NamePartSourcePair(String key, PartSource value) {
        super(key, value);
    }

}
