package main.java.servlet; 

import jakarta.servlet.ServletException;        
import jakarta.servlet.http.HttpServlet;          
import jakarta.servlet.http.HttpServletRequest;   
import jakarta.servlet.http.HttpServletResponse;  
import java.io.IOException;

public class FrontControllerServlet extends HttpServlet {

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
        System.out.println("Path: " + path);
    }
}