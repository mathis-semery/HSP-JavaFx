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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SecretaireDashboardController implements Initializable {

    // ── Header ───────────────────────────────────────────────────────────────
    @FXML private Label userNameLabel;
    @FXML private Label todayCountLabel;

    // ── Vues principales ─────────────────────────────────────────────────────
    @FXML private VBox dashboardView;
    @FXML private VBox registrationView;
    @FXML private VBox patientListView;
    @FXML private VBox triageView;
    @FXML private VBox editPatientView;
    @FXML private VBox nouvelleVisiteView;
    @FXML private VBox dossiersPatientView;
    @FXML private VBox rendezVousView;
    @FXML private VBox rdvFormView;
    @FXML private VBox historiqueView;

    // ── Dashboard ────────────────────────────────────────────────────────────
    @FXML private Label todayPatientsLabel;
    @FXML private Label waitingPatientsLabel;
    @FXML private Label triagedPatientsLabel;
    @FXML private TableView<ObservableList<String>> recentPatientsTable;

    // ── Création patient ─────────────────────────────────────────────────────
    @FXML private TextField  nomField;
    @FXML private TextField  prenomField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private TextField  telephoneField;
    @FXML private TextField  adresseField;
    @FXML private TextField  numSecuField;
    @FXML private TextField  emailPatientField;
    @FXML private TextArea   motifPremVisite;   // motif de la 1ère visite

    // ── Liste patients ───────────────────────────────────────────────────────
    @FXML private TextField  searchField;
    @FXML private TableView<ObservableList<String>> allPatientsTable;
    @FXML private Button     btnNouvelleVisite;
    @FXML private Button     btnVoirDossiers;
    @FXML private Button     btnModifierPatient;
    @FXML private Button     btnSupprimerPatient;

    // ── Nouvelle visite ──────────────────────────────────────────────────────
    @FXML private Label    nvPatientInfoLabel;
    @FXML private Label    nvPatientSecuLabel;
    @FXML private Label    nvDerniereVisiteLabel;
    @FXML private TextArea nvMotifArea;

    // ── Dossiers patient ─────────────────────────────────────────────────────
    @FXML private Label    dossiersPatientTitreLabel;
    @FXML private Label    ficheNomLabel;
    @FXML private Label    ficheTelLabel;
    @FXML private Label    ficheEmailLabel;
    @FXML private Label    ficheSecuLabel;
    @FXML private Label    statsTotalLabel;
    @FXML private Label    statsAttenteLabel;
    @FXML private Label    statsTermineLabel;
    @FXML private TableView<ObservableList<String>> dossiersTable;

    // ── Édition patient ──────────────────────────────────────────────────────
    @FXML private TextField editNomField;
    @FXML private TextField editPrenomField;
    @FXML private TextField editTelField;
    @FXML private TextField editEmailField;
    @FXML private TextField editAdresseField;
    @FXML private TextField editNumSecuField;

    // ── Triage ───────────────────────────────────────────────────────────────
    @FXML private TableView<ObservableList<String>> waitingTriageTable;

    // ── Rendez-vous liste ────────────────────────────────────────────────────
    @FXML private TextField  rdvSearchField;
    @FXML private TableView<ObservableList<String>> rdvTable;
    @FXML private Button     btnModifierRdv;
    @FXML private Button     btnAnnulerRdv;

    // ── Rendez-vous formulaire ───────────────────────────────────────────────
    @FXML private Label    rdvFormTitle;
    @FXML private ComboBox<String> rdvPatientCombo;
    @FXML private ComboBox<String> rdvMedecinCombo;
    @FXML private DatePicker       rdvDatePicker;
    @FXML private TextField        rdvHeureField;
    @FXML private ComboBox<String> rdvStatutCombo;
    @FXML private TextArea         rdvMotifArea;

    // ── Historique ───────────────────────────────────────────────────────────
    @FXML private TextField  historiqueSearchField;
    @FXML private ComboBox<String> historiqueActionFilter;
    @FXML private TableView<ObservableList<String>> historiqueTable;

    // ── Constantes DB ────────────────────────────────────────────────────────
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── État ─────────────────────────────────────────────────────────────────
    private int    currentSecretaireId   = -1;
    private String currentSecretaireName = "Secrétaire";
    private int    selectedPatientId     = -1;
    private int    selectedRdvId         = -1;

    // =========================================================================
    //  INIT
    // =========================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ComboBox création patient
        if (sexeComboBox != null)
            sexeComboBox.getItems().addAll("Homme", "Femme", "Autre");

        // Filtre historique
        if (historiqueActionFilter != null)
            historiqueActionFilter.getItems().addAll("Toutes", "Creation", "Modification", "Suppression");

        // Setup tables
        setupRecentPatientsTable();
        setupAllPatientsTable();
        setupWaitingTriageTable();
        setupRdvTable();
        setupHistoriqueTable();
        setupDossiersTable();

        // Binding boutons liste patients
        bindButtonToSelection(btnNouvelleVisite,  allPatientsTable);
        bindButtonToSelection(btnVoirDossiers,    allPatientsTable);
        bindButtonToSelection(btnModifierPatient, allPatientsTable);
        bindButtonToSelection(btnSupprimerPatient,allPatientsTable);

        // Binding boutons RDV
        bindButtonToSelection(btnModifierRdv, rdvTable);
        bindButtonToSelection(btnAnnulerRdv,  rdvTable);

        // Double-clic liste patients → modifier
        if (allPatientsTable != null) {
            allPatientsTable.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2
                        && allPatientsTable.getSelectionModel().getSelectedItem() != null)
                    onModifierPatient();
            });
        }

        showDashboard();
    }

    private void bindButtonToSelection(Button btn, TableView<?> table) {
        if (btn != null && table != null)
            btn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
    }

    // =========================================================================
    //  SETUP COLONNES
    // =========================================================================

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
        String[] cols = {"Patient", "Arrivée", "Gravité", "Motif"};
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

    @SuppressWarnings("unchecked")
    private void setupHistoriqueTable() {
        if (historiqueTable == null) return;
        historiqueTable.getColumns().clear();
        String[] cols = {"Date", "Action", "Table", "Utilisateur", "Détails"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            // Colorer la colonne Action
            if (i == 1) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); return; }
                        setText(item);
                        switch (item) {
                            case "Creation"     -> setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                            case "Modification" -> setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                            case "Suppression"  -> setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                            default             -> setStyle("");
                        }
                    }
                });
            }
            historiqueTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupDossiersTable() {
        if (dossiersTable == null) return;
        dossiersTable.getColumns().clear();
        String[] cols = {"Date arrivée", "Statut", "Gravité", "Motif", "Médecin"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            // Colorer la colonne Statut
            if (i == 1) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); return; }
                        setText(item);
                        switch (item) {
                            case "Attente"  -> setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                            case "EnCours"  -> setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                            case "Termine"  -> setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                            default         -> setStyle("");
                        }
                    }
                });
            }
            dossiersTable.getColumns().add(col);
        }
    }

    // =========================================================================
    //  NAVIGATION
    // =========================================================================

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
        clearRdvForm(); loadRdvFormCombos();
        hideAllViews(); show(rdvFormView);
    }
    @FXML public void showHistorique() {
        hideAllViews(); show(historiqueView); loadHistorique();
    }

    private void hideAllViews() {
        for (VBox v : new VBox[]{
                dashboardView, registrationView, patientListView,
                triageView, editPatientView,
                nouvelleVisiteView, dossiersPatientView,
                rendezVousView, rdvFormView,
                historiqueView
        }) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }
    private void show(VBox v) {
        if (v != null) { v.setVisible(true); v.setManaged(true); }
    }

    // =========================================================================
    //  DASHBOARD
    // =========================================================================

    private void loadDashboardData() {
        try (Connection conn = getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM dossier WHERE DATE(date_arrivee) = CURDATE()")) {
                if (rs.next()) {
                    int n = rs.getInt(1);
                    if (todayPatientsLabel != null) todayPatientsLabel.setText(String.valueOf(n));
                    if (todayCountLabel    != null) todayCountLabel.setText(String.valueOf(n));
                }
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM dossier WHERE statut = 'Attente'")) {
                if (rs.next() && waitingPatientsLabel != null)
                    waitingPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM dossier WHERE DATE(date_arrivee) = CURDATE() AND statut IN ('EnCours','Termine')")) {
                if (rs.next() && triagedPatientsLabel != null)
                    triagedPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadDashboardData : " + e.getMessage());
        }
    }

    private void loadRecentPatients() {
        if (recentPatientsTable == null) return;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = """
                SELECT p.nom, p.prenom, d.date_arrivee, d.statut
                FROM dossier d
                JOIN patient p ON d.id_patient = p.id_patient
                ORDER BY d.date_arrivee DESC LIMIT 10
                """;
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
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

    // =========================================================================
    //  LISTE PATIENTS
    // =========================================================================

    private void loadAllPatients() {
        if (allPatientsTable == null) return;
        String recherche = searchField != null ? searchField.getText().trim() : "";
        boolean avecFiltre = !recherche.isEmpty();
        String sql = avecFiltre
                ? """
                  SELECT p.id_patient, p.nom, p.prenom, p.telephone, p.email,
                         COALESCE((SELECT d.statut FROM dossier d WHERE d.id_patient = p.id_patient
                                   ORDER BY d.date_arrivee DESC LIMIT 1), 'Aucun dossier') AS statut
                  FROM patient p
                  WHERE LOWER(p.nom) LIKE ? OR LOWER(p.prenom) LIKE ?
                  ORDER BY p.nom
                  """
                : """
                  SELECT p.id_patient, p.nom, p.prenom, p.telephone, p.email,
                         COALESCE((SELECT d.statut FROM dossier d WHERE d.id_patient = p.id_patient
                                   ORDER BY d.date_arrivee DESC LIMIT 1), 'Aucun dossier') AS statut
                  FROM patient p
                  ORDER BY p.nom
                  """;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                row.add(String.valueOf(rs.getInt("id_patient"))); // index 5 — caché
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadAllPatients : " + e.getMessage());
        }
        allPatientsTable.setItems(data);
    }

    @FXML private void onRefreshList() { loadAllPatients(); }

    // =========================================================================
    //  CRÉATION PATIENT (1ère visite incluse)
    // =========================================================================

    @FXML
    private void onSavePatient() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le nom est obligatoire."); return;
        }
        if (prenomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le prénom est obligatoire."); return;
        }
        if (numSecuField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le numéro de sécurité sociale est obligatoire."); return;
        }
        String motif = motifPremVisite != null ? motifPremVisite.getText().trim() : "";
        if (motif.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez saisir le motif de la première visite."); return;
        }

        int secretaireId = currentSecretaireId > 0 ? currentSecretaireId : 1;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // 1. Créer le patient
            int patientId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO patient (nom, prenom, num_secu, email, telephone, adresse, date_naissance, sexe) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nomField.getText().trim());
                ps.setString(2, prenomField.getText().trim());
                ps.setString(3, numSecuField.getText().trim());
                ps.setString(4, emailPatientField != null ? emailPatientField.getText().trim() : "");
                ps.setString(5, telephoneField.getText().trim());
                ps.setString(6, adresseField.getText().trim());
                ps.setString(7, dateNaissancePicker != null && dateNaissancePicker.getValue() != null
                        ? dateNaissancePicker.getValue().toString() : null);
                ps.setString(8, sexeComboBox != null ? sexeComboBox.getValue() : null);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                patientId = keys.next() ? keys.getInt(1) : 0;
            }

            // 2. Créer le dossier de la 1ère visite avec le motif
            int dossierId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO dossier (id_patient, id_secretaire, date_arrivee, symptomes, niveau_gravite, statut) " +
                            "VALUES (?, ?, NOW(), ?, 3, 'Attente')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, patientId);
                ps.setInt(2, secretaireId);
                ps.setString(3, motif);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                dossierId = keys.next() ? keys.getInt(1) : 0;
            }

            // 3. Historique
            logHistorique(conn, secretaireId, "Creation", "patient", patientId,
                    "Nouveau patient : " + prenomField.getText().trim() + " " + nomField.getText().trim()
                            + " | motif : " + motif);

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Patient enregistré et mis en attente de triage !");
            clearRegistrationForm();
            showDashboard();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    private void clearRegistrationForm() {
        if (nomField            != null) nomField.clear();
        if (prenomField         != null) prenomField.clear();
        if (numSecuField        != null) numSecuField.clear();
        if (emailPatientField   != null) emailPatientField.clear();
        if (dateNaissancePicker != null) dateNaissancePicker.setValue(null);
        if (sexeComboBox        != null) sexeComboBox.setValue(null);
        if (telephoneField      != null) telephoneField.clear();
        if (adresseField        != null) adresseField.clear();
        if (motifPremVisite     != null) motifPremVisite.clear();
    }

    // =========================================================================
    //  NOUVELLE VISITE (patient existant)
    // =========================================================================

    @FXML
    private void onNouvelleVisite() {
        ObservableList<String> sel = allPatientsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        selectedPatientId = Integer.parseInt(sel.get(5));
        String nom    = sel.get(0);
        String prenom = sel.get(1);

        if (nvPatientInfoLabel != null)
            nvPatientInfoLabel.setText(prenom + " " + nom);

        // Récupérer N° sécu + infos dernière visite
        String sql = """
                SELECT p.num_secu,
                       (SELECT DATE_FORMAT(d.date_arrivee, '%d/%m/%Y')
                        FROM dossier d
                        WHERE d.id_patient = p.id_patient
                        ORDER BY d.date_arrivee DESC LIMIT 1) AS derniere_visite,
                       (SELECT COUNT(*) FROM dossier d
                        WHERE d.id_patient = p.id_patient) AS nb_visites
                FROM patient p
                WHERE p.id_patient = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedPatientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (nvPatientSecuLabel != null)
                    nvPatientSecuLabel.setText("N° Sécu : " +
                            (rs.getString("num_secu") != null ? rs.getString("num_secu") : "—"));
                String derniere = rs.getString("derniere_visite");
                int nbVisites   = rs.getInt("nb_visites");
                if (nvDerniereVisiteLabel != null) {
                    nvDerniereVisiteLabel.setText(derniere != null
                            ? "Dernière visite : " + derniere + "  (" + nbVisites + " visite(s) au total)"
                            : "Première visite");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur info patient nouvelle visite : " + e.getMessage());
        }

        if (nvMotifArea != null) nvMotifArea.clear();
        hideAllViews();
        show(nouvelleVisiteView);
    }

    @FXML
    private void onSauvegarderNouvelleVisite() {
        if (selectedPatientId < 0) return;

        String motif = nvMotifArea != null ? nvMotifArea.getText().trim() : "";
        if (motif.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez saisir le motif de la visite."); return;
        }

        int secretaireId = currentSecretaireId > 0 ? currentSecretaireId : 1;

        try (Connection conn = getConnection()) {
            // Créer un nouveau dossier pour ce patient existant
            int dossierId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO dossier (id_patient, id_secretaire, date_arrivee, symptomes, niveau_gravite, statut) " +
                            "VALUES (?, ?, NOW(), ?, 3, 'Attente')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, selectedPatientId);
                ps.setInt(2, secretaireId);
                ps.setString(3, motif);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                dossierId = keys.next() ? keys.getInt(1) : 0;
            }

            // Historique
            logHistorique(conn, secretaireId, "Creation", "dossier", dossierId,
                    "Nouvelle visite patient id=" + selectedPatientId + " | motif : " + motif);

            showAlert(Alert.AlertType.INFORMATION, "Visite enregistrée",
                    "La visite a été créée. Le patient est en attente de triage.");
            selectedPatientId = -1;
            showPatientList();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    // =========================================================================
    //  DOSSIERS PATIENT (historique complet)
    // =========================================================================

    @FXML
    private void onVoirDossiersPatientSelectionne() {
        ObservableList<String> sel = allPatientsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        int id = Integer.parseInt(sel.get(5));
        hideAllViews();
        show(dossiersPatientView);
        loadDossiersPatient(id);
    }

    private void loadDossiersPatient(int idPatient) {
        if (dossiersTable == null) return;

        // Fiche identité
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nom, prenom, telephone, email, num_secu FROM patient WHERE id_patient = ?")) {
            ps.setInt(1, idPatient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String prenom = rs.getString("prenom") != null ? rs.getString("prenom") : "—";
                String nom    = rs.getString("nom")    != null ? rs.getString("nom")    : "—";
                if (dossiersPatientTitreLabel != null)
                    dossiersPatientTitreLabel.setText("Dossiers de " + prenom + " " + nom);
                if (ficheNomLabel   != null) ficheNomLabel.setText("Nom : " + prenom + " " + nom);
                if (ficheTelLabel   != null) ficheTelLabel.setText("Tél : " +
                        (rs.getString("telephone") != null ? rs.getString("telephone") : "—"));
                if (ficheEmailLabel != null) ficheEmailLabel.setText("Email : " +
                        (rs.getString("email")     != null ? rs.getString("email")     : "—"));
                if (ficheSecuLabel  != null) ficheSecuLabel.setText("N° Sécu : " +
                        (rs.getString("num_secu")  != null ? rs.getString("num_secu")  : "—"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur fiche patient : " + e.getMessage());
        }

        // Tous les dossiers (passés + en cours)
        String sql = """
                SELECT d.date_arrivee, d.statut, d.niveau_gravite, d.symptomes,
                       COALESCE(CONCAT(u.prenom, ' ', u.nom), '—') AS medecin
                FROM dossier d
                LEFT JOIN utilisateur u ON d.id_medecin = u.id_utilisateur
                WHERE d.id_patient = ?
                ORDER BY d.date_arrivee DESC
                """;

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        int total = 0, attente = 0, termine = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPatient);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                total++;
                String statut = rs.getString("statut") != null ? rs.getString("statut") : "—";
                if ("Attente".equals(statut)) attente++;
                if ("Termine".equals(statut)) termine++;

                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee")));
                row.add(statut);
                row.add(rs.getObject("niveau_gravite") != null
                        ? "Gravité " + rs.getInt("niveau_gravite") : "—");
                row.add(truncate(rs.getString("symptomes"), 70));
                row.add(rs.getString("medecin"));
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadDossiersPatient : " + e.getMessage());
        }

        dossiersTable.setItems(data);
        if (statsTotalLabel   != null) statsTotalLabel.setText(String.valueOf(total));
        if (statsAttenteLabel != null) statsAttenteLabel.setText(String.valueOf(attente));
        if (statsTermineLabel != null) statsTermineLabel.setText(String.valueOf(termine));
    }

    // =========================================================================
    //  MODIFICATION PATIENT
    // =========================================================================

    @FXML
    private void onModifierPatient() {
        ObservableList<String> sel = allPatientsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        selectedPatientId = Integer.parseInt(sel.get(5));

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nom, prenom, telephone, email, adresse, num_secu FROM patient WHERE id_patient = ?")) {
            ps.setInt(1, selectedPatientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                editNomField.setText(     rs.getString("nom")       != null ? rs.getString("nom")       : "");
                editPrenomField.setText(  rs.getString("prenom")    != null ? rs.getString("prenom")    : "");
                editTelField.setText(     rs.getString("telephone") != null ? rs.getString("telephone") : "");
                editEmailField.setText(   rs.getString("email")     != null ? rs.getString("email")     : "");
                editAdresseField.setText( rs.getString("adresse")   != null ? rs.getString("adresse")   : "");
                editNumSecuField.setText( rs.getString("num_secu")  != null ? rs.getString("num_secu")  : "");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la fiche : " + e.getMessage()); return;
        }

        hideAllViews();
        show(editPatientView);
    }

    @FXML
    private void onSaveEditPatient() {
        if (selectedPatientId < 0) return;

        String nom    = editNomField.getText().trim();
        String prenom = editPrenomField.getText().trim();
        if (nom.isEmpty())    { showAlert(Alert.AlertType.WARNING, "Attention", "Le nom est obligatoire.");    return; }
        if (prenom.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Attention", "Le prénom est obligatoire."); return; }

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
            logHistorique(conn, currentSecretaireId > 0 ? currentSecretaireId : 1,
                    "Modification", "patient", selectedPatientId,
                    "Mise à jour fiche : " + prenom + " " + nom);

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Fiche de " + prenom + " " + nom + " mise à jour.");
            selectedPatientId = -1;
            showPatientList();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    // =========================================================================
    //  SUPPRESSION PATIENT
    // =========================================================================

    @FXML
    private void onSupprimerPatient() {
        ObservableList<String> sel = allPatientsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        String nom    = sel.get(0);
        String prenom = sel.get(1);
        int idPatient = Integer.parseInt(sel.get(5));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le patient " + prenom + " " + nom + " ?");
        confirm.setContentText("Tous les dossiers associés seront supprimés. Action irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM dossier WHERE id_patient = ?")) {
                    ps.setInt(1, idPatient); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM patient WHERE id_patient = ?")) {
                    ps.setInt(1, idPatient); ps.executeUpdate();
                }
                logHistorique(conn, currentSecretaireId > 0 ? currentSecretaireId : 1,
                        "Suppression", "patient", idPatient,
                        "Suppression du patient : " + prenom + " " + nom);

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

    // =========================================================================
    //  TRIAGE
    // =========================================================================

    private void loadWaitingTriage() {
        if (waitingTriageTable == null) return;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = """
                SELECT p.nom, p.prenom, d.date_arrivee, d.symptomes, d.niveau_gravite
                FROM dossier d
                JOIN patient p ON d.id_patient = p.id_patient
                WHERE d.statut = 'Attente'
                ORDER BY d.niveau_gravite DESC, d.date_arrivee ASC
                """;
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
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

    // =========================================================================
    //  RENDEZ-VOUS
    // =========================================================================

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
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                row.add(String.valueOf(rs.getInt("id_rdv"))); // index 5 — caché
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadAllRdv : " + e.getMessage());
        }
        rdvTable.setItems(data);
    }

    private void loadRdvFormCombos() {
        if (rdvPatientCombo != null) {
            rdvPatientCombo.getItems().clear();
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT id_patient, nom, prenom FROM patient ORDER BY nom, prenom")) {
                while (rs.next())
                    rdvPatientCombo.getItems().add(
                            rs.getInt("id_patient") + " - " + rs.getString("nom") + " " + rs.getString("prenom"));
            } catch (SQLException e) {
                System.err.println("Erreur combo patient : " + e.getMessage());
            }
        }
        if (rdvMedecinCombo != null) {
            rdvMedecinCombo.getItems().clear();
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT id_utilisateur, nom, prenom FROM utilisateur WHERE role='Medecin' ORDER BY nom, prenom")) {
                while (rs.next())
                    rdvMedecinCombo.getItems().add(
                            rs.getInt("id_utilisateur") + " - " + rs.getString("nom") + " " + rs.getString("prenom"));
            } catch (SQLException e) {
                System.err.println("Erreur combo médecin : " + e.getMessage());
            }
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

        int idPatient    = Integer.parseInt(rdvPatientCombo.getValue().split(" - ")[0]);
        int idMedecin    = Integer.parseInt(rdvMedecinCombo.getValue().split(" - ")[0]);
        int idSecretaire = currentSecretaireId > 0 ? currentSecretaireId : 1;
        String dateRdv   = rdvDatePicker.getValue().toString() + " " + heure + ":00";
        String motif     = rdvMotifArea != null ? rdvMotifArea.getText().trim() : "";
        String statut    = rdvStatutCombo != null && rdvStatutCombo.getValue() != null
                ? rdvStatutCombo.getValue() : "Planifie";

        try (Connection conn = getConnection()) {
            // Vérif conflit médecin
            String sqlConflictMed = "SELECT COUNT(*) FROM rendez_vous WHERE id_medecin=? AND date_rdv=? AND statut!='Annule'"
                    + (selectedRdvId >= 0 ? " AND id_rdv!=" + selectedRdvId : "");
            try (PreparedStatement ps = conn.prepareStatement(sqlConflictMed)) {
                ps.setInt(1, idMedecin); ps.setString(2, dateRdv);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.WARNING, "Conflit",
                            "Ce médecin a déjà un rendez-vous à cette date et heure."); return;
                }
            }
            // Vérif conflit patient
            String sqlConflictPat = "SELECT COUNT(*) FROM rendez_vous WHERE id_patient=? AND date_rdv=? AND statut!='Annule'"
                    + (selectedRdvId >= 0 ? " AND id_rdv!=" + selectedRdvId : "");
            try (PreparedStatement ps = conn.prepareStatement(sqlConflictPat)) {
                ps.setInt(1, idPatient); ps.setString(2, dateRdv);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.WARNING, "Conflit",
                            "Ce patient a déjà un rendez-vous à cette date et heure."); return;
                }
            }

            if (selectedRdvId < 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO rendez_vous (id_patient,id_medecin,id_secretaire,date_rdv,motif,statut) VALUES(?,?,?,?,?,?)")) {
                    ps.setInt(1, idPatient); ps.setInt(2, idMedecin); ps.setInt(3, idSecretaire);
                    ps.setString(4, dateRdv); ps.setString(5, motif); ps.setString(6, statut);
                    ps.executeUpdate();
                }
                logHistorique(conn, idSecretaire, "Creation", "rendez_vous", 0,
                        "Nouveau RDV patient id=" + idPatient + " médecin id=" + idMedecin + " date=" + dateRdv);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Rendez-vous créé !");
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE rendez_vous SET id_patient=?,id_medecin=?,id_secretaire=?,date_rdv=?,motif=?,statut=? WHERE id_rdv=?")) {
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
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedRdvId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rdvPatientCombo != null)
                    rdvPatientCombo.setValue(rs.getInt("id_patient") + " - " +
                            rs.getString("p_nom") + " " + rs.getString("p_prenom"));
                if (rdvMedecinCombo != null)
                    rdvMedecinCombo.setValue(rs.getInt("id_medecin") + " - " +
                            rs.getString("m_nom") + " " + rs.getString("m_prenom"));
                Timestamp ts = rs.getTimestamp("date_rdv");
                if (ts != null) {
                    if (rdvDatePicker != null) rdvDatePicker.setValue(ts.toLocalDateTime().toLocalDate());
                    if (rdvHeureField != null)
                        rdvHeureField.setText(String.format("%02d:%02d",
                                ts.toLocalDateTime().getHour(), ts.toLocalDateTime().getMinute()));
                }
                if (rdvMotifArea  != null) rdvMotifArea.setText(rs.getString("motif") != null ? rs.getString("motif") : "");
                if (rdvStatutCombo != null) rdvStatutCombo.setValue(rs.getString("statut"));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger le RDV : " + e.getMessage()); return;
        }
        hideAllViews();
        show(rdvFormView);
    }

    @FXML
    private void onAnnulerRdv() {
        ObservableList<String> sel = rdvTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        int idRdv    = Integer.parseInt(sel.get(5));
        String patient = sel.get(0);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler le rendez-vous");
        confirm.setHeaderText("Annuler le RDV de " + patient + " ?");
        confirm.setContentText("Le statut passera à 'Annule'. Réversible via modification.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE rendez_vous SET statut='Annule' WHERE id_rdv=?")) {
                ps.setInt(1, idRdv);
                ps.executeUpdate();
                logHistorique(conn, currentSecretaireId > 0 ? currentSecretaireId : 1,
                        "Modification", "rendez_vous", idRdv,
                        "Annulation RDV id=" + idRdv + " patient=" + patient);
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

    // =========================================================================
    //  HISTORIQUE
    // =========================================================================

    private void loadHistorique() {
        if (historiqueTable == null) return;

        String recherche = historiqueSearchField != null
                ? historiqueSearchField.getText().trim() : "";
        String action    = historiqueActionFilter != null
                ? historiqueActionFilter.getValue() : "Toutes";

        StringBuilder sql = new StringBuilder("""
                SELECT h.date_action, h.action, h.table_concernee,
                       COALESCE(CONCAT(u.prenom, ' ', u.nom), 'Système') AS utilisateur,
                       h.details
                FROM historique h
                LEFT JOIN utilisateur u ON h.id_utilisateur = u.id_utilisateur
                WHERE 1=1
                """);

        if (action != null && !action.equals("Toutes"))
            sql.append(" AND h.action = '").append(action).append("'");
        if (!recherche.isEmpty()) {
            String esc = recherche.replace("'", "\\'");
            sql.append(" AND (h.details LIKE '%").append(esc).append("%'")
                    .append(" OR CONCAT(u.prenom,' ',u.nom) LIKE '%").append(esc).append("%')");
        }
        sql.append(" ORDER BY h.date_action DESC LIMIT 300");

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(formatTimestamp(rs.getTimestamp("date_action")));
                row.add(rs.getString("action")          != null ? rs.getString("action")          : "—");
                row.add(rs.getString("table_concernee") != null ? rs.getString("table_concernee") : "—");
                row.add(rs.getString("utilisateur")     != null ? rs.getString("utilisateur")     : "—");
                row.add(rs.getString("details")         != null ? rs.getString("details")         : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadHistorique : " + e.getMessage());
        }
        historiqueTable.setItems(data);
    }

    @FXML private void onSearchHistorique() { loadHistorique(); }

    // =========================================================================
    //  DÉCONNEXION
    // =========================================================================

    @FXML
    private void onLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/view/auth/Login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) userNameLabel.getScene().getWindow();
                    stage.setScene(new Scene(root, 900, 700));
                    stage.setTitle("Connexion - HSP Urgences");
                    stage.show();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion.");
                }
            }
        });
    }

    // =========================================================================
    //  API PUBLIQUE
    // =========================================================================

    public void setSecretaireInfo(int id, String name) {
        this.currentSecretaireId   = id;
        this.currentSecretaireName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
        loadDashboardData();
        loadRecentPatients();
    }

    // =========================================================================
    //  UTILS PRIVÉS
    // =========================================================================

    private void logHistorique(Connection conn, int userId, String action,
                               String table, int idEnreg, String details) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details) " +
                        "VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, table);
            ps.setInt(4, idEnreg);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur logHistorique : " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) return "—";
        return ts.toLocalDateTime().format(FMT);
    }

    private String truncate(String s, int max) {
        if (s == null || s.isEmpty()) return "—";
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