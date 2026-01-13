package com.hsp.model;

import java.time.LocalDateTime;

public class Produit {

    private int id_produit;
    private String libelle;
    private String description;
    private int niveau_dangerosite;
    private int quantite_stock;
    private LocalDateTime date_creation;

    public Produit(int id_produit, String libelle, String description, int niveau_dangerosite, int quantite_stock, LocalDateTime date_creation) {
        this.id_produit = id_produit;
        this.libelle = libelle;
        this.description = description;
        this.niveau_dangerosite = niveau_dangerosite;
        this.quantite_stock = quantite_stock;
        this.date_creation = date_creation;
    }

    public int getId_produit() {
        return id_produit;
    }

    public void setId_produit(int id_produit) {
        this.id_produit = id_produit;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNiveau_dangerosite() {
        return niveau_dangerosite;
    }

    public void setNiveau_dangerosite(int niveau_dangerosite) {
        this.niveau_dangerosite = niveau_dangerosite;
    }

    public int getQuantite_stock() {
        return quantite_stock;
    }

    public void setQuantite_stock(int quantite_stock) {
        this.quantite_stock = quantite_stock;
    }

    public LocalDateTime getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(LocalDateTime date_creation) {
        this.date_creation = date_creation;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id_produit=" + id_produit +
                ", libelle='" + libelle + '\'' +
                ", description='" + description + '\'' +
                ", niveau_dangerosite=" + niveau_dangerosite +
                ", quantite_stock=" + quantite_stock +
                ", date_creation=" + date_creation +
                '}';
    }
}
