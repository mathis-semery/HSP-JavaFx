package com.hsp.model;

import java.time.LocalDate;

public class Reapprovisionnement {

    private int id_reappro;
    private int id_produit;
    private int id_fournisseur;
    private int id_gestionnaire;
    private int quantite;
    private LocalDate date_commande;
    private LocalDate date_reception;


    public Reapprovisionnement(int id_reappro, int id_produit, int id_fournisseur, int id_gestionnaire,
                   int quantite, LocalDate date_commande, LocalDate date_reception) {
        this.id_reappro = id_reappro;
        this.id_produit = id_produit;
        this.id_fournisseur = id_fournisseur;
        this.id_gestionnaire = id_gestionnaire;
        this.quantite = quantite;
        this.date_commande = date_commande;
        this.date_reception = date_reception;
    }

    public int getId_reappro() {
        return id_reappro;
    }

    public void setId_reappro(int id_reappro) {
        this.id_reappro = id_reappro;
    }

    public int getId_produit() {
        return id_produit;
    }

    public void setId_produit(int id_produit) {
        this.id_produit = id_produit;
    }

    public int getId_fournisseur() {
        return id_fournisseur;
    }

    public void setId_fournisseur(int id_fournisseur) {
        this.id_fournisseur = id_fournisseur;
    }

    public int getId_gestionnaire() {
        return id_gestionnaire;
    }

    public void setId_gestionnaire(int id_gestionnaire) {
        this.id_gestionnaire = id_gestionnaire;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public LocalDate getDate_commande() {
        return date_commande;
    }

    public void setDate_commande(LocalDate date_commande) {
        this.date_commande = date_commande;
    }

    public LocalDate getDate_reception() {
        return date_reception;
    }

    public void setDate_reception(LocalDate date_reception) {
        this.date_reception = date_reception;
    }

    @Override
    public String toString() {
        return "Reappro{" +
                "id_reappro=" + id_reappro +
                ", id_produit=" + id_produit +
                ", id_fournisseur=" + id_fournisseur +
                ", id_gestionnaire=" + id_gestionnaire +
                ", quantite=" + quantite +
                ", date_commande=" + date_commande +
                ", date_reception=" + date_reception +
                '}';
    }
}
