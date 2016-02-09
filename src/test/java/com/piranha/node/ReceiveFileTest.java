package com.piranha.node;

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

/**
 * Created by Padmaka on 2/8/16.
 */
public class ReceiveFileTest {
    private Communication comm;
    private static final Logger log = Logger.getLogger(SendFileTest.class);

    public ReceiveFileTest() {
        comm = new Communication();
    }

    public static void main(String[] args) {
        ReceiveFileTest receiveFileTest = new ReceiveFileTest();
        ServerSocket serverSocket = null;
        JsonParser parser = new JsonParser();

        try {
            serverSocket = new ServerSocket(45000);
        } catch (IOException e) {
            log.error("Error", e);
        }

        while (true) {
            try {
                Socket socket = serverSocket.accept();

                JsonObject responceJson = parser.parse(receiveFileTest.comm.readFromSocket(socket)).getAsJsonObject();

                String fileName = responceJson.get("className").getAsString();
                fileName = fileName.replace("/", Constants.PATH_SEPARATOR);
                fileName = fileName.replace("\\", Constants.PATH_SEPARATOR);

                File file = new File(Constants.DESTINATION_PATH + fileName);
                file.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                byte[] bytes = Base64.decodeBase64(responceJson.get("file").getAsString());

                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

                IOUtils.copy(bis, fileOutputStream);
                fileOutputStream.close();

            } catch (IOException | ClassNotFoundException e) {
                log.error("Error", e);
            }
        }
    }
}
