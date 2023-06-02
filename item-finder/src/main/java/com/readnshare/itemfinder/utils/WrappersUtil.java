package com.readnshare.itemfinder.utils;

public final class WrappersUtil {

    private WrappersUtil() {
    }

    public static int getOrDefaultValue(Integer integer) {
        return integer != null ? integer : 0;
    }

    public static double getOrDefaultValue(Double d) {
        return d != null ? d : 0;
    }

    public static String getOrDefaultValue(String s) {
        return s != null ? s : "";
    }


}
