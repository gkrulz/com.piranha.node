package com.piranha.node.compile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
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
        JsonArray dependencies = classJson.get("dependencies").getAsJsonArray();

        if (dependencies.size() > 0) {
            DependencyResolver dependencyResolver = null;
            try {
                dependencyResolver = new DependencyResolver();
            } catch (IOException e) {
                log.error("Error", e);
            }
            for (int i = 0; i < dependencies.size(); i++) {
                String dependency = dependencies.get(i).getAsString();
                String ipAddress = dependencyMap.get(dependency);

                dependencyResolver.resolve(ipAddress, dependency);

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
            log.debug("Stop");
        } catch (Exception e) {
            log.error("", e);
        }
        log.debug(classString);
    }

    public void compile(String className, String classString) throws Exception {

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) throw new Exception("Compiler unavailable");

//        String code = "public class CustomProcessor { " +
//                "public static void main(String[] args) { " +
//                "System.out.println(\"New class new\"); }}";

        JavaSourceFromString jsfs = new JavaSourceFromString(className, classString);

        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(jsfs);

        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(properties.getProperty("DESTINATION_PATH"));
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
