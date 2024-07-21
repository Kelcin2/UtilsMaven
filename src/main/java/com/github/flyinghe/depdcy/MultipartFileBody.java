package com.github.flyinghe.depdcy;

import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.core5.http.ContentType;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by FlyingHe on 2019/11/5.
 */
public class MultipartFileBody extends InputStreamBody {

    public MultipartFileBody(MultipartFile multipartFile) throws IOException {
        super(
                null != multipartFile ? multipartFile.getInputStream() : new ByteArrayInputStream(new byte[]{}),
                ContentType.DEFAULT_BINARY,
                Optional.ofNullable(multipartFile).map(MultipartFile::getOriginalFilename).orElse("noname"),
                Optional.ofNullable(multipartFile).map(MultipartFile::getSize).orElse(0L)
        );
    }
}
