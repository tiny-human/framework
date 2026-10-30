package src.java;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nom;
    private String[] role;

    public User() {
    }

    public User(String nom, String... role) {
        this.nom = nom;
        this.role = role;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String[] getRole() { return role; }
    public void setRole(String[] role) { this.role = role; }

    // Vrai si l'utilisateur possède au moins un des rôles demandés
    public boolean hasAnyRole(String[] wanted) {
        if (role == null || wanted == null) {
            return false;
        }
        for (String r : role) {
            for (String w : wanted) {
                if (r != null && r.equals(w)) {
                    return true;
                }
            }
        }
        return false;
    }
}