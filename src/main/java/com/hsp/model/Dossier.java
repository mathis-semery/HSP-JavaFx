package com.hsp.model;

public class Dossier {
    private int id_dossier;
    private int id_patient;
    private int id_medecin;
    private String date_arrivee;
    private String symptomes;
    private String niveau_gravite;
    private String statut;
    private String date_creation;

    public Dossier(int id_dossier,int id_patient,int id_medecin,String date_arrivee,String symptomes,String niveau_gravite,String statut,String date_creation){
        this.id_dossier = id_dossier;
        this.id_patient = id_patient;
        this.id_medecin = id_medecin;
        this.date_arrivee = date_arrivee;
        this.symptomes = symptomes;
        this.niveau_gravite = niveau_gravite;
        this.statut = statut;
        this.date_creation = date_creation;
    }

    public int getId_dossier() {
        return id_dossier;
    }

    public void setId_dossier(int id_dossier) {
        this.id_dossier = id_dossier;
    }

    public int getId_patient() {
        return id_patient;
    }

    public void setId_patient(int id_patient) {
        this.id_patient = id_patient;
    }

    public int getId_medecin() {
        return id_medecin;
    }

    public void setId_medecin(int id_medecin) {
        this.id_medecin = id_medecin;
    }

    public String getDate_arrivee() {
        return date_arrivee;
    }

    public void setDate_arrivee(String date_arrivee) {
        this.date_arrivee = date_arrivee;
    }

    public String getSymptomes() {
        return symptomes;
    }

    public void setSymptomes(String symptomes) {
        this.symptomes = symptomes;
    }

    public String getNiveau_gravite() {
        return niveau_gravite;
    }

    public void setNiveau_gravite(String niveau_gravite) {
        this.niveau_gravite = niveau_gravite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(String date_creation) {
        this.date_creation = date_creation;
    }
}


