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
//        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//        return in.readLine();

        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        return (String) in.readObject();
    }

    public void writeToSocket(Socket socket, JsonElement data) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        out.write(data.toString());
        out.flush();
        out.close();
    }
}