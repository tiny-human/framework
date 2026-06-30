package src.java;

import java.util.Objects;

import annotation.UrlMapping.HttpMethod;

public class MapKey {
    private String url;
    private String method;

    public MapKey(String url, String method) {
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
        if(!(obj instanceof MapKey)) return false;
        MapKey other = (MapKey) obj;
        return url.equals(other.url) && method.equals(other.method);    
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }
}
