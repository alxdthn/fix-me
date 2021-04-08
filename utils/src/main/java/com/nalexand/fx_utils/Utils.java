package com.nalexand.fx_utils;

class Utils {

    public static final int READ_BUFF_SIZE = Short.MAX_VALUE;

    public static String field(int key, String value) {
        if (value == null) return null;
        return String.format("%d=%s", key, value);
    }
}
