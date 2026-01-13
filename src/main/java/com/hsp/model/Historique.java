package com.hsp.model;

import java.time.LocalDateTime;

public class Historique {

    private int id_historique;
    private int id_utilisateur;
    private String action;
    private String table_concernee;
    private int id_enregistrement;
    private LocalDateTime date_action;
    private String details;

    public Historique() {
    }

    public Historique(int id_historique, int id_utilisateur, String action, String table_concernee, int id_enregistrement, LocalDateTime date_action, String details) {
        this.id_historique = id_historique;
        this.id_utilisateur = id_utilisateur;
        this.action = action;
        this.table_concernee = table_concernee;
        this.id_enregistrement = id_enregistrement;
        this.date_action = date_action;
        this.details = details;
    }

    public int getId_historique() {
        return id_historique;
    }

    public void setId_historique(int id_historique) {
        this.id_historique = id_historique;
    }

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTable_concernee() {
        return table_concernee;
    }

    public void setTable_concernee(String table_concernee) {
        this.table_concernee = table_concernee;
    }

    public int getId_enregistrement() {
        return id_enregistrement;
    }

    public void setId_enregistrement(int id_enregistrement) {
        this.id_enregistrement = id_enregistrement;
    }

    public LocalDateTime getDate_action() {
        return date_action;
    }

    public void setDate_action(LocalDateTime date_action) {
        this.date_action = date_action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Historique{" +
                "id_historique=" + id_historique +
                ", id_utilisateur=" + id_utilisateur +
                ", action='" + action + '\'' +
                ", table_concernee='" + table_concernee + '\'' +
                ", id_enregistrement=" + id_enregistrement +
                ", date_action=" + date_action +
                ", details='" + details + '\'' +
                '}';
    }
}
