package com.piranha.node.comm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.concurrent.Future;

/**
 * Created by Padmaka on 2/7/16.
 */
public class DependencyRequestListener extends Thread{
    private static final Logger log = Logger.getLogger(DependencyRequestListener.class);
    private ConcurrentHashMap<String, String> dependencyMap;
    private Communication comm;
    protected static ConcurrentHashMap<String, Future<?>> compilers = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<String, DependencyProvider> dependencyProviders = new ConcurrentHashMap<>();

    public DependencyRequestListener() {
        this.comm = new Communication();
    }

    public void setDependencyMap(ConcurrentHashMap<String, String> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    /***
     * The overridden run method of Thread class
     */
    public void run() {
        ServerSocket serverSocket = null;
        JsonParser parser = new JsonParser();

        try {
            serverSocket = new ServerSocket(10500);
        } catch (IOException e) {
            log.error("Error", e);
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();

                String requestString = null;
                try {
                    requestString = comm.readFromSocket(socket);
                } catch (IOException e) {
                    log.error("Unable to read from socket", e);
                } catch (ClassNotFoundException e) {
                    log.error("Class not found", e);
                }

                JsonObject requestJson = parser.parse(requestString).getAsJsonObject();

                String dependency = requestJson.get("dependency").getAsString();

                DependencyProvider dependencyProvider = new DependencyProvider(requestJson, socket);
                dependencyProvider.setDependencyMap(this.dependencyMap);
                dependencyProviders.put(dependency, dependencyProvider);
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
    public void setCompilers(ConcurrentHashMap<String, Future<?>> compilers) {
        this.compilers.putAll(compilers);
    }

    public static ConcurrentHashMap<String, DependencyProvider> getDependencyProviders() {
        return dependencyProviders;
    }
}
