package com.piranha.node;

import com.piranha.node.comm.CompilationListener;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

/**
 * Created by Padmaka on 1/27/16.
 */
public class Bootstrap {
    private static final Logger log = Logger.getLogger(Bootstrap.class);

    /***
     * Main Method
     * @param args
     */
    public static void main(String[] args) {
        Communication comm = new Communication();
        Properties properties = new Properties();

        //Get local IP address.
        String localIpAddress = null;
        try {
            localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();
        } catch (SocketException e) {
            log.error("Unable to get the local ip address", e);
        }

        log.info("Local IP: " + localIpAddress);

        //loading property file.
        try {
            properties.load(Bootstrap.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            log.error("Unable to load the property file 'config.properties'", e);
        }

        try {

            Socket socket = new Socket(properties.getProperty("MASTER_NODE_IP"), 9005);

            log.info("Connected to master node on - " + socket.getInetAddress().getHostAddress());

            // Initializing and stating the listener for compilation.
            CompilationListener compilationListener = new CompilationListener();
            compilationListener.start();

        } catch (IOException e) {
            log.error("Unable to connect to master node", e);
        }
    }
}
