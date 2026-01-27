package com.hsp.service;

import com.hsp.dao.ChambreDAO;
import com.hsp.dao.DossierDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.HospitalisationDAO;
import com.hsp.model.Chambre;
import com.hsp.model.Dossier;
import com.hsp.model.Historique;
import com.hsp.model.Hospitalisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des hospitalisations.
 * Utilise par le medecin pour :
 * - Creation d'hospitalisations (sous reserve de disponibilite des chambres)
 * - Liberation des chambres a la sortie des patients
 * - Suivi des hospitalisations en cours
 */
public class HospitalisationService {

    private HospitalisationDAO hospitalisationDAO;
    private ChambreDAO chambreDAO;
    private DossierDAO dossierDAO;
    private HistoriqueDAO historiqueDAO;

    public HospitalisationService() {
        this.hospitalisationDAO = new HospitalisationDAO();
        this.chambreDAO = new ChambreDAO();
        this.dossierDAO = new DossierDAO();
        this.historiqueDAO = new HistoriqueDAO();
    }

    /**
     * Recupere toutes les hospitalisations
     */
    public List<Hospitalisation> getAllHospitalisations() {
        return hospitalisationDAO.findAll();
    }

    /**
     * Recupere une hospitalisation par son ID
     */
    public Hospitalisation getHospitalisationById(int id) {
        return hospitalisationDAO.findById(id);
    }

    /**
     * Recupere les hospitalisations d'un dossier
     */
    public List<Hospitalisation> getHospitalisationsParDossier(int idDossier) {
        return hospitalisationDAO.findByDossierId(idDossier);
    }

    /**
     * Recupere les hospitalisations en cours (celles sans date de fin)
     */
    public List<Hospitalisation> getHospitalisationsEnCours() {
        List<Hospitalisation> toutes = hospitalisationDAO.findAll();
        List<Hospitalisation> enCours = new ArrayList<>();

        for (Hospitalisation hospitalisation : toutes) {
            // Si pas de date de fin, l'hospitalisation est en cours
            if (hospitalisation.getDate_fin() == null) {
                enCours.add(hospitalisation);
            }
        }

        return enCours;
    }

    /**
     * Recupere les hospitalisations d'un medecin
     */
    public List<Hospitalisation> getHospitalisationsParMedecin(int idMedecin) {
        List<Hospitalisation> toutes = hospitalisationDAO.findAll();
        List<Hospitalisation> duMedecin = new ArrayList<>();

        for (Hospitalisation hospitalisation : toutes) {
            if (hospitalisation.getId_medecin() == idMedecin) {
                duMedecin.add(hospitalisation);
            }
        }

        return duMedecin;
    }

    /**
     * Recupere les chambres disponibles
     */
    public List<Chambre> getChambresDisponibles() {
        return chambreDAO.findDisponibles();
    }

    /**
     * Recupere toutes les chambres
     */
    public List<Chambre> getAllChambres() {
        return chambreDAO.findAll();
    }

    /**
     * Recupere une chambre par son ID
     */
    public Chambre getChambreById(int id) {
        return chambreDAO.findById(id);
    }

    /**
     * Verifie si une chambre est disponible
     */
    public boolean estChambreDisponible(int idChambre) {
        Chambre chambre = chambreDAO.findById(idChambre);

        // Si la chambre n'existe pas, elle n'est pas disponible
        if (chambre == null) {
            return false;
        }

        // Verifier la disponibilite (peut etre "1", "true" ou autre selon la BD)
        String disponible = chambre.getDisponible();
        if (disponible == null) {
            return false;
        }

        return disponible.equals("1") || disponible.equalsIgnoreCase("true");
    }

    /**
     * Cree une nouvelle hospitalisation (par le medecin)
     * Verifie que la chambre est disponible avant de creer l'hospitalisation
     */
    public boolean creerHospitalisation(Hospitalisation hospitalisation, int idMedecin) {
        // Verifier que le dossier existe
        Dossier dossier = dossierDAO.findById(hospitalisation.getId_dossier());
        if (dossier == null) {
            return false;
        }

        // Verifier que la chambre existe
        Chambre chambre = chambreDAO.findById(hospitalisation.getId_chambre());
        if (chambre == null) {
            return false;
        }

        // Verifier que la chambre est disponible
        if (!estChambreDisponible(hospitalisation.getId_chambre())) {
            return false;
        }

        // Definir la date de debut si non specifiee
        if (hospitalisation.getDate_debut() == null) {
            hospitalisation.setDate_debut(LocalDate.now());
        }

        // Definir le medecin
        hospitalisation.setId_medecin(idMedecin);

        // Creer l'hospitalisation dans la base de donnees
        boolean resultat = hospitalisationDAO.insert(hospitalisation);

        if (resultat) {
            // Marquer la chambre comme non disponible
            chambre.setDisponible("0");
            chambreDAO.update(chambre);

            // Mettre a jour le statut du dossier
            dossier.setStatut("hospitalise");
            dossierDAO.update(dossier);

            // Enregistrer dans l'historique
            String details = "Hospitalisation creee - Chambre: " + chambre.getNumero() +
                    " - Dossier ID: " + hospitalisation.getId_dossier();
            enregistrerHistorique(idMedecin, "CREATION", "hospitalisation", hospitalisation.getId_hospitalisation(), details);
        }

        return resultat;
    }

