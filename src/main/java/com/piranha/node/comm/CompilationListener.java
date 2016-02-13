package com.piranha.node.comm;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.piranha.node.compile.Compiler;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 2/6/16.
 */
public class CompilationListener extends Thread {
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private Socket socket;
    protected static ConcurrentHashMap<String, String> dependencyMap;
    private Communication comm;
    protected static List<String> alreadyRequestedDependencies;
    protected DependencyRequestListener dependencyRequestListener;
    protected DependencyResponseListener dependencyResponseListener;

    public CompilationListener() {
        comm = new Communication();
        dependencyMap = new ConcurrentHashMap<>();
        alreadyRequestedDependencies = Collections.synchronizedList(new ArrayList<>());
        dependencyRequestListener = new DependencyRequestListener();
        dependencyResponseListener = new DependencyResponseListener();
    }

    /***
     * The overridden run method of Thread class
     */
    @Override
    public void run() {

        Gson gson = new Gson();

        //Starting the listener for dependency requests from other nodes.
        dependencyRequestListener.start();

        //Starting the listener for dependency responses from other nodes.
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

            CompilationInitializer compilationInitializer = new CompilationInitializer(incomingMessage);
            compilationInitializer.start();
        }
    }
}

class CompilationInitializer extends CompilationListener {
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private String incomingMessage;
    //    private HashMap<String, String> dependencyMap;
    private HashSet<String> locallyUnavailableDependencies;
    //    private DependencyResponseListener dependencyResponseListener;
//    private DependencyRequestListener dependencyRequestListener;
    private Communication comm;
//    private ArrayList<String> alreadyRequestedDependencies;

    public CompilationInitializer(String incomingMessage/*, HashMap<String, String> dependencyMap,
                                   DependencyResponseListener dependencyResponseListener,
                                   DependencyRequestListener dependencyRequestListener,
                                   ArrayList<String> alreadyRequestedDependencies*/) {
        this.incomingMessage = incomingMessage;
//        this.dependencyMap = new HashMap<>();
//        this.dependencyResponseListener = dependencyResponseListener;
//        this.dependencyRequestListener = dependencyRequestListener;
        this.locallyUnavailableDependencies = new HashSet<>();
        this.comm = new Communication();
//        this.alreadyRequestedDependencies = alreadyRequestedDependencies;
    }

    /***
     * The overridden run method of Thread class
     */
    public void run() {
        JsonParser parser = new JsonParser();
        HashMap<String, Compiler> compilers = new HashMap<>();
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

                    log.debug(dependency.getAsString() + " - " + alreadyRequestedDependencies);
//                    log.debug(dependency.getAsString() + " - " + dependencyMap);

                    while (dependencyMap.get(dependency.getAsString()) == null) {

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


            for (JsonElement classJson : classes) {
                Compiler compiler = null;
                try {
                    compiler = new Compiler(classJson.getAsJsonObject(), dependencyResponseListener.getFileWriters(), compilers);
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
}
