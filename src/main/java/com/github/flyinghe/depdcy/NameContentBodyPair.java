package com.github.flyinghe.depdcy;

import org.apache.hc.client5.http.entity.mime.ContentBody;

/**
 * Created by FlyingHe on 2019/11/2.
 */
public class NameContentBodyPair extends KeyValuePair<ContentBody> {

    public NameContentBodyPair() {
    }

    public NameContentBodyPair(String key, ContentBody value) {
        super(key, value);
    }

}
