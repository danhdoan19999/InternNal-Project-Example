package com.nals.rw360.helpers;

import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.YEARS;

public final class DateHelper {

    public static final String DATE_TIME_HYPHEN_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_SLASH_PATTERN = "dd/MM/yyyy";
    public static final String FULL_DATE_TIME_SLASH_PATTERN = "dd/MM/yyyy HH:mm:ss";
    public static final String FULL_DATE_TIME_PATTERN = "ddMMyyyyHHmmssSSS";
    public static final Instant MIN_INSTANT = Instant.ofEpochMilli(946659600000L);  //01/01/2000 00:00:00
    public static final Instant MAX_INSTANT = Instant.ofEpochMilli(2147446800000L); //19/01/2038 00:00:00
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");
    public static final ZoneId ASIA_HCM_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.ofHours(7);
    private static final String MIN_TIMESTAMP_SQL = "01/01/1970 00:00:01";
    private static final String MAX_TIMESTAMP_SQL = "19/01/2038 03:14:07";
    private static final int START_YEAR = 2016;
    private static long minTimestamp = 0L;
    private static long maxTimestamp = 0L;

    static {
        try {
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            minTimestamp = sdf.parse(MIN_TIMESTAMP_SQL).getTime();
            maxTimestamp = sdf.parse(MAX_TIMESTAMP_SQL).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private DateHelper() {
    }

    /**
     * To millis long.
     *
     * @param dateTime the date time
     * @return the long
     */
    public static Long toMillis(final Instant dateTime) {
        return Objects.nonNull(dateTime) ? dateTime.toEpochMilli() : null;
    }

    /**
     * Valid timestamp boolean.
     *
     * @param timestamp the timestamp
     * @return the boolean
     */
    public static boolean validTimestamp(final Long timestamp) {
        return timestamp != null && timestamp >= minTimestamp && timestamp <= maxTimestamp;
    }

    /**
     * Format date from Long to date time with time at start of day.
     *
     * @param dateTime the date time
     * @return the instant
     */
    public static Instant toInstantWithTimeAtStartOfDay(final Long dateTime) {
        if (Objects.isNull(dateTime)) {
            return null;
        }

        return DateHelper.toInstant(dateTime).truncatedTo(DAYS);
    }

    public static Instant truncatedToDay(final Long millis) {
        Instant instant = toInstant(millis);
        if (Objects.isNull(instant)) {
            return null;
        }
        return instant.truncatedTo(DAYS);
    }

    public static Instant truncatedToDay(final Instant instant) {
        if (Objects.isNull(instant)) {
            return null;
        }

        return instant.truncatedTo(DAYS);
    }

    public static Instant truncatedNowToDay() {
        return Instant.now().truncatedTo(DAYS);
    }

    public static Instant truncatedToSecond(final Instant instant) {
        if (Objects.isNull(instant)) {
            return null;
        }

        return instant.truncatedTo(SECONDS);
    }

    public static LocalDateTime toLocalDateTimeTruncateToDay(final Long millis) {
        Instant instant = toInstant(millis);
        if (Objects.isNull(instant)) {
            return null;
        }

        return LocalDateTime.from(instant.atZone(DEFAULT_ZONE_ID)).truncatedTo(DAYS);
    }

    public static Long toLongWithTimeAtStartOfDay(final Instant instant) {
        if (Objects.isNull(instant)) {
            return null;
        }

        return toMillis(instant.truncatedTo(DAYS));
    }

    /**
     * To date with day from long at start of year of project.
     *
     * @param dateTime the date time
     * @return the instant
     */
    public static Instant toInstantWithDayAtStartOfYear(final Long dateTime) {
        return LocalDateTime.ofInstant(Objects.nonNull(dateTime) ? Instant.ofEpochMilli(dateTime) : Instant.now(),
                                       DEFAULT_ZONE_ID)
                            .withYear(START_YEAR).withDayOfYear(1).toInstant(DEFAULT_ZONE_OFFSET);
    }

    /**
     * Gets start time of current year.
     *
     * @return the start time of current year
     */
    public static Instant getStartTimeOfCurrentYear() {
        return LocalDateTime.now().withDayOfYear(1).toInstant(DEFAULT_ZONE_OFFSET);
    }

    /**
     * Gets end time of current year.
     *
     * @return the end time of current year
     */
    public static Instant getEndTimeOfCurrentYear() {
        return getStartTimeOfCurrentYear().plus(1, YEARS).minusMillis(1);
    }

    public static Instant toInstant(final Date date) {
        if (Objects.isNull(date)) {
            return null;
        }

        return date.toInstant();
    }

    /**
     * To millis long.
     *
     * @param dateTime the date time
     * @return the long
     */
    public static Instant toInstant(final String dateTime) {
        if (!StringUtils.hasText(dateTime)) {
            return null;
        }
        try {
            Long millis = Long.parseLong(dateTime);
            if (validTimestamp(millis)) {
                return Instant.ofEpochMilli(millis);
            } else {
                return null;
            }
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * To date with time at end of day date time.
     *
     * @param dateTime the date time
     * @return the date time
     */
    public static Instant toInstantWithTimeAtEndOfDay(final Long dateTime) {
        return toInstantWithTimeAtStartOfDay(dateTime).plus(1, DAYS).minus(1, MILLIS);
    }

    /**
     * Get value of instant by chrono field.
     *
     * @param dateTime    the date time
     * @param chronoField the chrono field
     * @return the from instant
     */
    public static int getFieldFromInstant(final Instant dateTime, final ChronoField chronoField) {
        return LocalDateTime.ofInstant(dateTime != null ? dateTime : Instant.now(), DEFAULT_ZONE_ID).get(chronoField);
    }

    /**
     * Format date from current date to date time with time at start of day.
     *
     * @return the instant
     */
    public static Instant getCurrentDateTimeAtStartOfDay(final ZoneId zoneId) {
        LocalDate currentDate = LocalDate.now(zoneId);
        return currentDate.atStartOfDay(zoneId).toInstant();
    }

    /**
     * To date with time at end of current date.
     *
     * @return the instant
     */
    public static Instant getCurrentDateTimeAtEndOfDay(final ZoneId zoneId) {
        return getCurrentDateTimeAtStartOfDay(zoneId).plus(1, DAYS).minus(1, MILLIS);
    }

    public static LocalDateTime toLocalDateTime(final Instant instant) {
        if (Objects.isNull(instant)) {
            return null;
        }

        return LocalDateTime.from(instant.atZone(DEFAULT_ZONE_ID));
    }

    public static Instant toInstantWithoutSeconds(final Long timestamp) {
        return toInstantWithoutSeconds(toInstant(timestamp), DEFAULT_ZONE_ID);
    }

    public static Instant toInstantWithoutSeconds(final Long timestamp, final ZoneId zoneId) {
        return toInstantWithoutSeconds(toInstant(timestamp), zoneId);
    }

    public static Instant toInstantWithoutSeconds(final Instant instant, final ZoneId zoneId) {
        if (Objects.isNull(instant)) {
            return null;
        }

        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
        return zdt.withSecond(0).withNano(0).toInstant();
    }

    public static Instant toInstantWithoutHours(final Long timestamp) {
        return toInstantWithoutHours(toInstant(timestamp), DEFAULT_ZONE_ID);
    }

    public static Instant toInstantWithoutHours(final Long timestamp, final ZoneId zoneId) {
        return toInstantWithoutHours(toInstant(timestamp), zoneId);
    }

    public static Instant toInstantWithoutHours(final Instant instant, final ZoneId zoneId) {
        if (Objects.isNull(instant)) {
            return null;
        }

        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
        return zdt.withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant();
    }

    public static String toStringWithTimeZone(final Instant instant, final ZoneId zoneId, final String pattern) {
        if (instant == null) {
            return null;
        }

        return DateTimeFormatter.ofPattern(pattern).withZone(zoneId).format(instant);
    }

    public static String toStringWithDefaultTimeZone(final Instant instant) {
        return toStringWithTimeZone(instant, DEFAULT_ZONE_ID, DATE_TIME_SLASH_PATTERN);
    }

    public static String toStringWithTimeZone(final Instant instant, final String zoneId) {
        return toStringWithTimeZone(instant, ZoneId.of(zoneId), DATE_TIME_SLASH_PATTERN);
    }

    public static String toStringWithTimeZone(final Instant instant, final ZoneId zoneId) {
        return toStringWithTimeZone(instant, zoneId, DATE_TIME_SLASH_PATTERN);
    }

    public static String getCurrentTimeString(final ZoneId zoneId) {
        return toStringWithTimeZone(Instant.now(), zoneId, FULL_DATE_TIME_PATTERN);
    }

    public static String getCurrentTimeString(final ZoneId zoneId, final String pattern) {
        return toStringWithTimeZone(Instant.now(), zoneId, pattern);
    }

    public static Instant toInstant(final Long millis) {
        return DateHelper.validTimestamp(millis) ? Instant.ofEpochMilli(millis) : null;
    }

    public static Instant plusMonths(final Instant instant, final long amountToAdd) {
        if (Objects.isNull(instant)) {
            return null;
        }

        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
        return zdt.plusMonths(amountToAdd).toInstant();
    }

    public static Instant toInstant(final LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }

        return localDateTime.atZone(ZoneOffset.ofHours(0)).toInstant();
    }
}
