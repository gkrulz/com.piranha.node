package com.piranha.node;


import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Padmaka on 2/6/16.
 */
public class Test01 {

    public static void main(String[] args) {
        try {
            InetAddress ipAddress=InetAddress.getLocalHost();
            System.out.println(ipAddress.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println(System.getProperty("os.name"));
        String pathSeparator = null;

        if (System.getProperty("os.name").contains("Mac")) {
            pathSeparator = "/";
        } else if (System.getProperty("os.name").contains("Windows")) {
            pathSeparator = "\\";
        }

        String path = System.getProperty("user.dir") + pathSeparator + "destination";

        System.out.println(path);
    }
}
