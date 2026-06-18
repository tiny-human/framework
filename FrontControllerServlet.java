import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontControllerServlet extends HttpServlet {
    private List<String> controllers = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        String packageName = getServletContext().getInitParameter("packageName");

        if(packageName == null || packageName.isEmpty()){
            throw new ServletException("Package name is not specified in the servlet context parameters.");
        }

        try{
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            List<String> classNames = Utilitaire.getClassInPackage(packageName, classLoader);
            controllers = Utilitaire.getControllers(classNames, "annotation.Controller", ElementType.TYPE);
        } catch (Exception e) {
            throw new ServletException("Error initializing FrontControllerServlet: " + e.getMessage(), e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("Url de la requete: " + request.getRequestURL());

            for(String controller : controllers) {
                out.println("<br>Controller: " + controller);
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