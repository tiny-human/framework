import java.io.File;
import java.lang.annotation.*;
import java.lang.annotation.ElementType;
import java.net.URL;
import java.util.*;

public class Utilitaire {
    
    public static URL getPackageURL(String packageName,ClassLoader classloader) throws Exception{
        String path = packageName.replace('.', '/');
        URL url = classloader.getResource(path);
        if(url == null){
            throw new Exception("Package not found: " + packageName);
        }
        return url;
    }

    public static List<String> getClassInPackage(String packageName, ClassLoader classloader) throws Exception {
        List<String> classNames = new ArrayList<>();
        
        URL packageURL = getPackageURL(packageName, classloader);
        File directory = new File(packageURL.toURI());
        
        if(!directory.isDirectory()){
            throw new Exception("Not a directory: " + packageName);
        }

        File[] files = directory.listFiles();
        if(files != null){
            for(File file : files){
                if(file.isFile() && file.getName().endsWith(".class")){
                    String className = file.getName().replace(".class", "");
                    classNames.add(packageName + "." + className);  
                }
            }
        }
        return classNames;
    }

    public static List<String> getControllers(List<String> classNames, String annotation, ElementType elementType) throws Exception {
        List<String> controllers = new ArrayList<>();
        Class<?> annotationsClass = Class.forName(annotation);

        if(!annotationsClass.isAnnotation()){
            throw new Exception(annotation + " is not an annotation");
        }

        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);
            if(clazz.isAnnotationPresent((Class<? extends Annotation>) annotationsClass)){
                Target target = annotationsClass.getAnnotation(Target.class);
                if(target != null){
                    for (ElementType et : target.value()){
                        if(et == elementType){
                            controllers.add(className);
                            break;
                        }
                    }
                }else{
                    controllers.add(className);
                }
            }
        }
        return controllers;

    }
}
