package com.hsp.model;

public class Connexion {
    private int id_connexion;
    private int id_utilisateur;
    private String date_connexion;
    private String ip_adresse;

    public Connexion(int id_connexion, int id_utilisateur, String date_connexion, String ip_adresse) {
        this.id_connexion = id_connexion;
        this.id_utilisateur = id_utilisateur;
        this.date_connexion = date_connexion;
        this.ip_adresse = ip_adresse;
    }


    public int getId_connexion() {
        return id_connexion;
    }

    public void setId_connexion(int id_connexion) {
        this.id_connexion = id_connexion;
    }

    public String getIp_adresse() {
        return ip_adresse;
    }

    public void setIp_adresse(String ip_adresse) {
        this.ip_adresse = ip_adresse;
    }

    public String getDate_connexion() {
        return date_connexion;
    }

    public void setDate_connexion(String date_connexion) {
        this.date_connexion = date_connexion;
    }

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    @Override
    public String toString() {
        return "Connexion{" +
                "id_connexion=" + id_connexion +
                ", id_utilisateur=" + id_utilisateur +
                ", date_connexion='" + date_connexion + '\'' +
                ", ip_adresse='" + ip_adresse + '\'' +
                '}';
    }
}
