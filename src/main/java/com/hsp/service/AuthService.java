package com.hsp.service;

import com.hsp.dao.ConnexionDAO;
import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Connexion;
import com.hsp.model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuthService {

    private final UtilisateurDAO utilisateurDAO;
    private final ConnexionDAO connexionDAO;
    private Utilisateur utilisateurConnecte;

    public AuthService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.connexionDAO = new ConnexionDAO();
    }

    public Utilisateur login(String email, String motDePasse) {
        Utilisateur utilisateur = utilisateurDAO.findByEmail(email);

        if (utilisateur == null) {
            return null;
        }

        if (verifierMotDePasse(motDePasse, utilisateur.getMdp())) {
            this.utilisateurConnecte = utilisateur;
            return utilisateur;
        }

        return null;
    }

    public void logout() {
        this.utilisateurConnecte = null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }

    public boolean hasRole(String role) {
        if (utilisateurConnecte == null) {
            return false;
        }
        return role.equalsIgnoreCase(utilisateurConnecte.getRole());
    }

    public boolean changerMotDePasse(int idUtilisateur, String ancienMdp, String nouveauMdp) {
        Utilisateur utilisateur = utilisateurDAO.findById(idUtilisateur);

        if (utilisateur == null) {
            return false;
        }

        if (!verifierMotDePasse(ancienMdp, utilisateur.getMdp())) {
            return false;
        }

        utilisateur.setMdp(hashMotDePasse(nouveauMdp));
        return utilisateurDAO.update(utilisateur);
    }

    private String hashMotDePasse(String motDePasse) {
        return BCrypt.hashpw(motDePasse, BCrypt.gensalt());
    }

    private boolean verifierMotDePasse(String motDePasse, String hash) {
        try {
            return BCrypt.checkpw(motDePasse, hash);
        } catch (Exception e) {
            return motDePasse.equals(hash);
        }
    }


}
