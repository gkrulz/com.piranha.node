package com.piranha.node.compile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.piranha.node.comm.DependencyRequestListener;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Created by Padmaka on 2/6/16.
 */
public class CodeProvider extends DependencyRequestListener {
    private static final Logger log = Logger.getLogger(CodeProvider.class);
    private Communication comm;
    private ConcurrentHashMap<String, String> dependencyMap;
    private JsonObject requestJson;
    private int port;

    public CodeProvider(JsonObject requestJson, int port) throws IOException {
        this.comm = new Communication();
        this.requestJson = requestJson;
        this.port = port;
    }

    /***
     * The overridden run method of Thread class
     */
    @Override
    public void run() {
        String path = Constants.DESTINATION_PATH + Constants.PATH_SEPARATOR;
        String packagePath = requestJson.get("dependency").getAsString();
        packagePath = packagePath.replace(".", Constants.PATH_SEPARATOR) + ".class";

        File file = new File(path + packagePath);

        log.debug(requestJson.get("dependency").getAsString() + " - " + compilers);
        log.debug(compilers.get(requestJson.get("dependency").getAsString()) == null);

        while (compilers.get(requestJson.get("dependency").getAsString()) == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                log.error("Error", e);
            }
        }

        //checking whether the requested dependency is done compiling.
        Future<?> dependencyCompiler = compilers.get(requestJson.get("dependency").getAsString());
        while (!dependencyCompiler.isDone()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                log.error("Error", e);
            }
        }

        if (requestJson.get("op").getAsString().equals("DEPENDENCY_REQUEST")) {

            try {
                Socket responseSocket = new Socket("127.0.0.1", port);
                this.sendDependency(file, responseSocket);
                log.debug("successfully sent dependency: " + file.getName());
                responseSocket.close();
            } catch (IOException e) {
                log.error("Unable to send the dependency file", e);
            }
        }
    }

    /***
     * The method to send the dependency file.
     * @param classFile File object
     * @param socket socket that the file needs to be written in to
     * @throws IOException
     */
    public void sendDependency(File classFile, Socket socket) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(classFile);
        JsonParser parser = new JsonParser();

        byte[] bytes = IOUtils.toByteArray(fileInputStream);
        fileInputStream.close();
        String className = classFile.getAbsolutePath();
        className = className.replace(Constants.DESTINATION_PATH, "");

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("className", className);
        requestJson.addProperty("file", new String(Base64.encodeBase64(bytes)));

        comm.writeToSocket(socket, requestJson);
    }

    public void setDependencyMap(ConcurrentHashMap<String, String> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    public void addCompilers(HashMap<String, Future<?>> compilers) {
        this.compilers.putAll(compilers);
    }
}
