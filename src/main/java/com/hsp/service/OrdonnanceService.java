package com.hsp.service;

import com.hsp.dao.DossierDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.OrdonnanceDAO;
import com.hsp.dao.PatientDAO;
import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Dossier;
import com.hsp.model.Historique;
import com.hsp.model.Ordonnance;
import com.hsp.model.Patient;
import com.hsp.model.Utilisateur;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceService {

    public static final String STATUT_ACTIVE = "active";
    public static final String STATUT_TERMINEE = "terminee";

    private OrdonnanceDAO ordonnanceDAO;
    private DossierDAO dossierDAO;
    private PatientDAO patientDAO;
    private UtilisateurDAO utilisateurDAO;
    private HistoriqueDAO historiqueDAO;

    public OrdonnanceService() {
        this.ordonnanceDAO = new OrdonnanceDAO();
        this.dossierDAO = new DossierDAO();
        this.patientDAO = new PatientDAO();
        this.utilisateurDAO = new UtilisateurDAO();
        this.historiqueDAO = new HistoriqueDAO();
    }

    public List<Ordonnance> getAllOrdonnances() {
        return ordonnanceDAO.findAll();
    }

    public Ordonnance getOrdonnanceById(int id) {
        return ordonnanceDAO.findById(id);
    }

    public List<Ordonnance> getOrdonnancesParDossier(int idDossier) {
        return ordonnanceDAO.findByDossierId(idDossier);
    }

    public List<Ordonnance> getOrdonnancesParMedecin(int idMedecin) {
        return ordonnanceDAO.findByMedecinId(idMedecin);
    }

    public boolean creerOrdonnance(Ordonnance ordonnance, int idUtilisateur) {
        if (ordonnance.getDate_ordonnance() == null || ordonnance.getDate_ordonnance().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ordonnance.setDate_ordonnance(LocalDateTime.now().format(formatter));
        }

        if (ordonnance.getStatut() == null || ordonnance.getStatut().isEmpty()) {
            ordonnance.setStatut(STATUT_ACTIVE);
        }

        boolean resultat = ordonnanceDAO.insert(ordonnance);

        if (resultat) {
            String details = "Creation ordonnance pour dossier ID: " + ordonnance.getId_dossier();
            enregistrerHistorique(idUtilisateur, "CREATION", "ordonnance", ordonnance.getId_ordonnance(), details);
        }

        return resultat;
    }

    public boolean modifierOrdonnance(Ordonnance ordonnance, int idUtilisateur) {
        boolean resultat = ordonnanceDAO.update(ordonnance);

        if (resultat) {
            String details = "Modification ordonnance ID: " + ordonnance.getId_ordonnance();
            enregistrerHistorique(idUtilisateur, "MODIFICATION", "ordonnance", ordonnance.getId_ordonnance(), details);
        }

        return resultat;
    }

    public boolean supprimerOrdonnance(int idOrdonnance, int idUtilisateur) {
        boolean resultat = ordonnanceDAO.delete(idOrdonnance);

        if (resultat) {
            String details = "Suppression ordonnance ID: " + idOrdonnance;
            enregistrerHistorique(idUtilisateur, "SUPPRESSION", "ordonnance", idOrdonnance, details);
        }

        return resultat;
    }

    public List<Ordonnance> rechercherOrdonnances(String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            return getAllOrdonnances();
        }

        String rechercheMin = recherche.toLowerCase().trim();
        List<Ordonnance> toutes = ordonnanceDAO.findAll();
        List<Ordonnance> resultats = new ArrayList<>();

        for (Ordonnance o : toutes) {
            boolean correspond = false;

            if (o.getMedicaments() != null && o.getMedicaments().toLowerCase().contains(rechercheMin)) {
                correspond = true;
            }
            if (o.getPosologie() != null && o.getPosologie().toLowerCase().contains(rechercheMin)) {
                correspond = true;
            }
            if (o.getNotes() != null && o.getNotes().toLowerCase().contains(rechercheMin)) {
                correspond = true;
            }
            if (o.getStatut() != null && o.getStatut().toLowerCase().contains(rechercheMin)) {
                correspond = true;
            }

            // Chercher aussi par patient via dossier
            if (!correspond) {
                Dossier dossier = dossierDAO.findById(o.getId_dossier());
                if (dossier != null) {
                    Patient patient = patientDAO.findById(dossier.getId_patient());
                    if (patient != null) {
                        String nomComplet = (patient.getNom() + " " + patient.getPrenom()).toLowerCase();
                        if (nomComplet.contains(rechercheMin)) {
                            correspond = true;
                        }
                    }
                }
            }

            if (correspond) {
                resultats.add(o);
            }
        }

        return resultats;
    }

    public Patient getPatientDeLOrdonnance(Ordonnance ordonnance) {
        Dossier dossier = dossierDAO.findById(ordonnance.getId_dossier());
        if (dossier == null) return null;
        return patientDAO.findById(dossier.getId_patient());
    }

    public Patient getPatientParIdDossier(int idDossier) {
        Dossier dossier = dossierDAO.findById(idDossier);
        if (dossier == null) return null;
        return patientDAO.findById(dossier.getId_patient());
    }

    public Utilisateur getMedecinDeLOrdonnance(Ordonnance ordonnance) {
        return utilisateurDAO.findById(ordonnance.getId_medecin());
    }

    public List<Dossier> getTousDossiers() {
        return dossierDAO.findAll();
    }

    private void enregistrerHistorique(int idUtilisateur, String action, String table, int idEnregistrement, String details) {
        Historique historique = new Historique(
                0,
                idUtilisateur,
                action,
                table,
                idEnregistrement,
                LocalDateTime.now(),
                details
        );
        historiqueDAO.insert(historique);
    }
}
