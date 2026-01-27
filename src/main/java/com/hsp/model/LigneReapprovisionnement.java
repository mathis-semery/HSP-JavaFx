package com.hsp.model;

import java.math.BigDecimal;

public class LigneReapprovisionnement {

    private int idReappro;
    private int idFournisseur;
    private int idProduit;
    private int quantiteCommandee;
    private BigDecimal prixUnitaire;

    // Constructeurs
    public LigneReapprovisionnement() {
    }

    public LigneReapprovisionnement(int idReappro, int idFournisseur, int idProduit,
                                    int quantiteCommandee, BigDecimal prixUnitaire) {
        this.idReappro = idReappro;
        this.idFournisseur = idFournisseur;
        this.idProduit = idProduit;
        this.quantiteCommandee = quantiteCommandee;
        this.prixUnitaire = prixUnitaire;
    }

    // Getters
    public int getIdReappro() {
        return idReappro;
    }

    public int getIdFournisseur() {
        return idFournisseur;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public int getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    // Setters
    public void setIdReappro(int idReappro) {
        this.idReappro = idReappro;
    }

    public void setIdFournisseur(int idFournisseur) {
        this.idFournisseur = idFournisseur;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public void setQuantiteCommandee(int quantiteCommandee) {
        this.quantiteCommandee = quantiteCommandee;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    // Méthode utilitaire pour calculer le montant total de cette ligne
    public BigDecimal getMontantTotal() {
        if (prixUnitaire == null) {
            return BigDecimal.ZERO;
        }
        return prixUnitaire.multiply(new BigDecimal(quantiteCommandee));
    }


    // toString
    @Override
    public String toString() {
        return "LigneReapprovisionnement{" +
                "idReappro=" + idReappro +
                ", idFournisseur=" + idFournisseur +
                ", idProduit=" + idProduit +
                ", quantiteCommandee=" + quantiteCommandee +
                ", prixUnitaire=" + prixUnitaire +
                '}';
    }

    // equals et hashCode (important pour les clés composites à 3 champs)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LigneReapprovisionnement that = (LigneReapprovisionnement) o;

        if (idReappro != that.idReappro) return false;
        if (idFournisseur != that.idFournisseur) return false;
        return idProduit == that.idProduit;
    }

    @Override
    public int hashCode() {
        int result = idReappro;
        result = 31 * result + idFournisseur;
        result = 31 * result + idProduit;
        return result;
    }
}