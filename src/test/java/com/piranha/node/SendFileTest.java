package com.piranha.node;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * Created by Padmaka on 2/8/16.
 */
public class SendFileTest {
    private Communication comm;
    private static final Logger log = Logger.getLogger(SendFileTest.class);

    public SendFileTest() {
        comm = new Communication();
    }

    public static void main(String[] args) {
        SendFileTest sendFileTest = new SendFileTest();

        try {
            Socket socket = new Socket("192.168.1.4", 45000);

            File file = new File("/Users/Padmaka/dev/idea_projects/com.piranha.node/target/test-classes/com/piranha/node/Test01.class");

            sendFileTest.sendDependency(file, socket);
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
        requestJson.addProperty("file", new String(Base64.encodeBase64(bytes)));

        comm.writeToSocket(socket, requestJson);
    }
}
