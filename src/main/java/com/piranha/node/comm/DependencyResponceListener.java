package com.piranha.node.comm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
import com.sun.org.apache.xml.internal.security.transforms.TransformationException;
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
public class DependencyResponceListener extends Thread{
    private static final Logger log = Logger.getLogger(DependencyResponceListener.class);
    private Communication comm;
    private int noOfIterations;
    private ArrayList<String> dependencies;

    public DependencyResponceListener(int noOfIterations, ArrayList<String> dependencies) {
        comm = new Communication();
        this. noOfIterations = noOfIterations;
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
                }

            } catch (IOException e) {
                log.error("Error", e);
            }
        }
    }

    public void readAndSave(Socket socket, String className) throws IOException {
        String path = Constants.DESTINATION_PATH + "/";
        className = className.replace(".", "/") + ".class";
        File file = new File(path + className);
        file.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        IOUtils.copy(socket.getInputStream(), fileOutputStream);
    }
}
