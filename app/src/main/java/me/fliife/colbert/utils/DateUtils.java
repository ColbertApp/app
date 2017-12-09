package me.fliife.colbert.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static boolean isPast(String date) {
        return isPast(date, "19h00");
    }

    public static boolean isPast(String date, String time) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = toCalendar(simpleDateFormat.parse(date));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split("h")[0]) + 1);
            calendar.set(Calendar.MINUTE, Integer.parseInt(time.split("h")[1]));
            return calendar.before(toCalendar(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static boolean areSameDay(String date1, String date2) {
        // return date1.equals(date2);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return simpleDateFormat.parse(date1).equals(simpleDateFormat.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static String getDay(String date) {
        SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.FRANCE);
        try {
            String result = simpleDateFormat.format(dateParser.parse(date));
            return result.substring(0, 1).toUpperCase() + result.substring(1);
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}