    /**
     * Met a jour une hospitalisation
     */
    public boolean modifierHospitalisation(Hospitalisation hospitalisation, int idUtilisateur) {
        boolean resultat = hospitalisationDAO.update(hospitalisation);

        if (resultat) {
            String details = "Modification hospitalisation ID: " + hospitalisation.getId_hospitalisation();
            enregistrerHistorique(idUtilisateur, "MODIFICATION", "hospitalisation", hospitalisation.getId_hospitalisation(), details);
        }

        return resultat;
    }

    /**
     * Termine une hospitalisation et libere la chambre
     */
    public boolean terminerHospitalisation(int idHospitalisation, LocalDate dateFin, int idMedecin) {
        // Recuperer l'hospitalisation
        Hospitalisation hospitalisation = hospitalisationDAO.findById(idHospitalisation);
        if (hospitalisation == null) {
            return false;
        }

        // Verifier que l'hospitalisation n'est pas deja terminee
        if (hospitalisation.getDate_fin() != null) {
            return false;
        }

        // Definir la date de fin (aujourd'hui si non specifiee)
        if (dateFin == null) {
            dateFin = LocalDate.now();
        }
        hospitalisation.setDate_fin(dateFin);

        // Mettre a jour l'hospitalisation dans la base de donnees
        boolean resultat = hospitalisationDAO.update(hospitalisation);

        if (resultat) {
            // Liberer la chambre
            libererChambre(hospitalisation.getId_chambre());

            // Mettre a jour le statut du dossier
            Dossier dossier = dossierDAO.findById(hospitalisation.getId_dossier());
            if (dossier != null) {
                dossier.setStatut("termine");
                dossierDAO.update(dossier);
            }

            // Enregistrer dans l'historique
            String details = "Sortie du patient - Hospitalisation terminee le " + dateFin;
            enregistrerHistorique(idMedecin, "SORTIE", "hospitalisation", idHospitalisation, details);
        }

        return resultat;
    }

    /**
     * Libere une chambre (la rend disponible)
     */
    public boolean libererChambre(int idChambre) {
        Chambre chambre = chambreDAO.findById(idChambre);
        if (chambre == null) {
            return false;
        }

        // Marquer la chambre comme disponible
        chambre.setDisponible("1");
        return chambreDAO.update(chambre);
    }

    /**
     * Supprime une hospitalisation
     */
    public boolean supprimerHospitalisation(int idHospitalisation, int idUtilisateur) {
        // Recuperer l'hospitalisation
        Hospitalisation hospitalisation = hospitalisationDAO.findById(idHospitalisation);
        if (hospitalisation == null) {
            return false;
        }

        // Si l'hospitalisation est en cours, liberer la chambre
        if (hospitalisation.getDate_fin() == null) {
            libererChambre(hospitalisation.getId_chambre());
        }

        // Supprimer l'hospitalisation
        boolean resultat = hospitalisationDAO.delete(idHospitalisation);

        if (resultat) {
            String details = "Suppression de l'hospitalisation ID: " + idHospitalisation;
            enregistrerHistorique(idUtilisateur, "SUPPRESSION", "hospitalisation", idHospitalisation, details);
        }

        return resultat;
    }

    /**
     * Compte le nombre d'hospitalisations en cours
     */
    public int compterHospitalisationsEnCours() {
        List<Hospitalisation> enCours = getHospitalisationsEnCours();
        return enCours.size();
    }

    /**
     * Compte le nombre de chambres disponibles
     */
    public int compterChambresDisponibles() {
        List<Chambre> disponibles = chambreDAO.findDisponibles();
        return disponibles.size();
    }

    /**
     * Recupere le dossier associe a une hospitalisation
     */
    public Dossier getDossierDeLHospitalisation(int idHospitalisation) {
        Hospitalisation hospitalisation = hospitalisationDAO.findById(idHospitalisation);
        if (hospitalisation == null) {
            return null;
        }
        return dossierDAO.findById(hospitalisation.getId_dossier());
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
