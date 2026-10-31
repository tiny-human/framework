package src.java;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import annotation.WebAPI;
import org.springframework.web.context.WebApplicationContext;
import com.google.gson.Gson;
import java.util.Collection;
import jakarta.servlet.http.HttpSession;
import annotation.Secured;

public class FrontControllerServlet extends HttpServlet {

    private Map<MapKey, Mapping> urlMappings;
    private String viewPrefix;
    private String viewSuffix;
    private Map<Class<?>, Object> controllersInstance;
    private String sessionUserName;
    private String sessionRoleName;
    private WebApplicationContext springContext;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {

        urlMappings = (Map<MapKey, Mapping>) getServletContext().getAttribute("urlMappings");

        if (urlMappings == null) {
            throw new ServletException(
                    "Les URL mappings n'ont pas été initialisés.");
        }

        controllersInstance = (Map<Class<?>, Object>) getServletContext().getAttribute("controllersInstance");

        if (controllersInstance == null) {
            throw new ServletException("La table controllersInstance n'a pas été initialisée.");
        }

        viewPrefix = (String) getServletContext().getAttribute("viewPrefix");
        viewSuffix = (String) getServletContext().getAttribute("viewSuffix");

        if (viewPrefix == null || viewSuffix == null) {
            throw new ServletException(
                    "viewPrefix / viewSuffix ne sont pas définis dans web.xml.");
        }

        springContext = (WebApplicationContext) getServletContext().getAttribute("springContext");

        sessionUserName = (String) getServletContext().getAttribute("session-user-name");

        sessionRoleName = (String) getServletContext().getAttribute("session-role-name");

        if (sessionUserName == null || sessionRoleName == null) {
            throw new ServletException("Les noms des attributs de session ne sont pas initialisés.");
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = uri.substring(contextPath.length());
        String httpMethod = request.getMethod();

        MapKey key = new MapKey(url, httpMethod);

        if (!urlMappings.containsKey(key)) {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("<h4>l'url " + url + " n'est pas reconnue</h4>");
                out.println("les urls reconnues: ");
                for (MapKey mappedKey : urlMappings.keySet()) {
                    out.println("<br>" + mappedKey.getUrl());
                    out.println("<br>class: " + urlMappings.get(mappedKey).getClassName());
                    out.println("<br>method: " + urlMappings.get(mappedKey).getMethodName() + "<br>");
                }
            }
            return;
        }

        Mapping mapping = urlMappings.get(key);

        try {
            Class<?> clazz = Class.forName(mapping.getClassName());

            Object controllerInstance = controllersInstance.get(clazz);

            if (controllerInstance == null) {
                synchronized (controllersInstance) {
                    controllerInstance = controllersInstance.get(clazz);

                    if (controllerInstance == null) {
                        try {
                            controllerInstance = clazz.getDeclaredConstructor().newInstance();
                        } catch (ReflectiveOperationException e) {
                            throw new ServletException("Impossible de créer le contrôleur " + clazz.getName()
                                    + ". Il doit avoir un constructeur " + "sans argument.", e);
                        }
                        controllersInstance.put(clazz, controllerInstance);
                    }
                }
            }
            // Method method;
            // try {
            // method = clazz.getDeclaredMethod(mapping.getMethodName());
            // } catch (NoSuchMethodException e) {
            // method = clazz.getDeclaredMethod(mapping.getMethodName(),
            // WebApplicationContext.class);
            // }
            Method method = null;
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(mapping.getMethodName())) {
                    method = m;
                    break;
                }
            }
            if (method == null) {
                throw new NoSuchMethodException(mapping.getMethodName());
            }

            if (!checkAuthorization(method, clazz, request, response)) {
                return;
            }
            // if (Utilitaire.haveParameter(method, WebApplicationContext.class)) {
            // if (springContext == null) {
            // throw new ServletException("springContext non initialise");
            // }
            // result = method.invoke(controllerInstance, springContext);

            // } else {
            // result = method.invoke(controllerInstance);
            // }

            // Parameter[] params = method.getParameters();
            // Object[] args = new Object[params.length];

            // for (int i = 0; i < params.length; i++) {
            // Parameter parameter = params[i];
            // String name = parameter.getName();
            // Class<?> type = parameter.getType();

            // String value = request.getParameter(name);

            if (Utilitaire.haveParameter(method, WebApplicationContext.class) && springContext == null) {
                throw new ServletException("springContext non initialise");
            }

            Parameter[] params = method.getParameters();
            Object[] args = new Object[params.length];

            // for (int i = 0; i < params.length; i++) {
            // Parameter parameter = params[i];
            // Class<?> type = parameter.getType();

            // if (type.equals(WebApplicationContext.class)) {
            // args[i] = springContext;
            // continue;
            // }

            // String name = parameter.getName();
            // String value = request.getParameter(name);

            // if (value == null || value.isEmpty()) {
            // if (type.isPrimitive()) {
            // throw new IllegalArgumentException("Un argument obligatoire manque: " +
            // name);
            // }
            // args[i] = null;
            // } else {
            // if (type.equals(String.class)) {
            // args[i] = value;
            // } else if (type.equals(int.class) || type.equals(Integer.class)) {
            // args[i] = Integer.parseInt(value);
            // } else if (type.equals(long.class) || type.equals(Long.class)) {
            // args[i] = Long.parseLong(value);
            // } else if (type.equals(double.class) || type.equals(Double.class)) {
            // args[i] = Double.parseDouble(value);
            // } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            // args[i] = Boolean.parseBoolean(value);
            // } else {
            // throw new IllegalArgumentException("Type de parametre non supporte: " +
            // type.getName());
            // }
            // }
            // }
            // for (int i = 0; i < params.length; i++) {
            // Parameter parameter = params[i];
            // Class<?> type = parameter.getType();
            // String name = parameter.getName();

