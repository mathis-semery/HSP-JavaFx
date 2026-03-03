package com.hsp.model;

public class Ordonnance {

    private int id_ordonnance;
    private int id_dossier;
    private int id_medecin;
    private String date_ordonnance;
    private String medicaments;
    private String posologie;
    private String duree_traitement;
    private String notes;
    private String statut; // active, terminee
    private String date_creation;

    public Ordonnance(int id_ordonnance, int id_dossier, int id_medecin, String date_ordonnance,
                      String medicaments, String posologie, String duree_traitement,
                      String notes, String statut, String date_creation) {
        this.id_ordonnance = id_ordonnance;
        this.id_dossier = id_dossier;
        this.id_medecin = id_medecin;
        this.date_ordonnance = date_ordonnance;
        this.medicaments = medicaments;
        this.posologie = posologie;
        this.duree_traitement = duree_traitement;
        this.notes = notes;
        this.statut = statut;
        this.date_creation = date_creation;
    }

    public int getId_ordonnance() {
        return id_ordonnance;
    }

    public void setId_ordonnance(int id_ordonnance) {
        this.id_ordonnance = id_ordonnance;
    }

    public int getId_dossier() {
        return id_dossier;
    }

    public void setId_dossier(int id_dossier) {
        this.id_dossier = id_dossier;
    }

    public int getId_medecin() {
        return id_medecin;
    }

    public void setId_medecin(int id_medecin) {
        this.id_medecin = id_medecin;
    }

    public String getDate_ordonnance() {
        return date_ordonnance;
    }

    public void setDate_ordonnance(String date_ordonnance) {
        this.date_ordonnance = date_ordonnance;
    }

    public String getMedicaments() {
        return medicaments;
    }

    public void setMedicaments(String medicaments) {
        this.medicaments = medicaments;
    }

    public String getPosologie() {
        return posologie;
    }

    public void setPosologie(String posologie) {
        this.posologie = posologie;
    }

    public String getDuree_traitement() {
        return duree_traitement;
    }

    public void setDuree_traitement(String duree_traitement) {
        this.duree_traitement = duree_traitement;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    @Override
    public String toString() {
        return "Ordonnance{" +
                "id_ordonnance=" + id_ordonnance +
                ", id_dossier=" + id_dossier +
                ", id_medecin=" + id_medecin +
                ", date_ordonnance='" + date_ordonnance + '\'' +
                ", medicaments='" + medicaments + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
