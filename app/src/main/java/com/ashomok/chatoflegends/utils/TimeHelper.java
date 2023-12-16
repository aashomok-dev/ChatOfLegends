package com.ashomok.chatoflegends.utils;

import android.content.Context;

import com.ashomok.chatoflegends.R;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Devlomi on 24/02/2018.
 */

public enum TimeHelper {
    ;

    //this will return only the time of message with am or pm
    public static String getMessageTime(String timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        Date date = new Date(Long.parseLong(timestamp));
        return format.format(date);
    }


    //get chat time
    public static String getChatTime(long timestamp) {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);


        Date timestampDate = new Date();
        timestampDate.setTime(timestamp);
        long now = System.currentTimeMillis();

        //the last message was sent today return today
        if (TimeHelper.isSameDay(now, timestamp)) {
            return MyApp.context().getResources().getString(R.string.today).toUpperCase();
            //the last message was sent yesterday return yesterday
        } else if (TimeHelper.isYesterday(now, timestamp)) {
            return MyApp.context().getResources().getString(R.string.yesterday).toUpperCase();
        } else {
            //otherwise show the date of last message
            return fullDateFormat.format(timestampDate);
        }
    }

    //get chat time
    public static String getChatTimeShorted(Context context, long timestamp) {
        Locale current = context.getResources().getConfiguration().getLocales().get(0);
        SimpleDateFormat weekDayFormat = new SimpleDateFormat("EEE", current);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", current);
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("MM/dd", current);

        Date timestampDate = new Date();
        timestampDate.setTime(timestamp);
        long now = System.currentTimeMillis();
        //the last message was sent today return today
        if (TimeHelper.isSameDay(now, timestamp)) {
            return timeFormat.format(timestampDate);
            //the last message was sent yesterday return yesterday
        } else if (TimeHelper.isSameWeek(now, timestamp)) {
            return weekDayFormat.format(timestampDate);
        } else {
            //otherwise show the date of last message
            return fullDateFormat.format(timestampDate);
        }
    }

    //check if it's same day for the header date
    // if it's same day we will not show a new header
    public static boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(timestamp1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(timestamp2);
        boolean sameYear = TimeHelper.isSameYear(calendar1, calendar2);
        boolean sameMonth = TimeHelper.isSameMonth(calendar1, calendar2);
        boolean sameDay = TimeHelper.isSameDay(calendar1, calendar2);
        return (sameDay && sameMonth && sameYear);
    }
    public static boolean isSameWeek(long timestamp1, long timestamp2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(timestamp1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(timestamp2);
        return inSameCalendarWeek(calendar1, calendar2);
    }


    private static boolean isSameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean isSameMonth(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }

    private static boolean inSameCalendarWeek(Calendar firstCalendar, Calendar secondCalendar) {
        // create LocalDates from Instants created from the given Calendars
        LocalDate firstDate = LocalDate.from(firstCalendar.toInstant());
        LocalDate secondDate = LocalDate.from(secondCalendar.toInstant());
        // get a reference to the system of calendar weeks in your defaul locale
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        // find out the calendar week for each of the dates
        int firstDatesCalendarWeek = firstDate.get(weekFields.weekOfWeekBasedYear());
        int secondDatesCalendarWeek = secondDate.get(weekFields.weekOfWeekBasedYear());
        /*
         * find out the week based year, too,
         * two dates might be both in a calendar week number 1 for example,
         * but in different years
         */
        int firstWeekBasedYear = firstDate.get(weekFields.weekBasedYear());
        int secondWeekBasedYear = secondDate.get(weekFields.weekBasedYear());
        // return if they are equal or not
        return firstDatesCalendarWeek == secondDatesCalendarWeek
                && firstWeekBasedYear == secondWeekBasedYear;
    }

    private static boolean isSameYear(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }

    /*
    NOTE:timestamp1 should be greater that timestamp2 in order to give a correct result
     */
    private static boolean isYesterday(long timestamp1, long timestamp2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(timestamp1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(timestamp2);
        boolean isYesterday = calendar1.get(Calendar.DAY_OF_MONTH) - 1 == calendar2.get(Calendar.DAY_OF_MONTH);
        return TimeHelper.isSameYear(calendar1, calendar2) && TimeHelper.isSameMonth(calendar1, calendar2) && isYesterday;
    }
}
