package com.piranha.node.comm;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.piranha.node.compile.Compiler;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Padmaka on 2/6/16.
 */
public class CompilationListener extends Thread {
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private Socket socket;
    private HashMap<String, String> dependencyMap;
    private Communication comm;

    public CompilationListener() {
        comm = new Communication();
        dependencyMap = new HashMap<>();
    }

    /***
     * The overridden run method of Thread class
     */
    @Override
    public void run() {

        Gson gson = new Gson();
        DependencyRequestListener dependencyRequestListener = null;

        //Initialising and starting the listener for dependency requests from other nodes.
        dependencyRequestListener = new DependencyRequestListener();
        dependencyRequestListener.start();

        //Initialising and starting the listener for dependency responses from other nodes.
        DependencyResponseListener dependencyResponseListener =
                new DependencyResponseListener();
        dependencyResponseListener.start();

        // Listening to incoming work orders to compile on port 9006.
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

            String incomingMessage = null;
            try {
                incomingMessage = this.comm.readFromSocket(socket);
            } catch (IOException e) {
                log.error("Unable to read from the socket", e);
            } catch (ClassNotFoundException e) {
                log.error("Class not found", e);
            }

            CompilationInitializer compilationInitializer = new CompilationInitializer(incomingMessage,
                    dependencyMap, dependencyResponseListener, dependencyRequestListener);
            compilationInitializer.start();

            log.debug(gson.toJson(dependencyMap));
        }
    }
}

class CompilationInitializer extends Thread {
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private String incomingMessage;
    private HashMap<String, String> dependencyMap;
    private HashSet<String> locallyUnavailableDependencies;
    private DependencyResponseListener dependencyResponseListener;
    private DependencyRequestListener dependencyRequestListener;
    private Communication comm;

    public CompilationInitializer (String incomingMessage, HashMap<String, String> dependencyMap,
                                   DependencyResponseListener dependencyResponseListener,
                                   DependencyRequestListener dependencyRequestListener) {
        this.incomingMessage = incomingMessage;
        this.dependencyMap = dependencyMap;
        this.dependencyResponseListener = dependencyResponseListener;
        this.dependencyRequestListener = dependencyRequestListener;
        this.locallyUnavailableDependencies = new HashSet<>();
        this.comm = new Communication();
    }

    public void run () {
        JsonParser parser = new JsonParser();
        ArrayList<Thread> compilers = new ArrayList<>();
        Gson gson = new Gson();

        if (incomingMessage.charAt(0) == '[') {
            JsonArray incomingMsgJson = parser.parse(incomingMessage).getAsJsonArray();

            //resolving the dependencies
            for (JsonElement classJson : incomingMsgJson) {
                JsonArray dependencies = classJson.getAsJsonObject().get("dependencies").getAsJsonArray();

                for (JsonElement dependency : dependencies) {

                    String localIpAddress = null;
                    try {
                        localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();
                    } catch (SocketException e) {
                        log.error("Unable to get the local IP", e);
                    }

//                    log.debug(dependency.getAsString());
//                    log.debug(dependencyMap);
//                    log.debug(dependencyMap.get(dependency.getAsString()) + " and " + localIpAddress);

                    String filePath = Constants.DESTINATION_PATH + Constants.PATH_SEPARATOR;
                    String dependencyPath = dependency.getAsString().replace(".", Constants.PATH_SEPARATOR) + ".class";

                    File file = new File(filePath + dependencyPath);

                    if (!(dependencyMap.get(dependency.getAsString()).equals(localIpAddress)) && !(file.exists())) {
                        String className = dependency.getAsString();
                        locallyUnavailableDependencies.add(className);
                    }
                }
            }

            log.debug("Locally Unavailable dependencies - " + locallyUnavailableDependencies);

            //add dependencies in each round
            dependencyResponseListener.addDependencies(locallyUnavailableDependencies);

            for (String dependency : locallyUnavailableDependencies) {
                String ipAddress = dependencyMap.get(dependency);

                try {
                    this.resolve(ipAddress, dependency);
                } catch (IOException e) {
                    log.error("Unable to resolve dependency", e);
                }
            }


            for (JsonElement classJson : incomingMsgJson) {
                Compiler compiler = null;
                try {
                    compiler = new Compiler(classJson.getAsJsonObject(), dependencyMap, dependencyResponseListener);
                } catch (IOException e) {
                    log.error("Unable to initialize the compiler", e);
                }
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
                Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                HashMap<String, String> tempDependencyMap = gson.fromJson(incomingMsgJson.get("message").getAsString(), type);
                dependencyMap.putAll(tempDependencyMap);
                dependencyRequestListener.setDependencyMap(this.dependencyMap);
            }
        }
    }

    public void resolve(String ipAddress, String dependency) throws IOException {
        String localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();
        String filePath = Constants.DESTINATION_PATH + Constants.PATH_SEPARATOR;
        String dependencyPath = dependency.replace(".", Constants.PATH_SEPARATOR) + ".class";

        File file = new File(filePath + dependencyPath);

        if (!(ipAddress.equals(localIpAddress)) && !(file.exists())) {
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
