package com.hsp.controller.dashboard;

import com.hsp.controller.fournisseur.FournisseurFormController;
import com.hsp.controller.reappro.ReapproFormController;
import com.hsp.dao.FournisseurDAO;
import com.hsp.model.Fournisseur;
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
import javafx.stage.Modality;
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
    @FXML private VBox reapproView;
    @FXML private TableView<ObservableList<String>> reapproTable;
    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label pendingDemandesLabel;
    @FXML private TableView<ObservableList<String>> productsTable;
    @FXML private TableView<ObservableList<String>> stockProductsTable;
    @FXML private TableView<ObservableList<String>> demandesTable;
    @FXML private TableView<ObservableList<String>> fournisseursTable;

    private static final String DB_URL      = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";

    private int currentGestionnaireId = -1;
    private String currentGestionnaireName = "Gestionnaire";

    // ============= INIT =============

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProductsTable();
        setupStockProductsTable();
        setupDemandesTable();
        setupFournisseursTable();
        setupReapproTable();
        showDashboard();
    }

    // ============= SETUP COLONNES =============

    @SuppressWarnings("unchecked")
    private void setupProductsTable() {
        if (productsTable == null) return;
        productsTable.getColumns().clear();
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        String[] cols   = {"Produit", "Quantité", "Dangerosité"};
        double[] widths = {0.60,       0.20,        0.20};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            col.setMaxWidth(1f * Integer.MAX_VALUE * widths[i] * 100);
            productsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupStockProductsTable() {
        if (stockProductsTable == null) return;
        stockProductsTable.getColumns().clear();
        stockProductsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // data[0] = id_produit (caché), data[1..4] = données affichées
        String[] cols   = {"Produit",  "Description", "Quantité", "Dangerosité"};
        double[] widths = {0.25,        0.45,           0.15,       0.15};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            col.setMaxWidth(1f * Integer.MAX_VALUE * widths[i] * 100);
            stockProductsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupDemandesTable() {
        if (demandesTable == null) return;
        demandesTable.getColumns().clear();
        demandesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // data[0]=id, [1]=médecin, [2]=produit(s), [3]=qté, [4]=date, [5]=statut
        String[] cols   = {"N°",   "Médecin", "Produit(s)", "Qté",  "Date",  "Statut"};
        double[] widths = {0.05,    0.20,       0.35,         0.07,   0.18,    0.15};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            col.setMaxWidth(1f * Integer.MAX_VALUE * widths[i] * 100);
            demandesTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupFournisseursTable() {
        if (fournisseursTable == null) return;
        fournisseursTable.getColumns().clear();
        fournisseursTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // data[0] = id_fournisseur (caché), data[1..5] = données affichées
        String[] cols   = {"Nom",  "Contact", "Email", "Téléphone", "Adresse"};
        double[] widths = {0.20,    0.15,       0.25,    0.15,        0.25};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            col.setMaxWidth(1f * Integer.MAX_VALUE * widths[i] * 100);
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

    @FXML
    private void showReappro() {
        hideAllViews();
        if (reapproView != null) { reapproView.setVisible(true); reapproView.setManaged(true); }
        loadReappros();
    }

    private void hideAllViews() {
        VBox[] views = {dashboardView, stockView, demandesView, fournisseursView, reapproView};
        for (VBox v : views) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }

    // ============= CHARGEMENT DONNÉES =============

    private void loadDashboardData() {
        try (Connection conn = getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produit")) {
                if (rs.next() && totalProductsLabel != null)
                    totalProductsLabel.setText(String.valueOf(rs.getInt(1)));
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produit WHERE quantite_stock <= 10")) {
                if (rs.next()) {
                    int n = rs.getInt(1);
                    if (lowStockLabel != null) lowStockLabel.setText(String.valueOf(n));
                    if (lowStockCountLabel != null) lowStockCountLabel.setText(String.valueOf(n));
                }
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM demande_produit WHERE statut = 'Attente'")) {
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
        String query = "SELECT id_produit, libelle, description, quantite_stock, niveau_dangerosite FROM produit ORDER BY libelle";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_produit")));                          // 0 – caché
                row.add(rs.getString("libelle") != null ? rs.getString("libelle") : "—"); // 1
                row.add(rs.getString("description") != null ? rs.getString("description") : "—"); // 2
                row.add(String.valueOf(rs.getInt("quantite_stock")));                      // 3
                row.add(String.valueOf(rs.getInt("niveau_dangerosite")));                  // 4
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
                SELECT dp.id_demande,
                       u.nom, u.prenom,
                       GROUP_CONCAT(p.libelle ORDER BY p.libelle SEPARATOR ', ') AS produits,
                       COALESCE(SUM(ld.quantite_demandee), 0) AS total_qte,
                       dp.date_demande,
                       dp.statut
                FROM demande_produit dp
                JOIN utilisateur u ON dp.id_medecin = u.id_utilisateur
                LEFT JOIN ligne_demande ld ON dp.id_demande = ld.id_demande
                LEFT JOIN produit p ON ld.id_produit = p.id_produit
                GROUP BY dp.id_demande, u.nom, u.prenom, dp.date_demande, dp.statut
                ORDER BY dp.date_demande DESC
                """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_demande")));                                    // 0
                row.add("Dr. " + rs.getString("nom") + " " + rs.getString("prenom"));               // 1
                row.add(rs.getString("produits") != null ? rs.getString("produits") : "—");         // 2
                row.add(String.valueOf(rs.getInt("total_qte")));                                     // 3
                row.add(rs.getString("date_demande") != null ? rs.getString("date_demande") : "—"); // 4
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");             // 5
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadDemandes : " + e.getMessage());
        }
        demandesTable.setItems(data);
    }

    private void loadFournisseurs() {
        if (fournisseursTable == null) return;
        String query = "SELECT id_fournisseur, nom, contact, email, telephone, adresse FROM fournisseur ORDER BY nom";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_fournisseur")));                          // 0 – caché
                row.add(rs.getString("nom") != null ? rs.getString("nom") : "—");             // 1
                row.add(rs.getString("contact") != null ? rs.getString("contact") : "—");     // 2
                row.add(rs.getString("email") != null ? rs.getString("email") : "—");         // 3
                row.add(rs.getString("telephone") != null ? rs.getString("telephone") : "—"); // 4
                row.add(rs.getString("adresse")   != null ? rs.getString("adresse")   : "—"); // 5
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadFournisseurs : " + e.getMessage());
        }
        fournisseursTable.setItems(data);
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
        String statut = selected.get(5); // index 5 = statut
        if (!"Attente".equals(statut)) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Seules les demandes en attente peuvent être validées.");
            return;
        }

        int gestionnaireId = currentGestionnaireId > 0 ? currentGestionnaireId : 1;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Vérifier le stock disponible pour chaque ligne avant de valider
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ld.id_produit, ld.quantite_demandee, p.libelle, p.quantite_stock " +
                    "FROM ligne_demande ld JOIN produit p ON ld.id_produit = p.id_produit " +
                    "WHERE ld.id_demande = ?")) {
                ps.setInt(1, demandeId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int dispo = rs.getInt("quantite_stock");
                    int demande = rs.getInt("quantite_demandee");
                    if (dispo < demande) {
                        conn.rollback();
                        showAlert(Alert.AlertType.ERROR, "Stock insuffisant",
                                "Stock insuffisant pour \"" + rs.getString("libelle") + "\" : "
                                + dispo + " disponible(s), " + demande + " demandé(s).");
                        return;
                    }
                }
            }

            // Déduire le stock pour chaque ligne
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_produit, quantite_demandee FROM ligne_demande WHERE id_demande = ?")) {
                ps.setInt(1, demandeId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    try (PreparedStatement psUpdate = conn.prepareStatement(
                            "UPDATE produit SET quantite_stock = quantite_stock - ? WHERE id_produit = ?")) {
                        psUpdate.setInt(1, rs.getInt("quantite_demandee"));
                        psUpdate.setInt(2, rs.getInt("id_produit"));
                        psUpdate.executeUpdate();
                    }
                }
            }

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
        String statut = selected.get(5); // index 5 = statut
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

    // ============= GESTION FOURNISSEURS =============

    @FXML
    private void onAddFournisseur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/FournisseurForm.fxml"));
            Parent root = loader.load();
            FournisseurFormController controller = loader.getController();
            controller.setMode(FournisseurFormController.Mode.CREATION);
            controller.setIdUtilisateurConnecte(currentGestionnaireId > 0 ? currentGestionnaireId : 1);
            Stage stage = new Stage();
            stage.setTitle("Nouveau fournisseur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadFournisseurs();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void onEditFournisseur() {
        if (fournisseursTable == null) return;
        ObservableList<String> selected = fournisseursTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un fournisseur dans la liste.");
            return;
        }
        int fournisseurId = Integer.parseInt(selected.get(0));
        Fournisseur fournisseur = new FournisseurDAO().findById(fournisseurId);
        if (fournisseur == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fournisseur introuvable.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/FournisseurForm.fxml"));
            Parent root = loader.load();
            FournisseurFormController controller = loader.getController();
            controller.setMode(FournisseurFormController.Mode.MODIFICATION);
            controller.setFournisseur(fournisseur);
            controller.setIdUtilisateurConnecte(currentGestionnaireId > 0 ? currentGestionnaireId : 1);
            Stage stage = new Stage();
            stage.setTitle("Modifier le fournisseur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadFournisseurs();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteFournisseur() {
        if (fournisseursTable == null) return;
        ObservableList<String> selected = fournisseursTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un fournisseur dans la liste.");
            return;
        }
        int fournisseurId = Integer.parseInt(selected.get(0));
        String nom = selected.get(1);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer le fournisseur \"" + nom + "\" ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;
            boolean succes = new FournisseurDAO().delete(fournisseurId);
            if (succes) {
                int gestionnaireId = currentGestionnaireId > 0 ? currentGestionnaireId : 1;
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Suppression', 'fournisseur', ?, ?)")) {
                    ps.setInt(1, gestionnaireId);
                    ps.setInt(2, fournisseurId);
                    ps.setString(3, "Suppression fournisseur: " + nom);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Erreur historique : " + e.getMessage());
                }
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Fournisseur \"" + nom + "\" supprimé.");
                loadFournisseurs();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer ce fournisseur (lié à des produits ou réapprovisionnements).");
            }
        });
    }

    // ============= RÉAPPROVISIONNEMENT =============

    @SuppressWarnings("unchecked")
    private void setupReapproTable() {
        if (reapproTable == null) return;
        reapproTable.getColumns().clear();
        reapproTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // data[0] = id_reappro (caché), data[1..5] = colonnes affichées
        String[] cols   = {"Produit", "Fournisseur", "Quantité", "Date commande", "Date réception"};
        double[] widths = {0.28,       0.28,           0.10,       0.17,            0.17};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            col.setMaxWidth(1f * Integer.MAX_VALUE * widths[i] * 100);
            reapproTable.getColumns().add(col);
        }
    }

    private void loadReappros() {
        if (reapproTable == null) return;
        String query = """
                SELECT r.id_reappro,
                       COALESCE(GROUP_CONCAT(DISTINCT p.libelle ORDER BY p.libelle SEPARATOR ', '), '—') AS produits,
                       COALESCE(GROUP_CONCAT(DISTINCT f.nom ORDER BY f.nom SEPARATOR ', '), '—') AS fournisseurs,
                       COALESCE(SUM(lr.quantite_commandee), 0) AS total_quantite,
                       r.date_commande,
                       r.date_reception
                FROM reapprovisionnement r
                LEFT JOIN ligne_reapprovisionnement lr ON r.id_reappro = lr.id_reappro
                LEFT JOIN produit p ON lr.id_produit = p.id_produit
                LEFT JOIN fournisseur f ON lr.id_fournisseur = f.id_fournisseur
                GROUP BY r.id_reappro, r.date_commande, r.date_reception
                ORDER BY r.date_commande DESC
                """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_reappro")));
                row.add(rs.getString("produits") != null ? rs.getString("produits") : "—");
                row.add(rs.getString("fournisseurs") != null ? rs.getString("fournisseurs") : "—");
                row.add(String.valueOf(rs.getLong("total_quantite")));
                row.add(rs.getDate("date_commande") != null ? rs.getDate("date_commande").toString() : "—");
                row.add(rs.getDate("date_reception") != null ? rs.getDate("date_reception").toString() : "En attente");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadReappros : " + e.getMessage());
        }
        reapproTable.setItems(data);
    }

    @FXML
    private void onAddReappro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/ReapproForm.fxml"));
            Parent root = loader.load();
            ReapproFormController controller = loader.getController();
            controller.setMode(ReapproFormController.Mode.CREATION);
            controller.setIdGestionnaire(currentGestionnaireId > 0 ? currentGestionnaireId : 1);
            Stage stage = new Stage();
            stage.setTitle("Nouveau réapprovisionnement");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadReappros();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void onConfirmReception() {
        if (reapproTable == null) return;
        ObservableList<String> selected = reapproTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un réapprovisionnement.");
            return;
        }
        if (!"En attente".equals(selected.get(5))) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Ce réapprovisionnement a déjà été réceptionné.");
            return;
        }
        int reapproId = Integer.parseInt(selected.get(0));
        int gestionnaireId = currentGestionnaireId > 0 ? currentGestionnaireId : 1;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Mettre à jour le stock pour chaque ligne de réappro
            int totalQuantite = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_produit, quantite_commandee FROM ligne_reapprovisionnement WHERE id_reappro = ?")) {
                ps.setInt(1, reapproId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int idProduit = rs.getInt("id_produit");
                    int quantite  = rs.getInt("quantite_commandee");
                    totalQuantite += quantite;
                    try (PreparedStatement psUpdate = conn.prepareStatement(
                            "UPDATE produit SET quantite_stock = quantite_stock + ? WHERE id_produit = ?")) {
                        psUpdate.setInt(1, quantite);
                        psUpdate.setInt(2, idProduit);
                        psUpdate.executeUpdate();
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE reapprovisionnement SET date_reception = CURDATE() WHERE id_reappro = ?")) {
                ps.setInt(1, reapproId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Reception', 'reapprovisionnement', ?, ?)")) {
                ps.setInt(1, gestionnaireId);
                ps.setInt(2, reapproId);
                ps.setString(3, "Réception réappro #" + reapproId + " — stock +" + totalQuantite);
                ps.executeUpdate();
            }
            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réception confirmée. Stock mis à jour (+" + totalQuantite + " unités).");
            loadReappros();
            loadDashboardData();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
        }
    }

    @FXML
    private void onDeleteReappro() {
        if (reapproTable == null) return;
        ObservableList<String> selected = reapproTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un réapprovisionnement.");
            return;
        }
        int reapproId = Integer.parseInt(selected.get(0));
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer le réapprovisionnement #" + reapproId + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "DELETE FROM reapprovisionnement WHERE id_reappro = ?")) {
                ps.setInt(1, reapproId);
                ps.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Réapprovisionnement supprimé.");
                loadReappros();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
            }
        });
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
