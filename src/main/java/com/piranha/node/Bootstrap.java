package com.piranha.node;

import com.piranha.node.comm.CompilationListener;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import com.piranha.node.util.Utils;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.sun.xml.internal.bind.v2.runtime.output.SAXOutput;
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
     * @param args no arguments
     */
    public static void main(String[] args) {
        Utils.deleteDirectory(new File("/Users/bhanukayd/Desktop/Piranha/com.piranha.node/destination"));

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
//        try {
//            properties.load(Bootstrap.class.getClassLoader().getResourceAsStream("config.properties"));
//        } catch (IOException e) {
//            log.error("Unable to load the property file 'config.properties'", e);
//        }

        try {
            Socket socket = new Socket("192.168.0.180", 9005);
            log.info("Connected to master node on - " + socket.getInetAddress().getHostAddress());

            // Initializing and stating the listener for compilation.
            CompilationListener compilationListener = new CompilationListener();
            compilationListener.start();

        } catch (IOException e) {
            log.error("Unable to connect to master node", e);
        }
    }
}
