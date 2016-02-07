package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.piranha.node.util.Communication;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
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
//            log.debug(ipAddress + " == " + localIpAddress.getHostAddress() + " - " + ipAddress.equals(localIpAddress.getHostAddress()));
            if (!(ipAddress.equals(localIpAddress.getHostAddress()))) {
                log.debug("asking for dependency - " + dependency + " at - " + ipAddress);
                Socket socket = new Socket(ipAddress, 10500);

                JsonObject dependencyRequest = new JsonObject();
                dependencyRequest.addProperty("op", "DEPENDENCY_REQUEST");
                dependencyRequest.addProperty("dependency", dependency);

                comm.writeToSocket(socket, dependencyRequest);

                this.readAndSave(socket, dependency);
                socket.close();

                log.debug("Dependency Sent");
            }
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

    public void readAndSave(Socket socket, String className) throws IOException {
        String path = properties.getProperty("DESTINATION_PATH") + "/";
        className = className.replace(".", "/") + ".class";
        File file = new File(path + className);
        file.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        IOUtils.copy(socket.getInputStream(), fileOutputStream);
    }
}
