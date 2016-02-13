package com.piranha.node.compile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.FileWriter;
import org.apache.log4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.*;
import java.util.*;

/**
 * Created by Padmaka on 1/26/16.
 */
public class Compiler extends Thread {
    private static final Logger log = Logger.getLogger(Compiler.class);
    private Properties properties;
    private JsonObject classJson;
    private HashMap<String, FileWriter> fileWriters;
    private HashMap<String, Compiler> compilers;

    public Compiler(JsonObject classJson, HashMap<String, FileWriter> fileWriters, HashMap<String, Compiler> compilers) throws IOException {
        properties = new Properties();
        properties.load(Compiler.class.getClassLoader().getResourceAsStream("config.properties"));
        this.classJson = classJson;
        this.fileWriters = fileWriters;
        this.compilers = compilers;
    }

    /***
     * Overridden run method of Thread class
     */
    @Override
    public void run() {
        //resolving the dependencies
        JsonArray dependencies = classJson.getAsJsonObject().get("dependencies").getAsJsonArray();

        for (JsonElement dependency : dependencies) {
            HashMap<String, Thread> dependencyThreads = new HashMap<>();

            dependencyThreads.putAll(fileWriters);
            dependencyThreads.putAll(compilers);

            //Waiting for dependencies to be compiled or arrive
            while (dependencyThreads.get(dependency.getAsString()) == null) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error("Error", e);
                }

                dependencyThreads.putAll(fileWriters);
                dependencyThreads.putAll(compilers);
            }

            log.debug(dependencyThreads.get(dependency.getAsString()));

            //Checking thread liveliness
            while (dependencyThreads.get(dependency.getAsString()).isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error("Error", e);
                }
            }
        }

        StringBuilder packageName = new StringBuilder(classJson.get("package").getAsString());
        StringBuilder classString = new StringBuilder("package " + packageName.replace(packageName.length() - 1, packageName.length(), "") + ";\n");

        for (JsonElement importStatement : classJson.get("importStatements").getAsJsonArray()) {
            classString.append("import " + importStatement.getAsString() + ";\n");
        }
        classString.append(classJson.get("classDeclaration").getAsString());
        classString.append(classJson.get("classString").getAsString() + "}");
        try {
            this.compile(classJson.get("className").getAsString(), classString.toString());
        } catch (Exception e) {
            log.error("", e);
            System.exit(0);
        }
    }

    /***
     * The method to compile the given class
     * @param className name of the class to compile
     * @param classString Class code as a string
     * @throws Exception
     */
    public void compile(String className, String classString) throws Exception {

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) throw new Exception("Compiler unavailable");

        JavaSourceFromString jsfs = new JavaSourceFromString(className, classString);

        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(jsfs);

        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(Constants.DESTINATION_PATH);
        options.add("-classpath");
        URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        StringBuilder sb = new StringBuilder();
        for (URL url : urlClassLoader.getURLs()) {
            sb.append(url.getFile()).append(File.pathSeparator);
        }
        sb.append(properties.getProperty("CLASSPATH"));
        options.add(sb.toString());

        StringWriter output = new StringWriter();
        boolean success = jc.getTask(output, null, null, options, null, fileObjects).call();
        if (success) {
            log.info("Class " + className + " has been successfully compiled");
        } else {
            throw new Exception("Compilation failed :" + output);
        }

    }

    public String getClassName () {
        return classJson.get("absoluteClassName").getAsString();
    }
}