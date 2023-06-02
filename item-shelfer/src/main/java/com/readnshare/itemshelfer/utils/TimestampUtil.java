package com.readnshare.itemshelfer.utils;

import java.sql.Timestamp;

public final class TimestampUtil {

    private TimestampUtil() {
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(getCurrentTimeMillis());
    }

}
