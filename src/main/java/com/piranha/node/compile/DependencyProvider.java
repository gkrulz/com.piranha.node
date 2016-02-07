package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Padmaka on 2/6/16.
 */
public class DependencyProvider extends Thread {
    private static final Logger log = Logger.getLogger(DependencyProvider.class);
    private Communication comm;
    private HashMap<String, String> dependencyMap;
    private Socket socket;

    public DependencyProvider(Socket socket) throws IOException {
        this.comm = new Communication();
        this.socket = socket;
    }

    @Override
    public void run() {
        JsonParser parser = new JsonParser();

        try {
            String requestString = comm.readFromSocket(socket);
            JsonObject requestJson = parser.parse(requestString).getAsJsonObject();

            String path = Constants.DESTINATION_PATH + "/";
            String packagePath = requestJson.get("dependency").getAsString();
            packagePath = packagePath.replace(".", "/") + ".class";

            File file = new File(path + packagePath);

            if (requestJson.get("op").getAsString().equals("DEPENDENCY_REQUEST") && file.exists()) {

                this.sendDependency(file, socket);
                log.debug("successfully sent dependency: " + file.getName());
            }
            socket.close();
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

    public void sendDependency(File classFile, Socket socket) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(classFile);
        JsonParser parser = new JsonParser();

        byte[] bytes = IOUtils.toByteArray(fileInputStream);
        String className = classFile.getAbsolutePath();
        className = className.replace(Constants.DESTINATION_PATH, "");

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("className", className);
        requestJson.addProperty("file", new String(bytes));

        comm.writeToSocket(socket, requestJson);
    }

    public void setDependencyMap(HashMap<String, String> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }
}