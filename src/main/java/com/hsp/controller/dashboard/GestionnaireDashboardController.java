package com.hsp.controller.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GestionnaireDashboardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private VBox dashboardView;
    @FXML private VBox stockView;
    @FXML private VBox demandesView;
    @FXML private VBox fournisseursView;
    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label pendingDemandesLabel;
    @FXML private TableView<?> productsTable;
    @FXML private TableView<?> demandesTable;
    @FXML private TableView<?> fournisseursTable;
    @FXML private TextField productNameField;
    @FXML private TextField quantityField;
    @FXML private TextField seuilField;
    @FXML private ComboBox<String> categorieComboBox;

    private String currentGestionnaireName = "Gestionnaire";
    private int currentGestionnaireId = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (userNameLabel != null) userNameLabel.setText(currentGestionnaireName);
        if (categorieComboBox != null) {
            categorieComboBox.getItems().addAll("Médicaments", "Matériel médical", "Consommables", "Équipements");
        }
        loadDashboardData();
        showDashboard();
    }

    @FXML private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) { dashboardView.setVisible(true); dashboardView.setManaged(true); }
        loadDashboardData();
    }

    @FXML private void showStock() {
        hideAllViews();
        if (stockView != null) { stockView.setVisible(true); stockView.setManaged(true); }
    }

    @FXML private void showDemandes() {
        hideAllViews();
        if (demandesView != null) { demandesView.setVisible(true); demandesView.setManaged(true); }
    }

    @FXML private void showFournisseurs() {
        hideAllViews();
        if (fournisseursView != null) { fournisseursView.setVisible(true); fournisseursView.setManaged(true); }
    }

    private void hideAllViews() {
        if (dashboardView != null) { dashboardView.setVisible(false); dashboardView.setManaged(false); }
        if (stockView != null) { stockView.setVisible(false); stockView.setManaged(false); }
        if (demandesView != null) { demandesView.setVisible(false); demandesView.setManaged(false); }
        if (fournisseursView != null) { fournisseursView.setVisible(false); fournisseursView.setManaged(false); }
    }

    private void loadDashboardData() {
        if (totalProductsLabel != null) totalProductsLabel.setText("156");
        if (lowStockLabel != null) lowStockLabel.setText("12");
        if (pendingDemandesLabel != null) pendingDemandesLabel.setText("5");
        if (lowStockCountLabel != null) lowStockCountLabel.setText("12");
    }

    @FXML
    private void onAddProduct() {
        if (productNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le nom est obligatoire");
            return;
        }
        if (quantityField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "La quantité est obligatoire");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté au stock !");
        productNameField.clear();
        quantityField.clear();
        seuilField.clear();
        categorieComboBox.setValue(null);
    }

    @FXML
    private void onValidateDemande() {
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande validée !");
    }

    @FXML
    private void onRejectDemande() {
        showAlert(Alert.AlertType.INFORMATION, "Rejet", "Demande rejetée");
    }

    @FXML
    private void onLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/Login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) userNameLabel.getScene().getWindow();
                    stage.setScene(new Scene(root, 900, 700));
                    stage.setTitle("Connexion - HSP Urgences");
                    stage.show();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion");
                }
            }
        });
    }

    public void setGestionnaireInfo(int id, String name) {
        this.currentGestionnaireId = id;
        this.currentGestionnaireName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}