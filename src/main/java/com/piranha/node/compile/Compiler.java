package com.piranha.node.compile;

import org.apache.log4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Padmaka on 1/26/16.
 */
public class Compiler {
    private static final Logger log = Logger.getLogger(Compiler.class);
    private String classPath;

    public Compiler (String classPath) {
        this.classPath = classPath;
    }

    public void compile(String className, String classString) throws Exception {

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if( jc == null) throw new Exception("Compiler unavailable");

//        String code = "public class CustomProcessor { " +
//                "public static void main(String[] args) { " +
//                "System.out.println(\"New class new\"); }}";

        JavaSourceFromString jsfs = new JavaSourceFromString( className, classString);

        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList( jsfs);

        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(this.classPath);
        options.add( "-classpath");
        URLClassLoader urlClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
        StringBuilder sb = new StringBuilder();
        for (URL url : urlClassLoader.getURLs()) {
            sb.append(url.getFile()).append(File.pathSeparator);
        }
        sb.append(this.classPath);
        options.add(sb.toString());

        StringWriter output = new StringWriter();
        boolean success = jc.getTask( output, null, null, options, null, fileObjects).call();
        if( success) {
            log.info("Class " + className + " has been successfully compiled");
        } else {
            throw new Exception( "Compilation failed :" + output);
        }

    }
}
