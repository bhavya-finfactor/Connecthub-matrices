package com.ftpl.finfactor.reporting.utility;

import java.time.LocalDate;

public class DateUtil {

    public static LocalDate[] getCurrentQuarterDates() {
        LocalDate now = LocalDate.now();
        int[] quarterStartMonths = {1, 4, 7, 10};
        int currentQuarter = (now.getMonthValue() - 1) / 3;

        LocalDate startDate = LocalDate.of(now.getYear(), quarterStartMonths[currentQuarter], 1);

        // If we are in the first month of a new quarter, use the entire previous quarter
        LocalDate endDate = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
        if (now.getMonthValue() == quarterStartMonths[currentQuarter]) {
            startDate = startDate.minusMonths(3); // Start of the previous quarter
        }

        return new LocalDate[]{startDate, endDate};
    }

    public static LocalDate getStartDate() {
        return getCurrentQuarterDates()[0];
    }

    public static LocalDate getEndDate() {
        return getCurrentQuarterDates()[1];
    }
}
