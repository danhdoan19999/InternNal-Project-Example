package com.nals.rw360.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MonetaryHelper {

    private MonetaryHelper() {
    }

    public static BigDecimal moneyRounding(final BigDecimal amount) {
        return moneyRounding(amount, 0, RoundingMode.HALF_UP);
    }

    public static BigDecimal moneyRounding(final BigDecimal amount, final RoundingMode roundingMode) {
        return moneyRounding(amount, 0, roundingMode);
    }

    public static BigDecimal moneyRounding(final BigDecimal amount, final int scale, final RoundingMode roundingMode) {
        return amount.setScale(scale, roundingMode);
    }
}
