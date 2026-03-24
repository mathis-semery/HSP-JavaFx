package com.hsp.controller.dashboard;

import com.hsp.controller.utilisateur.UtilisateurFormController;
import com.hsp.dao.*;
import com.hsp.model.Historique;
import com.hsp.model.Utilisateur;
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
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label totalUsersLabel;

    @FXML private VBox dashboardView;
    @FXML private VBox usersView;
    @FXML private VBox systemView;
    @FXML private VBox logsView;
    @FXML private VBox statsView;
    @FXML private VBox patientsView;
    @FXML private VBox hospitalisationsView;
    @FXML private VBox ordonnancesView;
    @FXML private VBox produitsView;
    @FXML private VBox fournisseursView;
    @FXML private VBox demandesView;

    @FXML private Label totalUtilisateursLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalHospitalisationsLabel;
    @FXML private Label systemStatusLabel;
    @FXML private Label totalProduitsLabel;
    @FXML private Label totalFournisseursLabel;
    @FXML private Label totalDemandesAttenteLabel;
    @FXML private Label totalOrdonnancesActivesLabel;

    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, String> userIdCol;
    @FXML private TableColumn<Utilisateur, String> userNomCol;
    @FXML private TableColumn<Utilisateur, String> userPrenomCol;
    @FXML private TableColumn<Utilisateur, String> userEmailCol;
    @FXML private TableColumn<Utilisateur, String> userRoleCol;
    @FXML private TableColumn<Utilisateur, String> userDateCol;
    @FXML private TextField searchUserField;

    @FXML private TableView<ObservableList<String>> patientsTable;
    @FXML private TableView<ObservableList<String>> hospitalisationsTable;
    @FXML private TableView<ObservableList<String>> ordonnancesTable;
    @FXML private TableView<ObservableList<String>> produitsTable;
    @FXML private TableView<ObservableList<String>> fournisseursTable;
    @FXML private TableView<ObservableList<String>> demandesTable;

    @FXML private TextField searchPatientField;
    @FXML private TextField searchProduitField;
    @FXML private TextField searchFournisseurField;
    @FXML private ComboBox<String> demandeStatutFilter;

    @FXML private TextField hospitalNameField;
    @FXML private TextField hospitalAddressField;
    @FXML private TextField hospitalPhoneField;
    @FXML private TextField maxPatientsField;
    @FXML private CheckBox maintenanceModeCheckBox;

    @FXML private TableView<Historique> logsTable;
    @FXML private TableColumn<Historique, String> logDateCol;
    @FXML private TableColumn<Historique, String> logUtilisateurCol;
    @FXML private TableColumn<Historique, String> logActionCol;
    @FXML private TableColumn<Historique, String> logTableCol;
    @FXML private TableColumn<Historique, String> logDetailsCol;
    @FXML private ComboBox<String> logLevelFilter;
    @FXML private DatePicker logDateFilter;

    @FXML private Label statsPatientsMoisLabel;
    @FXML private Label statsConsultationsMoyenneLabel;
    @FXML private Label statsTauxOccupationLabel;

    private String currentAdminName = "Administrateur";
    private int currentAdminId = 1;
    private UtilisateurDAO utilisateurDAO;
    private HistoriqueDAO historiqueDAO;
    private ObservableList<Utilisateur> utilisateurs;
    private ObservableList<Historique> historiques;
    private Map<Integer, String> userNames = new HashMap<>();

    private static final String DB_URL = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utilisateurDAO = new UtilisateurDAO();
        historiqueDAO = new HistoriqueDAO();
        utilisateurs = FXCollections.observableArrayList();
        historiques = FXCollections.observableArrayList();

        if (userNameLabel != null) userNameLabel.setText(currentAdminName);

        configurerTableUtilisateurs();
        configurerTableLogs();

        setupPatientsTable();
        setupHospitalisationsTable();
        setupOrdonnancesTable();
        setupProduitsTable();
        setupFournisseursTable();
        setupDemandesTable();

        if (logLevelFilter != null) {
            logLevelFilter.getItems().addAll("Tous", "CREATE", "UPDATE", "DELETE", "Connexion");
            logLevelFilter.setValue("Tous");
        }

        if (demandeStatutFilter != null) {
            demandeStatutFilter.getItems().addAll("Tous", "Attente", "Validee", "Refusee");
            demandeStatutFilter.setValue("Tous");
        }

        if (hospitalNameField != null) hospitalNameField.setText("HSP Urgences");
        if (hospitalAddressField != null) hospitalAddressField.setText("123 Rue de la Santé, 75013 Paris");
        if (hospitalPhoneField != null) hospitalPhoneField.setText("01 23 45 67 89");
        if (maxPatientsField != null) maxPatientsField.setText("100");

        loadDashboardData();
        showDashboard();
    }

    private void configurerTableUtilisateurs() {
        if (usersTable == null) return;

        userIdCol.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        userNomCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getNom() != null ? cell.getValue().getNom() : ""));
        userPrenomCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPrenom() != null ? cell.getValue().getPrenom() : ""));
        userEmailCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEmail() != null ? cell.getValue().getEmail() : ""));
        userRoleCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getRole() != null ? cell.getValue().getRole() : ""));
        userDateCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDate_creation() != null ? cell.getValue().getDate_creation() : ""));

        usersTable.setItems(utilisateurs);
        usersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && usersTable.getSelectionModel().getSelectedItem() != null) {
                onEditUser();
            }
        });
    }

    private void configurerTableLogs() {
        if (logsTable == null) return;

        logDateCol.setCellValueFactory(cell -> {
            var dt = cell.getValue().getDate_action();
            return new SimpleStringProperty(dt != null ? dt.format(LOG_FORMATTER) : "");
        });
        logUtilisateurCol.setCellValueFactory(cell -> {
            int uid = cell.getValue().getId_utilisateur();
            return new SimpleStringProperty(userNames.getOrDefault(uid, "ID " + uid));
        });
        logActionCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAction() != null ? cell.getValue().getAction() : ""));
        logTableCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTable_concernee() != null ? cell.getValue().getTable_concernee() : ""));
        logDetailsCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDetails() != null ? cell.getValue().getDetails() : ""));

        logsTable.setItems(historiques);
    }

    @SuppressWarnings("unchecked")
    private void setupPatientsTable() {
        if (patientsTable == null) return;
        patientsTable.getColumns().clear();
        String[] cols = {"ID", "Nom", "Prénom", "N° Sécu", "Email", "Téléphone"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            patientsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupHospitalisationsTable() {
        if (hospitalisationsTable == null) return;
        hospitalisationsTable.getColumns().clear();
        String[] cols = {"ID", "Patient", "Chambre", "Médecin", "Date début", "Date fin", "Maladie"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            hospitalisationsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupOrdonnancesTable() {
        if (ordonnancesTable == null) return;
        ordonnancesTable.getColumns().clear();
        String[] cols = {"ID", "Patient", "Médecin", "Date", "Médicaments", "Statut"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            ordonnancesTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupProduitsTable() {
        if (produitsTable == null) return;
        produitsTable.getColumns().clear();
        String[] cols = {"ID", "Libellé", "Description", "Stock", "Dangerosité"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            produitsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupFournisseursTable() {
        if (fournisseursTable == null) return;
        fournisseursTable.getColumns().clear();
        String[] cols = {"ID", "Nom", "Contact", "Email", "Téléphone"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            fournisseursTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupDemandesTable() {
        if (demandesTable == null) return;
        demandesTable.getColumns().clear();
        String[] cols = {"ID", "Médecin", "Produit(s)", "Date", "Statut"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(dataIdx)));
            demandesTable.getColumns().add(col);
        }
    }

    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) { dashboardView.setVisible(true); dashboardView.setManaged(true); }
        loadDashboardData();
    }

    @FXML
    private void showUsers() {
        hideAllViews();
        if (usersView != null) { usersView.setVisible(true); usersView.setManaged(true); }
        loadUsers();
    }

    @FXML
    private void showPatients() {
        hideAllViews();
        if (patientsView != null) { patientsView.setVisible(true); patientsView.setManaged(true); }
        loadPatients();
    }

    @FXML
    private void showHospitalisations() {
        hideAllViews();
        if (hospitalisationsView != null) { hospitalisationsView.setVisible(true); hospitalisationsView.setManaged(true); }
        loadHospitalisations();
    }

    @FXML
    private void showOrdonnances() {
        hideAllViews();
        if (ordonnancesView != null) { ordonnancesView.setVisible(true); ordonnancesView.setManaged(true); }
        loadOrdonnances();
    }

    @FXML
    private void showProduits() {
        hideAllViews();
        if (produitsView != null) { produitsView.setVisible(true); produitsView.setManaged(true); }
        loadProduits();
    }

    @FXML
    private void showFournisseurs() {
        hideAllViews();
        if (fournisseursView != null) { fournisseursView.setVisible(true); fournisseursView.setManaged(true); }
        loadFournisseurs();
    }

    @FXML
    private void showDemandes() {
        hideAllViews();
        if (demandesView != null) { demandesView.setVisible(true); demandesView.setManaged(true); }
        loadDemandes();
    }

    @FXML
    private void showSystem() {
        hideAllViews();
        if (systemView != null) { systemView.setVisible(true); systemView.setManaged(true); }
    }

    @FXML
    private void showLogs() {
        hideAllViews();
        if (logsView != null) { logsView.setVisible(true); logsView.setManaged(true); }
        loadLogs();
    }

    @FXML
    private void showStats() {
        hideAllViews();
        if (statsView != null) { statsView.setVisible(true); statsView.setManaged(true); }
        loadStats();
    }

    private void hideAllViews() {
        VBox[] views = {dashboardView, usersView, patientsView, hospitalisationsView,
                ordonnancesView, produitsView, fournisseursView, demandesView,
                systemView, logsView, statsView};
        for (VBox v : views) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }

    private void loadDashboardData() {
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM utilisateur");
            if (rs.next()) {
                int count = rs.getInt("count");
                if (totalUtilisateursLabel != null) totalUtilisateursLabel.setText(String.valueOf(count));
                if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(count));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM patient");
            if (rs.next()) {
                if (totalPatientsLabel != null) totalPatientsLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM hospitalisation WHERE date_fin IS NULL");
            if (rs.next()) {
                if (totalHospitalisationsLabel != null) totalHospitalisationsLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM produit");
            if (rs.next()) {
                if (totalProduitsLabel != null) totalProduitsLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM fournisseur");
            if (rs.next()) {
                if (totalFournisseursLabel != null) totalFournisseursLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM demande_produit WHERE statut = 'Attente'");
            if (rs.next()) {
                if (totalDemandesAttenteLabel != null) totalDemandesAttenteLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM ordonnance WHERE statut = 'Active'");
            if (rs.next()) {
                if (totalOrdonnancesActivesLabel != null) totalOrdonnancesActivesLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            stmt.close();

            if (systemStatusLabel != null) systemStatusLabel.setText("Opérationnel");

        } catch (SQLException e) {
            System.err.println("Erreur chargement données dashboard : " + e.getMessage());
            if (totalUtilisateursLabel != null) totalUtilisateursLabel.setText("0");
            if (totalPatientsLabel != null) totalPatientsLabel.setText("0");
            if (totalHospitalisationsLabel != null) totalHospitalisationsLabel.setText("0");
            if (totalProduitsLabel != null) totalProduitsLabel.setText("0");
            if (totalFournisseursLabel != null) totalFournisseursLabel.setText("0");
            if (totalDemandesAttenteLabel != null) totalDemandesAttenteLabel.setText("0");
            if (totalOrdonnancesActivesLabel != null) totalOrdonnancesActivesLabel.setText("0");
            if (systemStatusLabel != null) systemStatusLabel.setText("Erreur BDD");
        } finally {
            closeConnection(conn);
        }
    }

    private void loadUsers() {
        List<Utilisateur> liste = utilisateurDAO.findAll();
        utilisateurs.clear();
        utilisateurs.addAll(liste);
    }

    private void loadPatients() {
        if (patientsTable == null) return;
        String query = "SELECT id_patient, nom, prenom, num_secu, email, telephone FROM patient ORDER BY nom, prenom";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_patient")));
                row.add(String.valueOf(rs.getInt("id_patient")));
                row.add(rs.getString("nom") != null ? rs.getString("nom") : "—");
                row.add(rs.getString("prenom") != null ? rs.getString("prenom") : "—");
                row.add(rs.getString("num_secu") != null ? rs.getString("num_secu") : "—");
                row.add(rs.getString("email") != null ? rs.getString("email") : "—");
                row.add(rs.getString("telephone") != null ? rs.getString("telephone") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadPatients : " + e.getMessage());
        }
        patientsTable.setItems(data);
    }

    private void loadHospitalisations() {
        if (hospitalisationsTable == null) return;
        String query = """
                SELECT h.id_hospitalisation,
                       CONCAT(p.nom, ' ', p.prenom) AS patient_nom,
                       CONCAT('Chambre ', c.numero_chambre) AS chambre,
                       CONCAT('Dr. ', u.nom, ' ', u.prenom) AS medecin,
                       h.date_debut, h.date_fin, h.description_maladie
                FROM hospitalisation h
                JOIN dossier d ON h.id_dossier = d.id_dossier
                JOIN patient p ON d.id_patient = p.id_patient
                JOIN chambre c ON h.id_chambre = c.id_chambre
                JOIN utilisateur u ON h.id_medecin = u.id_utilisateur
                ORDER BY h.date_debut DESC
                """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_hospitalisation")));
                row.add(String.valueOf(rs.getInt("id_hospitalisation")));
                row.add(rs.getString("patient_nom") != null ? rs.getString("patient_nom") : "—");
                row.add(rs.getString("chambre") != null ? rs.getString("chambre") : "—");
                row.add(rs.getString("medecin") != null ? rs.getString("medecin") : "—");
                row.add(rs.getDate("date_debut") != null ? rs.getDate("date_debut").toString() : "—");
                row.add(rs.getDate("date_fin") != null ? rs.getDate("date_fin").toString() : "En cours");
                row.add(rs.getString("description_maladie") != null ? rs.getString("description_maladie") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadHospitalisations : " + e.getMessage());
        }
        hospitalisationsTable.setItems(data);
    }

    private void loadOrdonnances() {
        if (ordonnancesTable == null) return;
        String query = """
                SELECT o.id_ordonnance,
                       CONCAT(p.nom, ' ', p.prenom) AS patient_nom,
                       CONCAT('Dr. ', u.nom, ' ', u.prenom) AS medecin,
                       o.date_ordonnance, o.medicaments, o.statut
                FROM ordonnance o
                JOIN dossier d ON o.id_dossier = d.id_dossier
                JOIN patient p ON d.id_patient = p.id_patient
                JOIN utilisateur u ON o.id_medecin = u.id_utilisateur
                ORDER BY o.date_ordonnance DESC
                """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_ordonnance")));
                row.add(String.valueOf(rs.getInt("id_ordonnance")));
                row.add(rs.getString("patient_nom") != null ? rs.getString("patient_nom") : "—");
                row.add(rs.getString("medecin") != null ? rs.getString("medecin") : "—");
                row.add(rs.getString("date_ordonnance") != null ? rs.getString("date_ordonnance") : "—");
                row.add(rs.getString("medicaments") != null ? rs.getString("medicaments") : "—");
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadOrdonnances : " + e.getMessage());
        }
        ordonnancesTable.setItems(data);
    }

    private void loadProduits() {
        if (produitsTable == null) return;
        String query = "SELECT id_produit, libelle, description, quantite_stock, niveau_dangerosite FROM produit ORDER BY libelle";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_produit")));
                row.add(String.valueOf(rs.getInt("id_produit")));
                row.add(rs.getString("libelle") != null ? rs.getString("libelle") : "—");
                row.add(rs.getString("description") != null ? rs.getString("description") : "—");
                row.add(String.valueOf(rs.getInt("quantite_stock")));
                row.add(String.valueOf(rs.getInt("niveau_dangerosite")));
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadProduits : " + e.getMessage());
        }
        produitsTable.setItems(data);
    }

    private void loadFournisseurs() {
        if (fournisseursTable == null) return;
        String query = "SELECT id_fournisseur, nom, contact, email, telephone FROM fournisseur ORDER BY nom";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_fournisseur")));
                row.add(String.valueOf(rs.getInt("id_fournisseur")));
                row.add(rs.getString("nom") != null ? rs.getString("nom") : "—");
                row.add(rs.getString("contact") != null ? rs.getString("contact") : "—");
                row.add(rs.getString("email") != null ? rs.getString("email") : "—");
                row.add(rs.getString("telephone") != null ? rs.getString("telephone") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadFournisseurs : " + e.getMessage());
        }
        fournisseursTable.setItems(data);
    }

    private void loadDemandes() {
        if (demandesTable == null) return;
        String query = """
                SELECT dp.id_demande,
                       CONCAT('Dr. ', u.nom, ' ', u.prenom) AS medecin,
                       GROUP_CONCAT(p.libelle ORDER BY p.libelle SEPARATOR ', ') AS produits,
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
                row.add(String.valueOf(rs.getInt("id_demande")));
                row.add(String.valueOf(rs.getInt("id_demande")));
                row.add(rs.getString("medecin") != null ? rs.getString("medecin") : "—");
                row.add(rs.getString("produits") != null ? rs.getString("produits") : "—");
                row.add(rs.getString("date_demande") != null ? rs.getString("date_demande") : "—");
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadDemandes : " + e.getMessage());
        }
        demandesTable.setItems(data);
    }

    private void loadLogs() {
        userNames.clear();
        for (Utilisateur u : utilisateurDAO.findAll()) {
            userNames.put(u.getId(), u.getPrenom() + " " + u.getNom());
        }

        List<Historique> logs = historiqueDAO.findAll();
        historiques.clear();
        historiques.addAll(logs);

        String filtre = logLevelFilter != null ? logLevelFilter.getValue() : "Tous";
        if (filtre != null && !filtre.equals("Tous")) {
            historiques.removeIf(h -> !filtre.equalsIgnoreCase(h.getAction()));
        }
    }

    private void loadStats() {
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) as count FROM dossier " +
                "WHERE MONTH(date_arrivee) = MONTH(CURDATE()) AND YEAR(date_arrivee) = YEAR(CURDATE())");
            if (rs.next() && statsPatientsMoisLabel != null) {
                statsPatientsMoisLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            rs = stmt.executeQuery(
                "SELECT COUNT(DISTINCT d.id_dossier) as count FROM dossier d " +
                "LEFT JOIN ordonnance o ON o.id_dossier = d.id_dossier " +
                "LEFT JOIN hospitalisation h ON h.id_dossier = d.id_dossier " +
                "WHERE (o.id_ordonnance IS NOT NULL OR h.id_hospitalisation IS NOT NULL) " +
                "AND MONTH(d.date_arrivee) = MONTH(CURDATE()) AND YEAR(d.date_arrivee) = YEAR(CURDATE())");
            if (rs.next() && statsConsultationsMoyenneLabel != null) {
                statsConsultationsMoyenneLabel.setText(String.valueOf(rs.getInt("count")));
            }
            rs.close();

            rs = stmt.executeQuery(
                "SELECT " +
                "(SELECT COUNT(*) FROM hospitalisation WHERE date_fin IS NULL) as occupees, " +
                "(SELECT COUNT(*) FROM chambre) as total");
            if (rs.next()) {
                int occupees = rs.getInt("occupees");
                int total = rs.getInt("total");
                String taux = total > 0 ? (occupees * 100 / total) + "%" : "0%";
                if (statsTauxOccupationLabel != null) statsTauxOccupationLabel.setText(taux);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Erreur stats : " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    @FXML
    private void onSearchPatient() {
        if (searchPatientField == null || patientsTable == null) return;
        String texte = searchPatientField.getText().toLowerCase().trim();
        if (texte.isEmpty()) {
            loadPatients();
            return;
        }
        String query = "SELECT id_patient, nom, prenom, num_secu, email, telephone FROM patient " +
                "WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ? OR LOWER(num_secu) LIKE ? " +
                "ORDER BY nom, prenom";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            String pattern = "%" + texte + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_patient")));
                row.add(String.valueOf(rs.getInt("id_patient")));
                row.add(rs.getString("nom") != null ? rs.getString("nom") : "—");
                row.add(rs.getString("prenom") != null ? rs.getString("prenom") : "—");
                row.add(rs.getString("num_secu") != null ? rs.getString("num_secu") : "—");
                row.add(rs.getString("email") != null ? rs.getString("email") : "—");
                row.add(rs.getString("telephone") != null ? rs.getString("telephone") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche patients : " + e.getMessage());
        }
        patientsTable.setItems(data);
    }

    @FXML
    private void onSearchProduit() {
        if (searchProduitField == null || produitsTable == null) return;
        String texte = searchProduitField.getText().toLowerCase().trim();
        if (texte.isEmpty()) {
            loadProduits();
            return;
        }
        String query = "SELECT id_produit, libelle, description, quantite_stock, niveau_dangerosite FROM produit " +
                "WHERE LOWER(libelle) LIKE ? ORDER BY libelle";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + texte + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_produit")));
                row.add(String.valueOf(rs.getInt("id_produit")));
                row.add(rs.getString("libelle") != null ? rs.getString("libelle") : "—");
                row.add(rs.getString("description") != null ? rs.getString("description") : "—");
                row.add(String.valueOf(rs.getInt("quantite_stock")));
                row.add(String.valueOf(rs.getInt("niveau_dangerosite")));
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche produits : " + e.getMessage());
        }
        produitsTable.setItems(data);
    }

    @FXML
    private void onSearchFournisseur() {
        if (searchFournisseurField == null || fournisseursTable == null) return;
        String texte = searchFournisseurField.getText().toLowerCase().trim();
        if (texte.isEmpty()) {
            loadFournisseurs();
            return;
        }
        String query = "SELECT id_fournisseur, nom, contact, email, telephone FROM fournisseur " +
                "WHERE LOWER(nom) LIKE ? OR LOWER(contact) LIKE ? ORDER BY nom";
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            String pattern = "%" + texte + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_fournisseur")));
                row.add(String.valueOf(rs.getInt("id_fournisseur")));
                row.add(rs.getString("nom") != null ? rs.getString("nom") : "—");
                row.add(rs.getString("contact") != null ? rs.getString("contact") : "—");
                row.add(rs.getString("email") != null ? rs.getString("email") : "—");
                row.add(rs.getString("telephone") != null ? rs.getString("telephone") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche fournisseurs : " + e.getMessage());
        }
        fournisseursTable.setItems(data);
    }

    @FXML
    private void onFilterDemandes() {
        if (demandeStatutFilter == null || demandesTable == null) return;
        String statut = demandeStatutFilter.getValue();
        if (statut == null || statut.equals("Tous")) {
            loadDemandes();
            return;
        }
        String query = """
                SELECT dp.id_demande,
                       CONCAT('Dr. ', u.nom, ' ', u.prenom) AS medecin,
                       GROUP_CONCAT(p.libelle ORDER BY p.libelle SEPARATOR ', ') AS produits,
                       dp.date_demande,
                       dp.statut
                FROM demande_produit dp
                JOIN utilisateur u ON dp.id_medecin = u.id_utilisateur
                LEFT JOIN ligne_demande ld ON dp.id_demande = ld.id_demande
                LEFT JOIN produit p ON ld.id_produit = p.id_produit
                WHERE dp.statut = ?
                GROUP BY dp.id_demande, u.nom, u.prenom, dp.date_demande, dp.statut
                ORDER BY dp.date_demande DESC
                """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_demande")));
                row.add(String.valueOf(rs.getInt("id_demande")));
                row.add(rs.getString("medecin") != null ? rs.getString("medecin") : "—");
                row.add(rs.getString("produits") != null ? rs.getString("produits") : "—");
                row.add(rs.getString("date_demande") != null ? rs.getString("date_demande") : "—");
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur filtre demandes : " + e.getMessage());
        }
        demandesTable.setItems(data);
    }

    @FXML
    private void onRefreshPatients() {
        if (searchPatientField != null) searchPatientField.clear();
        loadPatients();
    }

    @FXML
    private void onRefreshHospitalisations() {
        loadHospitalisations();
    }

    @FXML
    private void onRefreshOrdonnances() {
        loadOrdonnances();
    }

    @FXML
    private void onRefreshProduits() {
        if (searchProduitField != null) searchProduitField.clear();
        loadProduits();
    }

    @FXML
    private void onRefreshFournisseurs() {
        if (searchFournisseurField != null) searchFournisseurField.clear();
        loadFournisseurs();
    }

    @FXML
    private void onRefreshDemandes() {
        if (demandeStatutFilter != null) demandeStatutFilter.setValue("Tous");
        loadDemandes();
    }

    @FXML
    private void onDeletePatient() {
        if (patientsTable == null) return;
        ObservableList<String> selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un patient.");
            return;
        }
        int patientId = Integer.parseInt(selected.get(0));
        String nom = selected.get(2) + " " + selected.get(3);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer le patient \"" + nom + "\" ?");
        confirm.setContentText("Cette action supprimera le patient et tous ses dossiers associés. Cette action est irréversible.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM ordonnance WHERE id_dossier IN (SELECT id_dossier FROM dossier WHERE id_patient = ?)")) {
                    ps.setInt(1, patientId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM hospitalisation WHERE id_dossier IN (SELECT id_dossier FROM dossier WHERE id_patient = ?)")) {
                    ps.setInt(1, patientId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM dossier WHERE id_patient = ?")) {
                    ps.setInt(1, patientId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM patient WHERE id_patient = ?")) {
                    ps.setInt(1, patientId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Suppression', 'patient', ?, ?)")) {
                    ps.setInt(1, currentAdminId);
                    ps.setInt(2, patientId);
                    ps.setString(3, "Suppression patient: " + nom + " (et dossiers associés)");
                    ps.executeUpdate();
                }

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Patient \"" + nom + "\" supprimé.");
                loadPatients();
                loadDashboardData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer ce patient : " + e.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteHospitalisation() {
        if (hospitalisationsTable == null) return;
        ObservableList<String> selected = hospitalisationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une hospitalisation.");
            return;
        }
        int hospId = Integer.parseInt(selected.get(0));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer l'hospitalisation #" + hospId + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;
            boolean succes = new HospitalisationDAO().delete(hospId);
            if (succes) {
                logAction("Suppression", "hospitalisation", hospId, "Suppression hospitalisation #" + hospId);
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Hospitalisation #" + hospId + " supprimée.");
                loadHospitalisations();
                loadDashboardData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer cette hospitalisation.");
            }
        });
    }

    @FXML
    private void onDeleteOrdonnance() {
        if (ordonnancesTable == null) return;
        ObservableList<String> selected = ordonnancesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une ordonnance.");
            return;
        }
        int ordoId = Integer.parseInt(selected.get(0));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer l'ordonnance #" + ordoId + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;
            boolean succes = new OrdonnanceDAO().delete(ordoId);
            if (succes) {
                logAction("Suppression", "ordonnance", ordoId, "Suppression ordonnance #" + ordoId);
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Ordonnance #" + ordoId + " supprimée.");
                loadOrdonnances();
                loadDashboardData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer cette ordonnance.");
            }
        });
    }

    @FXML
    private void onDeleteProduit() {
        if (produitsTable == null) return;
        ObservableList<String> selected = produitsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un produit.");
            return;
        }
        int produitId = Integer.parseInt(selected.get(0));
        String libelle = selected.get(2);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer le produit \"" + libelle + "\" ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;
            boolean succes = new ProduitDAO().delete(produitId);
            if (succes) {
                logAction("Suppression", "produit", produitId, "Suppression produit: " + libelle);
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Produit \"" + libelle + "\" supprimé.");
                loadProduits();
                loadDashboardData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer ce produit (référencé dans des demandes ou réapprovisionnements).");
            }
        });
    }

    @FXML
    private void onDeleteFournisseur() {
        if (fournisseursTable == null) return;
        ObservableList<String> selected = fournisseursTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un fournisseur.");
            return;
        }
        int fournisseurId = Integer.parseInt(selected.get(0));
        String nom = selected.get(2);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer le fournisseur \"" + nom + "\" ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;
            boolean succes = new FournisseurDAO().delete(fournisseurId);
            if (succes) {
                logAction("Suppression", "fournisseur", fournisseurId, "Suppression fournisseur: " + nom);
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Fournisseur \"" + nom + "\" supprimé.");
                loadFournisseurs();
                loadDashboardData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer ce fournisseur (lié à des produits ou réapprovisionnements).");
            }
        });
    }

    @FXML
    private void onDeleteDemande() {
        if (demandesTable == null) return;
        ObservableList<String> selected = demandesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une demande.");
            return;
        }
        int demandeId = Integer.parseInt(selected.get(0));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer la demande #" + demandeId + " ?");
        confirm.setContentText("Cette action supprimera la demande et ses lignes associées. Cette action est irréversible.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM ligne_demande WHERE id_demande = ?")) {
                    ps.setInt(1, demandeId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM demande_produit WHERE id_demande = ?")) {
                    ps.setInt(1, demandeId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Suppression', 'demande_produit', ?, ?)")) {
                    ps.setInt(1, currentAdminId);
                    ps.setInt(2, demandeId);
                    ps.setString(3, "Suppression demande #" + demandeId + " (et lignes associées)");
                    ps.executeUpdate();
                }

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Demande #" + demandeId + " supprimée.");
                loadDemandes();
                loadDashboardData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer cette demande : " + e.getMessage());
            }
        });
    }

    @FXML
    private void onCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateur/UtilisateurForm.fxml"));
            Parent root = loader.load();

            UtilisateurFormController controller = loader.getController();
            controller.setMode(UtilisateurFormController.Mode.CREATION);

            Stage stage = new Stage();
            stage.setTitle("Nouvel utilisateur");
            stage.setScene(new Scene(root, 480, 420));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadUsers();
            loadDashboardData();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEditUser() {
        Utilisateur selection = usersTable.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateur/UtilisateurForm.fxml"));
            Parent root = loader.load();

            UtilisateurFormController controller = loader.getController();
            controller.setMode(UtilisateurFormController.Mode.MODIFICATION);
            controller.setUtilisateur(selection);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(root, 480, 420));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadUsers();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onDeleteUser() {
        Utilisateur selection = usersTable.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer \"" + selection.getPrenom() + " " + selection.getNom() + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = utilisateurDAO.delete(selection.getId());
            if (succes) {
                loadUsers();
                loadDashboardData();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer cet utilisateur.");
            }
        }
    }

    @FXML
    private void onResetPassword() {
        Utilisateur selection = usersTable.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur");
            return;
        }

        String newPassword = "HSP" + (int)(Math.random() * 90000 + 10000);

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Réinitialisation mot de passe");
        confirmation.setHeaderText("Réinitialiser le mot de passe de " + selection.getPrenom() + " " + selection.getNom() + " ?");
        confirmation.setContentText("Nouveau mot de passe : " + newPassword + "\nConfirmez pour appliquer.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                selection.setMdp(hashedPassword);
                boolean succes = utilisateurDAO.update(selection);
                if (succes) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Mot de passe réinitialisé !\n\nNouveau mot de passe : " + newPassword +
                            "\n\nCommuniquez ce mot de passe à l'utilisateur.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de réinitialiser le mot de passe.");
                }
            }
        });
    }

    @FXML
    private void onSearchUser() {
        String texte = searchUserField.getText().toLowerCase().trim();
        List<Utilisateur> tous = utilisateurDAO.findAll();
        utilisateurs.clear();

        if (texte.isEmpty()) {
            utilisateurs.addAll(tous);
        } else {
            for (Utilisateur u : tous) {
                boolean correspond =
                        (u.getNom() != null && u.getNom().toLowerCase().contains(texte)) ||
                        (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(texte)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(texte)) ||
                        (u.getRole() != null && u.getRole().toLowerCase().contains(texte)) ||
                        String.valueOf(u.getId()).contains(texte);
                if (correspond) utilisateurs.add(u);
            }
        }
    }

    @FXML
    private void onRefreshUsers() {
        if (searchUserField != null) searchUserField.clear();
        loadUsers();
    }

    @FXML
    private void onSaveSettings() {
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Paramètres système sauvegardés !");
    }

    @FXML
    private void onToggleMaintenance() {
        if (maintenanceModeCheckBox.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Mode maintenance");
            alert.setHeaderText("Activer le mode maintenance ?");
            alert.setContentText("Les utilisateurs ne pourront plus se connecter.");
            alert.showAndWait().ifPresent(response -> {
                if (response != ButtonType.OK) maintenanceModeCheckBox.setSelected(false);
                else showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Mode maintenance activé.");
            });
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Mode maintenance désactivé.");
        }
    }

    @FXML
    private void onBackupDatabase() {
        showAlert(Alert.AlertType.INFORMATION, "Sauvegarde",
                "Sauvegarde de la base de données lancée.\nFichier : backup_" + java.time.LocalDate.now() + ".sql");
    }

    @FXML
    private void onClearCache() {
        showAlert(Alert.AlertType.INFORMATION, "Cache", "Cache système vidé !");
    }

    @FXML
    private void onRefreshLogs() {
        loadLogs();
    }

    @FXML
    private void onExportLogs() {
        showAlert(Alert.AlertType.INFORMATION, "Export",
                "Logs exportés : logs_" + java.time.LocalDate.now() + ".csv");
    }

    @FXML
    private void onClearLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer tous les logs ?");
        alert.setContentText("Cette action est irréversible.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert(Alert.AlertType.INFORMATION, "Suppression", "Logs supprimés.");
            }
        });
    }

    @FXML
    private void onExportStats() {
        showAlert(Alert.AlertType.INFORMATION, "Export",
                "Statistiques exportées : stats_" + java.time.LocalDate.now() + ".pdf");
    }

    @FXML
    private void onGenerateReport() {
        showAlert(Alert.AlertType.INFORMATION, "Rapport",
                "Rapport mensuel généré : rapport_" + java.time.LocalDate.now() + ".pdf");
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
                    stage.setMaximized(false);
                    stage.show();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la connexion : " + e.getMessage());
                }
            }
        });
    }

    public void setAdminInfo(int id, String name) {
        this.currentAdminId = id;
        this.currentAdminName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
    }

    private void logAction(String action, String table, int idEnregistrement, String details) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, currentAdminId);
            ps.setString(2, action);
            ps.setString(3, table);
            ps.setInt(4, idEnregistrement);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur historique : " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
