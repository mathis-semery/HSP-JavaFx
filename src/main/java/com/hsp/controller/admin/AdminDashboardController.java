package com.hsp.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controleur du tableau de bord Administrateur.
 * L'admin a acces a tous les modules de l'application.
 */
public class AdminDashboardController implements Initializable {

    @FXML private Label lblBienvenue;
    @FXML private Button btnPatients;
    @FXML private Button btnDossiers;
    @FXML private Button btnHospitalisations;
    @FXML private Button btnChambres;
    @FXML private Button btnProduits;
    @FXML private Button btnFournisseurs;
    @FXML private Button btnDemandes;
    @FXML private Button btnReappro;
    @FXML private Button btnDeconnexion;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (lblBienvenue != null) {
            lblBienvenue.setText("Tableau de bord - Administrateur");
        }
    }

    @FXML
    private void ouvrirPatients() {
        chargerVue("/view/patient/PatientList.fxml", "Gestion des Patients");
    }

    @FXML
    private void ouvrirDossiers() {
        chargerVue("/view/dossier/DossierList.fxml", "Gestion des Dossiers");
    }

    @FXML
    private void ouvrirHospitalisations() {
        chargerVue("/view/hospitalisation/HospitalisationList.fxml", "Gestion des Hospitalisations");
    }

    @FXML
    private void ouvrirChambres() {
        chargerVue("/view/hospitalisation/ChambreGrid.fxml", "Gestion des Chambres");
    }

    @FXML
    private void ouvrirProduits() {
        chargerVue("/view/stock/ProduitList.fxml", "Gestion des Produits");
    }

    @FXML
    private void ouvrirFournisseurs() {
        chargerVue("/view/stock/FournisseurList.fxml", "Gestion des Fournisseurs");
    }

    @FXML
    private void ouvrirDemandes() {
        chargerVue("/view/stock/DemandeList.fxml", "Gestion des Demandes");
    }

    @FXML
    private void ouvrirReappro() {
        chargerVue("/view/stock/ReapproList.fxml", "Gestion des Reapprovisionnements");
    }

    @FXML
    private void deconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("HSP Urgences - Connexion");
            stage.show();
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de revenir a la page de connexion : " + e.getMessage());
        }
    }

    private void chargerVue(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnPatients.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle(titre);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir " + titre + " : " + e.getMessage());
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
