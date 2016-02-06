package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.piranha.node.util.Communication;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
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
    private Properties properties;

    public DependencyResolver() throws IOException {
        this.comm = new Communication();
        this.properties = new Properties();
        this.properties.load(DependencyResolver.class.getClassLoader().getResourceAsStream("config.properties"));
    }

    public void resolve(String ipAddress, String dependency) {
        InetAddress localIpAddress= null;
        try {
            localIpAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("Error", e);
        }

        try {
            log.debug(dependency);
            if (!ipAddress.equals(localIpAddress.getHostAddress())) {
                Socket socket = new Socket(ipAddress, 10500);

                JsonObject dependencyRequest = new JsonObject();
                dependencyRequest.addProperty("op", "DEPENDENCY_REQUEST");
                dependencyRequest.addProperty("dependency", dependency);

                comm.writeToSocket(socket, dependencyRequest);
                socket.close();

//                ServerSocket serverSocket = new ServerSocket(9007);
//                socket = serverSocket.accept();

                this.readAndSave(socket, dependency);
                socket.close();

                log.debug("Dependency Sent");
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
