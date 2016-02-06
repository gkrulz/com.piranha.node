package com.piranha.node;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Padmaka on 2/6/16.
 */
public class Test01 {
    private static final Logger log = Logger.getLogger(Test01.class);

    public static void main(String[] args) {
        try {
            InetAddress ipAddress=InetAddress.getLocalHost();
            log.info(ipAddress.getHostAddress());
        } catch (UnknownHostException e) {
            log.error("Error", e);
        }
    }
}
