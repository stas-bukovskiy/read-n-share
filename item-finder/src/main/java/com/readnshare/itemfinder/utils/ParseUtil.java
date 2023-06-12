package com.readnshare.itemfinder.utils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;

public final class ParseUtil {

    private static final String SPLITTER = ", ";

    private ParseUtil() {
    }

    public static Integer parseInteger(String s) {
        if (!hasText(s))
            return null;
        return Integer.valueOf(s);
    }

    public static Double parseDouble(String s) {
        if (!hasText(s))
            return null;
        return Double.valueOf(s);
    }

    public static Set<String> parseStringSet(String s) {
        Set<String> res = new LinkedHashSet<>();
        if (!hasText(s))
            return res;
        res.addAll(List.of(s.split(SPLITTER)));
        return res;
    }


}
