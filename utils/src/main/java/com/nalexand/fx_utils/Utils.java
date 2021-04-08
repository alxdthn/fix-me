package com.nalexand.fx_utils;

import java.util.ArrayList;
import java.util.List;

class Utils {

    public static List<String> listOfNotNull(String... items) {
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    public static String field(int key, String value) {
        if (value == null) return null;
        return String.format("%d=%s", key, value);
    }
}
