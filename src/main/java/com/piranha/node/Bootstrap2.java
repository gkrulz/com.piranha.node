package com.piranha.node;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bhanukayd on 4/9/16.
 */
public class Bootstrap2 {
    public static void main(String[] args) throws IOException {

        ExecutorService service = Executors.newFixedThreadPool(5);

        Runner[] runners = new Runner[10];
        for (int x = 0; x < 10; x++) {
            runners[x] = new Runner(x+"");
        }

        service.submit(runners[0]);
        service.submit(runners[1]);
        service.submit(runners[2]);
        service.submit(runners[3]);
        service.submit(runners[4]);
        service.submit(runners[5]);
        service.submit(runners[6]);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("WAITED");
        service.submit(runners[7]);
        service.submit(runners[8]);
        service.submit(runners[9]);

    }

    private static class Runner extends Thread {
        String name;

        public Runner(String name) {
            this.name = name;
        }

        public void run() {
            System.out.println(name);
        }
    }
}
