package com.piranha.node;


import java.net.*;
import java.util.Enumeration;

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

        System.out.println(path + "\n");

        Test01 test01 = new Test01();
        try {
            System.out.println(test01.getFirstNonLoopbackAddress(true, false).getHostAddress());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }
}
