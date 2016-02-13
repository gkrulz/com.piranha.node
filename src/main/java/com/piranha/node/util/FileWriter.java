package com.piranha.node.util;

import com.google.gson.JsonObject;
import com.piranha.node.constants.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Padmaka on 2/11/16.
 */
public class FileWriter extends Thread{
    private static final Logger log = Logger.getLogger(FileWriter.class);
    private JsonObject responseJson;
    private String className;

    public FileWriter(JsonObject responseJson, String className) {
        this.responseJson = responseJson;
        this.className = className;
    }

    /***
     * The overridden run method of Thread class
     */
    @Override
    public void run() {
        try {
            String fileName = responseJson.get("className").getAsString();
            fileName = fileName.replace("/", Constants.PATH_SEPARATOR);
            fileName = fileName.replace("\\", Constants.PATH_SEPARATOR);

            File file = new File(Constants.DESTINATION_PATH + fileName);
            file.getParentFile().mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] bytes = Base64.decodeBase64(responseJson.get("file").getAsString());

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

            IOUtils.copy(bis, fileOutputStream);
            fileOutputStream.close();

            log.debug("Dependency " + getClassName() + " received");
        } catch (IOException e) {
            log.error("Error" , e);
        }
    }

    /***
     * method to get the class name
     * @return class name
     */
    public String getClassName() {
        return className;
    }
}
