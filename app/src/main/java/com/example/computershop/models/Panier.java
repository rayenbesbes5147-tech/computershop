package com.example.computershop.models;

public class Panier {
    private String numPanier;
    private String idArt;
    private String idInt;
    private int quantite;
    private String emballage;

    public Panier() {}

    public Panier(String numPanier, String idArt, String idInt, int quantite, String emballage) {
        this.numPanier = numPanier;
        this.idArt = idArt;
        this.idInt = idInt;
        this.quantite = quantite;
        this.emballage = emballage;
    }

    public String getNumPanier() { return numPanier; }
    public void setNumPanier(String numPanier) { this.numPanier = numPanier; }

    public String getIdArt() { return idArt; }
    public void setIdArt(String idArt) { this.idArt = idArt; }

    public String getIdInt() { return idInt; }
    public void setIdInt(String idInt) { this.idInt = idInt; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public String getEmballage() { return emballage; }
    public void setEmballage(String emballage) { this.emballage = emballage; }
}
