package com.piranha.node.comm;

import com.piranha.node.compile.Compiler;
import com.piranha.node.compile.DependencyProvider;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 2/7/16.
 */
public class DependencyRequestListener extends Thread{
    private static final Logger log = Logger.getLogger(DependencyRequestListener.class);
    private ConcurrentHashMap<String, String> dependencyMap;
    private Communication comm;
    private HashMap<String, Compiler> compilers;

    public DependencyRequestListener() {
        this.comm = new Communication();
        this.compilers = new HashMap<>();
    }

    public void setDependencyMap(ConcurrentHashMap<String, String> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    /***
     * The overridden run method of Thread class
     */
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
                dependencyProvider.setDependencyMap(this.dependencyMap);
                dependencyProvider.addCompilers(compilers);
                dependencyProvider.start();
            } catch (IOException e) {
                log.error("Error", e);
            }
        }
    }

    /***
     * The method to set the compiler threads
     * @param compilers Hashmap of compiler threads
     */
    public void setCompilers(HashMap<String, Compiler> compilers) {
        this.compilers.putAll(compilers);
    }
}
