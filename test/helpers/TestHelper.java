package com.nals.rw360.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Utility class for testing REST controllers.
 */
public final class TestHelper {

    private static final ObjectMapper MAPPER = createObjectMapper();

    /**
     * MediaType for JSON UTF8
     */
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
        APPLICATION_JSON.getType(),
        APPLICATION_JSON.getSubtype(), UTF_8);

    private TestHelper() {
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert
     * @return the JSON byte array
     * @throws IOException
     */
    public static byte[] convertObjectToJsonBytes(final Object object)
        throws IOException {
        return MAPPER.writeValueAsBytes(object);
    }

    /**
     * Create a byte array with a specific size filled with specified data.
     *
     * @param size the size of the byte array
     * @param data the data to put in the byte array
     * @return the JSON byte array
     */
    public static byte[] createByteArray(final int size, final String data) {
        byte[] byteArray = new byte[size];
        for (int i = 0; i < size; i++) {
            byteArray[i] = Byte.parseByte(data, 2);
        }
        return byteArray;
    }

    /**
     * A matcher that tests that the examined string represents the same instant as the reference datetime.
     */
    public static class ZonedDateTimeMatcher
        extends TypeSafeDiagnosingMatcher<String> {

        private final ZonedDateTime date;

        public ZonedDateTimeMatcher(final ZonedDateTime date) {
            this.date = date;
        }

        @Override
        protected boolean matchesSafely(final String item, final Description mismatchDescription) {
            try {
                if (!date.isEqual(ZonedDateTime.parse(item))) {
                    mismatchDescription.appendText("was ").appendValue(item);
                    return false;
                }
                return true;
            } catch (DateTimeParseException e) {
                mismatchDescription.appendText("was ").appendValue(item)
                                   .appendText(", which could not be parsed as a ZonedDateTime");
                return false;
            }
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("a String representing the same Instant as ").appendValue(date);
        }
    }

    /**
     * Creates a matcher that matches when the examined string reprensents the same instant as the reference datetime
     *
     * @param date the reference datetime against which the examined string is checked
     */
    public static ZonedDateTimeMatcher sameInstant(final ZonedDateTime date) {
        return new ZonedDateTimeMatcher(date);
    }

    /**
     * Verifies the equals/hashcode contract on the domain object.
     */
    public static <T> void equalsVerifier(final Class<T> clazz)
        throws Exception {
        T domainObject1 = clazz.getConstructor().newInstance();
        Assertions.assertThat(domainObject1.toString()).isNotNull();
        Assertions.assertThat(domainObject1).isEqualTo(domainObject1);
        Assertions.assertThat(domainObject1.hashCode()).isEqualTo(domainObject1.hashCode());
        // Test with an instance of another class
        Object testOtherObject = new Object();
        Assertions.assertThat(domainObject1).isNotEqualTo(testOtherObject);
        Assertions.assertThat(domainObject1).isNotEqualTo(null);
        // Test with an instance of the same class
        T domainObject2 = clazz.getConstructor().newInstance();
        Assertions.assertThat(domainObject1).isNotEqualTo(domainObject2);
        // HashCodes are equals because the objects are not persisted yet
        Assertions.assertThat(domainObject1.hashCode()).isEqualTo(domainObject2.hashCode());
    }

    /**
     * Create a FormattingConversionService which use ISO date format, instead of the localized one.
     *
     * @return the FormattingConversionService
     */
    public static FormattingConversionService createFormattingConversionService() {
        DefaultFormattingConversionService dfcs = new DefaultFormattingConversionService();
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(dfcs);
        return dfcs;
    }
}
