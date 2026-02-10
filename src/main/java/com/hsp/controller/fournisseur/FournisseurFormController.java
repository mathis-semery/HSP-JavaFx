package com.hsp.controller.fournisseur;

import com.hsp.dao.FournisseurDAO;
import com.hsp.model.Fournisseur;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class FournisseurFormController implements Initializable {

    public enum Mode {
        CREATION,
        MODIFICATION
    }

    @FXML
    private TextField nomField;

    @FXML
    private TextField contactField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextArea adresseField;

    @FXML
    private Button valider;

    @FXML
    private Button annuler;

    @FXML
    private Label titre;

    private Mode mode = Mode.CREATION;
    private Fournisseur fournisseur;
    private FournisseurDAO fournisseurDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fournisseurDAO = new FournisseurDAO();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        mettreAJourTitre();
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
        remplirFormulaire();
    }

    private void mettreAJourTitre() {
        if (titre != null) {
            if (mode == Mode.CREATION) {
                titre.setText("Nouveau fournisseur");
            } else {
                titre.setText("Modifier le fournisseur");
            }
        }
    }

    private void remplirFormulaire() {
        if (fournisseur == null) {
            return;
        }

        if (nomField != null) {
            nomField.setText(fournisseur.getNom());
        }

        if (contactField != null) {
            contactField.setText(fournisseur.getContact());
        }

        if (emailField != null) {
            emailField.setText(fournisseur.getEmail());
        }

        if (telephoneField != null) {
            telephoneField.setText(fournisseur.getTelephone());
        }

        if (adresseField != null) {
            adresseField.setText(fournisseur.getAdresse());
        }
    }

    @FXML
    private void valider() {
        if (!validerFormulaire()) {
            return;
        }

        String nom = nomField.getText().trim();
        String contact = contactField != null ? contactField.getText().trim() : "";
        String email = emailField != null ? emailField.getText().trim() : "";
        String telephone = telephoneField != null ? telephoneField.getText().trim() : "";
        String adresse = adresseField != null ? adresseField.getText().trim() : "";

        boolean succes;

        if (mode == Mode.CREATION) {
            Fournisseur nouveauFournisseur = new Fournisseur(
                    0,
                    nom,
                    contact,
                    email,
                    telephone,
                    adresse,
                    LocalDate.now()
            );
            succes = fournisseurDAO.insert(nouveauFournisseur);
        } else {
            fournisseur.setNom(nom);
            fournisseur.setContact(contact);
            fournisseur.setEmail(email);
            fournisseur.setTelephone(telephone);
            fournisseur.setAdresse(adresse);
            succes = fournisseurDAO.update(fournisseur);
        }

        if (succes) {
            fermer();
        } else {
            afficherErreur("Erreur", "Impossible d'enregistrer le fournisseur.");
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (nomField == null || nomField.getText().trim().isEmpty()) {
            erreurs.append("- Le nom est obligatoire\n");
        }

        if (nomField != null && nomField.getText().trim().length() > 100) {
            erreurs.append("- Le nom ne peut pas dépasser 100 caractères\n");
        }

        if (emailField != null && !emailField.getText().trim().isEmpty()) {
            String email = emailField.getText().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                erreurs.append("- L'adresse email n'est pas valide\n");
            }
        }

        if (telephoneField != null && !telephoneField.getText().trim().isEmpty()) {
            String telephone = telephoneField.getText().trim();
            if (!telephone.matches("^[0-9+\\-\\s()]{8,20}$")) {
                erreurs.append("- Le numéro de téléphone n'est pas valide\n");
            }
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
