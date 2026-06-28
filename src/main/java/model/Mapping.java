package model;

public class Mapping {

    private String controller;
    private String method;

    public Mapping(String controller, String method) {
        this.controller = controller;
        this.method = method;
    }

    public String getController() {
        return controller;
    }

    public String getMethod() {
        return method;
    }
}
