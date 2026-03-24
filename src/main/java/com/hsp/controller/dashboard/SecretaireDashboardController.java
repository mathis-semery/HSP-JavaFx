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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SecretaireDashboardController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label todayCountLabel;
    @FXML private VBox  dashboardView;
    @FXML private VBox  registrationView;
    @FXML private VBox  patientListView;
    @FXML private VBox  triageView;
    @FXML private Label todayPatientsLabel;
    @FXML private Label waitingPatientsLabel;
    @FXML private Label triagedPatientsLabel;
    @FXML private TableView<ObservableList<String>> recentPatientsTable;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private ComboBox<String>  sexeComboBox;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextArea  motifTextArea;
    @FXML private TextField searchField;
    @FXML private TableView<ObservableList<String>> allPatientsTable;
    @FXML private TableView<ObservableList<String>> waitingTriageTable;
    @FXML private TextField numSecuField;
    @FXML private TextField emailPatientField;
    @FXML private ComboBox<Integer> niveauGraviteComboBox;
    @FXML private Button btnModifierPatient;
    @FXML private Button btnSupprimerPatient;

    @FXML private VBox      editPatientView;
    @FXML private TextField editNomField;
    @FXML private TextField editPrenomField;
    @FXML private TextField editTelField;
    @FXML private TextField editEmailField;
    @FXML private TextField editAdresseField;
    @FXML private TextField editNumSecuField;

    // ============= RENDEZ-VOUS =============
    @FXML private VBox      rendezVousView;
    @FXML private VBox      rdvFormView;
    @FXML private TextField rdvSearchField;
    @FXML private TableView<ObservableList<String>> rdvTable;
    @FXML private Button    btnModifierRdv;
    @FXML private Button    btnAnnulerRdv;

    @FXML private Label     rdvFormTitle;
    @FXML private ComboBox<String> rdvPatientCombo;
    @FXML private ComboBox<String> rdvMedecinCombo;
    @FXML private DatePicker       rdvDatePicker;
    @FXML private TextField        rdvHeureField;
    @FXML private ComboBox<String> rdvStatutCombo;
    @FXML private TextArea         rdvMotifArea;

    private int selectedRdvId = -1;

    private static final String DB_URL      = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int    currentSecretaireId   = -1;
    private String currentSecretaireName = "Secrétaire";
    private int    selectedPatientId     = -1;

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
        setupRdvTable();

        if (allPatientsTable != null && btnModifierPatient != null) {
            btnModifierPatient.disableProperty().bind(
                    allPatientsTable.getSelectionModel().selectedItemProperty().isNull());
        }
        if (allPatientsTable != null && btnSupprimerPatient != null) {
            btnSupprimerPatient.disableProperty().bind(
                    allPatientsTable.getSelectionModel().selectedItemProperty().isNull());
        }
        if (rdvTable != null && btnModifierRdv != null) {
            btnModifierRdv.disableProperty().bind(
                    rdvTable.getSelectionModel().selectedItemProperty().isNull());
        }
        if (rdvTable != null && btnAnnulerRdv != null) {
            btnAnnulerRdv.disableProperty().bind(
                    rdvTable.getSelectionModel().selectedItemProperty().isNull());
        }

        if (allPatientsTable != null) {
            allPatientsTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2
                        && allPatientsTable.getSelectionModel().getSelectedItem() != null) {
                    onModifierPatient();
                }
            });
        }
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
            col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            recentPatientsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupAllPatientsTable() {
        if (allPatientsTable == null) return;
        allPatientsTable.getColumns().clear();
        String[] cols = {"Nom", "Prénom", "Téléphone", "Email", "Statut"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
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
            col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            waitingTriageTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupRdvTable() {
        if (rdvTable == null) return;
        rdvTable.getColumns().clear();
        String[] cols = {"Patient", "Médecin", "Date RDV", "Motif", "Statut"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            rdvTable.getColumns().add(col);
        }
    }

    // ============= NAVIGATION =============

    @FXML public void showDashboard() {
        hideAllViews(); show(dashboardView);
        loadDashboardData(); loadRecentPatients();
    }
    @FXML public void showPatientRegistration() {
        hideAllViews(); show(registrationView); clearRegistrationForm();
    }
    @FXML public void showPatientList() {
        hideAllViews(); show(patientListView); loadAllPatients();
    }
    @FXML public void showTriage() {
        hideAllViews(); show(triageView); loadWaitingTriage();
    }
    @FXML public void showRendezVous() {
        hideAllViews(); show(rendezVousView); loadAllRdv();
    }
    @FXML public void showNouveauRdv() {
        selectedRdvId = -1;
        if (rdvFormTitle != null) rdvFormTitle.setText("Nouveau rendez-vous");
        clearRdvForm();
        loadRdvFormCombos();
        hideAllViews(); show(rdvFormView);
    }

    private void hideAllViews() {
        for (VBox v : new VBox[]{dashboardView, registrationView, patientListView, triageView,
                editPatientView, rendezVousView, rdvFormView}) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }
    private void show(VBox v) {
        if (v != null) { v.setVisible(true); v.setManaged(true); }
    }

    // ============= CHARGEMENT DONNÉES =============

    private void loadDashboardData() {
        try (Connection conn = getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dossier WHERE DATE(date_arrivee) = CURDATE()")) {
                if (rs.next()) {
                    int n = rs.getInt(1);
                    if (todayPatientsLabel != null) todayPatientsLabel.setText(String.valueOf(n));
                    if (todayCountLabel    != null) todayCountLabel.setText(String.valueOf(n));
                }
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dossier WHERE statut = 'Attente'")) {
                if (rs.next() && waitingPatientsLabel != null)
                    waitingPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM dossier WHERE DATE(date_arrivee) = CURDATE() AND statut IN ('EnCours','Termine')")) {
                if (rs.next() && triagedPatientsLabel != null)
                    triagedPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) { System.err.println("Erreur loadDashboardData : " + e.getMessage()); }
    }

    private void loadRecentPatients() {
        if (recentPatientsTable == null) return;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = """
                SELECT p.nom, p.prenom, d.date_arrivee, d.statut
                FROM dossier d JOIN patient p ON d.id_patient = p.id_patient
                ORDER BY d.date_arrivee DESC LIMIT 10
                """;
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("nom") + " " + rs.getString("prenom"));
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee")));
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");
                data.add(row);
            }
        } catch (SQLException e) { System.err.println("Erreur loadRecentPatients : " + e.getMessage()); }
        recentPatientsTable.setItems(data);
    }

    private void loadAllPatients() {
        if (allPatientsTable == null) return;
        String recherche = searchField != null ? searchField.getText().trim() : "";
        boolean avecFiltre = !recherche.isEmpty();
        String sql = avecFiltre
                ? """
                  SELECT p.id_patient, p.nom, p.prenom, p.telephone, p.email,
                         COALESCE((SELECT d.statut FROM dossier d WHERE d.id_patient = p.id_patient
                                   ORDER BY d.date_arrivee DESC LIMIT 1), '-') AS statut
                  FROM patient p WHERE LOWER(p.nom) LIKE ? OR LOWER(p.prenom) LIKE ? ORDER BY p.nom
                  """
                : """
                  SELECT p.id_patient, p.nom, p.prenom, p.telephone, p.email,
                         COALESCE((SELECT d.statut FROM dossier d WHERE d.id_patient = p.id_patient
                                   ORDER BY d.date_arrivee DESC LIMIT 1), '-') AS statut
                  FROM patient p ORDER BY p.nom
                  """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (avecFiltre) {
                String like = "%" + recherche.toLowerCase() + "%";
                ps.setString(1, like); ps.setString(2, like);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("nom")       != null ? rs.getString("nom")       : "—");
                row.add(rs.getString("prenom")    != null ? rs.getString("prenom")    : "—");
                row.add(rs.getString("telephone") != null ? rs.getString("telephone") : "—");
                row.add(rs.getString("email")     != null ? rs.getString("email")     : "—");
                row.add(rs.getString("statut")    != null ? rs.getString("statut")    : "—");
                row.add(String.valueOf(rs.getInt("id_patient"))); // index 5 caché
                data.add(row);
            }
        } catch (SQLException e) { System.err.println("Erreur loadAllPatients : " + e.getMessage()); }
        allPatientsTable.setItems(data);
    }

    private void loadWaitingTriage() {
        if (waitingTriageTable == null) return;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = """
                SELECT p.nom, p.prenom, d.date_arrivee, d.symptomes, d.niveau_gravite
                FROM dossier d JOIN patient p ON d.id_patient = p.id_patient
                WHERE d.statut = 'Attente' ORDER BY d.niveau_gravite DESC, d.date_arrivee ASC
                """;
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("nom") + " " + rs.getString("prenom"));
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee")));
                row.add("Gravité " + rs.getInt("niveau_gravite"));
                row.add(truncate(rs.getString("symptomes"), 60));
                data.add(row);
            }
        } catch (SQLException e) { System.err.println("Erreur loadWaitingTriage : " + e.getMessage()); }
        waitingTriageTable.setItems(data);
    }

    // ============= RENDEZ-VOUS =============

    private void loadAllRdv() {
        if (rdvTable == null) return;
        String recherche = rdvSearchField != null ? rdvSearchField.getText().trim() : "";
        boolean filtre = !recherche.isEmpty();
        String sql = filtre
                ? """
                  SELECT r.id_rdv, p.nom AS p_nom, p.prenom AS p_prenom,
                         u.nom AS m_nom, u.prenom AS m_prenom,
                         r.date_rdv, r.motif, r.statut
                  FROM rendez_vous r
                  JOIN patient p ON r.id_patient = p.id_patient
                  JOIN utilisateur u ON r.id_medecin = u.id_utilisateur
                  WHERE LOWER(p.nom) LIKE ? OR LOWER(p.prenom) LIKE ?
                     OR LOWER(u.nom) LIKE ? OR LOWER(u.prenom) LIKE ?
                  ORDER BY r.date_rdv DESC
                  """
                : """
                  SELECT r.id_rdv, p.nom AS p_nom, p.prenom AS p_prenom,
                         u.nom AS m_nom, u.prenom AS m_prenom,
                         r.date_rdv, r.motif, r.statut
                  FROM rendez_vous r
                  JOIN patient p ON r.id_patient = p.id_patient
                  JOIN utilisateur u ON r.id_medecin = u.id_utilisateur
                  ORDER BY r.date_rdv DESC
                  """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filtre) {
                String like = "%" + recherche.toLowerCase() + "%";
                ps.setString(1, like); ps.setString(2, like);
                ps.setString(3, like); ps.setString(4, like);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("p_nom") + " " + rs.getString("p_prenom"));
                row.add(rs.getString("m_nom") + " " + rs.getString("m_prenom"));
                row.add(formatTimestamp(rs.getTimestamp("date_rdv")));
                row.add(truncate(rs.getString("motif"), 50));
                row.add(rs.getString("statut") != null ? rs.getString("statut") : "—");
                row.add(String.valueOf(rs.getInt("id_rdv"))); // index 5 caché
                data.add(row);
            }
        } catch (SQLException e) { System.err.println("Erreur loadAllRdv : " + e.getMessage()); }
        rdvTable.setItems(data);
    }

    private void loadRdvFormCombos() {
        if (rdvPatientCombo != null) {
            rdvPatientCombo.getItems().clear();
            String sql = "SELECT id_patient, nom, prenom FROM patient ORDER BY nom, prenom";
            try (Connection conn = getConnection(); Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    rdvPatientCombo.getItems().add(
                            rs.getInt("id_patient") + " - " + rs.getString("nom") + " " + rs.getString("prenom"));
                }
            } catch (SQLException e) { System.err.println("Erreur combo patient : " + e.getMessage()); }
        }
        if (rdvMedecinCombo != null) {
            rdvMedecinCombo.getItems().clear();
            String sql = "SELECT id_utilisateur, nom, prenom FROM utilisateur WHERE role='Medecin' ORDER BY nom, prenom";
            try (Connection conn = getConnection(); Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    rdvMedecinCombo.getItems().add(
                            rs.getInt("id_utilisateur") + " - " + rs.getString("nom") + " " + rs.getString("prenom"));
                }
            } catch (SQLException e) { System.err.println("Erreur combo médecin : " + e.getMessage()); }
        }
        if (rdvStatutCombo != null) {
            rdvStatutCombo.getItems().setAll("Planifie", "Annule", "Effectue");
            rdvStatutCombo.setValue("Planifie");
        }
    }

    @FXML private void onRefreshRdv() { loadAllRdv(); }

    @FXML
    private void onSaveRdv() {
        if (rdvPatientCombo == null || rdvPatientCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un patient."); return;
        }
        if (rdvMedecinCombo == null || rdvMedecinCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un médecin."); return;
        }
        if (rdvDatePicker == null || rdvDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez choisir une date."); return;
        }
        String heure = rdvHeureField != null ? rdvHeureField.getText().trim() : "00:00";
        if (!heure.matches("\\d{2}:\\d{2}")) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Format heure invalide (HH:mm attendu)."); return;
        }

        int idPatient  = Integer.parseInt(rdvPatientCombo.getValue().split(" - ")[0]);
        int idMedecin  = Integer.parseInt(rdvMedecinCombo.getValue().split(" - ")[0]);
        int idSecretaire = currentSecretaireId > 0 ? currentSecretaireId : 1;
        String dateRdv = rdvDatePicker.getValue().toString() + " " + heure + ":00";
        String motif   = rdvMotifArea != null ? rdvMotifArea.getText().trim() : "";
        String statut  = rdvStatutCombo != null && rdvStatutCombo.getValue() != null
                         ? rdvStatutCombo.getValue() : "Planifie";

        try (Connection conn = getConnection()) {
            // Vérification conflit médecin
            String conflictMedecinSql = "SELECT COUNT(*) FROM rendez_vous WHERE id_medecin = ? AND date_rdv = ? AND statut != 'Annule'" +
                    (selectedRdvId >= 0 ? " AND id_rdv != " + selectedRdvId : "");
            try (PreparedStatement psCheck = conn.prepareStatement(conflictMedecinSql)) {
                psCheck.setInt(1, idMedecin);
                psCheck.setString(2, dateRdv);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.WARNING, "Conflit", "Ce médecin a déjà un rendez-vous à cette date et heure.");
                    return;
                }
            }
            // Vérification conflit patient
            String conflictPatientSql = "SELECT COUNT(*) FROM rendez_vous WHERE id_patient = ? AND date_rdv = ? AND statut != 'Annule'" +
                    (selectedRdvId >= 0 ? " AND id_rdv != " + selectedRdvId : "");
            try (PreparedStatement psCheck = conn.prepareStatement(conflictPatientSql)) {
                psCheck.setInt(1, idPatient);
                psCheck.setString(2, dateRdv);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.WARNING, "Conflit", "Ce patient a déjà un rendez-vous à cette date et heure.");
                    return;
                }
            }

            if (selectedRdvId < 0) {
                // INSERT
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO rendez_vous (id_patient, id_medecin, id_secretaire, date_rdv, motif, statut) VALUES (?,?,?,?,?,?)")) {
                    ps.setInt(1, idPatient); ps.setInt(2, idMedecin); ps.setInt(3, idSecretaire);
                    ps.setString(4, dateRdv); ps.setString(5, motif); ps.setString(6, statut);
                    ps.executeUpdate();
                }
                logHistorique(conn, idSecretaire, "Creation", "rendez_vous", 0,
                        "Nouveau RDV pour patient " + idPatient + " avec médecin " + idMedecin);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Rendez-vous créé avec succès !");
            } else {
                // UPDATE
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE rendez_vous SET id_patient=?, id_medecin=?, id_secretaire=?, date_rdv=?, motif=?, statut=? WHERE id_rdv=?")) {
                    ps.setInt(1, idPatient); ps.setInt(2, idMedecin); ps.setInt(3, idSecretaire);
                    ps.setString(4, dateRdv); ps.setString(5, motif); ps.setString(6, statut);
                    ps.setInt(7, selectedRdvId);
                    ps.executeUpdate();
                }
                logHistorique(conn, idSecretaire, "Modification", "rendez_vous", selectedRdvId,
                        "Modification RDV id=" + selectedRdvId);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Rendez-vous mis à jour !");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage()); return;
        }
        selectedRdvId = -1;
        showRendezVous();
    }

    @FXML
    private void onModifierRdv() {
        ObservableList<String> sel = rdvTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        selectedRdvId = Integer.parseInt(sel.get(5));
        if (rdvFormTitle != null) rdvFormTitle.setText("Modifier le rendez-vous");
        clearRdvForm();
        loadRdvFormCombos();

        String sql = """
                SELECT r.id_patient, r.id_medecin, r.date_rdv, r.motif, r.statut,
                       p.nom AS p_nom, p.prenom AS p_prenom,
                       u.nom AS m_nom, u.prenom AS m_prenom
                FROM rendez_vous r
                JOIN patient p ON r.id_patient = p.id_patient
                JOIN utilisateur u ON r.id_medecin = u.id_utilisateur
                WHERE r.id_rdv = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedRdvId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String patientEntry = rs.getInt("id_patient") + " - " + rs.getString("p_nom") + " " + rs.getString("p_prenom");
                String medecinEntry = rs.getInt("id_medecin") + " - " + rs.getString("m_nom") + " " + rs.getString("m_prenom");
                if (rdvPatientCombo != null) rdvPatientCombo.setValue(patientEntry);
                if (rdvMedecinCombo != null) rdvMedecinCombo.setValue(medecinEntry);
                Timestamp ts = rs.getTimestamp("date_rdv");
                if (ts != null) {
                    if (rdvDatePicker != null) rdvDatePicker.setValue(ts.toLocalDateTime().toLocalDate());
                    if (rdvHeureField != null) rdvHeureField.setText(
                            String.format("%02d:%02d", ts.toLocalDateTime().getHour(), ts.toLocalDateTime().getMinute()));
                }
                if (rdvMotifArea  != null) rdvMotifArea.setText(rs.getString("motif") != null ? rs.getString("motif") : "");
                if (rdvStatutCombo != null) rdvStatutCombo.setValue(rs.getString("statut"));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le RDV : " + e.getMessage()); return;
        }
        hideAllViews(); show(rdvFormView);
    }

    @FXML
    private void onAnnulerRdv() {
        ObservableList<String> sel = rdvTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        int idRdv = Integer.parseInt(sel.get(5));
        String patient = sel.get(0);

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Annuler le rendez-vous");
        confirmation.setHeaderText("Annuler le RDV de " + patient + " ?");
        confirmation.setContentText("Le statut passera à 'Annule'. Cette action est réversible via modification.");
        confirmation.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE rendez_vous SET statut='Annule' WHERE id_rdv=?")) {
                ps.setInt(1, idRdv);
                ps.executeUpdate();
                logHistorique(conn, currentSecretaireId > 0 ? currentSecretaireId : 1,
                        "Modification", "rendez_vous", idRdv, "Annulation RDV id=" + idRdv + " patient=" + patient);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le rendez-vous a été annulé.");
                loadAllRdv();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
            }
        });
    }

    private void clearRdvForm() {
        if (rdvPatientCombo != null) rdvPatientCombo.setValue(null);
        if (rdvMedecinCombo != null) rdvMedecinCombo.setValue(null);
        if (rdvDatePicker   != null) rdvDatePicker.setValue(LocalDate.now());
        if (rdvHeureField   != null) rdvHeureField.setText("09:00");
        if (rdvMotifArea    != null) rdvMotifArea.clear();
        if (rdvStatutCombo  != null) rdvStatutCombo.setValue("Planifie");
    }

    private void logHistorique(Connection conn, int userId, String action, String table, int idEnreg, String details) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, userId); ps.setString(2, action); ps.setString(3, table);
            ps.setInt(4, idEnreg); ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("Erreur historique : " + e.getMessage()); }
    }

    // ============= CRÉATION PATIENT =============

    @FXML
    private void onSavePatient() {
        if (nomField.getText().trim().isEmpty())    { showAlert(Alert.AlertType.WARNING, "Attention", "Le nom est obligatoire");    return; }
        if (prenomField.getText().trim().isEmpty())  { showAlert(Alert.AlertType.WARNING, "Attention", "Le prénom est obligatoire"); return; }
        if (numSecuField.getText().trim().isEmpty()) { showAlert(Alert.AlertType.WARNING, "Attention", "Le numéro de sécurité sociale est obligatoire"); return; }
        if (niveauGraviteComboBox != null && niveauGraviteComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le niveau de gravité est obligatoire"); return;
        }

        int gravite      = (niveauGraviteComboBox != null && niveauGraviteComboBox.getValue() != null) ? niveauGraviteComboBox.getValue() : 3;
        int secretaireId = currentSecretaireId > 0 ? currentSecretaireId : 1;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            int patientId;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO patient (nom, prenom, num_secu, email, telephone, adresse, date_naissance, sexe) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, nomField.getText().trim());
                pstmt.setString(2, prenomField.getText().trim());
                pstmt.setString(3, numSecuField.getText().trim());
                pstmt.setString(4, emailPatientField.getText().trim());
                pstmt.setString(5, telephoneField.getText().trim());
                pstmt.setString(6, adresseField.getText().trim());
                pstmt.setString(7, dateNaissancePicker != null && dateNaissancePicker.getValue() != null
                        ? dateNaissancePicker.getValue().toString() : null);
                pstmt.setString(8, sexeComboBox != null ? sexeComboBox.getValue() : null);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                patientId = rs.next() ? rs.getInt(1) : 0;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO dossier (id_patient, id_secretaire, date_arrivee, symptomes, niveau_gravite, statut) " +
                            "VALUES (?, ?, NOW(), ?, ?, 'Attente')")) {
                pstmt.setInt(1, patientId);
                pstmt.setInt(2, secretaireId);
                pstmt.setString(3, motifTextArea.getText().trim());
                pstmt.setInt(4, gravite);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Creation', 'patient', ?, ?)")) {
                pstmt.setInt(1, secretaireId);
                pstmt.setInt(2, patientId);
                pstmt.setString(3, "Patient: " + prenomField.getText() + " " + nomField.getText() + " (gravité " + gravite + ")");
                pstmt.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Patient enregistré avec succès !");
            clearRegistrationForm();
            showDashboard();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    // ============= MODIFICATION PATIENT =============

    @FXML
    private void onModifierPatient() {
        ObservableList<String> selection = allPatientsTable.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        selectedPatientId = Integer.parseInt(selection.get(5));

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nom, prenom, telephone, email, adresse, num_secu FROM patient WHERE id_patient = ?")) {
            ps.setInt(1, selectedPatientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                editNomField.setText(     rs.getString("nom")      != null ? rs.getString("nom")      : "");
                editPrenomField.setText(  rs.getString("prenom")   != null ? rs.getString("prenom")   : "");
                editTelField.setText(     rs.getString("telephone") != null ? rs.getString("telephone") : "");
                editEmailField.setText(   rs.getString("email")    != null ? rs.getString("email")    : "");
                editAdresseField.setText( rs.getString("adresse")  != null ? rs.getString("adresse")  : "");
                editNumSecuField.setText( rs.getString("num_secu") != null ? rs.getString("num_secu") : "");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la fiche : " + e.getMessage());
            return;
        }

        hideAllViews();
        show(editPatientView);
    }

    @FXML
    private void onSaveEditPatient() {
        if (selectedPatientId < 0) return;

        String nom    = editNomField.getText().trim();
        String prenom = editPrenomField.getText().trim();
        if (nom.isEmpty())    { showAlert(Alert.AlertType.WARNING, "Attention", "Le nom est obligatoire");    return; }
        if (prenom.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Attention", "Le prénom est obligatoire"); return; }

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE patient SET nom=?, prenom=?, telephone=?, email=?, adresse=?, num_secu=? WHERE id_patient=?")) {
                ps.setString(1, nom);
                ps.setString(2, prenom);
                ps.setString(3, editTelField.getText().trim());
                ps.setString(4, editEmailField.getText().trim());
                ps.setString(5, editAdresseField.getText().trim());
                ps.setString(6, editNumSecuField.getText().trim());
                ps.setInt(7, selectedPatientId);
                ps.executeUpdate();
            }
            try (PreparedStatement ph = conn.prepareStatement(
                    "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) VALUES (?, 'Modification', 'patient', ?, ?)")) {
                ph.setInt(1, currentSecretaireId > 0 ? currentSecretaireId : 1);
                ph.setInt(2, selectedPatientId);
                ph.setString(3, "Mise à jour fiche : " + prenom + " " + nom);
                ph.executeUpdate();
            }
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Fiche de " + prenom + " " + nom + " mise à jour.");
            selectedPatientId = -1;
            showPatientList();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    // ============= SUPPRESSION PATIENT ← AJOUT =============

    @FXML
    private void onSupprimerPatient() {
        ObservableList<String> selection = allPatientsTable.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        String nom      = selection.get(0);
        String prenom   = selection.get(1);
        int    idPatient = Integer.parseInt(selection.get(5));

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le patient " + prenom + " " + nom + " ?");
        confirmation.setContentText("Cette action supprimera aussi tous les dossiers associés. Elle est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                // 1. Supprimer les dossiers liés (contrainte FK)
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM dossier WHERE id_patient = ?")) {
                    ps.setInt(1, idPatient);
                    ps.executeUpdate();
                }

                // 2. Supprimer le patient
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM patient WHERE id_patient = ?")) {
                    ps.setInt(1, idPatient);
                    ps.executeUpdate();
                }

                // 3. Historique
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) " +
                                "VALUES (?, 'Suppression', 'patient', ?, ?)")) {
                    ps.setInt(1, currentSecretaireId > 0 ? currentSecretaireId : 1);
                    ps.setInt(2, idPatient);
                    ps.setString(3, "Suppression du patient : " + prenom + " " + nom);
                    ps.executeUpdate();
                }

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Le patient " + prenom + " " + nom + " a été supprimé.");
                loadAllPatients();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la suppression : " + e.getMessage());
            }
        });
    }

    // ============= DIVERS =============

    @FXML private void onRefreshList() { loadAllPatients(); }

    private void clearRegistrationForm() {
        if (nomField            != null) nomField.clear();
        if (prenomField         != null) prenomField.clear();
        if (numSecuField        != null) numSecuField.clear();
        if (emailPatientField   != null) emailPatientField.clear();
        if (dateNaissancePicker != null) dateNaissancePicker.setValue(null);
        if (sexeComboBox        != null) sexeComboBox.setValue(null);
        if (telephoneField      != null) telephoneField.clear();
        if (adresseField        != null) adresseField.clear();
        if (motifTextArea       != null) motifTextArea.clear();
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
        this.currentSecretaireId   = id;
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
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}