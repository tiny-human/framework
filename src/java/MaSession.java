package src.java;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

public class MaSession {
    private String id;
    private Map<String, Object> attributes = new HashMap<>();
    private Set<String> initialKeys = new HashSet<>();

    public MaSession() {
    }

    // Copie le contenu de la vraie session
    public static MaSession from(HttpSession httpSession) {
        MaSession session = new MaSession();
        session.id = httpSession.getId();

        Enumeration<String> names = httpSession.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = httpSession.getAttribute(name);
            if (value != null) {
                session.attributes.put(name, value);
                session.initialKeys.add(name);
            }
        }
        return session;
    }

    // Recopie les changements dans la vraie session
    public void flushTo(HttpSession httpSession) {
        // 1. Les clés présentes au départ et retirées depuis sont supprimées
        for (String key : initialKeys) {
            if (!attributes.containsKey(key)) {
                httpSession.removeAttribute(key);
            }
        }
        // 2. Tous les attributs actuels sont écrits
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public String getId() {
        return id;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    // Une valeur null retire la clé, comme sur une vraie HttpSession
    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }
}