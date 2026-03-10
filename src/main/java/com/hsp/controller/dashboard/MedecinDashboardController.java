package com.hsp.controller.dashboard;

import javafx.beans.property.SimpleIntegerProperty;
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

    // ============= STATE =============
    private ToggleGroup treatmentGroup;
    private int currentMedecinId = -1;
    private String currentMedecinName = "";
    // Dossier en cours de traitement
    private int currentDossierId = -1;

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
        if (ordonnanceRadio != null)      ordonnanceRadio.setToggleGroup(treatmentGroup);
        if (hospitalizationRadio != null) hospitalizationRadio.setToggleGroup(treatmentGroup);

        // Afficher/masquer les détails hospitalisation
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

        // Colonnes tables
        setupUrgentPatientsTable();
        setupWaitingPatientsTable();
        setupActiveHospitalizationsTable();
        setupCompletedHospitalizationsTable();
        setupMyRequestsTable();

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

        // data[0] = id_dossier (clé cachée), data[1..6] = données affichées
        String[] cols = {"Patient", "Heure d'arrivée", "Gravité", "Symptômes", "Statut", "Action"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1; // décalage de 1 pour ignorer l'id caché en data[0]
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().get(dataIdx)));

            if (i == 5) { // Colonne Action
                col.setCellFactory(tc -> new TableCell<>() {
                    private final Button btn = new Button("Traiter");
                    {
                        btn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-padding: 4 10 4 10; " +
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
                                "-fx-font-size: 11px; -fx-padding: 4 10 4 10; " +
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

        // data[0] = id_hospitalisation (clé cachée), data[1..6] = données affichées
        String[] cols = {"Patient", "Chambre", "Date début", "Diagnostic", "Durée (j)", "Action"};
        for (int i = 0; i < cols.length; i++) {
            final int dataIdx = i + 1; // décalage de 1 pour ignorer l'id caché en data[0]
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().get(dataIdx)));

            if (i == 5) {
                col.setCellFactory(tc -> new TableCell<>() {
                    private final Button btn = new Button("Clôturer");
                    {
                        btn.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-padding: 4 10 4 10; " +
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

    // ============= NAVIGATION =============

    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) { dashboardView.setVisible(true); dashboardView.setManaged(true); }
        loadDashboardData();
        loadUrgentPatients();
    }

    @FXML
    private void showWaitingPatients() {
        hideAllViews();
        if (waitingPatientsView != null) { waitingPatientsView.setVisible(true); waitingPatientsView.setManaged(true); }
        loadWaitingPatients();
    }

    @FXML
    private void showHospitalizations() {
        hideAllViews();
        if (hospitalizationsView != null) { hospitalizationsView.setVisible(true); hospitalizationsView.setManaged(true); }
        loadActiveHospitalizations();
        loadCompletedHospitalizations();
    }

    @FXML
    private void showProductRequests() {
        hideAllViews();
        if (productRequestsView != null) { productRequestsView.setVisible(true); productRequestsView.setManaged(true); }
        loadMyRequests();
    }

    @FXML
    private void showHistory() {
        showAlert(Alert.AlertType.INFORMATION, "Historique", "Fonctionnalité en cours de développement.");
    }

    private void hideAllViews() {
        VBox[] views = {dashboardView, waitingPatientsView, hospitalizationsView,
                productRequestsView, treatmentView};
        for (VBox v : views) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }

    // ============= OUVRIR VUE TRAITEMENT =============

    /**
     * row[0] = id_dossier, row[1] = nom prénom, row[2] = date_arrivee,
     * row[3] ou row[5] = gravité, row[4] ou row[6] = symptomes
     * (les index varient selon la table source, on stocke tout en extra columns)
     */
    private void openTreatmentView(ObservableList<String> row) {
        // On recharge depuis la BDD avec l'id_dossier (col 0 dans waitingTable, col 0 dans urgentTable)
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
                String nom    = rs.getString("nom") + " " + rs.getString("prenom");
                String arrive = rs.getTimestamp("date_arrivee")
                        .toLocalDateTime().format(FMT);
                int gravite   = rs.getInt("niveau_gravite");
                String symp   = rs.getString("symptomes");

                patientNameLabel.setText(nom);
                arrivalDateLabel.setText(arrive);
                gravityLevelLabel.setText("Gravité " + gravite + " / 5");
                symptomsTextArea.setText(symp != null ? symp : "—");

                // Réinitialiser les champs
                treatmentGroup.selectToggle(null);
                maladieTextArea.clear();
                if (hospitalizationDetails != null) {
                    hospitalizationDetails.setVisible(false);
                    hospitalizationDetails.setManaged(false);
                }

                // Charger les chambres disponibles
                loadChambresDisponibles();

                hideAllViews();
                if (treatmentView != null) { treatmentView.setVisible(true); treatmentView.setManaged(true); }
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

            // Patients aujourd'hui (tous statuts)
            String qToday = "SELECT COUNT(*) FROM dossier WHERE DATE(date_arrivee) = CURDATE()";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(qToday)) {
                if (rs.next()) todayPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Patients en attente (tous médecins)
            String qWaiting = "SELECT COUNT(*) FROM dossier WHERE statut = 'Attente'";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(qWaiting)) {
                if (rs.next()) {
                    int n = rs.getInt(1);
                    waitingPatientsLabel.setText(String.valueOf(n));
                    waitingCountLabel.setText(String.valueOf(n));
                }
            }

            // Mes hospitalisations actives
            String qHosp = """
                    SELECT COUNT(*) FROM hospitalisation
                    WHERE id_medecin = ? AND date_fin IS NULL
                    """;
            try (PreparedStatement ps = conn.prepareStatement(qHosp)) {
                ps.setInt(1, currentMedecinId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int n = rs.getInt(1);
                    hospitalizationsLabel.setText(String.valueOf(n));
                    hospitalizationCountLabel.setText(String.valueOf(n));
                }
            }

            // Mes demandes produits en attente
            String qProd = """
                    SELECT COUNT(*) FROM demande_produit
                    WHERE id_medecin = ? AND statut = 'Attente'
                    """;
            try (PreparedStatement ps = conn.prepareStatement(qProd)) {
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
                row.add(String.valueOf(rs.getInt("id_dossier")));       // 0 – id caché
                row.add(rs.getString("nom") + " " + rs.getString("prenom")); // 1
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee"))); // 2
                row.add("Gravité " + rs.getInt("niveau_gravite"));        // 3
                row.add(truncate(rs.getString("symptomes"), 60));          // 4
                row.add(rs.getString("statut"));                           // 5
                row.add("");                                               // 6 – colonne Action
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
                SELECT d.id_dossier,
                       p.nom, p.prenom,
                       TIMESTAMPDIFF(YEAR, NULL, NULL) AS age,
                       d.date_arrivee,
                       d.niveau_gravite,
                       d.symptomes
                FROM dossier d
                JOIN patient p ON d.id_patient = p.id_patient
                WHERE d.statut = 'Attente'
                ORDER BY d.niveau_gravite DESC, d.date_arrivee ASC
                """;
        // Note : la table patient n'a pas de date_naissance, on affiche '—' pour l'âge

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_dossier")));            // 0
                row.add(rs.getString("nom"));                                 // 1
                row.add(rs.getString("prenom"));                              // 2
                row.add("—");                                                 // 3 âge (absent du schéma)
                row.add(formatTimestamp(rs.getTimestamp("date_arrivee")));   // 4
                row.add("Gravité " + rs.getInt("niveau_gravite"));           // 5
                row.add(truncate(rs.getString("symptomes"), 60));             // 6
                row.add("");                                                   // 7 Action
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
                row.add(String.valueOf(rs.getInt("id_hospitalisation"))); // 0 – id caché
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
                SELECT p.nom, p.prenom,
                       c.numero AS chambre,
                       h.date_debut, h.date_fin,
                       h.description_maladie
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

    /** Clôture une hospitalisation (date_fin = NOW, chambre libérée, dossier Terminé) */
    private void cloturerHospitalisation(ObservableList<String> row) {
        int hospId = Integer.parseInt(row.get(0));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clôturer l'hospitalisation");
        confirm.setHeaderText("Confirmer la sortie du patient ?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp != ButtonType.OK) return;
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                // Récupérer id_chambre et id_dossier
                int chambreId = 0, dossierId = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id_chambre, id_dossier FROM hospitalisation WHERE id_hospitalisation = ?")) {
                    ps.setInt(1, hospId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        chambreId = rs.getInt("id_chambre");
                        dossierId = rs.getInt("id_dossier");
                    }
                }

                // date_fin
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE hospitalisation SET date_fin = NOW() WHERE id_hospitalisation = ?")) {
                    ps.setInt(1, hospId);
                    ps.executeUpdate();
                }

                // Libérer la chambre
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE chambre SET disponible = TRUE WHERE id_chambre = ?")) {
                    ps.setInt(1, chambreId);
                    ps.executeUpdate();
                }

                // Dossier → Terminé
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dossier SET statut = 'Termine' WHERE id_dossier = ?")) {
                    ps.setInt(1, dossierId);
                    ps.executeUpdate();
                }

                enregistrerHistorique(conn, "Cloture hospitalisation",
                        "hospitalisation", hospId, "Sortie patient");

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Hospitalisation clôturée, chambre libérée.");
                loadActiveHospitalizations();
                loadCompletedHospitalizations();
                loadDashboardData();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur BDD", e.getMessage());
            }
        });
    }

    // ============= DEMANDES PRODUITS =============

    private void loadMyRequests() {
        if (myRequestsTable == null || currentMedecinId < 0) return;

        // Utilise la vue vue_demandes_details filtrée sur ce médecin
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
                row.add(String.valueOf(rs.getInt("id_demande")));           // 0
                row.add(rs.getString("produit"));                            // 1
                row.add(String.valueOf(rs.getInt("quantite_demandee")));    // 2
                row.add(formatTimestamp(rs.getTimestamp("date_demande")));  // 3
                row.add(rs.getString("statut_demande"));                     // 4
                row.add(rs.getString("statut_ligne"));                       // 5
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadMyRequests : " + e.getMessage());
        }
        myRequestsTable.setItems(data);
    }

    // ============= CHAMBRES DISPONIBLES =============

    private void loadChambresDisponibles() {
        if (chambreComboBox == null) return;
        chambreComboBox.getItems().clear();

        String query = "SELECT numero FROM chambre WHERE disponible = TRUE ORDER BY numero";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                chambreComboBox.getItems().add(rs.getString("numero"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur loadChambresDisponibles : " + e.getMessage());
        }
    }

    // ============= VALIDER TRAITEMENT =============

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

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            if (hospitalizationRadio.isSelected()) {
                // ── Hospitalisation ──
                if (chambreComboBox.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une chambre.");
                    return;
                }
                String maladie = maladieTextArea.getText().trim();
                if (maladie.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Attention", "Décrivez le diagnostic.");
                    return;
                }

                // Récupérer id_chambre
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

                // Créer l'hospitalisation
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO hospitalisation (id_dossier, id_chambre, id_medecin, date_debut, description_maladie) " +
                                "VALUES (?, ?, ?, NOW(), ?)")) {
                    ps.setInt(1, currentDossierId);
                    ps.setInt(2, chambreId);
                    ps.setInt(3, currentMedecinId);
                    ps.setString(4, maladie);
                    ps.executeUpdate();
                }

                // Marquer la chambre occupée
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE chambre SET disponible = FALSE WHERE id_chambre = ?")) {
                    ps.setInt(1, chambreId);
                    ps.executeUpdate();
                }

                // Dossier → EnCours, assigner le médecin
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

            } else {
                // ── Ordonnance / Sortie ──
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dossier SET statut = 'Termine', id_medecin = ? WHERE id_dossier = ?")) {
                    ps.setInt(1, currentMedecinId);
                    ps.setInt(2, currentDossierId);
                    ps.executeUpdate();
                }

                enregistrerHistorique(conn, "Ordonnance", "dossier",
                        currentDossierId, "Patient sorti avec ordonnance");

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ordonnance délivrée — le patient peut sortir.");
            }

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

    // ============= HISTORIQUE =============

    private void enregistrerHistorique(Connection conn, String action, String table,
                                       int recordId, String details) {
        String query = """
                INSERT INTO historique (id_utilisateur, action, table_concernee, id_enregistrement, details)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(query)) {
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

    /**
     * Appelé depuis le LoginController après authentification.
     * @param id   id_utilisateur du médecin connecté
     * @param name nom complet affiché
     */
    public void setMedecinInfo(int id, String name) {
        this.currentMedecinId   = id;
        this.currentMedecinName = name;
        if (userNameLabel != null) userNameLabel.setText(name);
        // Recharger les données maintenant qu'on a l'id
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