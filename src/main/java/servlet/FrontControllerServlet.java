package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import model.ModelAndView;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import controller.Controller;
import controller.JsonResponse;
import controller.Param;
import controller.UrlMapping;
import controller.Utilitaire;
import model.Mapping;
import model.UrlMethod;

public class FrontControllerServlet extends HttpServlet {

    List<Class<?>> controllers;
    HashMap<UrlMethod, Mapping> urlMappings = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                            getServletContext().log("Route dupliquée ignorée : "
                                    + url.method() + " "
                                    + url.value());
                            continue;
                        }

                        System.out.println("AppStartupListener démarré");

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
        String servletPath = request.getServletPath();

        if (path == null || "/".equals(path)) {
            if (servletPath != null && !"".equals(servletPath) && !"/".equals(servletPath)) {
                path = servletPath;
            } else {
                String requestUri = request.getRequestURI();
                String contextPath = request.getContextPath();
                if (requestUri != null && contextPath != null && requestUri.startsWith(contextPath)) {
                    String suffix = requestUri.substring(contextPath.length());
                    if (suffix != null && !"".equals(suffix) && !"/".equals(suffix)) {
                        path = suffix;
                    } else {
                        path = "/";
                    }
                } else {
                    path = "/";
                }
            }
        }

        if (path.endsWith(".jsp") || path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
            request.getRequestDispatcher(path).forward(request, response);
            return;
        }

        String httpMethod = request.getMethod();

        UrlMethod key = new UrlMethod(path, httpMethod);

        Mapping mapping = urlMappings.get(key);

        if (mapping == null) {
            PrintWriter out = response.getWriter();

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

            Method controllerMethod = findControllerMethod(controllerClass, mapping, key);
            Object[] arguments = bindArguments(controllerMethod, request);

            Object result = controllerMethod.invoke(controllerInstance, arguments);

            if (controllerMethod.isAnnotationPresent(JsonResponse.class)) {
                if (result instanceof String) {
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().print(result);
                } else {
                    response.setContentType("application/json;charset=UTF-8");
                    objectMapper.writeValue(response.getWriter(), result);
                }
                return;
            }

            if (result != null && result instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) result;

                if (mv.getData().containsKey("html")) {
                    response.getWriter().println(mv.getData().get("html"));
                    return;
                }

                for (String keyAttr : mv.getData().keySet()) {
                    request.setAttribute(keyAttr, mv.getData().get(keyAttr));
                }

                String viewPath = mv.getView();
                if (viewPath == null || viewPath.isBlank()) {
                    response.getWriter().println("Vue introuvable");
                    return;
                }

                if (!viewPath.startsWith("/")) {
                    viewPath = "/" + viewPath;
                }

                request.getRequestDispatcher(viewPath).forward(request, response);
                return;
            }

            PrintWriter out = response.getWriter();

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

    private Method findControllerMethod(Class<?> controllerClass,
            Mapping mapping,
            UrlMethod requestedRoute) throws NoSuchMethodException {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (!method.getName().equals(mapping.getMethod())) {
                continue;
            }

            UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
            if (urlMapping != null
                    && urlMapping.value().equals(requestedRoute.getUrl())
                    && urlMapping.method().equalsIgnoreCase(requestedRoute.getMethod())) {
                return method;
            }
        }

        throw new NoSuchMethodException(
                "Méthode introuvable : " + mapping.getController() + "." + mapping.getMethod());
    }

    private Object[] bindArguments(Method controllerMethod,
            HttpServletRequest request) throws ServletException {
        Parameter[] parameters = controllerMethod.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            Param param = parameter.getAnnotation(Param.class);
            String parameterName = param != null ? param.value() : parameter.getName();
            String value = request.getParameter(parameterName);

            try {
                arguments[index] = convertValue(value, parameter.getType());
            } catch (IllegalArgumentException e) {
                throw new ServletException(
                        "Impossible de convertir le paramètre '"
                                + parameterName + "' vers " + parameter.getType().getSimpleName(),
                        e);
            }
        }

        return arguments;
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (value == null) {
            if (!targetType.isPrimitive()) {
                return null;
            }
            if (targetType == boolean.class) {
                return false;
            }
            if (targetType == char.class) {
                return '\0';
            }
            if (targetType == byte.class) {
                return (byte) 0;
            }
            if (targetType == short.class) {
                return (short) 0;
            }
            if (targetType == int.class) {
                return 0;
            }
            if (targetType == long.class) {
                return 0L;
            }
            if (targetType == float.class) {
                return 0F;
            }
            if (targetType == double.class) {
                return 0D;
            }
        }

        if (targetType == String.class) return value;
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.valueOf(value);
        if (targetType == char.class || targetType == Character.class) return value.charAt(0);
        if (targetType == byte.class || targetType == Byte.class) return Byte.valueOf(value);
        if (targetType == short.class || targetType == Short.class) return Short.valueOf(value);
        if (targetType == int.class || targetType == Integer.class) return Integer.valueOf(value);
        if (targetType == long.class || targetType == Long.class) return Long.valueOf(value);
        if (targetType == float.class || targetType == Float.class) return Float.valueOf(value);
        if (targetType == double.class || targetType == Double.class) return Double.valueOf(value);
        if (targetType.isEnum()) {
            return Enum.valueOf(targetType.asSubclass(Enum.class), value);
        }

        throw new IllegalArgumentException("Type non supporté");
    }
}