package com.piranha.node.compile;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.piranha.node.comm.CompilationInitializer;
import com.piranha.node.comm.CompilationListener;
import com.piranha.node.comm.DependencyRequestListener;
import com.piranha.node.comm.DependencyResponseListener;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.FileWriter;
import org.apache.log4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 1/26/16.
 */
public class Compiler extends Thread {
    private static final Logger log = Logger.getLogger(Compiler.class);
    private Properties properties;
    private JsonObject classJson;

    public Compiler(JsonObject classJson) throws IOException {
        properties = new Properties();
        properties.load(Compiler.class.getClassLoader().getResourceAsStream("config.properties"));
        this.classJson = classJson;
    }

    /***
     * Overridden run method of Thread class
     */
    @Override
    public void run() {

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
            log.error("Unable to compile the file", e);
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
        sb.append("/Users/bhanukayd/Desktop/Piranha/com.piranha.node/destination");
        options.add(sb.toString());

        StringWriter output = new StringWriter();
        boolean success = jc.getTask(output, null, null, options, null, fileObjects).call();
        if (success) {
            log.info("Class " + className + " has been successfully compiled");
        } else {
            throw new Exception("Compilation failed :" + output);
        }

    }
}