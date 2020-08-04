package com.xh.publicgoods.util;

import java.util.concurrent.ThreadLocalRandom;

public class GenerateUtil {
    public static String generate() {
        return System.nanoTime()+"-"+ThreadLocalRandom.current().nextInt(9);
    }
}
