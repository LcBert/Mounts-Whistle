package com.lucab.mounts_whistle;

import java.util.ArrayList;

public class Functions {
    public static boolean listContains(Object list, String item) {
        for (Object obj : (ArrayList<?>) list) {
            if (obj instanceof String) {
                if (String.valueOf(obj).equals(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
