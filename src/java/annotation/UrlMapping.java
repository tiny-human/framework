package annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMapping {
    String url();
    HttpMethod method() default HttpMethod.GET;

    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }
}
