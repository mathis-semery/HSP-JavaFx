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
import java.sql.*;
import java.util.ResourceBundle;

import static com.hsp.controller.login.LoginController.*;

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
    @FXML private ComboBox<Integer> dangerositeProduitComboBox;
    @FXML private TextField descriptionProduitField;

    private String currentGestionnaireName = "Gestionnaire";
    private int currentGestionnaireId = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (userNameLabel != null) userNameLabel.setText(currentGestionnaireName);

        if (categorieComboBox != null) {
            categorieComboBox.getItems().addAll("Médicaments", "Matériel médical", "Consommables", "Équipements");
        }

        if (dangerositeProduitComboBox != null) {
            dangerositeProduitComboBox.getItems().addAll(1, 2, 3, 4, 5);
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
        if (dangerositeProduitComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le niveau de dangerosité est obligatoire");
            return;
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String query = "INSERT INTO produit (libelle, description, niveau_dangerosite, quantite_stock) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, productNameField.getText());
            pstmt.setString(2, descriptionProduitField.getText());
            pstmt.setInt(3, dangerositeProduitComboBox.getValue());
            pstmt.setInt(4, Integer.parseInt(quantityField.getText()));
            pstmt.executeUpdate();

            // Récupérer l'ID du produit
            ResultSet rs = pstmt.getGeneratedKeys();
            int productId = 0;
            if (rs.next()) {
                productId = rs.getInt(1);
            }

            // Enregistrer dans l'historique
            String queryHisto = "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Creation', 'produit', ?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(queryHisto);
            pstmt2.setInt(1, currentGestionnaireId);
            pstmt2.setInt(2, productId);
            pstmt2.setString(3, "Produit: " + productNameField.getText());
            pstmt2.executeUpdate();

            pstmt.close();
            pstmt2.close();
            conn.close();


            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté au stock !");

            productNameField.clear();
            quantityField.clear();
            descriptionProduitField.clear();
            dangerositeProduitComboBox.setValue(null);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur BD : " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être un nombre");
        }
    }

    @FXML
    private void onValidateDemande() {
        int demandeId = 1; // À remplacer

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String query = "UPDATE demande_produit SET statut = 'Validee', id_gestionnaire = ? WHERE id_demande = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentGestionnaireId);
            pstmt.setInt(2, demandeId);
            pstmt.executeUpdate();

            // Valider aussi les lignes de demande
            String queryLignes = "UPDATE ligne_demande SET statut = 'Validee' WHERE id_demande = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(queryLignes);
            pstmt2.setInt(1, demandeId);
            pstmt2.executeUpdate();

            pstmt.close();
            pstmt2.close();
            conn.close();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande validée !");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur BD : " + e.getMessage());
        }
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