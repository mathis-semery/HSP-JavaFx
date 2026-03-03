package com.hsp.controller.utilisateur;

import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.util.ResourceBundle;

public class UtilisateurFormController implements Initializable {

    public enum Mode {
        CREATION,
        MODIFICATION
    }

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField mdpField;
    @FXML private ComboBox<String> roleField;
    @FXML private Button valider;
    @FXML private Button annuler;
    @FXML private Label titre;

    private Mode mode = Mode.CREATION;
    private Utilisateur utilisateur;
    private UtilisateurDAO utilisateurDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        utilisateurDAO = new UtilisateurDAO();
        if (roleField != null) {
            roleField.getItems().addAll("Medecin", "Secretaire", "Gestionnaire");
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        mettreAJourTitre();
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        remplirFormulaire();
    }

    private void mettreAJourTitre() {
        if (titre != null) {
            titre.setText(mode == Mode.CREATION ? "Nouvel utilisateur" : "Modifier l'utilisateur");
        }
    }

    private void remplirFormulaire() {
        if (utilisateur == null) return;
        if (nomField != null) nomField.setText(utilisateur.getNom());
        if (prenomField != null) prenomField.setText(utilisateur.getPrenom());
        if (emailField != null) emailField.setText(utilisateur.getEmail());
        if (roleField != null) roleField.setValue(utilisateur.getRole());
        // Ne pas pré-remplir le mot de passe pour des raisons de sécurité
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) return;

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String mdp = mdpField.getText();
        String role = roleField.getValue();

        boolean succes;

        if (mode == Mode.CREATION) {
            String mdpHash = BCrypt.hashpw(mdp, BCrypt.gensalt());
            Utilisateur nouvelUtilisateur = new Utilisateur(0, nom, prenom, email, mdpHash, role, null);
            succes = utilisateurDAO.insert(nouvelUtilisateur);
        } else {
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setEmail(email);
            utilisateur.setRole(role);
            if (!mdp.isEmpty()) {
                utilisateur.setMdp(BCrypt.hashpw(mdp, BCrypt.gensalt()));
            }
            succes = utilisateurDAO.update(utilisateur);
        }

        if (succes) {
            fermer();
        } else {
            afficherErreur("Erreur", "Impossible d'enregistrer l'utilisateur.");
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (nomField == null || nomField.getText().trim().isEmpty()) {
            erreurs.append("- Le nom est obligatoire\n");
        }

        if (prenomField == null || prenomField.getText().trim().isEmpty()) {
            erreurs.append("- Le prénom est obligatoire\n");
        }

        if (emailField == null || emailField.getText().trim().isEmpty()) {
            erreurs.append("- L'email est obligatoire\n");
        } else if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            erreurs.append("- L'adresse email n'est pas valide\n");
        }

        if (mode == Mode.CREATION && (mdpField == null || mdpField.getText().isEmpty())) {
            erreurs.append("- Le mot de passe est obligatoire\n");
        }

        if (roleField == null || roleField.getValue() == null) {
            erreurs.append("- Le rôle est obligatoire\n");
        }

        if (erreurs.length() > 0) {
            afficherErreur("Erreurs de validation", erreurs.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) valider.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
