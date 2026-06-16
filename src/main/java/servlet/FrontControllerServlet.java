package servlet; 

import jakarta.servlet.ServletException;        
import jakarta.servlet.http.HttpServlet;          
import jakarta.servlet.http.HttpServletRequest;   
import jakarta.servlet.http.HttpServletResponse;  
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FrontControllerServlet extends HttpServlet {

    @Deprecated
    public void  init(){
        List<String> listeClasseAnnoter = new ArrayList<>();

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
    }
}