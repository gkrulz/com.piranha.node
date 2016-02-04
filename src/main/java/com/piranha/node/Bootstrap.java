package com.piranha.node;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Padmaka on 1/27/16.
 */
public class Bootstrap {
    private static final Logger log = Logger.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 9005);
//            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            InputStreamReader in = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);

            StringBuilder portInfoString  = new StringBuilder();
            int data = in.read();
            while(data != -1) {
                portInfoString.append((char) data);
                data = in.read();
            }
            log.debug(portInfoString);
//            int portNo = Integer.parseInt(in.re);
//            log.debug(portNo);
//
//            socket = new Socket("127.0.0.1", portNo);
//
//            NodeCommLine nodeCommLine = new NodeCommLine();
//            nodeCommLine.setSocket(socket);
//
//            log.debug("TCP comm line created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
