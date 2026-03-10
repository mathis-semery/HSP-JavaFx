package com.hsp.controller.dashboard;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.Optional;
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
    @FXML private TableView<ObservableList<String>> productsTable;
    @FXML private TableView<ObservableList<String>> stockProductsTable;
    @FXML private TableView<ObservableList<String>> demandesTable;
    @FXML private TableView<ObservableList<String>> fournisseursTable;
    @FXML private TextField productNameField;
    @FXML private TextField quantityField;
    @FXML private TextField seuilField;
    @FXML private TextField descriptionProduitField;
    @FXML private ComboBox<Integer> dangerositeProduitComboBox;

    private static final String DB_URL      = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";

    private int currentGestionnaireId = -1;
    private String currentGestionnaireName = "Gestionnaire";

    // ============= INIT =============

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (dangerositeProduitComboBox != null)
            dangerositeProduitComboBox.getItems().addAll(1, 2, 3, 4, 5);
        setupProductsTable();
        setupStockProductsTable();
        setupDemandesTable();
        setupFournisseursTable();
        showDashboard();
    }

    // ============= SETUP COLONNES =============

    @SuppressWarnings("unchecked")
    private void setupProductsTable() {
        if (productsTable == null) return;
        productsTable.getColumns().clear();
        String[] cols = {"Produit", "Quantité", "Dangerosité"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            productsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupStockProductsTable() {
        if (stockProductsTable == null) return;
        stockProductsTable.getColumns().clear();
        String[] cols = {"Produit", "Quantité", "Dangerosité"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            stockProductsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupDemandesTable() {
        if (demandesTable == null) return;
        demandesTable.getColumns().clear();
        String[] cols = {"N°", "Médecin", "Date", "Statut"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            demandesTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupFournisseursTable() {
        if (fournisseursTable == null) return;
        fournisseursTable.getColumns().clear();
        String[] cols = {"Nom", "Email", "Téléphone"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            fournisseursTable.getColumns().add(col);
        }
    }

    // ============= NAVIGATION =============

    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) { dashboardView.setVisible(true); dashboardView.setManaged(true); }
        loadDashboardData();
        loadProductsDashboard();
    }

    @FXML
    private void showStock() {
        hideAllViews();
        if (stockView != null) { stockView.setVisible(true); stockView.setManaged(true); }
        loadStockProducts();
    }

    @FXML
    private void showDemandes() {
        hideAllViews();
        if (demandesView != null) { demandesView.setVisible(true); demandesView.setManaged(true); }
        loadDemandes();
    }

    @FXML
    private void showFournisseurs() {
        hideAllViews();
        if (fournisseursView != null) { fournisseursView.setVisible(true); fournisseursView.setManaged(true); }
        loadFournisseurs();
    }

    private void hideAllViews() {
        VBox[] views = {dashboardView, stockView, demandesView, fournisseursView};
        for (VBox v : views) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }

    // ============= CHARGEMENT DONNÉES =============

    private void loadDashboardData() {
        try (Connection conn = getConnection()) {
            // Total produits
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produit")) {
                if (rs.next() && totalProductsLabel != null)
                    totalProductsLabel.setText(String.valueOf(rs.getInt(1)));
            }
            // Stock faible (≤ 10)
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produit WHERE quantite_stock <= 10")) {
                if (rs.next()) {
                    int n = rs.getInt(1);
                    if (lowStockLabel != null) lowStockLabel.setText(String.valueOf(n));
                    if (lowStockCountLabel != null) lowStockCountLabel.setText(String.valueOf(n));
                }
            }
            // Demandes en attente
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM demande_produit WHERE statut = 'Attente'")) {
                if (rs.next() && pendingDemandesLabel != null)
                    pendingDemandesLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadDashboardData (Gestionnaire) : " + e.getMessage());
        }
    }

    private void loadProductsDashboard() {
        if (productsTable == null) return;
        String query = "SELECT libelle, quantite_stock, niveau_dangerosite FROM produit ORDER BY quantite_stock ASC LIMIT 20";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("libelle") != null ? rs.getString("libelle") : "—");
                row.add(String.valueOf(rs.getInt("quantite_stock")));
                row.add(String.valueOf(rs.getInt("niveau_dangerosite")));
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadProductsDashboard : " + e.getMessage());
        }
        productsTable.setItems(data);
    }

    private void loadStockProducts() {
        if (stockProductsTable == null) return;
        String query = "SELECT libelle, quantite_stock, niveau_dangerosite FROM produit ORDER BY libelle";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("libelle") != null ? rs.getString("libelle") : "—");
                row.add(String.valueOf(rs.getInt("quantite_stock")));
                row.add(String.valueOf(rs.getInt("niveau_dangerosite")));
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadStockProducts : " + e.getMessage());
        }
        stockProductsTable.setItems(data);
    }

    private void loadDemandes() {
        if (demandesTable == null) return;
        String query = """
                SELECT dp.id_demande, u.nom, u.prenom, dp.date_demande, dp.statut
                FROM demande_produit dp
                JOIN utilisateur u ON dp.id_medecin = u.id_utilisateur
                ORDER BY dp.date_demande DESC
                """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_demande")));
                row.add("Dr. " + rs.getString("nom") + " " + rs.getString("prenom"));
                row.add(rs.getString("date_demande") != null ? rs.getString("date_demande") : "—");
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadDemandes : " + e.getMessage());
        }
        demandesTable.setItems(data);
    }

    private void loadFournisseurs() {
        if (fournisseursTable == null) return;
        String query = "SELECT nom, email, telephone FROM fournisseur ORDER BY nom";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("nom") != null ? rs.getString("nom") : "—");
                row.add(rs.getString("email") != null ? rs.getString("email") : "—");
                row.add(rs.getString("telephone") != null ? rs.getString("telephone") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadFournisseurs : " + e.getMessage());
        }
        fournisseursTable.setItems(data);
    }

    // ============= GESTION PRODUITS =============

    @FXML
    private void onAddProduct() {
        if (productNameField == null || productNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le nom est obligatoire");
            return;
        }
        if (quantityField == null || quantityField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "La quantité est obligatoire");
            return;
        }
        if (dangerositeProduitComboBox == null || dangerositeProduitComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le niveau de dangerosité est obligatoire");
            return;
        }

        int gestionnaireId = currentGestionnaireId > 0 ? currentGestionnaireId : 1;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            int productId;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO produit (libelle, description, niveau_dangerosite, quantite_stock) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, productNameField.getText().trim());
                pstmt.setString(2, descriptionProduitField != null ? descriptionProduitField.getText().trim() : "");
                pstmt.setInt(3, dangerositeProduitComboBox.getValue());
                pstmt.setInt(4, Integer.parseInt(quantityField.getText().trim()));
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                productId = rs.next() ? rs.getInt(1) : 0;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Creation', 'produit', ?, ?)")) {
                pstmt.setInt(1, gestionnaireId);
                pstmt.setInt(2, productId);
                pstmt.setString(3, "Produit ajouté: " + productNameField.getText());
                pstmt.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté au stock !");
            productNameField.clear();
            quantityField.clear();
            if (descriptionProduitField != null) descriptionProduitField.clear();
            dangerositeProduitComboBox.setValue(null);
            loadStockProducts();
            loadDashboardData();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être un nombre entier.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    // ============= GESTION DEMANDES =============

    @FXML
    private void onValidateDemande() {
        if (demandesTable == null) return;
        ObservableList<String> selected = demandesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une demande.");
            return;
        }

        int demandeId = Integer.parseInt(selected.get(0));
        String statut = selected.get(3);
        if (!"Attente".equals(statut)) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Seules les demandes en attente peuvent être validées.");
            return;
        }

        int gestionnaireId = currentGestionnaireId > 0 ? currentGestionnaireId : 1;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE demande_produit SET statut = 'Validee', id_gestionnaire = ? WHERE id_demande = ?")) {
                pstmt.setInt(1, gestionnaireId);
                pstmt.setInt(2, demandeId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Validation', 'demande_produit', ?, 'Demande validée')")) {
                pstmt.setInt(1, gestionnaireId);
                pstmt.setInt(2, demandeId);
                pstmt.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande #" + demandeId + " validée !");
            loadDemandes();
            loadDashboardData();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    @FXML
    private void onRejectDemande() {
        if (demandesTable == null) return;
        ObservableList<String> selected = demandesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une demande.");
            return;
        }

        int demandeId = Integer.parseInt(selected.get(0));
        String statut = selected.get(3);
        if (!"Attente".equals(statut)) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Seules les demandes en attente peuvent être refusées.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Refus de demande");
        dialog.setHeaderText("Motif du refus pour la demande #" + demandeId);
        dialog.setContentText("Motif :");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) return;
        String motif = result.get().trim();

        int gestionnaireId = currentGestionnaireId > 0 ? currentGestionnaireId : 1;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE demande_produit SET statut = 'Refusee', id_gestionnaire = ?, motif_refus = ? WHERE id_demande = ?")) {
                pstmt.setInt(1, gestionnaireId);
                pstmt.setString(2, motif);
                pstmt.setInt(3, demandeId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Refus', 'demande_produit', ?, ?)")) {
                pstmt.setInt(1, gestionnaireId);
                pstmt.setInt(2, demandeId);
                pstmt.setString(3, "Motif: " + motif);
                pstmt.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Refusée", "Demande #" + demandeId + " refusée.");
            loadDemandes();
            loadDashboardData();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    // ============= DÉCONNEXION =============

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

    // ============= API PUBLIQUE =============

    public void setGestionnaireInfo(int id, String name) {
        this.currentGestionnaireId = id;
        this.currentGestionnaireName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
        loadDashboardData();
        loadProductsDashboard();
    }

    // ============= UTILS =============

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
