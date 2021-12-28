package net.techcable.minitoml;

import net.techcable.minitoml.errors.TomlUnexpectedDateException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.time.temporal.Temporal;
import java.util.Objects;

/**
 * Represents TOMl date/time types,
 * with conversion to the appropriate `java.time` types.
 *
 * To be clear, this uses the new `java.time` api instead of `java.util.Date`
 */
public final class TomlDate extends TomlPrimitive {
    private final ZoneOffset timezoneOffset;
    @Nullable
    private final LocalDate localDate;
    @Nullable
    private final LocalTime localTime;
    /* package */ TomlDate(Temporal rawObject, TomlLocation location) {
        super(rawObject, location);
        // I don't care enough to do better than this....
        if (rawObject instanceof LocalDate) {
            localDate = (LocalDate) rawObject;
            localTime = null;
            timezoneOffset = null;
        } else if (rawObject instanceof LocalDateTime) {
            localDate = ((LocalDateTime) rawObject).toLocalDate();
            localTime = ((LocalDateTime) rawObject).toLocalTime();
            timezoneOffset = null;
        } else if (rawObject instanceof LocalTime) {
            localDate = null;
            localTime = (LocalTime) rawObject;
            timezoneOffset = null;
        } else if (rawObject instanceof OffsetTime) {
            localDate = null;
            localTime = ((OffsetTime) rawObject).toLocalTime();
            timezoneOffset = ((OffsetTime) rawObject).getOffset();
        } else if (rawObject instanceof OffsetDateTime) {
            localDate = ((OffsetDateTime) rawObject).toLocalDate();
            localTime = ((OffsetDateTime) rawObject).toLocalTime();
            timezoneOffset = ((OffsetDateTime) rawObject).getOffset();
        } else {
            throw new IllegalArgumentException("Unsupported type: " + rawObject.getClass().getName());
        }
        if (!isDate() && !isTime()) throw new AssertionError();
    }

    /**
     * Determine whether this value has date information.
     *
     * This is false if it is only has time information.
     *
     * @return if it has time information
     */
    public boolean isDate() {
        return localDate != null;
    }

    /**
     * Determine whether this value has time information.
     *
     * This is false if it only has date information.
     *
     * @return if this value has time information.
     */
    public boolean isTime() {
        return localTime != null;
    }

    /**
     * Determine whether this object has both date and time information.
     *
     * @return if this is a date-time
     */
    public boolean isDateTime() {
        return isDate() && isTime();
    }

    /**
     * Determines whether this date has an explicit timezone offset.
     *
     * @return if this date has an explicit timezone offset
     */
    public boolean hasTimzoneOffset() {
        return timezoneOffset != null;
    }

    /**
     * Get the timezone information associated with this type.
     *
     * Unlike the other methods in this type, this returns `null` if the timezone is not specified.
     *
     * Generally, your application should fail gracefully if a configuration is missing timezone information.
     *
     * @return the explicitly specified timezone, or null if none was specified
     */
    @Nullable
    public ZoneOffset getTimzeoneOffset() {
        return timezoneOffset;
    }

    /**
     * Determine whether this date is implicitly bound to the local time.
     *
     * This is false if it has a timezone offset
     *
     * @return if this time is a local time
     */
    public boolean isLocalTime() {
        return timezoneOffset == null;
    }

    /**
     * Get the local time information associated with this value.
     *
     * Completely discards any timezone information associated with this object.
     *
     * @throws TomlUnexpectedDateException if this value has no time information
     * @return local time
     */
    @NotNull
    public LocalTime getLocalTime() {
        if (localTime == null) throw new TomlUnexpectedDateException(this, "missing time information", getLocation());
        return localTime;
    }

    /**
     * Get the local date information associated with this value.
     *
     * Completely discards any timezone information associated with this object.
     *
     * @throws TomlUnexpectedDateException if this value has no time information
     * @return the local date information
     */
    @NotNull
    public LocalDate getLocalDate() {
        if (localDate == null) throw new TomlUnexpectedDateException(this, "missing date information", getLocation());
        return localDate;
    }

    /**
     * Convert this value into a `LocalDateTime`, throwing an appropriate error if it's missing either.
     *
     * This completely discards any timezone information. Use {@link #resolveDateTime()} if you want to take advantage of that.
     *
     * @throws TomlUnexpectedDateException if this value is missing either date or time information
     * @return this value as a local date time
     */
    public LocalDateTime asLocalDateTime() {
        return LocalDateTime.of(getLocalDate(), getLocalTime());
    }

    /**
     * Resolve this value into a specific date time,
     * using the system's default timezone if none is specified.
     *
     * This should be preferred over {@link #asLocalDateTime()} because it allows
     * the user to specify an explicit timezone.
     *
     * @throws TomlUnexpectedDateException if either date or time information is missing
     * @return a datetime, resolved into a specific timzeone
     */
    public OffsetDateTime resolveDateTime() {
        LocalDateTime local = asLocalDateTime();
        return OffsetDateTime.of(
                local,
                timezoneOffset != null ? timezoneOffset : ZoneId.systemDefault().getRules().getOffset(local)
        );
    }

    /**
     * Resolve this value into a specific time,
     * using the system's default timezone if none is specified.
     *
     * This should generally be preferred over {@link #getLocalTime()} because it allows
     * the user to specify an explicit timezone.
     *
     * The reason this can't just use the "system offset" is
     * because of Daylight Savings Time. Depending on the specific date,
     * clocks may be set forwards or backwards differently.
     *
     *
     * @param  fallbackOffset the fallback offset to use if none is specified
     * @throws TomlUnexpectedDateException if either date or time information is missing
     * @return a time, resolved to a specific timezone
     */
    public OffsetTime resolveTime(ZoneOffset fallbackOffset) {
        Objects.requireNonNull(fallbackOffset, "Must specify fallback");
        LocalTime local = getLocalTime();
        return OffsetTime.of(
                local,
                Objects.requireNonNullElse(this.timezoneOffset, fallbackOffset)
        );
    }

    /**
     * Get this value as a "temporal" object, the base interface for the JDK time API.
     *
     * This can be used to convert to any type you want.
     *
     * @return this value as a temporal object.
     */
    public Temporal asTemporal() {
        return (Temporal) this.value;
    }

    @Override
    protected void toJson(StringBuilder builder) {
        EscapeUtil.writeJsonString(this.toString(), builder);
    }

    @Override
    public String toString() {
        // as far as I can tell, this round-trips all the supported date/time types
        return this.value.toString();
    }
}
