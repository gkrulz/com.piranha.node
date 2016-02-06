package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.piranha.node.util.Communication;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created by Padmaka on 2/6/16.
 */
public class DependencyResolver{
    private static final Logger log = Logger.getLogger(DependencyResolver.class);
    private Communication comm;
    private String ipAddress;
    private String dependency;
    private Properties properties;

    public DependencyResolver(String ipAddress, String dependency) throws IOException {
        this.ipAddress = ipAddress;
        this.dependency = dependency;
        this.comm = new Communication();
        this.properties = new Properties();
        this.properties.load(DependencyResolver.class.getClassLoader().getResourceAsStream("config.properties"));
    }

    public void resolve() {
        InetAddress localIpAddress= null;
        try {
            localIpAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("Error", e);
        }

        try {

            if (!this.ipAddress.equals(localIpAddress.getHostAddress())) {
                Socket socket = new Socket(this.ipAddress, 9007);

                JsonObject dependencyRequest = new JsonObject();
                dependencyRequest.addProperty("op", "DEPENDENCY_REQUEST");
                dependencyRequest.addProperty("dependency", dependency);

                comm.writeToSocket(socket, dependencyRequest);
                socket.close();

                ServerSocket serverSocket = new ServerSocket(9007);
                socket = serverSocket.accept();

                this.readAndSave(socket, dependency);
                socket.close();
            }
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

    public void readAndSave(Socket socket, String className) throws IOException {
        String path = properties.getProperty("DESTINATION_PATH");
        className = className.replace(".", "/") + ".class";
        File file = new File(path + className);
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        IOUtils.copy(socket.getInputStream(), fileOutputStream);
    }
}
