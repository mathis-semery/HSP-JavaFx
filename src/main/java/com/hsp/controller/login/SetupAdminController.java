package com.hsp.controller.login;

import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Utilisateur;
import com.hsp.util.PasswordUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SetupAdminController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    private void onCreateClicked() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Tous les champs sont obligatoires.");
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("Format d'email invalide.");
            return;
        }
        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        Utilisateur admin = new Utilisateur(0, nom, prenom, email, PasswordUtils.hash(password), "Admin", null);
        boolean success = new UtilisateurDAO().insert(admin);

        if (success) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/Login.fxml"));
                Stage stage = (Stage) nomField.getScene().getWindow();
                stage.setScene(new Scene(loader.load(), 800, 600));
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors du chargement de la page de connexion.");
            }
        } else {
            showError("Erreur lors de la création. L'email est peut-être déjà utilisé.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}