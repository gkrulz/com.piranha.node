package com.piranha.node.comm;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.piranha.node.compile.Compiler;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 2/14/16.
 */
public class CompilationInitializer extends CompilationListener {
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private String incomingMessage;
    private HashSet<String> locallyUnavailableDependencies;
    private Communication comm;
    private static ConcurrentHashMap<String, Compiler> compilers = new ConcurrentHashMap<>();

    public CompilationInitializer(String incomingMessage) {
        this.incomingMessage = incomingMessage;
        this.locallyUnavailableDependencies = new HashSet<>();
        this.comm = new Communication();
    }

    /***
     * The overridden run method of Thread class
     */
    public void run() {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonObject incomingMsgJson = parser.parse(incomingMessage).getAsJsonObject();
        if (incomingMsgJson.get("op").getAsString().equals("COMPILATION")) {
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            HashMap<String, String> tempDependencyMap = gson.fromJson(incomingMsgJson.get("dependencyMap").getAsString(), type);
            this.dependencyMap.putAll(tempDependencyMap);
            dependencyRequestListener.setDependencyMap(dependencyMap);

            Type arrayListType = new TypeToken<ArrayList<JsonObject>>() {
            }.getType();
            ArrayList<JsonObject> classes = gson.fromJson(incomingMsgJson.get("classes").getAsString(), arrayListType);

            synchronized (CompilationInitializer.class) {
                //resolving the dependencies
                for (JsonElement classJson : classes) {
                    JsonArray dependencies = classJson.getAsJsonObject().get("dependencies").getAsJsonArray();

                    for (JsonElement dependency : dependencies) {

                        String localIpAddress = null;
                        try {
                            localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();
                        } catch (SocketException e) {
                            log.error("Unable to get the local IP", e);
                        }

//                        log.debug(dependency.getAsString() + " - " + alreadyRequestedDependencies);
//                        log.debug(dependency.getAsString() + " - " + dependencyMap);

                        while (dependencyMap.get(dependency.getAsString()) == null) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                log.error("Unable to sleep thread");
                            }
                        }

                        if (!(dependencyMap.get(dependency.getAsString()).equals(localIpAddress)) &&
                                !(alreadyRequestedDependencies.contains(dependency.getAsString()))) {

                            String className = dependency.getAsString();
                            locallyUnavailableDependencies.add(className);

                        }
                    }
                }

//            log.debug("Locally Unavailable dependencies - " + locallyUnavailableDependencies);

                //add dependencies in each round
                dependencyResponseListener.addDependencies(locallyUnavailableDependencies);

                for (String dependency : locallyUnavailableDependencies) {
                    String ipAddress = dependencyMap.get(dependency);

                    try {
                        alreadyRequestedDependencies.add(dependency);
                        this.requestDependency(ipAddress, dependency);
                    } catch (IOException e) {
                        log.error("Unable to request dependency", e);
                    }
                }

            }


            for (JsonElement classJson : classes) {
                Compiler compiler = null;
                try {
                    compiler = new Compiler(classJson.getAsJsonObject());
                } catch (IOException e) {
                    log.error("Unable to initialize the compiler", e);
                }
                compilers.put(classJson.getAsJsonObject().get("absoluteClassName").getAsString(), compiler);
                compiler.start();
            }

            //Add all compilation threads to dependency request listener
            dependencyRequestListener.setCompilers(compilers);


//        log.debug(gson.toJson(dependencyMap));

        }
    }

    /***
     * The method to request the dependencies needed.
     *
     * @param ipAddress  ip address of the node which has the .class file
     * @param dependency absolute class name of the dependency.
     * @throws IOException
     */
    public void requestDependency(String ipAddress, String dependency) throws IOException {
        String localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();

        log.debug("asking for dependency - " + dependency + " at - " + ipAddress);
        Socket socket = new Socket(ipAddress, 10500);

        JsonObject dependencyRequest = new JsonObject();
        dependencyRequest.addProperty("op", "DEPENDENCY_REQUEST");
        dependencyRequest.addProperty("dependency", dependency);

        comm.writeToSocket(socket, dependencyRequest);
        socket.close();
    }

    public static ConcurrentHashMap<String, Compiler> getCompilers() {
        return compilers;
    }
}
