package com.hsp.model;

public class RendezVous {
    private int id_rdv;
    private int id_patient;
    private int id_medecin;
    private int id_secretaire;
    private String date_rdv;
    private String motif;
    private String statut;
    private String date_creation;

    public RendezVous(int id_rdv, int id_patient, int id_medecin, int id_secretaire,
                      String date_rdv, String motif, String statut, String date_creation) {
        this.id_rdv = id_rdv;
        this.id_patient = id_patient;
        this.id_medecin = id_medecin;
        this.id_secretaire = id_secretaire;
        this.date_rdv = date_rdv;
        this.motif = motif;
        this.statut = statut;
        this.date_creation = date_creation;
    }

    public int getId_rdv() { return id_rdv; }
    public void setId_rdv(int id_rdv) { this.id_rdv = id_rdv; }

    public int getId_patient() { return id_patient; }
    public void setId_patient(int id_patient) { this.id_patient = id_patient; }

    public int getId_medecin() { return id_medecin; }
    public void setId_medecin(int id_medecin) { this.id_medecin = id_medecin; }

    public int getId_secretaire() { return id_secretaire; }
    public void setId_secretaire(int id_secretaire) { this.id_secretaire = id_secretaire; }

    public String getDate_rdv() { return date_rdv; }
    public void setDate_rdv(String date_rdv) { this.date_rdv = date_rdv; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDate_creation() { return date_creation; }
    public void setDate_creation(String date_creation) { this.date_creation = date_creation; }

    @Override
    public String toString() {
        return "RendezVous{id_rdv=" + id_rdv + ", id_patient=" + id_patient +
                ", id_medecin=" + id_medecin + ", date_rdv='" + date_rdv + "', statut='" + statut + "'}";
    }
}
