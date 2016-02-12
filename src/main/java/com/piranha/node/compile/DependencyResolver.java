package com.piranha.node.compile;

import com.piranha.node.util.Communication;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by Padmaka on 2/6/16.
 */
public class DependencyResolver extends Thread {
    private static final Logger log = Logger.getLogger(DependencyResolver.class);
    private Communication comm;

    public DependencyResolver() throws IOException {
        this.comm = new Communication();
    }

    @Override
    public void run() {

    }
}
