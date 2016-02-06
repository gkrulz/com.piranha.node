package com.piranha.node;

import com.piranha.node.compile.CompilationListener;
import com.piranha.node.compile.DependencyProvider;
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
            InputStreamReader in = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);

            StringBuilder portInfoString  = new StringBuilder();
            int data = in.read();
            while(data != -1) {
                portInfoString.append((char) data);
                data = in.read();
            }
            log.debug(portInfoString);

            CompilationListener compilationListener = new CompilationListener();
            compilationListener.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
