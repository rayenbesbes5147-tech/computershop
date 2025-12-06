package com.example.computershop.models;

public class Article {
    private String idArt;
    private String libArt;
    private double prixArt;
    private String catArt;

    public Article() {}

    public Article(String idArt, String libArt, double prixArt, String catArt) {
        this.idArt = idArt;
        this.libArt = libArt;
        this.prixArt = prixArt;
        this.catArt = catArt;
    }

    public String getIdArt() { return idArt; }
    public void setIdArt(String idArt) { this.idArt = idArt; }

    public String getLibArt() { return libArt; }
    public void setLibArt(String libArt) { this.libArt = libArt; }

    public double getPrixArt() { return prixArt; }
    public void setPrixArt(double prixArt) { this.prixArt = prixArt; }

    public String getCatArt() { return catArt; }
    public void setCatArt(String catArt) { this.catArt = catArt; }
}
