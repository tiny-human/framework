package src.java;

import java.io.File;
// import java.lang.ModuleLayer.Controller;
import annotation.Controller;
import java.lang.annotation.*;
import java.net.URL;
import java.util.*;

import jakarta.servlet.ServletException;

public class Utilitaire {

    public static URL getPackageURL(String packageName, ClassLoader classloader) throws Exception {
        String path = packageName.replace('.', '/');
        URL url = classloader.getResource(path);
        if (url == null) {
            throw new Exception("Package not found: " + packageName);
        }
        return url;
    }

    public static List<String> getClassInPackage(String packageName, ClassLoader classloader) throws Exception {
        List<String> classNames = new ArrayList<>();

        URL packageURL = getPackageURL(packageName, classloader);
        File directory = new File(packageURL.toURI());

        if (!directory.isDirectory()) {
            throw new Exception("Not a directory: " + packageName);
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = file.getName().replace(".class", "");
                    classNames.add(packageName + "." + className);
                }
            }
        }
        return classNames;
    }

    public void getControllers(List<String> classNames, String annotation, ElementType elementType,
            Map<MapKey, Mapping> urlMappings)
            throws Exception, ServletException {
        List<String> controllers = new ArrayList<>();
        Class<?> annotationsClass = Class.forName(annotation);

        if (!annotationsClass.isAnnotation()) {
            throw new Exception(annotation + " is not an annotation");
        }

        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent((Class<? extends Annotation>) annotationsClass)) {
                Target target = annotationsClass.getAnnotation(Target.class);
                if (target != null) {
                    for (ElementType et : target.value()) {
                        if (et == elementType) {
                            controllers.add(className);
                            break;
                        }
                    }
                } else {
                    controllers.add(className);
                }

            }

        }
        this.getMethod(controllers, urlMappings);

    }

    public void getMethod(List<String> controllers,Map<MapKey, Mapping> urlMappings)throws ServletException, ClassNotFoundException {

        for (String controller : controllers) {
            Class<?> clazz = Class.forName(controller);

            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation.UrlMapping.class)) {

                    String url = method.getAnnotation(annotation.UrlMapping.class).url();
                    String httpMethod = method.getAnnotation(annotation.UrlMapping.class).method().name();

                    MapKey key = new MapKey(url, httpMethod);

                    if (urlMappings.containsKey(key)) {
                        throw new ServletException("Duplicate URL mapping found for URL: " + url);
                    }

                    urlMappings.put(key, new Mapping(controller, method.getName()));
                }
            }
        }
    }
}
