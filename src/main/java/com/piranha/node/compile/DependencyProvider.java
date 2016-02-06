package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Padmaka on 2/6/16.
 */
public class DependencyProvider extends Thread{
    private static final Logger log = Logger.getLogger(DependencyProvider.class);
    private Communication comm;
    private HashMap<String, String> dependencyMap;

    public DependencyProvider(HashMap<String, String> dependencyMap) {
        this.comm = new Communication();
        this.dependencyMap = dependencyMap;
    }

    @Override
    public void run() {
        JsonParser parser = new JsonParser();

        while (true) {
            try {
                ServerSocket serverSocket = new ServerSocket(9007);
                Socket socket = serverSocket.accept();

                String requestString = comm.readFromSocket(socket);
                JsonObject requestJson = parser.parse(requestString).getAsJsonObject();

                if (requestJson.get("op").getAsString().equals("DEPENDENCY_REQUEST")) {
                    this.sendDependency(new File(""), socket);
                }
            } catch (IOException e) {
                log.error("Error", e);
            }
        }
    }

    public void sendDependency(File classFile, Socket socket) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(classFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        byte[] bytes = new byte[(int)classFile.length()];

        bufferedInputStream.read(bytes, 0, bytes.length);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(bytes, 0, bytes.length);

        outputStream.flush();
        outputStream.close();
    }
}
