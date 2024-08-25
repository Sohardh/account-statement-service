package com.sohardh.account.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

  public static final String YYYY_MM_DD = "yyyy-MM-dd";

  private DateUtil() {
  }

  public static LocalDate parseDate(String dateString, String pattern) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return LocalDate.parse(dateString, formatter);
  }

  public static String getFirstDateOfTheMonth(LocalDate lastDate) {
    // Set the day of the month to 1 to get the first day.
    var firstDay = lastDate.withDayOfMonth(1);

    return firstDay.format(DateTimeFormatter.ofPattern(YYYY_MM_DD));
  }

  public static LocalDate getNextMonth(LocalDate lastDate) {
    return lastDate.plusMonths(1);
  }

  public static String convertToString(LocalDate date, String pattern) {
    return date.format(DateTimeFormatter.ofPattern(pattern));
  }

}
