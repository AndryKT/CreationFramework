package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import model.ModelAndView;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import controller.Controller;
import controller.UrlMapping;
import controller.Utilitaire;
import model.Mapping;
import model.UrlMethod;

public class FrontControllerServlet extends HttpServlet {

    List<Class<?>> controllers;
    HashMap<UrlMethod, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() {
        // Lire la map des routes construite par AppStartupListener
        Object attr = getServletContext().getAttribute("urlMappings");

        if (attr != null && attr instanceof HashMap) {
            try {
                //noinspection unchecked
                urlMappings = (HashMap<UrlMethod, Mapping>) attr;
                return;
            } catch (ClassCastException e) {
                throw new RuntimeException("Attribut urlMappings invalide dans ServletContext", e);
            }
        }

        // Fallback: construire la map ici si le listener n'est pas présent
        try {
            controllers = Utilitaire.getClassesAnnotated(
                    "controller",
                    Controller.class);

            for (Class<?> c : controllers) {
                Method[] methods = c.getDeclaredMethods();

                for (Method m : methods) {

                    if (m.isAnnotationPresent(UrlMapping.class)) {

                        UrlMapping url = m.getAnnotation(UrlMapping.class);

                        UrlMethod key = new UrlMethod(
                                url.value(),
                                url.method());

                        if (urlMappings.containsKey(key)) {
                            throw new ServletException(
                                    "Route dupliquée : "
                                            + url.method() + " "
                                            + url.value());
                        }

                        urlMappings.put(
                                key,
                                new Mapping(c.getName(), m.getName()));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
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

    private void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String path = request.getPathInfo();

        if (path == null) {
            path = "/";
        }

        String httpMethod = request.getMethod();

        UrlMethod key = new UrlMethod(path, httpMethod);

        Mapping mapping = urlMappings.get(key);

        PrintWriter out = response.getWriter();

        if (mapping == null) {

            out.println("<h3>URL inconnue</h3>");
            out.println("URL demandée : " + path);
            out.println("<br>Méthode HTTP : " + httpMethod);
            out.println("<br><br>Routes disponibles :<br><br>");

            for (UrlMethod urlKey : urlMappings.keySet()) {

                Mapping m = urlMappings.get(urlKey);

                out.println(
                        urlKey.getMethod()
                                + " "
                                + urlKey.getUrl()
                                + "<br>"
                                + "Classe : " + m.getController()
                                + "<br>"
                                + "Méthode : " + m.getMethod()
                                + "<br><br>");
            }

            throw new ServletException("URL inconnue : " + path);
        }

        // Invocation du controller et traitement du ModelAndView si présent
        try {
            Class<?> controllerClass = Class.forName(mapping.getController());
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

            Method controllerMethod = controllerClass.getDeclaredMethod(mapping.getMethod());

            Object result = controllerMethod.invoke(controllerInstance);

            if (result != null && result instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) result;

                out.println("<h3>ModelAndView</h3>");
                out.println("Vue : " + mv.getView() + "<br>");
                out.println("Données : <br>");

                for (String keyAttr : mv.getData().keySet()) {
                    out.println(keyAttr + " : " + mv.getData().get(keyAttr) + "<br>");
                }

                return;
            }

            // Si la méthode ne retourne pas ModelAndView, afficher les infos basiques
            out.println(
                    "URL : " + path
                            + "<br>"
                            + "Méthode HTTP : " + httpMethod
                            + "<br>"
                            + "Classe : " + mapping.getController()
                            + "<br>"
                            + "Méthode : " + mapping.getMethod());

        } catch (ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e) {
            throw new ServletException("Erreur lors de l'invocation du controller", e);
        }
    }
}