package com.piranha.node.util;

import com.google.gson.JsonElement;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Padmaka on 2/6/16.
 */
public class Communication {

    public String readFromSocket(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        return (String) in.readObject();
    }

    public void writeToSocket(Socket socket, JsonElement data) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(data.toString());
    }
}