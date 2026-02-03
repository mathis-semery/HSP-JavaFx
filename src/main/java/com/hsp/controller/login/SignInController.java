package com.hsp.controller.login;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignInController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    /**
     * Méthode appelée quand l'utilisateur clique sur "S'inscrire"
     */
    @FXML
    private void handleSignUp() {
        // Récupérer les valeurs des champs
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation des champs vides
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", true);
            return;
        }

        // Validation du prénom et nom (au moins 2 caractères)
        if (firstName.length() < 2 || lastName.length() < 2) {
            showMessage("Le prénom et le nom doivent contenir au moins 2 caractères", true);
            return;
        }

        // Validation de l'email (format simple)
        if (!email.contains("@") || !email.contains(".")) {
            showMessage("Veuillez entrer une adresse email valide", true);
            return;
        }

        // Validation du nom d'utilisateur (au moins 4 caractères)
        if (username.length() < 4) {
            showMessage("Le nom d'utilisateur doit contenir au moins 4 caractères", true);
            return;
        }

        // Validation du mot de passe (au moins 6 caractères)
        if (password.length() < 6) {
            showMessage("Le mot de passe doit contenir au moins 6 caractères", true);
            return;
        }

        // Vérification de la correspondance des mots de passe
        if (!password.equals(confirmPassword)) {
            showMessage("Les mots de passe ne correspondent pas", true);
            return;
        }

        // Si toutes les validations passent
        showMessage("Inscription réussie ! Bienvenue " + firstName + " " + lastName + " !", false);

        // Ici vous pourriez :
        // - Enregistrer l'utilisateur dans une base de données
        // - Rediriger vers la page de connexion
        // - Envoyer un email de confirmation

        // Pour l'exemple, on vide les champs après inscription
        clearFields();
    }

    /**
     * Affiche un message à l'utilisateur
     */
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);

        if (isError) {
            messageLabel.setStyle("-fx-text-fill: red;");
        } else {
            messageLabel.setStyle("-fx-text-fill: green;");
        }
    }

    /**
     * Vide tous les champs du formulaire
     */
    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}