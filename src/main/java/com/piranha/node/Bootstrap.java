package com.piranha.node;

import com.piranha.node.compile.CompilationListener;
import com.piranha.node.compile.DependencyProvider;
import com.piranha.node.util.Communication;
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
        Communication comm = new Communication();

        try {
            Socket socket = new Socket("127.0.0.1", 9005);
            log.debug(comm.readFromSocket(socket));

            CompilationListener compilationListener = new CompilationListener();
            compilationListener.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
