package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Created by Padmaka on 2/6/16.
 */
public class DependencyResolver extends Thread{
    private static final Logger log = Logger.getLogger(DependencyResolver.class);
    private Communication comm;
    private String ipAddress;
    private String dependency;
    private HashMap<String, String> dependencyMap;

    public DependencyResolver(String ipAddress, String dependency, HashMap<String, String> dependencyMap) {
        this.ipAddress = ipAddress;
        this.dependency = dependency;
        this.comm = new Communication();
        this.dependencyMap = dependencyMap;
    }

    @Override
    public void run() {
        InetAddress ipAddress= null;
        try {
            ipAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("Error", e);
        }

        try {

            if (!dependencyMap.get(dependency).equals(ipAddress.getHostAddress())) {
                Socket socket = new Socket(ipAddress, 9007);

                JsonObject dependencyRequest = new JsonObject();
                dependencyRequest.addProperty("op", "DEPENDENCY_REQUEST");
                dependencyRequest.addProperty("dependency", dependency);

                comm.writeToSocket(socket, dependencyRequest);
                socket.close();

                ServerSocket serverSocket = new ServerSocket(9007);
                socket = serverSocket.accept();

                this.readAndSave(socket, dependency);

            }
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

    public void readAndSave(Socket socket, String className) {

    }
}
