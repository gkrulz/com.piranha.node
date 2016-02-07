package com.piranha.node.comm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Padmaka on 2/7/16.
 */
public class DependencyResponceListener extends Thread{
    private static final Logger log = Logger.getLogger(DependencyResponceListener.class);
    private Communication comm;

    public DependencyResponceListener() {
        comm = new Communication();
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

                JsonObject responceJson = parser.parse(comm.readFromSocket(socket)).getAsJsonObject();

                String fileName = responceJson.get("className").getAsString();
                fileName = fileName.replace("/", Constants.PATH_SEPARATOR);
                fileName = fileName.replace("\\", Constants.PATH_SEPARATOR);

                File file = new File(Constants.DESTINATION_PATH + fileName);
                file.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                byte[] bytes = responceJson.get("file").getAsString().getBytes();

                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

                IOUtils.copy(bis, fileOutputStream);
                fileOutputStream.close();

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
