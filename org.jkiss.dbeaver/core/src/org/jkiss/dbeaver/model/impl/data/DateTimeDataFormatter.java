package org.jkiss.dbeaver.model.impl.data;

import org.jkiss.dbeaver.model.data.DBDDataFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DateTimeDataFormatter implements DBDDataFormatter {

    public static final String PROP_PATTERN = "pattern";

    private DateFormat dateFormat;

    public Map<String, String> getDefaultProperties(Locale locale)
    {
        return null;
    }

    public void init(Locale locale, Map<String, String> properties)
    {
        dateFormat = new SimpleDateFormat(properties.get(PROP_PATTERN), locale);
    }

    public Object getSampleValue()
    {
        return new Date();
    }

    public String formatValue(Object value)
    {
        return dateFormat.format(value);
    }

    public Object parseValue(String value) throws ParseException
    {
        return dateFormat.parse(value);
    }
}
