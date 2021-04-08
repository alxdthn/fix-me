package com.nalexand.fx_utils;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {

    public static List<String> listOfNotNull(String... items) {
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }
}
