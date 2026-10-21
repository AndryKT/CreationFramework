package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

import model.ModelAndView;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import controller.Controller;
import controller.JsonResponse;
import controller.Param;
import controller.UrlMapping;
import controller.Utilitaire;
import model.Mapping;
import model.UrlMethod;
import model.MapFile;
import model.MySession;

@MultipartConfig
public class FrontControllerServlet extends HttpServlet {

    private static final String MY_SESSION_ATTRIBUTE ="__creation_framework_my_session";

    List<Class<?>> controllers;
    HashMap<UrlMethod, Mapping> urlMappings = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path uploadDirectory;

    @Override
    public void init() {
        String configuredUploadPath = getServletContext().getInitParameter("upload.path");
        if (configuredUploadPath != null && !configuredUploadPath.isBlank()) {
            uploadDirectory = Paths.get(configuredUploadPath).toAbsolutePath().normalize();
        }

        // Lire la map des routes construite par AppStartupListener
        Object attr = getServletContext().getAttribute("urlMappings");

        if (attr != null && attr instanceof HashMap) {
            try {
                // noinspection unchecked
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

            synchronizeMySession(request);

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

            if (parameter.getType() == MySession.class) {
                arguments[index] = getMySession(request);
                continue;
            }

            Param param = parameter.getAnnotation(Param.class);
            String parameterName = param != null ? param.value() : parameter.getName();

            try {
                if (isSimpleType(parameter.getType())) {
                    String value = request.getParameter(parameterName);
                    arguments[index] = convertValue(value, parameter.getType());
                } else {
                    arguments[index] = bindObject(parameter.getType(), request);
                }
            } catch (IllegalArgumentException e) {
                throw new ServletException(
                        "Impossible de convertir le paramètre '"
                                + parameterName + "' vers " + parameter.getType().getSimpleName(),
                        e);
            } catch (ReflectiveOperationException e) {
                throw new ServletException(
                        "Impossible de construire l'objet "
                                + parameter.getType().getName(),
                        e);
            }
        }

        return arguments;
    }

    
    // Retourne l'unique MySession associee a la session HTTP courante.
    private MySession getMySession(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(true);
        synchronized (httpSession) {
            Object value = httpSession.getAttribute(MY_SESSION_ATTRIBUTE);

            if (value instanceof MySession) {
                return (MySession) value;
            }

            MySession session = new MySession();
            httpSession.setAttribute(MY_SESSION_ATTRIBUTE, session);
            return session;
        }
    }

    // Mise à jour de MySession
    private void synchronizeMySession(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            return;
        }

        Object value = httpSession.getAttribute(MY_SESSION_ATTRIBUTE);
        if (value instanceof MySession) {
            httpSession.setAttribute(MY_SESSION_ATTRIBUTE, value);
        }
    }

    // Construit automatiquement un objet à partir des paramètres HTTP
    // Les noms des paramètres doivent correspondre aux noms des attributs

    private Object bindObject(Class<?> objectType,
            HttpServletRequest request) throws ReflectiveOperationException {
        Object object = createObject(objectType);
        // populateObject(object, request);
        
        populateObject(object, request, "");

        return object;
    }

    // Crée l'objet avec son constructeur sans argument
    private Object createObject(Class<?> objectType)
            throws ReflectiveOperationException {
        return objectType.getDeclaredConstructor().newInstance();
    }

  
    private void populateObject(Object object,
            HttpServletRequest request, String prefix)
            throws ReflectiveOperationException {

        Class<?> objectType = object.getClass();
        for (Field field : objectType.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            String fieldName = prefix + field.getName();
            Class<?> fieldType = field.getType();

            if (fieldType == MapFile.class) {
                if (!isMultipartRequest(request)) {
                    continue;
                }

                try {
                    MapFile uploadedFile = convertPartToMapFile(request.getPart(fieldName));
                    if (uploadedFile != null) {
                        saveUploadedFile(uploadedFile);
                        field.set(object, uploadedFile);
                    }
                } catch (IOException | ServletException e) {
                    throw new ReflectiveOperationException(
                            "Impossible de récupérer le fichier '" + fieldName + "'", e);
                }
                continue;
            }

            if (isSimpleType(fieldType)) {
                String value = request.getParameter(fieldName);
                if (value == null) {
                    continue;
                }
                try {
                    Object convertedValue = convertValue(value, fieldType);
                    field.set(object, convertedValue);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Impossible de convertir le champ '" + fieldName + "' vers " + fieldType.getSimpleName(),e);
                }

            } else {
                String nestedPrefix = fieldName + ".";
                if (!hasParameters(request, nestedPrefix)) {
                    continue;
                }

                try {
                    Object nestedObject = createObject(fieldType);
                    populateObject(
                            nestedObject,
                            request,
                            nestedPrefix);

                    field.set(object, nestedObject);

                } catch (ReflectiveOperationException e) {
                    throw e;
                }
            }
        }
    }

    // Vérifie si la requête contient des paramètres pour l'objet imbriqué
    private boolean hasParameters(HttpServletRequest request, String prefix) {

        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();

            if (name.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    // Indique si le type correspond à un paramètre simple
    private boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == boolean.class || type == Boolean.class
                || type == char.class || type == Character.class
                || type == byte.class || type == Byte.class
                || type == short.class || type == Short.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == float.class || type == Float.class
                || type == double.class || type == Double.class
                || type.isEnum();
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

        if (targetType == String.class)
            return value;
        if (targetType == boolean.class || targetType == Boolean.class)
            return Boolean.valueOf(value);
        if (targetType == char.class || targetType == Character.class)
            return value.charAt(0);
        if (targetType == byte.class || targetType == Byte.class)
            return Byte.valueOf(value);
        if (targetType == short.class || targetType == Short.class)
            return Short.valueOf(value);
        if (targetType == int.class || targetType == Integer.class)
            return Integer.valueOf(value);
        if (targetType == long.class || targetType == Long.class)
            return Long.valueOf(value);
        if (targetType == float.class || targetType == Float.class)
            return Float.valueOf(value);
        if (targetType == double.class || targetType == Double.class)
            return Double.valueOf(value);
        if (targetType.isEnum()) {
            return Enum.valueOf(targetType.asSubclass(Enum.class), value);
        }

        throw new IllegalArgumentException("Type non supporté");
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private MapFile convertPartToMapFile(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        String submittedName = part.getSubmittedFileName();
        String fileName = submittedName == null ? "" : Paths.get(submittedName).getFileName().toString();

        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex + 1);
        }

        byte[] bytes = part.getInputStream().readAllBytes();

        MapFile mapFile = new MapFile();
        mapFile.setNameFile(fileName);
        mapFile.setExtensionFile(extension);
        mapFile.setContentFile(bytes);
        mapFile.setContentType(part.getContentType());

        return mapFile;
    }
    
    private void saveUploadedFile(MapFile mapFile) throws IOException, ServletException {
        if (uploadDirectory == null) {
            throw new ServletException(
                    "Aucun chemin d'upload configure. Definissez le parametre de contexte 'upload.path'.");
        }

        Files.createDirectories(uploadDirectory);
        String safeFileName = Paths.get(mapFile.getNameFile()).getFileName().toString();
        Path destination = uploadDirectory.resolve(safeFileName).normalize();

        if (!destination.getParent().equals(uploadDirectory)) {
            throw new ServletException("Nom de fichier invalide : " + mapFile.getNameFile());
        }

        Files.write(destination, mapFile.getContentFile());
    }
}
