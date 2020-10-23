package com.shediz.post.model;

import java.util.Calendar;

public class MyDate
{
    int year;

    int month;

    int day;

    public MyDate()
    {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)  + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public MyDate(String date)
    {
        if (date.length() == 10)
        {
            year = Integer.parseInt(date.substring(0, 4));
            month = Integer.parseInt(date.substring(5, 7));
            day = Integer.parseInt(date.substring(8, 10));
        }
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year = year;
    }

    public int getMonth()
    {
        return month;
    }

    public void setMonth(int month)
    {
        this.month = month;
    }

    public int getDay()
    {
        return day;
    }

    public void setDay(int day)
    {
        this.day = day;
    }

    @Override
    public String toString()
    {
        String monthStr = String.valueOf(month);
        if (monthStr.length() == 1)
            monthStr = "0" + monthStr;

        String dayStr = String.valueOf(day);
        if (dayStr.length() == 1)
            dayStr = "0" + dayStr;

        return year + "-" + monthStr + "-" + dayStr;
    }
}
