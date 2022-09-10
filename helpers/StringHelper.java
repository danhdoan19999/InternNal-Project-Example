package com.nals.rw360.helpers;

import com.google.common.base.Strings;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.regex.Pattern;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.text.Normalizer.Form.NFD;

public final class StringHelper {
    public static final String EMPTY = "";
    public static final String COMMA = ",";
    private static final Pattern REMOVE_ACCENT_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private StringHelper() {
    }

    public static String removeAccent(final String value) {
        if (isBlank(value)) {
            return value;
        }
        return REMOVE_ACCENT_PATTERN.matcher(Normalizer.normalize(value, NFD))
                                    .replaceAll(EMPTY)
                                    .replace('đ', 'd')
                                    .replace('Đ', 'D');
    }

    public static String underscoreToUppercase(final String string) {
        if (!Strings.isNullOrEmpty(string)) {
            String upper = LOWER_UNDERSCORE.to(UPPER_CAMEL, string);
            return upper.isEmpty() ? "" : Character.toLowerCase(upper.charAt(0)) + upper.substring(1);
        }

        return "";
    }

    public static String uppercaseToUnderscore(final String string) {
        if (!Strings.isNullOrEmpty(string)) {
            return UPPER_CAMEL.to(LOWER_UNDERSCORE, string);
        }

        return "";
    }

    public static <T extends Number> T toNumber(final String value, final Class<T> clazz) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return NumberUtils.parseNumber(value, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNotBlank(final String str) {
        return StringUtils.hasText(str);
    }

    public static boolean isBlank(final String str) {
        return !StringUtils.hasText(str);
    }
}
