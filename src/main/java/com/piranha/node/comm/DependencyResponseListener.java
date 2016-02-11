package com.piranha.node.comm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import com.piranha.node.util.FileWriter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Padmaka on 2/7/16.
 */
public class DependencyResponseListener extends Thread{
    private static final Logger log = Logger.getLogger(DependencyResponseListener.class);
    private Communication comm;
    private int noOfIterations;
    private HashSet<String> dependencies;
    private HashMap<String, Thread> fileWriters;

    public DependencyResponseListener() {
        comm = new Communication();
        this.dependencies = new HashSet<>();
    }

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

                if (dependencies.contains(testString) && !(fileWriters.keySet().contains(testString))) {
                    FileWriter fileWriter = new FileWriter(responseJson, testString);
                    fileWriters.put(testString, fileWriter);
                }

            } catch (IOException | ClassNotFoundException e) {
                log.error("Error", e);
            }
        }
    }

    public void addDependencies(ArrayList<String> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    public Thread getFileWriter(String className) {
        return fileWriters.get(className);
    }
}
