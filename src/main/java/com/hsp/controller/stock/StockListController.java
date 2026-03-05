package com.hsp.controller.stock;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur principal du module Stock - Tableau de bord
 * Permet d'accéder aux différentes fonctionnalités de gestion des stocks
 */
public class StockListController implements Initializable {

    @FXML
    private Label titre;

    @FXML
    private Button btnProduits;

    @FXML
    private Button btnFournisseurs;

    @FXML
    private Button btnDemandes;

    @FXML
    private Button btnReapprovisionnement;

    @FXML
    private Button btnStatistiques;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (titre != null) {
            titre.setText("Gestion des Stocks");
        }
    }

    @FXML
    private void ouvrirProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ProduitList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Produits");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la gestion des produits.");
        }
    }

    @FXML
    private void ouvrirFournisseurs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/FournisseurList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Fournisseurs");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la gestion des fournisseurs.");
        }
    }

    @FXML
    private void ouvrirDemandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/DemandeList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Demandes");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la gestion des demandes.");
        }
    }

    @FXML
    private void ouvrirReapprovisionnement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ReapproList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Réapprovisionnements");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la gestion des réapprovisionnements.");
        }
    }

    @FXML
    private void ouvrirHistorique() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/historique/HistoriqueList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Historique");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la gestion des Historiques.");
        }
    }

    private void afficherErreur(String titre, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}