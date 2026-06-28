package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import model.Mapping;
import controller.Controller;
import controller.UrlMapping;
import controller.Utilitaire;

public class FrontControllerServlet extends HttpServlet {

    List<Class<?>> controllers;
    HashMap<String, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() {
        try {
            controllers = Utilitaire.getClassesAnnotated(
                    "controller",
                    Controller.class);

            for (Class<?> c : controllers) {
                Method[] methods = c.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping url = m.getAnnotation(UrlMapping.class);
                        urlMappings.put(url.value(), new Mapping(c.getName(), m.getName()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        Mapping mapping = urlMappings.get(path);

        if (path == null) {
            path = "/";
        }

        PrintWriter out = response.getWriter();
        // out.println(path);

        if (mapping == null) {
            out.println("URL inconnue : " + path);
            out.println("URLs connues :");

            for (String url : urlMappings.keySet()) {

                Mapping m = urlMappings.get(url);

                out.println(
                        url
                                + " <br> "
                                + "Classe : " + m.getController()
                                + " <br> "
                                + "Methode : " + m.getMethod()
                );

            }

            throw new ServletException("URL inconnue : " + path);
        }

        out.println(

                path
                        + " <br> "
                        + "Classe : " + mapping.getController()
                        + " <br> "
                        + "Methode : " + mapping.getMethod()

        );
        response.setContentType("text/html;charset=UTF-8");

        // for (Class<?> c : controllers) {
            // out.println(c.getName());
        // }

    }
}