package com.piranha.node.util;

import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Padmaka on 2/6/16.
 */
public class Communication {

    public String readFromSocket(Socket socket) throws IOException {
        InputStreamReader in = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
        StringBuilder portInfoString  = new StringBuilder();
        int data = in.read();

        while(data != -1) {
            portInfoString.append((char) data);
            data = in.read();
        }

        return portInfoString.toString();
    }

    public void writeToSocket(Socket socket, JsonElement data) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        out.write(data.toString());
        out.flush();
        out.close();
    }
}