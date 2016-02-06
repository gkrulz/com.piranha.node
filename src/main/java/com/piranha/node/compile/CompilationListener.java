package com.piranha.node.compile;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Padmaka on 2/6/16.
 */
public class CompilationListener extends Thread{
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private Socket socket;
    private HashMap<String, String> dependencyMap;
    private Communication comm;

    public CompilationListener() {
        comm = new Communication();
        dependencyMap = new HashMap<>();
    }

    @Override
    public void run() {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();
        DependencyProvider dependencyProvider = null;
        try {
            dependencyProvider = new DependencyProvider();
        } catch (IOException e) {
            log.error("Error", e);
        }

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
                String incomingMessage = this.comm.readFromSocket(socket);


                if (incomingMessage.charAt(0) == '[') {
                    JsonArray incomingMsgJson = parser.parse(incomingMessage).getAsJsonArray();
                    for (JsonElement classJson : incomingMsgJson) {
                        Compiler compiler = new Compiler(classJson.getAsJsonObject(), dependencyMap);
                        compiler.start();
                    }

                } else if (incomingMessage.charAt(0) == '{') {
                    JsonObject incomingMsgJson = parser.parse(incomingMessage).getAsJsonObject();
                    if (incomingMsgJson.get("op").getAsString().equals("dependencyMap")) {
                        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                        HashMap<String, String> tempDependencyMap = gson.fromJson(incomingMsgJson.get("message").getAsString(), type);
                        dependencyMap.putAll(tempDependencyMap);
                        dependencyProvider.setDependencyMap(this.dependencyMap);
                    }
                }

                log.debug(gson.toJson(dependencyMap));

            } catch (IOException e) {
                log.error("Error", e);
            }

        }
    }
}
