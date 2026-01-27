package com.hsp.service;

import com.hsp.dao.DossierDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.HospitalisationDAO;
import com.hsp.dao.PatientDAO;
import com.hsp.model.Dossier;
import com.hsp.model.Historique;
import com.hsp.model.Hospitalisation;
import com.hsp.model.Patient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des dossiers de prise en charge.
 * Utilise par :
 * - La secretaire : creation des dossiers de prise en charge
 * - Le medecin : consultation et traitement des dossiers
 */
public class DossierService {

    // Statuts possibles d'un dossier
    public static final String STATUT_EN_ATTENTE = "en_attente";
    public static final String STATUT_EN_COURS = "en_cours";
    public static final String STATUT_TERMINE = "termine";
    public static final String STATUT_HOSPITALISE = "hospitalise";

    private DossierDAO dossierDAO;
    private PatientDAO patientDAO;
    private HospitalisationDAO hospitalisationDAO;
    private HistoriqueDAO historiqueDAO;

    public DossierService() {
        this.dossierDAO = new DossierDAO();
        this.patientDAO = new PatientDAO();
        this.hospitalisationDAO = new HospitalisationDAO();
        this.historiqueDAO = new HistoriqueDAO();
    }

    /**
     * Recupere tous les dossiers
     */
    public List<Dossier> getAllDossiers() {
        return dossierDAO.findAll();
    }

    /**
     * Recupere un dossier par son ID
     */
    public Dossier getDossierById(int id) {
        return dossierDAO.findById(id);
    }

    /**
     * Recupere les dossiers d'un patient
     */
    public List<Dossier> getDossiersParPatient(int idPatient) {
        return dossierDAO.findByPatientId(idPatient);
    }

    /**
     * Recupere les dossiers en salle d'attente (statut en_attente)
     * Tries par niveau de gravite (les plus graves en premier)
     */
    public List<Dossier> getDossiersEnAttente() {
        // Recuperer tous les dossiers
        List<Dossier> tousDossiers = dossierDAO.findAll();

        // Filtrer seulement ceux en attente
        List<Dossier> dossiersEnAttente = new ArrayList<>();
        for (Dossier dossier : tousDossiers) {
            if (STATUT_EN_ATTENTE.equals(dossier.getStatut())) {
                dossiersEnAttente.add(dossier);
            }
        }

        // Trier par niveau de gravite (du plus grave au moins grave)
        // Tri a bulles simple
        for (int i = 0; i < dossiersEnAttente.size() - 1; i++) {
            for (int j = 0; j < dossiersEnAttente.size() - i - 1; j++) {
                Dossier d1 = dossiersEnAttente.get(j);
                Dossier d2 = dossiersEnAttente.get(j + 1);

                // Comparer les niveaux de gravite (ordre decroissant)
                int gravite1 = convertirGravite(d1.getNiveau_gravite());
                int gravite2 = convertirGravite(d2.getNiveau_gravite());

                if (gravite1 < gravite2) {
                    // Echanger les dossiers
                    dossiersEnAttente.set(j, d2);
                    dossiersEnAttente.set(j + 1, d1);
                }
            }
        }

        return dossiersEnAttente;
    }

