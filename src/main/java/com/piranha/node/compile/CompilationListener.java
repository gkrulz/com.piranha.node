package com.piranha.node.compile;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.piranha.node.comm.DependencyRequestListener;
import com.piranha.node.comm.DependencyResponseListener;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
        DependencyRequestListener dependencyRequestListener = null;
        ArrayList<Thread> compilers = new ArrayList<>();

        dependencyRequestListener = new DependencyRequestListener();
        dependencyRequestListener.start();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9006);
        } catch (IOException e) {
            log.error("Error", e);
        }

        DependencyResponseListener dependencyResponseListener =
                new DependencyResponseListener();
        dependencyResponseListener.start();

        while (true) {
            ArrayList<String> locallyUnavailableDependencies = new ArrayList<>();

            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                log.error("Error", e);
            }

            try {
                String incomingMessage = this.comm.readFromSocket(socket);

                if (incomingMessage.charAt(0) == '[') {
                    JsonArray incomingMsgJson = parser.parse(incomingMessage).getAsJsonArray();

                    //resolving the dependencies
                    for (JsonElement classJson : incomingMsgJson) {
                        JsonArray dependencies = classJson.getAsJsonObject().get("dependencies").getAsJsonArray();

                        for (JsonElement dependency : dependencies){

                              String localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();

//                            log.debug(dependency.getAsString());
//                            log.debug(dependencyMap);
//                            log.debug(dependencyMap.get(dependency.getAsString()) + " and " + localIpAddress);
                            if (!(dependencyMap.get(dependency.getAsString()).equals(localIpAddress))) {
                                String className  = dependency.getAsString();
                                locallyUnavailableDependencies.add(className);
                            }
                        }
                    }

                    log.debug("Locally Unavailable dependencies - " + locallyUnavailableDependencies);

                    //add dependencies in each round
                    dependencyResponseListener.addDependencies(locallyUnavailableDependencies);

                    for (String dependency : locallyUnavailableDependencies) {
                        String ipAddress = dependencyMap.get(dependency);

                        this.resolve(ipAddress, dependency);
                    }



                    for (JsonElement classJson : incomingMsgJson) {
                        Compiler compiler = new Compiler(classJson.getAsJsonObject(), dependencyMap, dependencyResponseListener);
                        compilers.add(compiler);
                        compiler.start();
                    }

//                    for (Thread compiler : compilers) {
//                        try {
//                            compiler.join();
//                        } catch (InterruptedException e) {
//                            log.error("Error", e);
//                        }
//                    }

                } else if (incomingMessage.charAt(0) == '{') {
                    JsonObject incomingMsgJson = parser.parse(incomingMessage).getAsJsonObject();
                    if (incomingMsgJson.get("op").getAsString().equals("dependencyMap")) {
                        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                        HashMap<String, String> tempDependencyMap = gson.fromJson(incomingMsgJson.get("message").getAsString(), type);
                        dependencyMap.putAll(tempDependencyMap);
                        dependencyRequestListener.setDependencyMap(this.dependencyMap);
                    }
                }

                log.debug(gson.toJson(dependencyMap));

            } catch (IOException | ClassNotFoundException e) {
                log.error("Error", e);
            }

        }
    }

    public void resolve(String ipAddress, String dependency) throws IOException {
        String localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();

            if (!(ipAddress.equals(localIpAddress))) {
                log.debug("asking for dependency - " + dependency + " at - " + ipAddress);
                Socket socket = new Socket(ipAddress, 10500);

                JsonObject dependencyRequest = new JsonObject();
                dependencyRequest.addProperty("op", "DEPENDENCY_REQUEST");
                dependencyRequest.addProperty("dependency", dependency);

                comm.writeToSocket(socket, dependencyRequest);
                socket.close();
            }
    }
}
