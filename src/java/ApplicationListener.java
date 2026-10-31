package src.java;

import jakarta.servlet.*;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.context.WebApplicationContext;

public class ApplicationListener implements ServletContextListener {
    static final String SPRING_ROOT = WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;

    @Override
    public void contextInitialized(ServletContextEvent event) {

        ServletContext servletContext = event.getServletContext();

        String packageName = servletContext.getInitParameter("packageName");

        if (packageName == null || packageName.isEmpty()) {
            throw new RuntimeException("Package name is not specified.");
        }

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            List<String> classNames = Utilitaire.getClassInPackage(packageName, classLoader);

            Map<MapKey, Mapping> urlMappings = new HashMap<>();

            Utilitaire utilitaire = new Utilitaire();

            utilitaire.getControllers(classNames, "annotation.Controller", ElementType.TYPE, urlMappings);

            servletContext.setAttribute("urlMappings", urlMappings);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation", e);
        }

        servletContext.setAttribute("springContext", servletContext.getAttribute(SPRING_ROOT));

        String viewPrefix = servletContext.getInitParameter("viewPrefix");
        String viewSuffix = servletContext.getInitParameter("viewSuffix");

        String sessionUserName = servletContext.getInitParameter("session-user-name");

        String sessionRoleName = servletContext.getInitParameter("session-role-name");

        if (sessionUserName == null || sessionUserName.isBlank()) {
            throw new RuntimeException("Le context-param session-user-name n'est pas défini.");
        }

        if (sessionRoleName == null || sessionRoleName.isBlank()) {
            throw new RuntimeException("Le context-param session-role-name n'est pas défini.");
        }

        servletContext.setAttribute("session-user-name", sessionUserName);
        servletContext.setAttribute("session-role-name", sessionRoleName);

        servletContext.setAttribute("viewPrefix", viewPrefix);
        servletContext.setAttribute("viewSuffix", viewSuffix);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().log("Application arrêtée");
    }
}