    /**
     * Convertit le niveau de gravite en entier pour le tri
     */
    private int convertirGravite(String niveauGravite) {
        if (niveauGravite == null) {
            return 0;
        }
        try {
            return Integer.parseInt(niveauGravite);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Recupere les dossiers en cours de traitement
     */
    public List<Dossier> getDossiersEnCours() {
        List<Dossier> tousDossiers = dossierDAO.findAll();
        List<Dossier> dossiersEnCours = new ArrayList<>();

        for (Dossier dossier : tousDossiers) {
            if (STATUT_EN_COURS.equals(dossier.getStatut())) {
                dossiersEnCours.add(dossier);
            }
        }

        return dossiersEnCours;
    }

    /**
     * Recupere les dossiers par statut
     */
    public List<Dossier> getDossiersParStatut(String statut) {
        List<Dossier> tousDossiers = dossierDAO.findAll();
        List<Dossier> resultat = new ArrayList<>();

        for (Dossier dossier : tousDossiers) {
            if (statut.equals(dossier.getStatut())) {
                resultat.add(dossier);
            }
        }

        return resultat;
    }

    /**
     * Recupere les dossiers assignes a un medecin
     */
    public List<Dossier> getDossiersParMedecin(int idMedecin) {
        List<Dossier> tousDossiers = dossierDAO.findAll();
        List<Dossier> dossiersduMedecin = new ArrayList<>();

        for (Dossier dossier : tousDossiers) {
            if (dossier.getId_medecin() == idMedecin) {
                dossiersduMedecin.add(dossier);
            }
        }

        return dossiersduMedecin;
    }

    /**
     * Cree un nouveau dossier de prise en charge (par la secretaire)
     */
    public boolean creerDossier(Dossier dossier, int idUtilisateur) {
        // Verifier que le patient existe
        Patient patient = patientDAO.findById(dossier.getId_patient());
        if (patient == null) {
            return false;
        }

        // Definir le statut initial si non specifie
        if (dossier.getStatut() == null || dossier.getStatut().isEmpty()) {
            dossier.setStatut(STATUT_EN_ATTENTE);
        }

        // Definir la date d'arrivee si non specifiee
        if (dossier.getDate_arrivee() == null || dossier.getDate_arrivee().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dossier.setDate_arrivee(LocalDateTime.now().format(formatter));
        }

        // Inserer le dossier dans la base de donnees
        boolean resultat = dossierDAO.insert(dossier);

        // Enregistrer dans l'historique si reussi
        if (resultat) {
            String details = "Creation dossier pour patient ID: " + dossier.getId_patient() +
                    " - Gravite: " + dossier.getNiveau_gravite();
            enregistrerHistorique(idUtilisateur, "CREATION", "dossier", dossier.getId_dossier(), details);
        }

        return resultat;
    }

    /**
     * Met a jour un dossier
     */
    public boolean modifierDossier(Dossier dossier, int idUtilisateur) {
        boolean resultat = dossierDAO.update(dossier);

        if (resultat) {
            String details = "Modification dossier ID: " + dossier.getId_dossier() +
                    " - Statut: " + dossier.getStatut();
            enregistrerHistorique(idUtilisateur, "MODIFICATION", "dossier", dossier.getId_dossier(), details);
        }

        return resultat;
    }

    /**
     * Le medecin prend en charge un dossier (passe de en_attente a en_cours)
     */
    public boolean prendreEnCharge(int idDossier, int idMedecin) {
        // Recuperer le dossier
        Dossier dossier = dossierDAO.findById(idDossier);
        if (dossier == null) {
            return false;
        }

        // Verifier que le dossier est en attente
        if (!STATUT_EN_ATTENTE.equals(dossier.getStatut())) {
            return false;
        }

        // Changer le statut et assigner le medecin
        dossier.setStatut(STATUT_EN_COURS);
        dossier.setId_medecin(idMedecin);

        // Mettre a jour dans la base de donnees
        boolean resultat = dossierDAO.update(dossier);

        if (resultat) {
            String details = "Prise en charge du dossier par medecin ID: " + idMedecin;
            enregistrerHistorique(idMedecin, "PRISE_EN_CHARGE", "dossier", idDossier, details);
        }

        return resultat;
    }

    /**
     * Cloture un dossier avec une ordonnance (sortie du patient)
     */
    public boolean cloturerAvecOrdonnance(int idDossier, int idMedecin) {
        // Recuperer le dossier
        Dossier dossier = dossierDAO.findById(idDossier);
        if (dossier == null) {
            return false;
        }

        // Verifier que le dossier est en cours
        if (!STATUT_EN_COURS.equals(dossier.getStatut())) {
            return false;
        }

        // Changer le statut en termine
        dossier.setStatut(STATUT_TERMINE);

        // Mettre a jour dans la base de donnees
        boolean resultat = dossierDAO.update(dossier);

        if (resultat) {
            String details = "Cloture avec ordonnance - Sortie du patient";
            enregistrerHistorique(idMedecin, "CLOTURE", "dossier", idDossier, details);
        }

        return resultat;
    }

    /**
     * Marque un dossier comme hospitalise
     */
    public boolean marquerHospitalise(int idDossier, int idMedecin) {
        // Recuperer le dossier
        Dossier dossier = dossierDAO.findById(idDossier);
        if (dossier == null) {
            return false;
        }

        // Changer le statut en hospitalise
        dossier.setStatut(STATUT_HOSPITALISE);

        // Mettre a jour dans la base de donnees
        boolean resultat = dossierDAO.update(dossier);

        if (resultat) {
            String details = "Dossier marque comme hospitalise";
            enregistrerHistorique(idMedecin, "HOSPITALISATION", "dossier", idDossier, details);
        }

        return resultat;
    }

    /**
     * Supprime un dossier (seulement si pas d'hospitalisation associee)
     */
    public boolean supprimerDossier(int idDossier, int idUtilisateur) {
        // Verifier qu'il n'y a pas d'hospitalisation associee
        List<Hospitalisation> hospitalisations = hospitalisationDAO.findByDossierId(idDossier);
        if (hospitalisations != null && hospitalisations.size() > 0) {
            return false;
        }

        // Supprimer le dossier
        boolean resultat = dossierDAO.delete(idDossier);

        if (resultat) {
            String details = "Suppression du dossier ID: " + idDossier;
            enregistrerHistorique(idUtilisateur, "SUPPRESSION", "dossier", idDossier, details);
        }

        return resultat;
    }

    /**
     * Recupere le patient associe a un dossier
     */
    public Patient getPatientDuDossier(int idDossier) {
        Dossier dossier = dossierDAO.findById(idDossier);
        if (dossier == null) {
            return null;
        }
        return patientDAO.findById(dossier.getId_patient());
    }

    /**
     * Verifie si un dossier a des hospitalisations
     */
    public boolean aDesHospitalisations(int idDossier) {
        List<Hospitalisation> hospitalisations = hospitalisationDAO.findByDossierId(idDossier);
        return hospitalisations != null && hospitalisations.size() > 0;
    }

    /**
     * Compte le nombre de dossiers en attente
     */
    public int compterDossiersEnAttente() {
        List<Dossier> dossiersEnAttente = getDossiersEnAttente();
        return dossiersEnAttente.size();
    }

    /**
     * Enregistre une action dans l'historique pour la tracabilite (RGPD)
     */
    private void enregistrerHistorique(int idUtilisateur, String action, String table, int idEnregistrement, String details) {
        Historique historique = new Historique(
                0,                      // ID auto-genere
                idUtilisateur,          // Qui a fait l'action
                action,                 // Type d'action
                table,                  // Table concernee
                idEnregistrement,       // ID de l'enregistrement concerne
                LocalDateTime.now(),    // Date et heure de l'action
                details                 // Details de l'action
        );
        historiqueDAO.insert(historique);
    }
}
