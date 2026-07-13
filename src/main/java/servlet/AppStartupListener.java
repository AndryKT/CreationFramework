package servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import controller.Controller;
import controller.UrlMapping;
import controller.Utilitaire;
import model.Mapping;
import model.UrlMethod;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        try {
            List<Class<?>> controllers = Utilitaire.getClassesAnnotated(
                    "controller",
                    Controller.class);

            HashMap<UrlMethod, Mapping> urlMappings = new HashMap<>();

            for (Class<?> c : controllers) {
                Method[] methods = c.getDeclaredMethods();

                for (Method m : methods) {
                    if (m.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping url = m.getAnnotation(UrlMapping.class);

                        UrlMethod key = new UrlMethod(
                                url.value(),
                                url.method());

                        if (urlMappings.containsKey(key)) {
                            ctx.log("Route dupliquée ignorée : "
                                    + url.method() + " "
                                    + url.value());
                            continue;
                        }

                        ctx.log("AppStartupListener démarré");
                        System.out.println("AppStartupListener démarré");

                        urlMappings.put(
                                key,
                                new Mapping(c.getName(), m.getName()));
                    }
                }
            }

            System.out.println("AppStartupListener démarré");

            ctx.setAttribute("urlMappings", urlMappings);

            ctx.log("Routes chargées : " + urlMappings.size() + " routes");

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation des routes", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to do
    }

}
