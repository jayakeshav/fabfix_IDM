package edu.uci.ics.jkotha.service.idm.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ServiceFormatter extends Formatter {
    private static final int MAX_BUFF =100;
    public String format(LogRecord record){
        StringBuffer buffer = new StringBuffer(MAX_BUFF);
        buffer.append(calcDate(record.getMillis()));
        buffer.append("["+record.getLevel()+"]");
        buffer.append("["+record.getSourceMethodName()+"] ");
        buffer.append(record.getMessage()+"\n");
        return buffer.toString();
    }

    private String calcDate(long ms){
        SimpleDateFormat sdf =
                new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]", Locale.getDefault());
        Date date = new Date(ms);
        return sdf.format(date);
    }
}
