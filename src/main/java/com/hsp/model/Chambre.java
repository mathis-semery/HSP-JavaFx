package com.hsp.model;

public class Chambre {
    private int id_chambre;
    private String numero;
    private String etage;
    private String nb_lits;
    private String disponible;

    public Chambre(int id_chambre,String numero, String etage, String nb_lits, String disponible) {
        this.id_chambre = id_chambre;
        this.numero = numero;
        this.etage = etage;
        this.nb_lits = nb_lits;
        this.disponible = disponible;
    }

    public int getId_chambre() {
        return id_chambre;
    }

    public void setId_chambre(int id_chambre) {
        this.id_chambre = id_chambre;
    }

    public String getDisponible() {
        return disponible;
    }

    public void setDisponible(String disponible) {
        this.disponible = disponible;
    }

    public String getNb_lits() {
        return nb_lits;
    }

    public void setNb_lits(String nb_lits) {
        this.nb_lits = nb_lits;
    }

    public String getEtage() {
        return etage;
    }

    public void setEtage(String etage) {
        this.etage = etage;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
