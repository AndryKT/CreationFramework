package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import controller.Controller;
import controller.Utilitaire;

public class FrontControllerServlet extends HttpServlet {

    List<Class<?>> controllers;

    @Override
    public void init() {

        try {
            controllers = Utilitaire.getClassesAnnotated(
                    "controller",
                    Controller.class);

            for (Class<?> c : controllers) {

                System.out.println(
                        "Controller trouvé : "
                                + c.getName());
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
        if (path == null) {
            path = "/";
        }
        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();
        out.println(path);

        for (Class<?> c : controllers) {
            out.println(c.getName());
        }

    }
}