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
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SecretaireDashboardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label todayCountLabel;
    @FXML private VBox dashboardView;
    @FXML private VBox registrationView;
    @FXML private VBox patientListView;
    @FXML private VBox triageView;
    @FXML private Label todayPatientsLabel;
    @FXML private Label waitingPatientsLabel;
    @FXML private Label triagedPatientsLabel;
    @FXML private TableView<ObservableList<String>> recentPatientsTable;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextArea motifTextArea;
    @FXML private TextField searchField;
    @FXML private TableView<ObservableList<String>> allPatientsTable;
    @FXML private TableView<ObservableList<String>> waitingTriageTable;
    @FXML private TextField numSecuField;
    @FXML private TextField emailPatientField;
    @FXML private ComboBox<Integer> niveauGraviteComboBox;

    private static final String DB_URL      = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int currentSecretaireId = -1;
    private String currentSecretaireName = "Secrétaire";

    // ============= INIT =============

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sexeComboBox != null)
            sexeComboBox.getItems().addAll("Homme", "Femme", "Autre");
        if (niveauGraviteComboBox != null) {
            niveauGraviteComboBox.getItems().addAll(1, 2, 3, 4, 5);
            niveauGraviteComboBox.setValue(3);
        }
        setupRecentPatientsTable();
        setupAllPatientsTable();
        setupWaitingTriageTable();
        showDashboard();
    }

    // ============= SETUP COLONNES =============

    @SuppressWarnings("unchecked")
    private void setupRecentPatientsTable() {
        if (recentPatientsTable == null) return;
        recentPatientsTable.getColumns().clear();
        String[] cols = {"Nom", "Arrivée", "Statut"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            recentPatientsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupAllPatientsTable() {
        if (allPatientsTable == null) return;
        allPatientsTable.getColumns().clear();
        String[] cols = {"Nom", "Prénom", "Téléphone", "Statut"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            allPatientsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupWaitingTriageTable() {
        if (waitingTriageTable == null) return;
        waitingTriageTable.getColumns().clear();
        String[] cols = {"Patient", "Arrivée", "Gravité", "Symptômes"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(idx)));
            waitingTriageTable.getColumns().add(col);
        }
    }

    // ============= NAVIGATION =============

    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) { dashboardView.setVisible(true); dashboardView.setManaged(true); }
        loadDashboardData();
        loadRecentPatients();
    }

    @FXML
    private void showPatientRegistration() {
        hideAllViews();
        if (registrationView != null) { registrationView.setVisible(true); registrationView.setManaged(true); }
        clearRegistrationForm();
    }

    @FXML
    private void showPatientList() {
        hideAllViews();
        if (patientListView != null) { patientListView.setVisible(true); patientListView.setManaged(true); }
        loadAllPatients();
    }

    @FXML
    private void showTriage() {
        hideAllViews();
        if (triageView != null) { triageView.setVisible(true); triageView.setManaged(true); }
        loadWaitingTriage();
    }

    private void hideAllViews() {
        VBox[] views = {dashboardView, registrationView, patientListView, triageView};
        for (VBox v : views) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }

    // ============= CHARGEMENT DONNÉES =============

    private void loadDashboardData() {
        try (Connection conn = getConnection()) {
            // Patients aujourd'hui
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dossier WHERE DATE(date_arrivee) = CURDATE()")) {
                if (rs.next()) {
                    int n = rs.getInt(1);
                    if (todayPatientsLabel != null) todayPatientsLabel.setText(String.valueOf(n));
                    if (todayCountLabel != null) todayCountLabel.setText(String.valueOf(n));
                }
            }
            // En attente
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dossier WHERE statut = 'Attente'")) {
                if (rs.next() && waitingPatientsLabel != null)
                    waitingPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }
            // Triés aujourd'hui
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM dossier WHERE DATE(date_arrivee) = CURDATE() AND statut IN ('EnCours','Termine')")) {
                if (rs.next() && triagedPatientsLabel != null)
                    triagedPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadDashboardData (Secrétaire) : " + e.getMessage());
        }
    }

    private void loadRecentPatients() {
        if (recentPatientsTable == null) return;
        String query = """
                SELECT p.nom, p.prenom, d.date_arrivee, d.statut
                FROM dossier d
                JOIN patient p ON d.id_patient = p.id_patient
                ORDER BY d.date_arrivee DESC
                LIMIT 10
                """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("nom") + " " + rs.getString("prenom"));
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee")));
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadRecentPatients : " + e.getMessage());
        }
        recentPatientsTable.setItems(data);
    }

    private void loadAllPatients() {
        if (allPatientsTable == null) return;
        String recherche = searchField != null ? searchField.getText().trim() : "";
        String query;
        if (recherche.isEmpty()) {
            query = """
                    SELECT p.nom, p.prenom, p.telephone,
                           COALESCE((SELECT d.statut FROM dossier d WHERE d.id_patient = p.id_patient
                                     ORDER BY d.date_arrivee DESC LIMIT 1), '-') AS statut
                    FROM patient p
                    ORDER BY p.nom
                    """;
        } else {
            query = """
                    SELECT p.nom, p.prenom, p.telephone,
                           COALESCE((SELECT d.statut FROM dossier d WHERE d.id_patient = p.id_patient
                                     ORDER BY d.date_arrivee DESC LIMIT 1), '-') AS statut
                    FROM patient p
                    WHERE LOWER(p.nom) LIKE ? OR LOWER(p.prenom) LIKE ?
                    ORDER BY p.nom
                    """;
        }
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if (!recherche.isEmpty()) {
                String like = "%" + recherche.toLowerCase() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("nom") != null ? rs.getString("nom") : "—");
                row.add(rs.getString("prenom") != null ? rs.getString("prenom") : "—");
                row.add(rs.getString("telephone") != null ? rs.getString("telephone") : "—");
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadAllPatients : " + e.getMessage());
        }
        allPatientsTable.setItems(data);
    }

    private void loadWaitingTriage() {
        if (waitingTriageTable == null) return;
        String query = """
                SELECT p.nom, p.prenom, d.date_arrivee, d.symptomes, d.niveau_gravite
                FROM dossier d
                JOIN patient p ON d.id_patient = p.id_patient
                WHERE d.statut = 'Attente'
                ORDER BY d.niveau_gravite DESC, d.date_arrivee ASC
                """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("nom") + " " + rs.getString("prenom"));
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee")));
                row.add("Gravité " + rs.getInt("niveau_gravite"));
                row.add(truncate(rs.getString("symptomes"), 60));
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadWaitingTriage : " + e.getMessage());
        }
        waitingTriageTable.setItems(data);
    }

    // ============= ENREGISTREMENT PATIENT =============

    @FXML
    private void onSavePatient() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le nom est obligatoire");
            return;
        }
        if (prenomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le prénom est obligatoire");
            return;
        }
        if (numSecuField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le numéro de sécurité sociale est obligatoire");
            return;
        }
        if (dateNaissancePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "La date de naissance est obligatoire");
            return;
        }
        if (sexeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le sexe est obligatoire");
            return;
        }
        if (niveauGraviteComboBox != null && niveauGraviteComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le niveau de gravité est obligatoire");
            return;
        }

        int gravite = (niveauGraviteComboBox != null && niveauGraviteComboBox.getValue() != null)
                ? niveauGraviteComboBox.getValue() : 3;
        int secretaireId = currentSecretaireId > 0 ? currentSecretaireId : 1;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Insérer le patient
            int patientId;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO patient (nom, prenom, num_secu, email, telephone, adresse) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, nomField.getText().trim());
                pstmt.setString(2, prenomField.getText().trim());
                pstmt.setString(3, numSecuField.getText().trim());
                pstmt.setString(4, emailPatientField.getText().trim());
                pstmt.setString(5, telephoneField.getText().trim());
                pstmt.setString(6, adresseField.getText().trim());
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                patientId = rs.next() ? rs.getInt(1) : 0;
            }

            // Créer le dossier de prise en charge
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO dossier (id_patient, id_secretaire, date_arrivee, symptomes, niveau_gravite, statut) VALUES (?, ?, NOW(), ?, ?, 'Attente')")) {
                pstmt.setInt(1, patientId);
                pstmt.setInt(2, secretaireId);
                pstmt.setString(3, motifTextArea.getText().trim());
                pstmt.setInt(4, gravite);
                pstmt.executeUpdate();
            }

            // Historique
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Creation', 'patient', ?, ?)")) {
                pstmt.setInt(1, secretaireId);
                pstmt.setInt(2, patientId);
                pstmt.setString(3, "Patient: " + prenomField.getText() + " " + nomField.getText() + " (gravité " + gravite + ")");
                pstmt.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Patient " + prenomField.getText() + " " + nomField.getText() + " enregistré avec succès !");
            clearRegistrationForm();
            showDashboard();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void onRefreshList() {
        loadAllPatients();
    }

    private void clearRegistrationForm() {
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (numSecuField != null) numSecuField.clear();
        if (emailPatientField != null) emailPatientField.clear();
        if (dateNaissancePicker != null) dateNaissancePicker.setValue(null);
        if (sexeComboBox != null) sexeComboBox.setValue(null);
        if (telephoneField != null) telephoneField.clear();
        if (adresseField != null) adresseField.clear();
        if (motifTextArea != null) motifTextArea.clear();
        if (niveauGraviteComboBox != null) niveauGraviteComboBox.setValue(3);
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

    public void setSecretaireInfo(int id, String name) {
        this.currentSecretaireId = id;
        this.currentSecretaireName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
        loadDashboardData();
        loadRecentPatients();
    }

    // ============= UTILS =============

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) return "—";
        return ts.toLocalDateTime().format(FMT);
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
