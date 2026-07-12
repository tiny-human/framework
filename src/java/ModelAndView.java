package src.java;

import java.util.HashMap;

public class ModelAndView {

    private HashMap<String, Object> attributes;
    private String viewName;

    public ModelAndView() {
        this.attributes = new HashMap<>();
    }

    public ModelAndView(String viewName) {
        this.attributes = new HashMap<>();
        this.viewName = viewName;
    }

    public void addAttribute(String nom, Object valeur) {
        attributes.put(nom, valeur);
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}