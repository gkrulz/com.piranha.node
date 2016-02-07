package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.node.util.Communication;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Padmaka on 2/6/16.
 */
public class DependencyProvider extends Thread {
    private static final Logger log = Logger.getLogger(DependencyProvider.class);
    private Communication comm;
    private HashMap<String, String> dependencyMap;
    private Properties properties;
    private Socket socket;

    public DependencyProvider(Socket socket) throws IOException {
        this.comm = new Communication();
        this.properties = new Properties();
        this.properties.load(DependencyProvider.class.getClassLoader().getResourceAsStream("config.properties"));
        this.socket = socket;
    }

    @Override
    public void run() {
        JsonParser parser = new JsonParser();

        try {
            String requestString = comm.readFromSocket(socket);
            JsonObject requestJson = parser.parse(requestString).getAsJsonObject();

            String path = properties.getProperty("DESTINATION_PATH") + "/";
            String packagePath = requestJson.get("dependency").getAsString();
            packagePath = packagePath.replace(".", "/") + ".class";

            File file = new File(path + packagePath);

            if (requestJson.get("op").getAsString().equals("DEPENDENCY_REQUEST") && file.exists()) {

                this.sendDependency(file, socket);
            }
            socket.close();
            log.debug("successfully sent dependency class file");
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

    public void sendDependency(File classFile, Socket socket) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(classFile);

        IOUtils.copy(fileInputStream, socket.getOutputStream());
    }

    public void setDependencyMap(HashMap<String, String> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }
}
