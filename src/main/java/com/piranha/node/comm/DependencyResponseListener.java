package com.piranha.node.comm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
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

/**
 * Created by Padmaka on 2/7/16.
 */
public class DependencyResponseListener extends Thread{
    private static final Logger log = Logger.getLogger(DependencyResponseListener.class);
    private Communication comm;
    private int noOfIterations;
    private ArrayList<String> dependencies;

    public DependencyResponseListener(ArrayList<String> dependencies) {
        comm = new Communication();
        this. noOfIterations = dependencies.size();
        this.dependencies = dependencies;
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

        int i = 0;
        while (i < noOfIterations) {
            try {
                Socket socket = serverSocket.accept();

                JsonObject responseJson = parser.parse(comm.readFromSocket(socket)).getAsJsonObject();

                String testString = responseJson.get("className").getAsString();
                testString = testString.replace("/", ".");
                testString = testString.replace("\\", ".");
                testString = testString.substring(1);
                testString = testString.replace(".class", "");
                log.debug(testString);

                if (dependencies.contains(testString)) {
                    String fileName = responseJson.get("className").getAsString();
                    fileName = fileName.replace("/", Constants.PATH_SEPARATOR);
                    fileName = fileName.replace("\\", Constants.PATH_SEPARATOR);

                    File file = new File(Constants.DESTINATION_PATH + fileName);
                    file.getParentFile().mkdirs();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    byte[] bytes = Base64.decodeBase64(responseJson.get("file").getAsString());

                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

                    IOUtils.copy(bis, fileOutputStream);
                    fileOutputStream.close();
                    i++;
                    log.debug("Dependency " + testString + " received");
                }

            } catch (IOException e) {
                log.error("Error", e);
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.error("Error", e);
        }
    }
}
