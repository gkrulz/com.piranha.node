package com.piranha.node.comm;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Padmaka on 2/6/16.
 */
public class CompilationListener extends Thread{
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private Socket socket;
    private HashMap<String, String> dependencyMap;

    public CompilationListener() {
        dependencyMap = new HashMap<>();
    }

    @Override
    public void run() {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9006);
        } catch (IOException e) {
            log.error("Error", e);
        }

        while (true) {

            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                log.error("Error", e);
            }

            try {
                String incomingMessage = this.readFromSocket(socket);


                if (incomingMessage.charAt(0) == '[') {
                    JsonArray incomingMsgJson = parser.parse(incomingMessage).getAsJsonArray();

                } else if (incomingMessage.charAt(0) == '{') {
                    JsonObject incomingMsgJson = parser.parse(incomingMessage).getAsJsonObject();
                    if (incomingMsgJson.get("op").getAsString().equals("dependencyMap")) {
                        Type type = new TypeToken<Map<String, String>>(){}.getType();
                        HashMap<String, String> tempDependencyMap = gson.fromJson(incomingMsgJson.get("op"), type);
                        dependencyMap.putAll(tempDependencyMap);

                    }
                }

                log.debug(dependencyMap);

            } catch (IOException e) {
                log.error("Error", e);
            }

        }
    }

    private String readFromSocket(Socket socket) throws IOException {
        InputStreamReader in = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
        StringBuilder portInfoString  = new StringBuilder();
        int data = in.read();

        while(data != -1) {
            portInfoString.append((char) data);
            data = in.read();
        }

        return portInfoString.toString();
    }
}