            // if (type.equals(WebApplicationContext.class)) {
            // args[i] = springContext;

            // } else if (Utilitaire.isSimpleType(type)) {
            // String value = request.getParameter(name);
            // if (value == null || value.isEmpty()) {
            // if (type.isPrimitive()) {
            // throw new IllegalArgumentException("Un argument obligatoire manque: " +
            // name);
            // }
            // args[i] = null;
            // } else {
            // args[i] = Utilitaire.convert(value, type);
            // }

            // } else {
            // args[i] = Utilitaire.bindObject(type, name, request);
            // }
            // }

            ModelAndView injectedMav = null;
            boolean responseInjected = false;
            MaSession maSession = null;

            for (int i = 0; i < params.length; i++) {
                Parameter parameter = params[i];
                Class<?> type = parameter.getType();
                String name = parameter.getName();

                if (type.equals(WebApplicationContext.class)) {
                    args[i] = springContext;

                } else if (type.equals(HttpServletRequest.class)) {
                    args[i] = request;

                } else if (type.equals(HttpServletResponse.class)) {
                    args[i] = response;
                    responseInjected = true;

                } else if (type.equals(HttpSession.class)) {
                    args[i] = request.getSession();

                } else if (type.equals(MaSession.class)) {
                    // Une seule copie partagée, même si la méthode déclare plusieurs MaSession
                    if (maSession == null) {
                        maSession = MaSession.from(request.getSession(true));
                    }
                    args[i] = maSession;

                } else if (type.equals(ModelAndView.class)) {
                    injectedMav = new ModelAndView();
                    args[i] = injectedMav;

                } else if (type.equals(MapFile.class)) {
                    args[i] = Utilitaire.readMapFile(request, name);

                } else if (Utilitaire.isSimpleType(type)) {
                    String value = request.getParameter(name);
                    if (value == null || value.isEmpty()) {
                        if (type.isPrimitive()) {
                            throw new IllegalArgumentException("Un argument obligatoire manque: " + name);
                        }
                        args[i] = null;
                    } else {
                        args[i] = Utilitaire.convert(value, type);
                    }

                } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
                    args[i] = Utilitaire.bindMultiple(parameter, request);

                } else {
                    args[i] = Utilitaire.bindObject(type, name, request);
                }
            }
            Object result = method.invoke(controllerInstance, args);

            // Recopie dans la vraie session AVANT le forward : la JSP lit la vraie session
            if (maSession != null) {
                HttpSession realSession = request.getSession(false);
                if (realSession != null) {
                    maSession.flushTo(realSession);
                }
            }

            // Le développeur a rempli le ModelAndView reçu en paramètre sans le renvoyer
            if (result == null && injectedMav != null && injectedMav.getViewName() != null) {
                result = injectedMav;
            }

            // Il a écrit lui-même dans la réponse : on n'y touche plus
            if (result == null && responseInjected) {
                return;
            }

            if (method.isAnnotationPresent(WebAPI.class)) {
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    if (result instanceof String) {
                        out.print(result);
                    } else {
                        out.print(gson.toJson(result));
                    }
                }
                return;
            } else if (result instanceof ModelAndView mv) {
                // On pose les attributs du modele sur la requete
                for (Map.Entry<String, Object> entry : mv.getAttributes().entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }

                String page = viewPrefix + mv.getViewName() + viewSuffix;
                request.getRequestDispatcher(page).forward(request, response);

            } else {
                // Comportement d'origine conserve pour les controleurs qui ne renvoient pas de
                // ModelAndView
                response.setContentType("text/html;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.println(result != null ? result.toString() : "");
                }
            }

        } catch (Exception e) {
            throw new ServletException("Error invoking controller method: " + e.getMessage(), e);
        }
    }

    private boolean checkAuthorization(Method method, Class<?> controllerClass, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Secured secured = method.getAnnotation(Secured.class);

        if (secured == null) {
            secured = controllerClass.getAnnotation(Secured.class);
        }

        if (secured == null) {
            return true;
        }

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(sessionUserName) == null) {
            handleUnauthorized(request, response);
            return false;
        }

        String[] requiredRoles = secured.roles();

        if (requiredRoles.length == 0) {
            return true;
        }

        Object sessionRoles = session.getAttribute(sessionRoleName);

        if (!hasRequiredRole(sessionRoles, requiredRoles)) {
            handleForbidden(request, response);
            return false;
        }

        return true;
    }

    private boolean containsRole(String sessionRole, String[] requiredRoles) {
        for (String requiredRole : requiredRoles) {
            if (requiredRole.equals(sessionRole)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRequiredRole(Object sessionRoles, String[] requiredRoles) {
        if (sessionRoles == null) {
            return false;
        }

        if (sessionRoles instanceof String) {
            return containsRole((String) sessionRoles, requiredRoles);
        }

        if (sessionRoles instanceof Collection<?>) {
            for (Object role : (Collection<?>) sessionRoles) {
                if (role instanceof String && containsRole((String) role, requiredRoles)) {
                    return true;
                }
            }
            return false;
        }

        if (sessionRoles.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(sessionRoles);
            for (int i = 0; i < length; i++) {
                Object role = java.lang.reflect.Array.get(sessionRoles, i);
                if (role instanceof String && containsRole((String) role, requiredRoles)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (acceptsJson(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Authentification requise\"}");
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("<h1>401 - Authentification requise</h1>");
    }

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (acceptsJson(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Droits insuffisants\"}");
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("<h1>403 - Droits insuffisants</h1>");
    }

    private boolean acceptsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null&& accept.toLowerCase().contains("application/json");
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