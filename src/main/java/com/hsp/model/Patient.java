package com.hsp.model;

import java.time.LocalDateTime;

public class Patient {

    private int id_patient;
    private String nom;
    private String prenom;
    private String num_secu;
    private String email;
    private String telephone;
    private String adresse;
    private LocalDateTime date_creation;


    public Patient(int id_patient, String nom, String prenom, String num_secu, String email, String telephone, String adresse, LocalDateTime date_creation) {
        this.id_patient = id_patient;
        this.nom = nom;
        this.prenom = prenom;
        this.num_secu = num_secu;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.date_creation = date_creation;
    }

    public int getId_patient() {
        return id_patient;
    }

    public void setId_patient(int id_patient) {
        this.id_patient = id_patient;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNum_secu() {
        return num_secu;
    }

    public void setNum_secu(String num_secu) {
        this.num_secu = num_secu;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public LocalDateTime getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(LocalDateTime date_creation) {
        this.date_creation = date_creation;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id_patient=" + id_patient +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", num_secu='" + num_secu + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", adresse='" + adresse + '\'' +
                ", date_creation=" + date_creation +
                '}';
    }
}
