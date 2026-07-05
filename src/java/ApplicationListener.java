package src.java;

import jakarta.servlet.*;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {

        ServletContext servletContext = event.getServletContext();

        String packageName = servletContext.getInitParameter("packageName");

        if (packageName == null || packageName.isEmpty()) {
            throw new RuntimeException("Package name is not specified.");
        }

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            List<String> classNames =
                    Utilitaire.getClassInPackage(packageName, classLoader);

            Map<MapKey, Mapping> urlMappings = new HashMap<>();

            Utilitaire utilitaire = new Utilitaire();

            utilitaire.getControllers(
                    classNames,
                    "annotation.Controller",
                    ElementType.TYPE,
                    urlMappings);

            servletContext.setAttribute("urlMappings", urlMappings);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().log("Application arrêtée");
    }
}