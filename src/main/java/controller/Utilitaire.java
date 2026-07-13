package controller;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utilitaire {

    public static List<Class<?>> getClassesAnnotated(
            String packageName,
            Class<? extends Annotation> annotation)
            throws Exception {

        List<Class<?>> result = new ArrayList<>();

        // controller -> controller/
        String path = packageName.replace('.', '/');

        // Récupérer le ClassLoader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Trouver le dossier correspondant au package
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            throw new RuntimeException(
                    "Package introuvable : " + packageName);
        }

        File folder = new File(resource.toURI());

        File[] files = folder.listFiles();

        if (files == null) {
            return result;
        }

        for (File file : files) {

            String fileName = file.getName();

            if (fileName.endsWith(".class")) {

                // Enlever .class
                String className =
                        packageName + "."
                        + fileName.substring(
                                0,
                                fileName.length() - 6);

                // Charger la classe
                Class<?> c = Class.forName(className);

                // Vérifier l'annotation
                if (c.isAnnotationPresent(annotation)) {

                    result.add(c);

                }

            }

        }

        return result;
    }

}