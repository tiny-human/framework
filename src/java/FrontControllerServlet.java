package src.java;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontControllerServlet extends HttpServlet {
    private List<String> controllers = new ArrayList<>();
    private Map<MapKey, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        String packageName = getInitParameter("packageName");

        if(packageName == null || packageName.isEmpty()){
            throw new ServletException("Package name is not specified in the servlet context parameters.");
        }

        try{
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            List<String> classNames = Utilitaire.getClassInPackage(packageName, classLoader);
            controllers = Utilitaire.getControllers(classNames, "annotation.Controller", ElementType.TYPE);

            for(String controller : controllers){
                Class<?> clazz = Class.forName(controller);
                for(java.lang.reflect.Method method : clazz.getDeclaredMethods()){
                    if(method.isAnnotationPresent(annotation.UrlMapping.class)){
                        String url = method.getAnnotation(annotation.UrlMapping.class).url();
                        String httpMethod = method.getAnnotation(annotation.UrlMapping.class).method().name();

                        MapKey key = new MapKey(url, httpMethod);
                        if(urlMappings.containsKey(key)){
                            throw new ServletException("Duplicate URL mapping found for URL: " + url);
                        }
                        urlMappings.put(key, new Mapping(controller, method.getName()));
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Error initializing FrontControllerServlet: " + e.getMessage(), e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            // out.println("Url de la requete: " + request.getRequestURL());

            // for(String controller : controllers) {
            //     out.println("<br>Controller: " + controller);
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String url = uri.substring(contextPath.length());   
            String httpMethod = request.getMethod();

            MapKey key = new MapKey(url, httpMethod);
            
            if (urlMappings.containsKey(key)) {
                Mapping mapping = urlMappings.get(key);
                // out.println(mapping.getClassName() + " - " + mapping.getMethodName());
                try {
                    Class<?> clazz = Class.forName(mapping.getClassName());
                    Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                    Method method = clazz.getDeclaredMethod(mapping.getMethodName());
                    Object result = method.invoke(controllerInstance);
                    out.println(result !=null ? result.toString() : "");
                } catch (Exception e) {
                    throw new ServletException("Error invoking controller method: " + e.getMessage(), e);
                }
            }else{ 
                out.println("<h4>l'url " + url + " n'est pas reconnue</h4>");
                out.println("les urls reconnues: ");
                for (MapKey mappedKey : urlMappings.keySet()) {
                    out.println("<br>" + mappedKey.getUrl());
                    out.println("<br>class: " + urlMappings.get(mappedKey).getClassName());
                    out.println("<br>method: " + urlMappings.get(mappedKey).getMethodName() + "<br>");
                }
                
            }
            
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}