package com.piranha.node.comm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Padmaka on 2/6/16.
 */
public class CompilationListener extends Thread{
    private static final Logger log = Logger.getLogger(CompilationListener.class);
    private Socket socket;

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9006);
        } catch (IOException e) {
            log.error("Error", e);
        }

        while (true) {

            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                log.error("Error", e);
            }

            try {
                log.debug(this.readFromSocket(socket));
            } catch (IOException e) {
                log.error("Error", e);
            }

        }
    }

    private JsonObject readFromSocket(Socket socket) throws IOException {
        InputStreamReader in = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
        StringBuilder portInfoString  = new StringBuilder();
        int data = in.read();
        JsonParser jsonParser = new JsonParser();

        while(data != -1) {
            portInfoString.append((char) data);
            data = in.read();
        }
        log.debug(portInfoString);

        return jsonParser.parse(portInfoString.toString()).getAsJsonObject();
    }
}
