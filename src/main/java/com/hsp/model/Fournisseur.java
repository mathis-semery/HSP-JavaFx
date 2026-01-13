package com.hsp.model;

import java.time.LocalDate;

public class Fournisseur {

    private int id_fournisseur;
    private String nom;
    private String contact;
    private String email;
    private String telephone;
    private String adresse;
    private LocalDate date_creation;


    public Fournisseur(int id_fournisseur, String nom, String contact, String email, String telephone, String adresse, LocalDate date_creation) {
        this.id_fournisseur = id_fournisseur;
        this.nom = nom;
        this.contact = contact;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.date_creation = date_creation;
    }

    public int getId_fournisseur() {
        return id_fournisseur;
    }

    public void setId_fournisseur(int id_fournisseur) {
        this.id_fournisseur = id_fournisseur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
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

    public LocalDate getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(LocalDate date_creation) {
        this.date_creation = date_creation;
    }

    @Override
    public String toString() {
        return "Fournisseur{" +
                "id_fournisseur=" + id_fournisseur +
                ", nom='" + nom + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", adresse='" + adresse + '\'' +
                ", date_creation=" + date_creation +
                '}';
    }
}
