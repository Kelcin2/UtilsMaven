package com.github.flyinghe.tools.date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * Created by FlyingHe on 2018/11/17.
 * 自定义Jackson反序列化日期类型时应用的类型转换器
 */
public class DateJacksonDeserializer extends JsonDeserializer<Date> {

    private String[] datePatterns = com.github.flyinghe.tools.date.DateUtils.pattern;

    public DateJacksonDeserializer() {
    }

    public DateJacksonDeserializer(String[] datePatterns) {
        this.datePatterns = datePatterns;
    }

    public String[] getDatePatterns() {
        return datePatterns;
    }

    public void setDatePatterns(String[] datePatterns) {
        this.datePatterns = datePatterns;
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return DateUtils.strToDate(p.getText(), this.datePatterns);
    }

    @Override
    public Class<?> handledType() {
        return Date.class;
    }
}
