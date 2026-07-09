package model;

public class UrlMethod {
    private String url;
    private String method;

    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UrlMethod)) return false;

        UrlMethod other = (UrlMethod) obj;

        return url.equals(other.url)
                && method.equals(other.method);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(url, method);
    }
}