package com.github.flyinghe.tools.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by FlyingHe on 2019/10/20.
 */
public class PrefixLogFilter extends LevelFilter {
    protected Level level;
    private String prefixes;
    private String[] prefixArray;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }
        return this.level.equals(event.getLevel()) &&
                ArrayUtils.contains(this.prefixArray, StringUtils.substringBefore(event.getLoggerName(), ",")) ?
                this.onMatch : this.onMismatch;
    }

    public void setPrefixes(String prefixes) {
        this.prefixes = prefixes;
        this.prefixArray = StringUtils.split(this.prefixes, ",");
        if (ArrayUtils.isNotEmpty(this.prefixArray)) {
            for (int i = 0; i < prefixArray.length; i++) {
                this.prefixArray[i] = this.prefixArray[i].trim();
            }
        }
    }

    @Override
    public void setLevel(Level level) {
        this.level = level;
        super.setLevel(level);
    }
}
