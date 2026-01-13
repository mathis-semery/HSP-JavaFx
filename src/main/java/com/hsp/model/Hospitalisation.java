package com.hsp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Hospitalisation {

    private int id_hospitalisation;
    private int id_dossier;
    private int id_chambre;
    private int id_medecin;
    private LocalDate date_debut;
    private LocalDate date_fin;
    private String description_maladie;
    private LocalDateTime date_creation;

    public Hospitalisation() {
    }

    public Hospitalisation(int id_hospitalisation, int id_dossier, int id_chambre, int id_medecin, LocalDate date_debut, LocalDate date_fin, String description_maladie, LocalDateTime date_creation) {
        this.id_hospitalisation = id_hospitalisation;
        this.id_dossier = id_dossier;
        this.id_chambre = id_chambre;
        this.id_medecin = id_medecin;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.description_maladie = description_maladie;
        this.date_creation = date_creation;
    }

    public int getId_hospitalisation() {
        return id_hospitalisation;
    }

    public void setId_hospitalisation(int id_hospitalisation) {
        this.id_hospitalisation = id_hospitalisation;
    }

    public int getId_dossier() {
        return id_dossier;
    }

    public void setId_dossier(int id_dossier) {
        this.id_dossier = id_dossier;
    }

    public int getId_chambre() {
        return id_chambre;
    }

    public void setId_chambre(int id_chambre) {
        this.id_chambre = id_chambre;
    }

    public int getId_medecin() {
        return id_medecin;
    }

    public void setId_medecin(int id_medecin) {
        this.id_medecin = id_medecin;
    }

    public LocalDate getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(LocalDate date_debut) {
        this.date_debut = date_debut;
    }

    public LocalDate getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(LocalDate date_fin) {
        this.date_fin = date_fin;
    }

    public String getDescription_maladie() {
        return description_maladie;
    }

    public void setDescription_maladie(String description_maladie) {
        this.description_maladie = description_maladie;
    }

    public LocalDateTime getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(LocalDateTime date_creation) {
        this.date_creation = date_creation;
    }

    @Override
    public String toString() {
        return "Hospitalisation{" +
                "id_hospitalisation=" + id_hospitalisation +
                ", id_dossier=" + id_dossier +
                ", id_chambre=" + id_chambre +
                ", id_medecin=" + id_medecin +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                ", description_maladie='" + description_maladie + '\'' +
                ", date_creation=" + date_creation +
                '}';
    }
}
