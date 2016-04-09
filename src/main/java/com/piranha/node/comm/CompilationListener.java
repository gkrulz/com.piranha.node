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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    ExecutorService service = Executors.newFixedThreadPool(1);

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
            log.debug("Innitalizing");
            service.submit(compilationInitializer);
        }
    }
}
