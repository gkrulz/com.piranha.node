package com.piranha.node;

import com.piranha.node.compile.CompilationListener;
import com.piranha.node.compile.DependencyProvider;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Created by Padmaka on 1/27/16.
 */
public class Bootstrap {
    private static final Logger log = Logger.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        InetAddress localIpAddress= null;
        try {
            localIpAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("Error", e);
        }
        log.info("IP: " + localIpAddress.getHostAddress());

        Communication comm = new Communication();
        Properties properties = new Properties();
        try {
            properties.load(Bootstrap.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            log.error("Error", e);
        }

        try {
            Socket socket = new Socket(properties.getProperty("MASTER_NODE_IP"), 9005);
            log.debug(comm.readFromSocket(socket));

            CompilationListener compilationListener = new CompilationListener();
            compilationListener.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
