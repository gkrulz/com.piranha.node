package com.piranha.node.constants;

import java.io.File;

/**
 * Created by Padmaka on 2/7/16.
 */
public class Constants {
    public static final String DESTINATION_PATH;
    public static final String PATH_SEPARATOR;

    static {
        String pathSeparator = null;

        if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux")) {
            pathSeparator = "/";
        } else if (System.getProperty("os.name").contains("Windows")) {
            pathSeparator = "\\";
        }

        String path = System.getProperty("user.dir") + pathSeparator + "destination";
        File file = new File(path);
        file.mkdir();

        PATH_SEPARATOR = pathSeparator;
        DESTINATION_PATH = path;
    }
}
