package com.piranha.node.comm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.node.util.Communication;
import com.piranha.node.util.FileWriter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 2/7/16.
 */
public class DependencyResponseListener extends Thread{
    private static final Logger log = Logger.getLogger(DependencyResponseListener.class);
    private Communication comm;
    private HashSet<String> dependencies;
    private static ConcurrentHashMap<String, FileWriter> fileWriters = new ConcurrentHashMap<>();

    public DependencyResponseListener() {
        comm = new Communication();
        this.dependencies = new HashSet<>();
    }

    /***
     * The overridden run method of Thread class
     */
    @Override
    public void run() {
        ServerSocket serverSocket = null;
        JsonParser parser = new JsonParser();

        try {
            serverSocket = new ServerSocket(10501);
        } catch (IOException e) {
            log.error("Error", e);
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();

                JsonObject responseJson = parser.parse(comm.readFromSocket(socket)).getAsJsonObject();

                String testString = responseJson.get("className").getAsString();
                testString = testString.replace("/", ".");
                testString = testString.replace("\\", ".");
                testString = testString.substring(1);
                testString = testString.replace(".class", "");
//                log.debug(testString);

                if (fileWriters.get(testString) == null) {
                    FileWriter fileWriter = new FileWriter(responseJson, testString);
                    fileWriters.put(testString, fileWriter);
                    fileWriter.start();
                }

            } catch (IOException | ClassNotFoundException e) {
                log.error("Error", e);
            }
        }
    }

    /***
     * The method to add dependencies
     * @param dependencies list of dependencies without duplicates.
     */
    public void addDependencies(HashSet<String> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    /***
     * The method to get a hashmap of file writer threads
     * @return hashmap of file writer threads
     */
    public static ConcurrentHashMap<String, FileWriter> getFileWriters() {
        return fileWriters;
    }
}
