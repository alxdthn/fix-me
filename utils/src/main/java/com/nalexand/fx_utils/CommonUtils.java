package com.nalexand.fx_utils;

import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {

    public static List<String> listOfNotNull(@Nullable String... items) {
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    public static List<String> listOf(String... items) {
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }
}
