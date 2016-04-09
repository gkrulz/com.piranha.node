package com.piranha.node.comm;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.piranha.node.compile.Compiler;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.StringMatchFilter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Padmaka on 2/14/16.
 */
public class CompilationInitializer extends CompilationListener {
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private String incomingMessage;
    private HashSet<String> locallyUnavailableDependencies;
    private Communication comm;
    private static ConcurrentHashMap<String, Future<?>> compilers = new ConcurrentHashMap<>();

    public CompilationInitializer(String incomingMessage) {
        this.incomingMessage = incomingMessage;
        this.locallyUnavailableDependencies = new HashSet<>();
        this.comm = new Communication();
    }

    /***
     * The overridden run method of Thread class
     */
    public void run() {
        log.debug("Init Started " + "THREAD - " + Thread.currentThread().getId());

        JsonParser parser = new JsonParser();
        Gson gson = new Gson();
        Type mapType = new TypeToken<ConcurrentHashMap<String, String>>() {
        }.getType();

        JsonObject incomingMsgJson = parser.parse(incomingMessage).getAsJsonObject();
        if (incomingMsgJson.get("op").getAsString().equals("COMPILATION")) {

            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();

            HashMap<String, String> tempDependencyMap = gson.fromJson(incomingMsgJson.get("dependencyMap").getAsString(), type);
            this.dependencyMap.putAll(tempDependencyMap);
            dependencyRequestListener.setDependencyMap(dependencyMap);
            log.debug(gson.toJson(dependencyMap));

            Type arrayListType = new TypeToken<ArrayList<JsonObject>>() {
            }.getType();
            ArrayList<JsonObject> classes = gson.fromJson(incomingMsgJson.get("classes").getAsString(), arrayListType);

            synchronized (CompilationInitializer.class) {
                //resolving the dependencies
                for (JsonElement classJson : classes) {
                    ConcurrentHashMap<String, String> dependencies = gson.fromJson(classJson.getAsJsonObject().get("dependencies").getAsString(), mapType);

                    for (String dependency : dependencies.values()) {

                        String localIpAddress = null;
                        try {
                            localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();
                        } catch (SocketException e) {
                            log.error("Unable to get the local IP", e);
                        }

//                        log.debug(dependency.getAsString() + " - " + alreadyRequestedDependencies);
//                        log.debug(dependency.getAsString() + " - " + dependencyMap);

                        while (dependencyMap.get(dependency) == null) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                log.error("Unable to sleep thread");
                            }
                        }

                        if (!(dependencyMap.get(dependency).equals(localIpAddress)) &&
                                !(alreadyRequestedDependencies.contains(dependency))) {

                            String className = dependency;
                            locallyUnavailableDependencies.add(className);

                        }
                    }
                }

//            log.debug("Locally Unavailable dependencies - " + locallyUnavailableDependencies);
                log.debug("Mada " + "THREAD - " + Thread.currentThread().getId());

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
            //-------------------------------------------------------
            int threadCount = 8; //TODO load the threadCount from properties
            ExecutorService service = Executors.newFixedThreadPool(threadCount);
            log.debug(classes.size());

            int x = 0;
            innerloop:
            while (x < classes.size()) {


                if (x >= classes.size()) {
                    break;
                }

                JsonElement currentElement = classes.get(x);
                ConcurrentHashMap<String, String> dependencies = gson.fromJson(currentElement.getAsJsonObject().get("dependencies").getAsString(), mapType);

                for (String dependency : dependencies.values()) {

                    HashMap<String, Thread> dependencyThreads = new HashMap<>();
                    HashMap<String, Future<?>> futureDependencyThreads = new HashMap<>();

                    futureDependencyThreads.putAll(CompilationInitializer.getCompilers());
                    dependencyThreads.putAll(DependencyResponseListener.getFileWriters());

                    if ((dependencyThreads.get(dependency) == null ||
                            dependencyThreads.get(dependency).isAlive()) && (
                            futureDependencyThreads.get(dependency) == null ||
                                    !futureDependencyThreads.get(dependency).isDone())) {

                        classes.remove(x);
                        classes.add(currentElement.getAsJsonObject());
                        continue innerloop;
                    }

                }
                x++;
                Compiler compiler = null;
                try {
                    compiler = new Compiler(currentElement.getAsJsonObject());
                } catch (IOException e) {
                    log.error("Unable to initialize the compiler", e);
                }

                Future<?> futureThread = service.submit(compiler);
                compilers.put(currentElement.getAsJsonObject().get("absoluteClassName").getAsString(), futureThread);


            }

            //-------------------------------------------------------


            /*for (JsonElement classJson : classes) {


                Compiler compiler = null;
                try {
                    compiler = new Compiler(classJson.getAsJsonObject());
                } catch (IOException e) {
                    log.error("Unable to initialize the compiler", e);
                }
                compilers.put(classJson.getAsJsonObject().get("absoluteClassName").getAsString(), compiler);
                compiler.start();
            }*/

            //Add all compilation threads to dependency request listener
            dependencyRequestListener.setCompilers(compilers);


//        log.debug(gson.toJson(dependencyMap));

        } else if (incomingMsgJson.get("op").getAsString().equals("TERMINATE")) {
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            HashMap<String, String> fullDependencyMap = gson.fromJson(incomingMsgJson.get("classes").getAsString(), type);
            ArrayList<String> filesRequired = new ArrayList<>();
            String localIpAddress = null;
            try {
                localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();
            } catch (SocketException e) {
                log.error("Unable to get the local ip", e);
            }

            for (Object key : fullDependencyMap.keySet()) {
                String className = (String) key;
                String ipAddress = fullDependencyMap.get(className);
                if (localIpAddress.equals(ipAddress)) {
                    filesRequired.add(className);
                }
            }

            log.debug(filesRequired);
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

    public static ConcurrentHashMap<String, Future<?>> getCompilers() {
        return compilers;
    }
}
