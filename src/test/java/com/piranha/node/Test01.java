package com.piranha.node;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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
        test01.getIps();
    }

    public void getIps () {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println(" IP Addr: " + localhost.getHostAddress());
            // Just in case this host has multiple IP addresses....
            InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getCanonicalHostName());
            if (allMyIps != null && allMyIps.length > 1) {
                System.out.println(" Full list of IP addresses:");
                for (int i = 0; i < allMyIps.length; i++) {
                    System.out.println("    " + allMyIps[i]);
                }
            }
        } catch (UnknownHostException e) {
            System.out.println(" (error retrieving server host name)");
        }

        try {
            System.out.println("Full list of Network Interfaces:");
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                System.out.println("    " + intf.getName() + " " + intf.getDisplayName());
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    System.out.println("        " + enumIpAddr.nextElement().toString());
                }
            }
        } catch (SocketException e) {
            System.out.println(" (error retrieving network interface list)");
        }
    }
}
