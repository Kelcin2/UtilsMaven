/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.github.flyinghe.depdcy;

import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by FlyingHe on 2017/8/12.
 */
public class FormatTrackingHSSFListenerPlus implements HSSFListener {
    private final static POILogger logger = POILogFactory.getLogger(FormatTrackingHSSFListener.class);
    private final HSSFListener _childListener;
    private final HSSFDataFormatter _formatter;
    private final NumberFormat _defaultFormat;
    private final Map<Integer, FormatRecord> _customFormatRecords = new HashMap<Integer, FormatRecord>();
    private final List<ExtendedFormatRecord> _xfRecords = new ArrayList<ExtendedFormatRecord>();

    /**
     * Creates a format tracking wrapper around the given listener, using
     * the {@link Locale#getDefault() default locale} for the formats.
     *
     * @param childListener the listener to be wrapped
     */
    public FormatTrackingHSSFListenerPlus(HSSFListener childListener) {
        this(childListener, LocaleUtil.getUserLocale());
    }

    /**
     * Creates a format tracking wrapper around the given listener, using
     * the given locale for the formats.
     *
     * @param childListener the listener to be wrapped
     * @param locale        the locale for the formats
     */
    public FormatTrackingHSSFListenerPlus(
            HSSFListener childListener, Locale locale) {
        _childListener = childListener;
        _formatter = new HSSFDataFormatter(locale);
        _defaultFormat = NumberFormat.getInstance(locale);
    }

    protected int getNumberOfCustomFormats() {
        return _customFormatRecords.size();
    }

    protected int getNumberOfExtendedFormats() {
        return _xfRecords.size();
    }

    /**
     * Process this record ourselves, and then pass it on to our child listener
     */
    @Override
    public void processRecord(Record record) {
        // Handle it ourselves
        processRecordInternally(record);

        // Now pass on to our child
        _childListener.processRecord(record);
    }

    /**
     * Process the record ourselves, but do not pass it on to the child
     * Listener.
     *
     * @param record the record to be processed
     */
    public void processRecordInternally(Record record) {
        if (record instanceof FormatRecord) {
            FormatRecord fr = (FormatRecord) record;
            _customFormatRecords.put(Integer.valueOf(fr.getIndexCode()), fr);
        }
        if (record instanceof ExtendedFormatRecord) {
            ExtendedFormatRecord xr = (ExtendedFormatRecord) record;
            _xfRecords.add(xr);
        }
    }

    /**
     * Formats the given numeric of date cells contents as a String, in as
     * close as we can to the way that Excel would do so. Uses the various
     * format records to manage this.
     * <p>
     * TODO - move this to a central class in such a way that hssf.usermodel can
     * make use of it too
     *
     * @param cell the cell
     * @return the given numeric of date cells contents as a String
     */
    public String formatNumberDateCell(CellValueRecordInterface cell) {
        double value;
        if (cell instanceof NumberRecord) {
            value = ((NumberRecord) cell).getValue();
        } else if (cell instanceof FormulaRecord) {
            value = ((FormulaRecord) cell).getValue();
        } else {
            throw new IllegalArgumentException("Unsupported CellValue Record passed in " + cell);
        }

        // Get the built in format, if there is one
        int formatIndex = getFormatIndex(cell);
        String formatString = getFormatString(cell);

        if (formatString == null) {
            return _defaultFormat.format(value);
        }
        // Format, using the nice new
        // HSSFDataFormatter to do the work for us
        return this.formatRawCellContents(value, formatIndex, formatString);
    }

    /**
     * 判断是否是日期，若是返回日期字符串，否则返回数字字符串
     *
     * @param value
     * @param formatIndex
     * @param formatString
     * @return
     */
    private String formatRawCellContents(double value, int formatIndex, String formatString) {

        // Is it a date?
        if (DateUtil.isADateFormat(formatIndex, formatString)) {
            if (DateUtil.isValidExcelDate(value)) {

                Date d = DateUtil.getJavaDate(value);
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                return f.format(d);
            }
        }
        String _value = NumberToTextConverter.toText(value);
        if (_value.indexOf('E') > -1) {
            //若是科学计数法,则转换为非科学计数法
            return new BigDecimal(value).toString();
        }
        return _value;
    }

    /**
     * Returns the format string, eg $##.##, for the given number format index.
     *
     * @param formatIndex the format index
     * @return the format string
     */
    public String getFormatString(int formatIndex) {
        String format = null;
        if (formatIndex >= HSSFDataFormat.getNumberOfBuiltinBuiltinFormats()) {
            FormatRecord tfr = _customFormatRecords.get(Integer.valueOf(formatIndex));
            if (tfr == null) {
                logger.log(POILogger.ERROR, "Requested format at index " + formatIndex
                    + ", but it wasn't found");
            } else {
                format = tfr.getFormatString();
            }
        } else {
            format = HSSFDataFormat.getBuiltinFormat((short) formatIndex);
        }
        return format;
    }

    /**
     * Returns the format string, eg $##.##, used by your cell
     *
     * @param cell the cell
     * @return the format string
     */
    public String getFormatString(CellValueRecordInterface cell) {
        int formatIndex = getFormatIndex(cell);
        if (formatIndex == -1) {
            // Not found
            return null;
        }
        return getFormatString(formatIndex);
    }

    /**
     * Returns the index of the format string, used by your cell, or -1 if none found
     *
     * @param cell the cell
     * @return the index of the format string
     */
    public int getFormatIndex(CellValueRecordInterface cell) {
        ExtendedFormatRecord xfr = _xfRecords.get(cell.getXFIndex());
        if (xfr == null) {
            logger.log(POILogger.ERROR, "Cell " + cell.getRow() + "," + cell.getColumn()
                + " uses XF with index " + cell.getXFIndex() + ", but we don't have that");
            return -1;
        }
        return xfr.getFormatIndex();
    }
}
