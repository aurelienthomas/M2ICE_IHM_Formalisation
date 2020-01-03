package main;

public class Figure {
    private String nom;
    private int x;
    private int y;
    private int longueur;
    private int hauteur;
    private String couleurFond;
    private String couleurContour;

    public Figure(String nom, int x, int y, int longueur, int hauteur, String couleurFond, String couleurContour) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.longueur = longueur;
        this.hauteur = hauteur;
        this.couleurFond = couleurFond;
        this.couleurContour = couleurContour;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getLongueur() {
        return longueur;
    }

    public void setLongueur(int longueur) {
        this.longueur = longueur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public void setHauteur(int hauteur) {
        this.hauteur = hauteur;
    }

    public String getCouleurFond() {
        return couleurFond;
    }

    public void setCouleurFond(String couleurFond) {
        this.couleurFond = couleurFond;
    }

    public String getCouleurContour() {
        return couleurContour;
    }

    public void setCouleurContour(String couleurContour) {
        this.couleurContour = couleurContour;
    }
}
