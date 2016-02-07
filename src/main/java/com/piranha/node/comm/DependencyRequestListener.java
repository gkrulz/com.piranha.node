package com.piranha.node.comm;

import com.piranha.node.compile.DependencyProvider;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Padmaka on 2/7/16.
 */
public class DependencyRequestListener extends Thread{
    private static final Logger log = Logger.getLogger(DependencyRequestListener.class);
    private Communication comm;

    public DependencyRequestListener() {
        comm = new Communication();
    }

    public void run() {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(10500);
        } catch (IOException e) {
            log.error("Error", e);
        }

        while (true) {

            try {
                Socket socket = serverSocket.accept();

                DependencyProvider dependencyProvider = new DependencyProvider(socket);
                dependencyProvider.start();
            } catch (IOException e) {
                log.error("Error", e);
            }
        }
    }
}
