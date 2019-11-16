package com.github.flyinghe.tools.date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.IOException;
import java.util.Date;

/**
 * Created by FlyingHe on 2019/10/19.
 * 自定义Jackson序列化日期类型时应用的类型转换器
 */
public class DateJacksonSerializer extends JsonSerializer<Date> {
    private String datePattern = "yyyy-MM-dd HH:mm:ss";

    public DateJacksonSerializer() {
    }

    public DateJacksonSerializer(String datePattern) {
        this.datePattern = datePattern;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (date != null) {
            jsonGenerator.writeString(DateFormatUtils.format(date, this.datePattern));
        } else {
            jsonGenerator.writeNull();
        }
    }

    @Override
    public Class<Date> handledType() {
        return Date.class;
    }
}
