package com.hsp.service;

import com.hsp.dao.DossierDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.PatientDAO;
import com.hsp.model.Dossier;
import com.hsp.model.Historique;
import com.hsp.model.Patient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des patients.
 * Utilisé principalement par la secrétaire pour :
 * - Création et mise à jour des fiches patients
 * - Recherche de patients
 */
public class PatientService {

    private PatientDAO patientDAO;
    private DossierDAO dossierDAO;
    private HistoriqueDAO historiqueDAO;

    public PatientService() {
        this.patientDAO = new PatientDAO();
        this.dossierDAO = new DossierDAO();
        this.historiqueDAO = new HistoriqueDAO();
    }

    /**
     * Recupere tous les patients
     */
    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }

    /**
     * Recupere un patient par son ID
     */
    public Patient getPatientById(int id) {
        return patientDAO.findById(id);
    }

    /**
     * Cree un nouveau patient (par la secretaire)
     */
    public boolean creerPatient(Patient patient, int idUtilisateur) {
        // Inserer le patient dans la base de donnees
        boolean resultat = patientDAO.insert(patient);

        // Si la creation a reussi, enregistrer dans l'historique
        if (resultat) {
            String details = "Creation du patient: " + patient.getNom() + " " + patient.getPrenom();
            enregistrerHistorique(idUtilisateur, "CREATION", "patient", patient.getId_patient(), details);
        }

        return resultat;
    }

    /**
     * Met a jour les informations d'un patient
     */
    public boolean modifierPatient(Patient patient, int idUtilisateur) {
        // Mettre a jour le patient dans la base de donnees
        boolean resultat = patientDAO.update(patient);

        // Si la modification a reussi, enregistrer dans l'historique
        if (resultat) {
            String details = "Modification du patient: " + patient.getNom() + " " + patient.getPrenom();
            enregistrerHistorique(idUtilisateur, "MODIFICATION", "patient", patient.getId_patient(), details);
        }

        return resultat;
    }

    /**
     * Supprime un patient (seulement si aucun dossier associe)
     */
    public boolean supprimerPatient(int idPatient, int idUtilisateur) {
        // Verifier qu'il n'y a pas de dossiers associes a ce patient
        List<Dossier> dossiers = dossierDAO.findByPatientId(idPatient);
        if (dossiers != null && dossiers.size() > 0) {
            // On ne peut pas supprimer un patient qui a des dossiers
            return false;
        }

        // Recuperer le patient avant suppression pour l'historique
        Patient patient = patientDAO.findById(idPatient);
        if (patient == null) {
            return false;
        }

        // Supprimer le patient
        boolean resultat = patientDAO.delete(idPatient);

        // Si la suppression a reussi, enregistrer dans l'historique
        if (resultat) {
            String details = "Suppression du patient: " + patient.getNom() + " " + patient.getPrenom();
            enregistrerHistorique(idUtilisateur, "SUPPRESSION", "patient", idPatient, details);
        }

        return resultat;
    }

    /**
     * Recherche des patients par nom, prenom, email ou numero de securite sociale
     */
    public List<Patient> rechercherPatients(String recherche) {
        // Si la recherche est vide, retourner tous les patients
        if (recherche == null || recherche.trim().isEmpty()) {
            return getAllPatients();
        }

        // Mettre la recherche en minuscules pour une recherche insensible a la casse
        String rechercheMinuscule = recherche.toLowerCase().trim();

        // Recuperer tous les patients
        List<Patient> tousLesPatients = patientDAO.findAll();

        // Filtrer les patients qui correspondent a la recherche
        List<Patient> resultats = new ArrayList<>();

        for (Patient patient : tousLesPatients) {
            boolean correspond = false;

            // Verifier si le nom correspond
            if (patient.getNom() != null && patient.getNom().toLowerCase().contains(rechercheMinuscule)) {
                correspond = true;
            }

            // Verifier si le prenom correspond
            if (patient.getPrenom() != null && patient.getPrenom().toLowerCase().contains(rechercheMinuscule)) {
                correspond = true;
            }

            // Verifier si le numero de securite sociale correspond
            if (patient.getNum_secu() != null && patient.getNum_secu().contains(rechercheMinuscule)) {
                correspond = true;
            }

            // Verifier si l'email correspond
            if (patient.getEmail() != null && patient.getEmail().toLowerCase().contains(rechercheMinuscule)) {
                correspond = true;
            }

            // Si le patient correspond, l'ajouter aux resultats
            if (correspond) {
                resultats.add(patient);
            }
        }

        return resultats;
    }

    /**
     * Recherche un patient par son numero de securite sociale
     */
    public Patient rechercherParNumSecu(String numSecu) {
        if (numSecu == null || numSecu.trim().isEmpty()) {
            return null;
        }

        // Recuperer tous les patients
        List<Patient> tousLesPatients = patientDAO.findAll();

        // Chercher le patient avec ce numero de securite sociale
        for (Patient patient : tousLesPatients) {
            if (patient.getNum_secu() != null && patient.getNum_secu().equals(numSecu)) {
                return patient;
            }
        }

        // Aucun patient trouve
        return null;
    }

    /**
     * Verifie si un numero de securite sociale existe deja
     * @param excludePatientId ID du patient a exclure de la verification (pour modification)
     */
    public boolean numSecuExiste(String numSecu, int excludePatientId) {
        if (numSecu == null || numSecu.trim().isEmpty()) {
            return false;
        }

        // Recuperer tous les patients
        List<Patient> tousLesPatients = patientDAO.findAll();

        // Verifier si un autre patient a deja ce numero
        for (Patient patient : tousLesPatients) {
            // Ignorer le patient qu'on modifie
            if (patient.getId_patient() == excludePatientId) {
                continue;
            }

            // Verifier si le numero de securite sociale existe
            if (patient.getNum_secu() != null && patient.getNum_secu().equals(numSecu)) {
                return true; // Le numero existe deja
            }
        }

        return false; // Le numero n'existe pas
    }

    /**
     * Recupere les dossiers d'un patient
     */
    public List<Dossier> getDossiersPatient(int idPatient) {
        return dossierDAO.findByPatientId(idPatient);
    }

    /**
     * Enregistre une action dans l'historique pour la tracabilite (RGPD)
     */
    private void enregistrerHistorique(int idUtilisateur, String action, String table, int idEnregistrement, String details) {
        Historique historique = new Historique(
                0,                      // ID auto-genere
                idUtilisateur,          // Qui a fait l'action
                action,                 // Type d'action (CREATION, MODIFICATION, SUPPRESSION)
                table,                  // Table concernee
                idEnregistrement,       // ID de l'enregistrement concerne
                LocalDateTime.now(),    // Date et heure de l'action
                details                 // Details de l'action
        );
        historiqueDAO.insert(historique);
    }
}
