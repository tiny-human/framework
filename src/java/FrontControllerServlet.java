package src.java;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import annotation.Controller;

public class FrontControllerServlet extends HttpServlet {
    // private List<String> controllers = new ArrayList<>();
    private Map<MapKey, Mapping> urlMappings ;

    @Override
    public void init() throws ServletException {

        urlMappings = (Map<MapKey, Mapping>) getServletContext().getAttribute("urlMappings");

        if (urlMappings == null) {
            throw new ServletException(
                    "Les URL mappings n'ont pas été initialisés.");
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            // out.println("Url de la requete: " + request.getRequestURL());

            // for(String controller : controllers) {
            // out.println("<br>Controller: " + controller);
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
                    out.println(result != null ? result.toString() : "");
                } catch (Exception e) {
                    throw new ServletException("Error invoking controller method: " + e.getMessage(), e);
                }
            } else {
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