package com.hsp.model;

public class LigneDemande {

    private int idDemande;
    private int idProduit;
    private int quantiteDemandee;
    private String statut;

    // Constructeurs
    public LigneDemande() {
    }

    public LigneDemande(int idDemande, int idProduit, int quantiteDemandee, String statut) {
        this.idDemande = idDemande;
        this.idProduit = idProduit;
        this.quantiteDemandee = quantiteDemandee;
        this.statut = statut;
    }

    // Getters
    public int getIdDemande() {
        return idDemande;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public int getQuantiteDemandee() {
        return quantiteDemandee;
    }

    public String getStatut() {
        return statut;
    }

    // Setters
    public void setIdDemande(int idDemande) {
        this.idDemande = idDemande;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public void setQuantiteDemandee(int quantiteDemandee) {
        this.quantiteDemandee = quantiteDemandee;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // toString
    @Override
    public String toString() {
        return "LigneDemande{" +
                "idDemande=" + idDemande +
                ", idProduit=" + idProduit +
                ", quantiteDemandee=" + quantiteDemandee +
                ", statut='" + statut + '\'' +
                '}';
    }

    // equals et hashCode (important pour les clés composites)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LigneDemande that = (LigneDemande) o;

        if (idDemande != that.idDemande) return false;
        return idProduit == that.idProduit;
    }

    @Override
    public int hashCode() {
        int result = idDemande;
        result = 31 * result + idProduit;
        return result;
    }
}