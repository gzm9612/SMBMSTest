package cn.smbms.tools;

import org.springframework.core.convert.converter.Converter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringTODateCOnverter implements Converter<String, Date> {
    private String datePattern;

    public StringTODateCOnverter(String datePattern) {
        this.datePattern = datePattern;
    }

    @Override
    public Date convert(String s) {
        Date date = null;
        try{
            date = new SimpleDateFormat(datePattern).parse(s);

        }catch (Exception e){
            e.printStackTrace();
        }
        return date;
    }
}
