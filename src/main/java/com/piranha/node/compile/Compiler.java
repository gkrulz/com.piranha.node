package com.piranha.node.compile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.piranha.node.constants.Constants;
import com.piranha.node.util.Communication;
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
    private HashMap<String, String> dependencyMap;

    public Compiler(JsonObject classJson, HashMap<String, String> dependencyMap) throws IOException {
        properties = new Properties();
        properties.load(Compiler.class.getClassLoader().getResourceAsStream("config.properties"));
        this.classJson = classJson;
        this.dependencyMap = dependencyMap;
    }

    @Override
    public void run() {
        //resolving the dependencies
        JsonArray dependencies = classJson.getAsJsonObject().get("dependencies").getAsJsonArray();

        for (JsonElement dependency : dependencies) {
            String currentDir = System.getProperty("user.dir") + Constants.PATH_SEPARATOR;
            String dependencyPath = dependency.getAsString().replace(".", Constants.PATH_SEPARATOR) + ".class";
            File file = new File(currentDir + dependencyPath);

            while(!file.exists()) {
//                log.debug("waiting for dependency - " + dependency);
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
}
