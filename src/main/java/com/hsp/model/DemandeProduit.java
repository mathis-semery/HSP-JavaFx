package com.hsp.model;

public class DemandeProduit {
    private int id_demande;
    private int id_medecin;
    private int id_produit;
    private int id_gestionnaire;
    private double quantite;
    private String date_demande;
    private String statut;
    private String motif_refus;

    public DemandeProduit(int id_demande, int id_medecin, int id_produit, int id_gestionnaire, double quantite, String date_demande, String statut, String motif_refus) {
        this.id_demande = id_demande;
        this.id_medecin = id_medecin;
        this.id_produit = id_produit;
        this.id_gestionnaire = id_gestionnaire;
        this.quantite = quantite;
        this.date_demande = date_demande;
        this.statut = statut;
        this.motif_refus = motif_refus;
    }


    public int getId_demande() {
        return id_demande;
    }

    public void setId_demande(int id_demande) {
        this.id_demande = id_demande;
    }

    public int getId_medecin() {
        return id_medecin;
    }

    public void setId_medecin(int id_medecin) {
        this.id_medecin = id_medecin;
    }

    public int getId_gestionnaire() {
        return id_gestionnaire;
    }

    public void setId_gestionnaire(int id_gestionnaire) {
        this.id_gestionnaire = id_gestionnaire;
    }

    public int getId_produit() {
        return id_produit;
    }

    public void setId_produit(int id_produit) {
        this.id_produit = id_produit;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDate_demande() {
        return date_demande;
    }

    public void setDate_demande(String date_demande) {
        this.date_demande = date_demande;
    }

    public String getMotif_refus() {
        return motif_refus;
    }

    public void setMotif_refus(String motif_refus) {
        this.motif_refus = motif_refus;
    }

    @Override
    public String toString() {
        return "DemandeProduit{" +
                "id_demande=" + id_demande +
                ", id_medecin=" + id_medecin +
                ", id_produit=" + id_produit +
                ", id_gestionnaire=" + id_gestionnaire +
                ", quantite=" + quantite +
                ", date_demande='" + date_demande + '\'' +
                ", statut='" + statut + '\'' +
                ", motif_refus='" + motif_refus + '\'' +
                '}';
    }
}
