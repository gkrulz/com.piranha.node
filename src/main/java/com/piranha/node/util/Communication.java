package com.piranha.node.util;

import com.google.gson.JsonElement;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Created by Padmaka on 2/6/16.
 */
public class Communication {

    /***
     * Util method to read from socket
     * @param socket the socket that need to be read from
     * @return String object read from the socket
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public String readFromSocket(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        return (String) in.readObject();
    }

    /***
     * Util method to write to a socket
     * @param socket the socket that needs to be written in to
     * @param data json element that needs to be written to the socket
     * @throws IOException
     */
    public void writeToSocket(Socket socket, JsonElement data) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(data.toString());
        out.flush();
    }

    /***
     * Util method to get the non loopback local IP address
     * @param preferIpv4 boolean true if Ipv4 is needed
     * @param preferIPv6 boolean true if Ipv6 is needed
     * @return non loopback local IP address
     * @throws SocketException
     */
    public static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }
}