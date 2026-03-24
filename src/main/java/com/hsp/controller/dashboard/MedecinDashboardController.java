package com.hsp.controller.dashboard;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MedecinDashboardController implements Initializable {

    // ============= FXML COMPONENTS =============
    @FXML private Label userNameLabel;
    @FXML private Label waitingCountLabel;
    @FXML private Label hospitalizationCountLabel;
    @FXML private VBox dashboardView;
    @FXML private VBox waitingPatientsView;
    @FXML private VBox hospitalizationsView;
    @FXML private VBox productRequestsView;
    @FXML private VBox treatmentView;
    @FXML private VBox historiqueView;
    @FXML private VBox ordonnancesView;

    // Dashboard KPI
    @FXML private Label todayPatientsLabel;
    @FXML private Label waitingPatientsLabel;
    @FXML private Label hospitalizationsLabel;
    @FXML private Label productRequestsLabel;

    // Tables
    @FXML private TableView<ObservableList<String>> urgentPatientsTable;
    @FXML private ComboBox<String> gravityFilterComboBox;
    @FXML private TableView<ObservableList<String>> waitingPatientsTable;
    @FXML private TableView<ObservableList<String>> activeHospitalizationsTable;
    @FXML private TableView<ObservableList<String>> completedHospitalizationsTable;
    @FXML private TableView<ObservableList<String>> myRequestsTable;
    @FXML private TableView<ObservableList<String>> ordonnancesTable;
    @FXML private Label ordonnancesCountLabel;

    // Vue traitement
    @FXML private Label patientNameLabel;
    @FXML private Label arrivalDateLabel;
    @FXML private Label gravityLevelLabel;
    @FXML private TextArea symptomsTextArea;
    @FXML private RadioButton ordonnanceRadio;
    @FXML private RadioButton hospitalizationRadio;
    @FXML private VBox hospitalizationDetails;
    @FXML private ComboBox<String> chambreComboBox;
    @FXML private TextArea maladieTextArea;

    // Vue historique
    @FXML private TableView<ObservableList<String>> historiqueTable;
    @FXML private TableColumn<ObservableList<String>, String> histoDateCol;
    @FXML private TableColumn<ObservableList<String>, String> histoActionCol;
    @FXML private TableColumn<ObservableList<String>, String> histoTableCol;
    @FXML private TableColumn<ObservableList<String>, String> histoDetailsCol;
    @FXML private DatePicker histoDateFilter;
    @FXML private ComboBox<String> histoActionFilter;
    @FXML private Label historiqueCountLabel;

    // ============= STATE =============
    private ToggleGroup treatmentGroup;
    private int currentMedecinId   = -1;
    private String currentMedecinName = "";
    private int currentDossierId   = -1;

    // ============= DB =============
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/hsp_urgences";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ============= INIT =============

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ToggleGroup pour les radios
        treatmentGroup = new ToggleGroup();
        if (ordonnanceRadio      != null) ordonnanceRadio.setToggleGroup(treatmentGroup);
        if (hospitalizationRadio != null) hospitalizationRadio.setToggleGroup(treatmentGroup);

        // Afficher/masquer les détails hospitalisation selon la sélection
        if (hospitalizationRadio != null) {
            hospitalizationRadio.selectedProperty().addListener((obs, old, val) -> {
                if (hospitalizationDetails != null) {
                    hospitalizationDetails.setVisible(val);
                    hospitalizationDetails.setManaged(val);
                }
            });
        }

        // Filtre gravité
        if (gravityFilterComboBox != null) {
            gravityFilterComboBox.getItems().addAll(
                    "Tous", "Gravité 5", "Gravité 4", "Gravité 3", "Gravité 2", "Gravité 1");
            gravityFilterComboBox.setValue("Tous");
            gravityFilterComboBox.setOnAction(e -> loadUrgentPatients());
        }

        // Filtre historique – actions
        if (histoActionFilter != null) {
            histoActionFilter.getItems().addAll(
                    "Tous", "Hospitalisation", "Ordonnance", "Cloture hospitalisation");
            histoActionFilter.setValue("Tous");
        }

        // Colonnes tables
        setupUrgentPatientsTable();
        setupWaitingPatientsTable();
        setupActiveHospitalizationsTable();
        setupCompletedHospitalizationsTable();
        setupMyRequestsTable();
        setupHistoriqueTable();
        setupOrdonnancesTable();

        // Masquer hospitalizationDetails par défaut
        if (hospitalizationDetails != null) {
            hospitalizationDetails.setVisible(false);
            hospitalizationDetails.setManaged(false);
        }

        showDashboard();
    }

    // ============= COLONNES TABLES =============

    @SuppressWarnings("unchecked")
    private void setupUrgentPatientsTable() {
        if (urgentPatientsTable == null) return;
        urgentPatientsTable.getColumns().clear();

        // data[0] = id_dossier (caché), data[1..5] = affiché, data[6] = bouton Action
        String[] cols = {"Patient", "Heure d'arrivée", "Gravité", "Symptômes", "Statut", "Action"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);

            if (i == 5) { // colonne Action — bouton Traiter
                col.setCellValueFactory(data -> new SimpleStringProperty(""));
                col.setCellFactory(tc -> new TableCell<>() {
                    private final Button btn = new Button("Traiter");
                    {
                        btn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-padding: 4 10; " +
                                "-fx-background-radius: 6; -fx-cursor: hand;");
                        btn.setOnAction(e -> {
                            ObservableList<String> row = getTableView().getItems().get(getIndex());
                            openTreatmentView(row);
                        });
                    }
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                });
            } else {
                col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().get(dataIdx)));
            }
            urgentPatientsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupWaitingPatientsTable() {
        if (waitingPatientsTable == null) return;
        waitingPatientsTable.getColumns().clear();

        String[] cols = {"N° Dossier", "Nom", "Prénom", "Âge", "Arrivée", "Gravité", "Symptômes", "Action"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().get(idx)));

            if (i == 7) {
                col.setCellFactory(tc -> new TableCell<>() {
                    private final Button btn = new Button("Traiter");
                    {
                        btn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-padding: 4 10; " +
                                "-fx-background-radius: 6; -fx-cursor: hand;");
                        btn.setOnAction(e -> {
                            ObservableList<String> row = getTableView().getItems().get(getIndex());
                            openTreatmentView(row);
                        });
                    }
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                });
            }
            waitingPatientsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupActiveHospitalizationsTable() {
        if (activeHospitalizationsTable == null) return;
        activeHospitalizationsTable.getColumns().clear();

        // data[0] = id_hospitalisation (caché), data[1..6] = affiché, data[7] = bouton
        String[] cols = {"Patient", "Chambre", "Date début", "Diagnostic", "Durée (j)", "Action"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);

            if (i == 5) { // colonne Action — bouton Clôturer
                col.setCellValueFactory(data -> new SimpleStringProperty(""));
                col.setCellFactory(tc -> new TableCell<>() {
                    private final Button btn = new Button("Clôturer");
                    {
                        btn.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-padding: 4 10; " +
                                "-fx-background-radius: 6; -fx-cursor: hand;");
                        btn.setOnAction(e -> {
                            ObservableList<String> row = getTableView().getItems().get(getIndex());
                            cloturerHospitalisation(row);
                        });
                    }
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                });
            } else {
                col.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().get(dataIdx)));
            }
            activeHospitalizationsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupCompletedHospitalizationsTable() {
        if (completedHospitalizationsTable == null) return;
        completedHospitalizationsTable.getColumns().clear();

        String[] cols = {"Patient", "Chambre", "Date début", "Date fin", "Diagnostic"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().get(idx)));
            completedHospitalizationsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupMyRequestsTable() {
        if (myRequestsTable == null) return;
        myRequestsTable.getColumns().clear();

        String[] cols = {"N° Demande", "Produit", "Qté demandée", "Date", "Statut demande", "Statut ligne"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().get(idx)));
            myRequestsTable.getColumns().add(col);
        }
    }

    @SuppressWarnings("unchecked")
    private void setupOrdonnancesTable() {
        if (ordonnancesTable == null) return;
        ordonnancesTable.getColumns().clear();

        String[] cols   = {"Date", "Patient", "Médicaments", "Posologie", "Durée", "Statut", "Notes"};
        int[]    widths = { 130,    160,        200,           180,         100,     80,        180 };
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setPrefWidth(widths[i]);
            col.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().get(idx)));
            ordonnancesTable.getColumns().add(col);
        }
    }

    private void setupHistoriqueTable() {
        if (historiqueTable == null) return;
        if (histoDateCol    != null) histoDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        if (histoActionCol  != null) histoActionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        if (histoTableCol   != null) histoTableCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        if (histoDetailsCol != null) histoDetailsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
    }

    // ============= NAVIGATION =============

    @FXML
    private void showDashboard() {
        hideAllViews();
        show(dashboardView);
        loadDashboardData();
        loadUrgentPatients();
    }

    @FXML
    private void showWaitingPatients() {
        hideAllViews();
        show(waitingPatientsView);
        loadWaitingPatients();
    }

    @FXML
    private void showHospitalizations() {
        hideAllViews();
        show(hospitalizationsView);
        loadActiveHospitalizations();
        loadCompletedHospitalizations();
    }

    @FXML
    private void showProductRequests() {
        hideAllViews();
        show(productRequestsView);
        loadMyRequests();
    }

    @FXML
    private void showHistory() {
        hideAllViews();
        show(historiqueView);
        loadHistorique(null, null);
    }

    @FXML
    private void showOrdonnances() {
        hideAllViews();
        show(ordonnancesView);
        loadOrdonnances();
    }

    @FXML
    private void onRefreshOrdonnances() {
        loadOrdonnances();
    }

    /** Affiche une VBox (visible + managed). */
    private void show(VBox v) {
        if (v != null) { v.setVisible(true); v.setManaged(true); }
    }

    /** Masque toutes les vues (visible=false ET managed=false pour éviter les espaces fantômes). */
    private void hideAllViews() {
        VBox[] views = {dashboardView, waitingPatientsView, hospitalizationsView,
                productRequestsView, treatmentView, historiqueView, ordonnancesView};
        for (VBox v : views) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }

    // ============= OUVRIR VUE TRAITEMENT =============

    private void openTreatmentView(ObservableList<String> row) {
        try {
            int dossierId = Integer.parseInt(row.get(0).trim());
            loadPatientForTreatment(dossierId);
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'identifier le dossier.");
        }
    }

    private void loadPatientForTreatment(int dossierId) {
        String query = """
                SELECT d.id_dossier, p.nom, p.prenom, d.date_arrivee,
                       d.niveau_gravite, d.symptomes
                FROM dossier d
                JOIN patient p ON d.id_patient = p.id_patient
                WHERE d.id_dossier = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, dossierId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentDossierId = rs.getInt("id_dossier");
                patientNameLabel.setText(rs.getString("nom") + " " + rs.getString("prenom"));
                arrivalDateLabel.setText(formatTimestamp(rs.getTimestamp("date_arrivee")));
                gravityLevelLabel.setText("Gravité " + rs.getInt("niveau_gravite") + " / 5");
                String symp = rs.getString("symptomes");
                symptomsTextArea.setText(symp != null ? symp : "—");

                // Réinitialiser
                treatmentGroup.selectToggle(null);
                if (maladieTextArea != null) maladieTextArea.clear();
                if (hospitalizationDetails != null) {
                    hospitalizationDetails.setVisible(false);
                    hospitalizationDetails.setManaged(false);
                }
                loadChambresDisponibles();

                hideAllViews();
                show(treatmentView);
            }
            rs.close();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
        }
    }

    // ============= CHARGEMENT DONNÉES DASHBOARD =============

    private void loadDashboardData() {
        if (currentMedecinId < 0) return;
        try (Connection conn = getConnection()) {

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM dossier WHERE DATE(date_arrivee) = CURDATE()")) {
                if (rs.next()) todayPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) FROM dossier WHERE statut = 'Attente'")) {
                if (rs.next()) {
                    int n = rs.getInt(1);
                    waitingPatientsLabel.setText(String.valueOf(n));
                    waitingCountLabel.setText(String.valueOf(n));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM hospitalisation WHERE id_medecin = ? AND date_fin IS NULL")) {
                ps.setInt(1, currentMedecinId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int n = rs.getInt(1);
                    hospitalizationsLabel.setText(String.valueOf(n));
                    hospitalizationCountLabel.setText(String.valueOf(n));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM demande_produit WHERE id_medecin = ? AND statut = 'Attente'")) {
                ps.setInt(1, currentMedecinId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) productRequestsLabel.setText(String.valueOf(rs.getInt(1)));
            }

        } catch (SQLException e) {
            System.err.println("Erreur loadDashboardData : " + e.getMessage());
        }
    }

    // ============= PATIENTS URGENTS (dashboard) =============

    private void loadUrgentPatients() {
        if (urgentPatientsTable == null) return;

        String filter = gravityFilterComboBox != null ? gravityFilterComboBox.getValue() : "Tous";
        StringBuilder query = new StringBuilder("""
                SELECT d.id_dossier, p.nom, p.prenom, d.date_arrivee,
                       d.niveau_gravite, d.symptomes, d.statut
                FROM dossier d
                JOIN patient p ON d.id_patient = p.id_patient
                WHERE d.statut IN ('Attente', 'EnCours')
                """);

        if (filter != null && !filter.equals("Tous")) {
            int gravite = Integer.parseInt(filter.replace("Gravité ", "").trim());
            query.append(" AND d.niveau_gravite = ").append(gravite);
        }
        query.append(" ORDER BY d.niveau_gravite DESC, d.date_arrivee ASC");

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query.toString())) {

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_dossier")));                  // 0 – id caché
                row.add(rs.getString("nom") + " " + rs.getString("prenom"));       // 1
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee")));          // 2
                row.add("Gravité " + rs.getInt("niveau_gravite"));                  // 3
                row.add(truncate(rs.getString("symptomes"), 60));                   // 4
                row.add(rs.getString("statut"));                                    // 5
                row.add("");                                                        // 6 – placeholder Action
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadUrgentPatients : " + e.getMessage());
        }
        urgentPatientsTable.setItems(data);
    }

    // ============= PATIENTS EN ATTENTE =============

    private void loadWaitingPatients() {
        if (waitingPatientsTable == null) return;

        String query = """
                SELECT d.id_dossier, p.nom, p.prenom,
                       d.date_arrivee, d.niveau_gravite, d.symptomes
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
                row.add(String.valueOf(rs.getInt("id_dossier")));                 // 0
                row.add(rs.getString("nom"));                                      // 1
                row.add(rs.getString("prenom"));                                   // 2
                row.add("—");                                                      // 3 âge
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee")));        // 4
                row.add("Gravité " + rs.getInt("niveau_gravite"));                // 5
                row.add(truncate(rs.getString("symptomes"), 60));                  // 6
                row.add("");                                                        // 7 Action
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadWaitingPatients : " + e.getMessage());
        }
        waitingPatientsTable.setItems(data);
    }

    @FXML
    private void onRefreshWaiting() {
        loadWaitingPatients();
        loadDashboardData();
    }

    // ============= HOSPITALISATIONS =============

    private void loadActiveHospitalizations() {
        if (activeHospitalizationsTable == null || currentMedecinId < 0) return;

        String query = """
                SELECT h.id_hospitalisation,
                       p.nom, p.prenom,
                       c.numero AS chambre,
                       h.date_debut,
                       h.description_maladie,
                       DATEDIFF(NOW(), h.date_debut) AS duree
                FROM hospitalisation h
                JOIN dossier d ON h.id_dossier = d.id_dossier
                JOIN patient p ON d.id_patient = p.id_patient
                JOIN chambre c ON h.id_chambre = c.id_chambre
                WHERE h.id_medecin = ? AND h.date_fin IS NULL
                ORDER BY h.date_debut DESC
                """;

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, currentMedecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_hospitalisation"))); // 0 – caché
                row.add(rs.getString("nom") + " " + rs.getString("prenom")); // 1
                row.add(rs.getString("chambre"));                              // 2
                row.add(formatTimestamp(rs.getTimestamp("date_debut")));       // 3
                row.add(truncate(rs.getString("description_maladie"), 60));    // 4
                row.add(rs.getInt("duree") + " j");                            // 5
                row.add("");                                                    // 6 Action
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadActiveHospitalizations : " + e.getMessage());
        }
        activeHospitalizationsTable.setItems(data);
    }

    private void loadCompletedHospitalizations() {
        if (completedHospitalizationsTable == null || currentMedecinId < 0) return;

        String query = """
                SELECT p.nom, p.prenom, c.numero AS chambre,
                       h.date_debut, h.date_fin, h.description_maladie
                FROM hospitalisation h
                JOIN dossier d ON h.id_dossier = d.id_dossier
                JOIN patient p ON d.id_patient = p.id_patient
                JOIN chambre c ON h.id_chambre = c.id_chambre
                WHERE h.id_medecin = ? AND h.date_fin IS NOT NULL
                ORDER BY h.date_fin DESC
                """;

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, currentMedecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("nom") + " " + rs.getString("prenom")); // 0
                row.add(rs.getString("chambre"));                              // 1
                row.add(formatTimestamp(rs.getTimestamp("date_debut")));       // 2
                row.add(formatTimestamp(rs.getTimestamp("date_fin")));         // 3
                row.add(truncate(rs.getString("description_maladie"), 80));    // 4
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadCompletedHospitalizations : " + e.getMessage());
        }
        completedHospitalizationsTable.setItems(data);
    }

    @FXML
    private void onNewHospitalization() {
        showWaitingPatients();
    }

    /**
     * Clôture une hospitalisation.
     * Ouvre un dialog structuré (médicaments obligatoires) pour saisir l'ordonnance de sortie,
     * puis effectue en une seule transaction :
     *   1. INSERT ordonnance
     *   2. UPDATE hospitalisation.date_fin = NOW()
     *   3. UPDATE chambre.disponible = TRUE
     *   4. UPDATE dossier.statut = 'Termine'
     */
    private void cloturerHospitalisation(ObservableList<String> row) {
        int hospId = Integer.parseInt(row.get(0));

        int[] ids = fetchHospitalisationIds(hospId);
        if (ids == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Hospitalisation introuvable en base.");
            return;
        }
        final int chambreId = ids[0];
        final int dossierId = ids[1];

        // ── Dialog ordonnance de sortie ──
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ordonnance de sortie");
        dialog.setHeaderText("Remplissez l'ordonnance avant de clôturer l'hospitalisation\n"
                + "(Les médicaments sont obligatoires)");

        TextArea medicamentsArea = new TextArea();
        medicamentsArea.setPromptText("Médicaments prescrits (obligatoire)...");
        medicamentsArea.setPrefRowCount(3);
        medicamentsArea.setWrapText(true);

        TextArea posologieArea = new TextArea();
        posologieArea.setPromptText("Posologie et instructions...");
        posologieArea.setPrefRowCount(2);
        posologieArea.setWrapText(true);

        TextField dureeField = new TextField();
        dureeField.setPromptText("Ex : 7 jours");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes complémentaires (optionnel)...");
        notesArea.setPrefRowCount(2);
        notesArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);
        grid.add(new Label("Médicaments *"), 0, 0);
        grid.add(medicamentsArea,            1, 0);
        grid.add(new Label("Posologie"),     0, 1);
        grid.add(posologieArea,              1, 1);
        grid.add(new Label("Durée traitement"), 0, 2);
        grid.add(dureeField,                 1, 2);
        grid.add(new Label("Notes"),         0, 3);
        grid.add(notesArea,                  1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Désactiver OK tant que médicaments est vide
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        medicamentsArea.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty()));

        dialog.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;

            String medicaments = medicamentsArea.getText().trim();
            String posologie   = posologieArea.getText().trim();
            String duree       = dureeField.getText().trim();
            String notes       = notesArea.getText().trim();

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                // 1. Ordonnance de sortie
                String sqlOrd = """
                        INSERT INTO ordonnance
                            (id_dossier, id_medecin, date_ordonnance,
                             medicaments, posologie, duree_traitement, notes, statut)
                        VALUES (?, ?, NOW(), ?, ?, ?, ?, 'active')
                        """;
                try (PreparedStatement ps = conn.prepareStatement(sqlOrd)) {
                    ps.setInt(1, dossierId);
                    ps.setInt(2, currentMedecinId);
                    ps.setString(3, medicaments);
                    ps.setString(4, posologie.isEmpty()   ? null : posologie);
                    ps.setString(5, duree.isEmpty()       ? null : duree);
                    ps.setString(6, notes.isEmpty()       ? null : notes);
                    ps.executeUpdate();
                }

                // 2. Clôturer l'hospitalisation
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE hospitalisation SET date_fin = NOW() WHERE id_hospitalisation = ?")) {
                    ps.setInt(1, hospId);
                    ps.executeUpdate();
                }

                // 3. Libérer la chambre
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE chambre SET disponible = TRUE WHERE id_chambre = ?")) {
                    ps.setInt(1, chambreId);
                    ps.executeUpdate();
                }

                // 4. Dossier → Terminé
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dossier SET statut = 'Termine' WHERE id_dossier = ?")) {
                    ps.setInt(1, dossierId);
                    ps.executeUpdate();
                }

                enregistrerHistorique(conn, "Cloture hospitalisation",
                        "hospitalisation", hospId, "Sortie patient avec ordonnance de sortie");

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Hospitalisation clôturée et ordonnance de sortie enregistrée.");
                loadActiveHospitalizations();
                loadCompletedHospitalizations();
                loadDashboardData();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
            }
        });
    }

    /** Retourne {chambreId, dossierId} ou null si introuvable. */
    private int[] fetchHospitalisationIds(int hospId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id_chambre, id_dossier FROM hospitalisation WHERE id_hospitalisation = ?")) {
            ps.setInt(1, hospId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new int[]{rs.getInt("id_chambre"), rs.getInt("id_dossier")};
        } catch (SQLException e) {
            System.err.println("Erreur fetchHospitalisationIds : " + e.getMessage());
        }
        return null;
    }

    // ============= DEMANDES PRODUITS =============

    private void loadMyRequests() {
        if (myRequestsTable == null || currentMedecinId < 0) return;

        String query = """
                SELECT dp.id_demande,
                       p.libelle AS produit,
                       ld.quantite_demandee,
                       dp.date_demande,
                       dp.statut AS statut_demande,
                       ld.statut AS statut_ligne
                FROM demande_produit dp
                JOIN ligne_demande ld ON dp.id_demande = ld.id_demande
                JOIN produit p ON ld.id_produit = p.id_produit
                WHERE dp.id_medecin = ?
                ORDER BY dp.date_demande DESC
                """;

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, currentMedecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_demande")));
                row.add(rs.getString("produit"));
                row.add(String.valueOf(rs.getInt("quantite_demandee")));
                row.add(formatTimestamp(rs.getTimestamp("date_demande")));
                row.add(rs.getString("statut_demande"));
                row.add(rs.getString("statut_ligne"));
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadMyRequests : " + e.getMessage());
        }
        myRequestsTable.setItems(data);
    }

    // ============= NOUVELLE DEMANDE MULTI-PRODUITS =============

    /**
     * Ouvre un dialog permettant de composer une demande avec N lignes produit.
     * Chaque ligne : ComboBox(produit) + Spinner(quantité) + bouton "−".
     * Tout est inséré en une seule transaction via executeBatch().
     */
    @FXML
    private void onNewProductRequest() {
        if (currentMedecinId < 0) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Impossible d'identifier le médecin connecté.");
            return;
        }

        // Charger la liste des produits
        List<String[]> produits = new ArrayList<>(); // [id, libelle]
        try (Connection conn = getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT id_produit, libelle FROM produit ORDER BY libelle")) {
            while (rs.next())
                produits.add(new String[]{rs.getString("id_produit"), rs.getString("libelle")});
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
            return;
        }

        if (produits.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun produit", "Aucun produit disponible en base.");
            return;
        }

        // ── Construire le dialog ──
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle demande de produits");
        dialog.setHeaderText("Composez votre demande (plusieurs produits possibles) :");
        dialog.getDialogPane().setPrefWidth(580);

        VBox lignesBox   = new VBox(8);
        Label totalLabel = new Label("Lignes : 1");
        totalLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        List<ComboBox<String>>  combos   = new ArrayList<>();
        List<Spinner<Integer>>  spinners = new ArrayList<>();

        Runnable addLigne = () -> {
            ComboBox<String> combo = new ComboBox<>();
            combo.setPrefWidth(310);
            for (String[] p : produits) combo.getItems().add(p[1]);
            combo.getSelectionModel().selectFirst();

            Spinner<Integer> spinner = new Spinner<>(1, 9999, 1);
            spinner.setPrefWidth(100);
            spinner.setEditable(true);

            Button btnRetirer = new Button("−");
            btnRetirer.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 4;");

            HBox ligne = new HBox(10, combo, spinner, btnRetirer);
            ligne.setAlignment(Pos.CENTER_LEFT);

            btnRetirer.setOnAction(e -> {
                if (lignesBox.getChildren().size() > 1) {
                    lignesBox.getChildren().remove(ligne);
                    combos.remove(combo);
                    spinners.remove(spinner);
                    totalLabel.setText("Lignes : " + lignesBox.getChildren().size());
                } else {
                    showAlert(Alert.AlertType.WARNING, "Minimum",
                            "La demande doit contenir au moins un produit.");
                }
            });

            combos.add(combo);
            spinners.add(spinner);
            lignesBox.getChildren().add(ligne);
            totalLabel.setText("Lignes : " + lignesBox.getChildren().size());
        };

        // Première ligne par défaut
        addLigne.run();

        Button btnAjouterLigne = new Button("➕ Ajouter un produit");
        btnAjouterLigne.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-padding: 6 16; -fx-background-radius: 6;");
        btnAjouterLigne.setOnAction(e -> addLigne.run());

        VBox content = new VBox(12,
                new Label("Produit(s) et quantité(s) :"),
                lignesBox,
                btnAjouterLigne,
                totalLabel);
        content.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;

            // Construire la liste [id_produit, quantite]
            List<int[]> lignes = new ArrayList<>();
            for (int i = 0; i < combos.size(); i++) {
                int idx = combos.get(i).getSelectionModel().getSelectedIndex();
                int qte = spinners.get(i).getValue();
                lignes.add(new int[]{Integer.parseInt(produits.get(idx)[0]), qte});
            }
            if (lignes.isEmpty()) return;

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                // Créer la demande principale
                int demandeId = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO demande_produit (id_medecin, date_demande, statut) VALUES (?, NOW(), 'Attente')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, currentMedecinId);
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) demandeId = rs.getInt(1);
                }

                // Insérer toutes les lignes en batch
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO ligne_demande (id_demande, id_produit, quantite_demandee, statut) " +
                                "VALUES (?, ?, ?, 'Attente')")) {
                    for (int[] ligne : lignes) {
                        ps.setInt(1, demandeId);
                        ps.setInt(2, ligne[0]);
                        ps.setInt(3, ligne[1]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                enregistrerHistorique(conn, "Demande produit", "demande_produit",
                        demandeId, lignes.size() + " ligne(s) produit");

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Demande #" + demandeId + " créée avec " + lignes.size() + " produit(s).");
                loadMyRequests();
                loadDashboardData();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
            }
        });
    }

    // ============= CHAMBRES DISPONIBLES =============

    private void loadChambresDisponibles() {
        if (chambreComboBox == null) return;
        chambreComboBox.getItems().clear();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT numero FROM chambre WHERE disponible = TRUE ORDER BY numero")) {
            while (rs.next()) chambreComboBox.getItems().add(rs.getString("numero"));
        } catch (SQLException e) {
            System.err.println("Erreur loadChambresDisponibles : " + e.getMessage());
        }
    }

    // ============= VALIDER TRAITEMENT =============

    /**
     * Valide la prise en charge :
     *  - Ordonnance → ouvre un dialog structuré (médicaments obligatoires) puis passe le dossier à Terminé
     *  - Hospitalisation → insère l'hospitalisation, réserve la chambre, passe le dossier à EnCours
     */
    @FXML
    private void onValidateTreatment() {
        if (treatmentGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une décision.");
            return;
        }
        if (currentDossierId < 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun dossier sélectionné.");
            return;
        }

        if (hospitalizationRadio.isSelected()) {
            validerHospitalisation();
        } else {
            ouvrirDialogOrdonnanceSortie();
        }
    }

    /** Ouvre le dialog de rédaction d'ordonnance (médicaments obligatoires) pour la sortie directe. */
    private void ouvrirDialogOrdonnanceSortie() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ordonnance de sortie");
        dialog.setHeaderText("Rédigez l'ordonnance avant de valider la sortie du patient\n"
                + "(Les médicaments sont obligatoires)");

        TextArea medicamentsArea = new TextArea();
        medicamentsArea.setPromptText("Médicaments prescrits (obligatoire)...");
        medicamentsArea.setPrefRowCount(3);
        medicamentsArea.setWrapText(true);

        TextArea posologieArea = new TextArea();
        posologieArea.setPromptText("Posologie et instructions...");
        posologieArea.setPrefRowCount(2);
        posologieArea.setWrapText(true);

        TextField dureeField = new TextField();
        dureeField.setPromptText("Ex : 5 jours");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes complémentaires (optionnel)...");
        notesArea.setPrefRowCount(2);
        notesArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);
        grid.add(new Label("Médicaments *"), 0, 0); grid.add(medicamentsArea, 1, 0);
        grid.add(new Label("Posologie"),     0, 1); grid.add(posologieArea,   1, 1);
        grid.add(new Label("Durée"),         0, 2); grid.add(dureeField,      1, 2);
        grid.add(new Label("Notes"),         0, 3); grid.add(notesArea,       1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        medicamentsArea.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty()));

        dialog.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;

            String medicaments = medicamentsArea.getText().trim();
            String posologie   = posologieArea.getText().trim();
            String duree       = dureeField.getText().trim();
            String notes       = notesArea.getText().trim();

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                int ordonnanceId = 0;
                String sqlOrd = """
                        INSERT INTO ordonnance
                            (id_dossier, id_medecin, date_ordonnance,
                             medicaments, posologie, duree_traitement, notes, statut)
                        VALUES (?, ?, NOW(), ?, ?, ?, ?, 'active')
                        """;
                try (PreparedStatement ps = conn.prepareStatement(sqlOrd,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, currentDossierId);
                    ps.setInt(2, currentMedecinId);
                    ps.setString(3, medicaments);
                    ps.setString(4, posologie.isEmpty() ? null : posologie);
                    ps.setString(5, duree.isEmpty()     ? null : duree);
                    ps.setString(6, notes.isEmpty()     ? null : notes);
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) ordonnanceId = rs.getInt(1);
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dossier SET statut = 'Termine', id_medecin = ? WHERE id_dossier = ?")) {
                    ps.setInt(1, currentMedecinId);
                    ps.setInt(2, currentDossierId);
                    ps.executeUpdate();
                }

                enregistrerHistorique(conn, "Ordonnance", "ordonnance",
                        ordonnanceId, "Ordonnance créée pour dossier #" + currentDossierId);

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Ordonnance #" + ordonnanceId + " enregistrée — le patient peut sortir.");
                currentDossierId = -1;
                showDashboard();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /** Valide l'hospitalisation (chambre + diagnostic obligatoires). */
    private void validerHospitalisation() {
        if (chambreComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une chambre.");
            return;
        }
        String maladie = maladieTextArea != null ? maladieTextArea.getText().trim() : "";
        if (maladie.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Décrivez le diagnostic.");
            return;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            int chambreId = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_chambre FROM chambre WHERE numero = ? AND disponible = TRUE")) {
                ps.setString(1, chambreComboBox.getValue());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) chambreId = rs.getInt("id_chambre");
                else {
                    showAlert(Alert.AlertType.WARNING, "Chambre indisponible",
                            "Cette chambre n'est plus disponible.");
                    conn.rollback();
                    return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO hospitalisation (id_dossier, id_chambre, id_medecin, date_debut, description_maladie) " +
                            "VALUES (?, ?, ?, NOW(), ?)")) {
                ps.setInt(1, currentDossierId);
                ps.setInt(2, chambreId);
                ps.setInt(3, currentMedecinId);
                ps.setString(4, maladie);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE chambre SET disponible = FALSE WHERE id_chambre = ?")) {
                ps.setInt(1, chambreId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE dossier SET statut = 'EnCours', id_medecin = ? WHERE id_dossier = ?")) {
                ps.setInt(1, currentMedecinId);
                ps.setInt(2, currentDossierId);
                ps.executeUpdate();
            }

            enregistrerHistorique(conn, "Hospitalisation", "hospitalisation",
                    currentDossierId, "Chambre: " + chambreComboBox.getValue());

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Patient hospitalisé avec succès !");
            currentDossierId = -1;
            showDashboard();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancelTreatment() {
        currentDossierId = -1;
        showWaitingPatients();
    }

    // ============= ORDONNANCES =============

    private void loadOrdonnances() {
        if (ordonnancesTable == null || currentMedecinId < 0) return;

        String query = """
                SELECT o.date_ordonnance,
                       CONCAT(p.nom, ' ', p.prenom) AS patient,
                       o.medicaments,
                       o.posologie,
                       o.duree_traitement,
                       o.statut,
                       o.notes
                FROM ordonnance o
                JOIN dossier d ON o.id_dossier = d.id_dossier
                JOIN patient p ON d.id_patient  = p.id_patient
                WHERE o.id_medecin = ?
                ORDER BY o.date_ordonnance DESC
                """;

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, currentMedecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> r = FXCollections.observableArrayList();
                r.add(rs.getString("date_ordonnance") != null ? rs.getString("date_ordonnance") : "—");
                r.add(rs.getString("patient"));
                r.add(truncate(rs.getString("medicaments"),    60));
                r.add(truncate(rs.getString("posologie"),      50));
                r.add(rs.getString("duree_traitement") != null ? rs.getString("duree_traitement") : "—");
                r.add(rs.getString("statut")           != null ? rs.getString("statut")           : "—");
                r.add(truncate(rs.getString("notes"),          50));
                data.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadOrdonnances : " + e.getMessage());
        }
        ordonnancesTable.setItems(data);
        if (ordonnancesCountLabel != null) {
            int n = data.size();
            ordonnancesCountLabel.setText(n + " ordonnance" + (n > 1 ? "s" : ""));
        }
    }

    // ============= HISTORIQUE =============

    private void loadHistorique(LocalDate dateFilter, String actionFilter) {
        if (historiqueTable == null || currentMedecinId < 0) return;

        StringBuilder query = new StringBuilder("""
                SELECT h.date_action, h.action, h.table_concernee, h.details
                FROM historique h
                WHERE h.id_utilisateur = ?
                """);

        if (dateFilter != null)
            query.append(" AND DATE(h.date_action) = '").append(dateFilter).append("'");
        if (actionFilter != null && !actionFilter.equals("Tous"))
            query.append(" AND h.action = '").append(actionFilter.replace("'", "''")).append("'");
        query.append(" ORDER BY h.date_action DESC");

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {

            ps.setInt(1, currentMedecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(formatTimestamp(rs.getTimestamp("date_action")));
                row.add(rs.getString("action"));
                row.add(rs.getString("table_concernee"));
                row.add(rs.getString("details") != null ? rs.getString("details") : "—");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadHistorique : " + e.getMessage());
        }

        historiqueTable.setItems(data);
        if (historiqueCountLabel != null) {
            int n = data.size();
            historiqueCountLabel.setText(n + " entrée" + (n > 1 ? "s" : ""));
        }
    }

    @FXML
    private void onFilterHistorique() {
        loadHistorique(
                histoDateFilter  != null ? histoDateFilter.getValue()  : null,
                histoActionFilter != null ? histoActionFilter.getValue() : null);
    }

    @FXML
    private void onResetFilters() {
        if (histoDateFilter   != null) histoDateFilter.setValue(null);
        if (histoActionFilter != null) histoActionFilter.setValue("Tous");
        loadHistorique(null, null);
    }

    @FXML
    private void onExportHistorique() {
        showAlert(Alert.AlertType.INFORMATION, "Export",
                "Fonctionnalité d'export en cours de développement.");
    }

    private void enregistrerHistorique(Connection conn, String action, String table,
                                       int recordId, String details) {
        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details)
                VALUES (?, ?, ?, ?, ?)""")) {
            ps.setInt(1, currentMedecinId);
            ps.setString(2, action);
            ps.setString(3, table);
            ps.setInt(4, recordId);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur historique : " + e.getMessage());
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
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/view/auth/Login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) userNameLabel.getScene().getWindow();
                    stage.setScene(new Scene(root, 900, 700));
                    stage.setTitle("Connexion - HSP Urgences");
                    stage.show();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de charger la page de connexion.");
                }
            }
        });
    }

    // ============= API PUBLIQUE =============

    public void setMedecinInfo(int id, String name) {
        this.currentMedecinId   = id;
        this.currentMedecinName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
        loadDashboardData();
        loadUrgentPatients();
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