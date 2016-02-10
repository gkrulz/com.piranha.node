package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Padmaka on 2/6/16.
 */
public class DependencyResolver{
    private static final Logger log = Logger.getLogger(DependencyResolver.class);
    private Communication comm;

    public DependencyResolver() throws IOException {
        this.comm = new Communication();
    }

    public void resolve(String ipAddress, String dependency) throws IOException {

        String localIpAddress = Communication.getFirstNonLoopbackAddress(true, false).getHostAddress();

            log.debug(dependency);
//            log.debug(ipAddress + " == " + localIpAddress.getHostAddress() + " - " + ipAddress.equals(localIpAddress.getHostAddress()));
            if (!(ipAddress.equals(localIpAddress))) {
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
