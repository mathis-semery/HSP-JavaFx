package com.hsp.model;

public class ProduitFournisseur {
    private int id_produit;
    private int id_fournisseur;
    private double prix;

    public ProduitFournisseur(int id_produit, int id_fournisseur, double prix) {
        this.id_produit = id_produit;
        this.id_fournisseur = id_fournisseur;
        this.prix = prix;
    }

    public int getId_produit() {
        return id_produit;
    }

    public void setId_produit(int id_produit) {
        this.id_produit = id_produit;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getId_fournisseur() {
        return id_fournisseur;
    }

    public void setId_fournisseur(int id_fournisseur) {
        this.id_fournisseur = id_fournisseur;
    }

    @Override
    public String toString() {
        return "ProduitFournisseur{" +
                "id_produit=" + id_produit +
                ", id_fournisseur=" + id_fournisseur +
                ", prix=" + prix +
                '}';
    }
}
