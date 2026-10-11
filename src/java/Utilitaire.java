package src.java;

import java.io.File;
// import java.lang.ModuleLayer.Controller;
import annotation.Controller;
import java.lang.annotation.*;
import java.net.URL;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    public void getMethod(List<String> controllers, Map<MapKey, Mapping> urlMappings)
            throws ServletException, ClassNotFoundException {

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

    public static boolean haveParameter(java.lang.reflect.Method method, Class<?> param) {
        for (Class<?> type : method.getParameterTypes()) {
            if (type == param) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || type.equals(String.class)
                || type.equals(Integer.class)
                || type.equals(Long.class)
                || type.equals(Double.class)
                || type.equals(Float.class)
                || type.equals(Short.class)
                || type.equals(Byte.class)
                || type.equals(Boolean.class)
                || type.equals(Character.class)
                || type.equals(BigDecimal.class)
                || type.equals(LocalDate.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object convert(String value, Class<?> type) {
        if (type.equals(String.class)) {
            return value;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.parseLong(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.parseFloat(value);
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            return Short.parseShort(value);
        } else if (type.equals(byte.class) || type.equals(Byte.class)) {
            return Byte.parseByte(value);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            // une case à cocher sans attribut value envoie "on"
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equals("1");
        } else if (type.equals(char.class) || type.equals(Character.class)) {
            return value.charAt(0);
        } else if (type.equals(BigDecimal.class)) {
            return new BigDecimal(value);
        } else if (type.equals(LocalDate.class)) {
            return LocalDate.parse(value); // format AAAA-MM-JJ, celui de <input type="date">
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, value);
        }
        throw new IllegalArgumentException("Type de parametre non supporte: " + type.getName());
    }

    // Cherche "setXxx" pour l'attribut "xxx" : on compare le nom et on exige 1
    // paramètre
    public static Method findSetter(Class<?> clazz, String fieldName) {
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                return m;
            }
        }
        return null;
    }

    // Construit l'objet et le remplit à partir des champs "prefixe.attribut"
    public static Object bindObject(Class<?> clazz, String prefix, HttpServletRequest request) throws Exception {
        Object instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "La classe " + clazz.getName() + " doit avoir un constructeur sans argument");
        }

        // On remonte la hiérarchie pour prendre aussi les attributs hérités
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }

                String value = request.getParameter(prefix + "." + field.getName());
                if (value == null || value.isEmpty()) {
                    continue;
                }

                Method setter = findSetter(clazz, field.getName());
                if (setter == null) {
                    continue;
                }

                Class<?> setterType = setter.getParameterTypes()[0];
                setter.invoke(instance, convert(value, setterType));
            }
        }
        return instance;
    }

    // Étape 2 : tableaux et collections de types simples
    public static Object bindMultiple(Parameter parameter, HttpServletRequest request) throws Exception {
        Class<?> type = parameter.getType();
        String[] values = request.getParameterValues(parameter.getName());

        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            checkElementType(componentType);

            List<String> kept = nonEmpty(values);
            Object array = Array.newInstance(componentType, kept.size());
            for (int i = 0; i < kept.size(); i++) {
                Array.set(array, i, convert(kept.get(i), componentType));
            }
            return array;
        }

        Class<?> elementType = getElementType(parameter);
        checkElementType(elementType);

        Collection<Object> collection = createCollection(type);
        for (String v : nonEmpty(values)) {
            collection.add(convert(v, elementType));
        }
        return collection;
    }

    // On ignore les valeurs vides (même règle que pour un champ simple vide)
    private static List<String> nonEmpty(String[] values) {
        List<String> kept = new ArrayList<>();
        if (values != null) {
            for (String v : values) {
                if (v != null && !v.isEmpty()) {
                    kept.add(v);
                }
            }
        }
        return kept;
    }

    private static void checkElementType(Class<?> elementType) {
        if (!isSimpleType(elementType)) {
            throw new IllegalArgumentException(
                    "Seuls les types simples sont pris en charge dans les listes et tableaux: "
                            + elementType.getName());
        }
    }

    // List<Integer> -> Integer ; liste sans type précisé -> String
    private static Class<?> getElementType(Parameter parameter) {
        Type generic = parameter.getParameterizedType();
        if (generic instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) generic).getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class) {
                return (Class<?>) args[0];
            }
        }
        return String.class;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> createCollection(Class<?> type) throws Exception {
        if (type.isAssignableFrom(ArrayList.class)) { // List, Collection...
            return new ArrayList<Object>();
        } else if (type.isAssignableFrom(LinkedHashSet.class)) { // Set, HashSet...
            return new LinkedHashSet<Object>();
        } else if (type.isAssignableFrom(TreeSet.class)) { // SortedSet, NavigableSet
            return new TreeSet<Object>();
        } else if (type.isAssignableFrom(LinkedList.class)) { // Queue, Deque
            return new LinkedList<Object>();
        } else if (!type.isInterface() && !java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            return (Collection<Object>) type.getDeclaredConstructor().newInstance(); // Vector...
        }
        throw new IllegalArgumentException("Type de collection non supporte: " + type.getName());
    }
}
