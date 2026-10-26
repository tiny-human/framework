package src.java;

public class MapFile {
    private String nom;        // sans l'extension
    private String extension;  // sans le point, vide s'il n'y en a pas
    private byte[] data;

    public MapFile() {
    }

    public MapFile(String nom, String extension, byte[] data) {
        this.nom = nom;
        this.extension = extension;
        this.data = data;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    // Nom complet reconstitué (nom + extension)
    public String getNomComplet() {
        if (extension == null || extension.isEmpty()) {
            return nom;
        }
        return nom + "." + extension;
    }
}