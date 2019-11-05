package com.github.flyinghe.depdcy;

import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by FlyingHe on 2019/11/5.
 */
public class MultipartFilePartSource implements PartSource {
    private MultipartFile multipartFile;

    public MultipartFilePartSource(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    @Override
    public long getLength() {
        return null != this.multipartFile ? this.multipartFile.getSize() : 0L;
    }

    @Override
    public String getFileName() {
        return null != this.multipartFile ? this.multipartFile.getOriginalFilename() : "noname";
    }

    @Override
    public InputStream createInputStream() throws IOException {
        return null != this.multipartFile ? this.multipartFile.getInputStream() :
                new ByteArrayInputStream(new byte[]{});
    }
}
