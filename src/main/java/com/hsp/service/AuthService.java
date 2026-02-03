package com.hsp.service;

import com.hsp.dao.ConnexionDAO;
import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Connexion;
import com.hsp.model.Historique;
import com.hsp.model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service d'authentification.
 * Gere :
 * - La connexion des utilisateurs
 * - La deconnexion
 * - Le changement de mot de passe
 * - La verification des roles
 */
public class AuthService {

    private UtilisateurDAO utilisateurDAO;
    private ConnexionDAO connexionDAO;
    private HistoriqueDAO historiqueDAO;
    private Utilisateur utilisateurConnecte;

    public AuthService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.connexionDAO = new ConnexionDAO();
        this.historiqueDAO = new HistoriqueDAO();
        this.utilisateurConnecte = null;
    }

    /**
     * Connecte un utilisateur avec son email et mot de passe
     * @param email Email de l'utilisateur
     * @param motDePasse Mot de passe en clair
     * @return L'utilisateur connecte ou null si echec
     */
    public Utilisateur login(String email, String motDePasse) {
        // Verifier que les parametres ne sont pas vides
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        if (motDePasse == null || motDePasse.isEmpty()) {
            return null;
        }

        // Chercher l'utilisateur par email
        Utilisateur utilisateur = utilisateurDAO.findByEmail(email);

        // Si l'utilisateur n'existe pas, retourner null
        if (utilisateur == null) {
            return null;
        }

        // Verifier le mot de passe
        boolean motDePasseValide = verifierMotDePasse(motDePasse, utilisateur.getMdp());

        if (motDePasseValide) {
            // Connexion reussie
            this.utilisateurConnecte = utilisateur;

            // Enregistrer la connexion dans l'historique
            enregistrerConnexion(utilisateur.getId());

            return utilisateur;
        }

        // Mot de passe incorrect
        return null;
    }

    /**
     * Deconnecte l'utilisateur actuel
     */
    public void logout() {
        if (utilisateurConnecte != null) {
            // Enregistrer la deconnexion dans l'historique
            enregistrerHistorique(utilisateurConnecte.getId(), "DECONNEXION", "utilisateur",
                    utilisateurConnecte.getId(), "Deconnexion de l'utilisateur");
        }
        this.utilisateurConnecte = null;
    }

    /**
     * Retourne l'utilisateur actuellement connecte
     */
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Verifie si un utilisateur est connecte
     */
    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }

    /**
     * Verifie si l'utilisateur connecte a un role specifique
     * @param role Le role a verifier (ex: "medecin", "secretaire", "gestionnaire")
     */
    public boolean hasRole(String role) {
        // Si personne n'est connecte, retourner false
        if (utilisateurConnecte == null) {
            return false;
        }

        // Si le role demande est null, retourner false
        if (role == null) {
            return false;
        }

        // Comparer les roles (insensible a la casse)
        String roleUtilisateur = utilisateurConnecte.getRole();
        if (roleUtilisateur == null) {
            return false;
        }

        return role.equalsIgnoreCase(roleUtilisateur);
    }

    /**
     * Change le mot de passe d'un utilisateur
     * @param idUtilisateur ID de l'utilisateur
     * @param ancienMdp Ancien mot de passe (pour verification)
     * @param nouveauMdp Nouveau mot de passe
     * @return true si le changement a reussi
     */
    public boolean changerMotDePasse(int idUtilisateur, String ancienMdp, String nouveauMdp) {
        // Verifier les parametres
        if (ancienMdp == null || ancienMdp.isEmpty()) {
            return false;
        }
        if (nouveauMdp == null || nouveauMdp.isEmpty()) {
            return false;
        }

        // Recuperer l'utilisateur
        Utilisateur utilisateur = utilisateurDAO.findById(idUtilisateur);
        if (utilisateur == null) {
            return false;
        }

        // Verifier l'ancien mot de passe
        boolean ancienMdpValide = verifierMotDePasse(ancienMdp, utilisateur.getMdp());
        if (!ancienMdpValide) {
            return false;
        }

        // Hasher le nouveau mot de passe
        String nouveauMdpHash = hashMotDePasse(nouveauMdp);

        // Mettre a jour le mot de passe
        utilisateur.setMdp(nouveauMdpHash);
        boolean resultat = utilisateurDAO.update(utilisateur);

        if (resultat) {
            enregistrerHistorique(idUtilisateur, "CHANGEMENT_MDP", "utilisateur",
                    idUtilisateur, "Changement de mot de passe");
        }

        return resultat;
    }

    /**
     * Hash un mot de passe avec BCrypt
     */
    private String hashMotDePasse(String motDePasse) {
        return BCrypt.hashpw(motDePasse, BCrypt.gensalt());
    }

    /**
     * Verifie si un mot de passe correspond au hash
     */
    private boolean verifierMotDePasse(String motDePasse, String hash) {
        // Si le hash est null ou vide, comparer directement
        if (hash == null || hash.isEmpty()) {
            return false;
        }

        try {
            // Essayer de verifier avec BCrypt
            return BCrypt.checkpw(motDePasse, hash);
        } catch (Exception e) {
            // Si le hash n'est pas un hash BCrypt valide,
            // comparer directement (pour les anciens mots de passe non hashes)
            return motDePasse.equals(hash);
        }
    }
// Inserer dans la base de donnees

    /**
     * Enregistre une connexion dans la table connexion
     */
    private void enregistrerConnexion(int idUtilisateur) {
        // Formater la date actuelle
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateConnexion = LocalDateTime.now().format(formatter);

        // Creer l'enregistrement de connexion
        Connexion connexion = new Connexion(
                0,                  // ID auto-genere
                idUtilisateur,      // ID de l'utilisateur
                dateConnexion,      // Date et heure de connexion
                "127.0.0.1"         // Adresse IP (localhost par defaut)
        );

        connexionDAO.insert(connexion);

        // Enregistrer aussi dans l'historique
        enregistrerHistorique(idUtilisateur, "CONNEXION", "utilisateur",
                idUtilisateur, "Connexion de l'utilisateur");
    }

    /**
     * Enregistre une action dans l'historique pour la tracabilite (RGPD)
     */
    private void enregistrerHistorique(int idUtilisateur, String action, String table, int idEnregistrement, String details) {
        Historique historique = new Historique(
                0,           // ID auto-genere
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
