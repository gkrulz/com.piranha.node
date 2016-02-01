package com.piranha.node.comm;

import java.net.Socket;

/**
 * Created by Padmaka on 1/27/16.
 */
public class NodeCommLine {
    private Socket socket;


    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
