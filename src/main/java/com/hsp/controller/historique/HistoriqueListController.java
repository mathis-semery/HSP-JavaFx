package com.hsp.controller.historique;

import com.hsp.dao.HistoriqueDAO;
import com.hsp.dao.UtilisateurDAO;
import com.hsp.model.Historique;
import com.hsp.model.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HistoriqueListController implements Initializable {

    @FXML private TableView<Historique> table;
    @FXML private TableColumn<Historique, String> idCol;
    @FXML private TableColumn<Historique, String> utilisateurCol;
    @FXML private TableColumn<Historique, String> actionCol;
    @FXML private TableColumn<Historique, String> tableCol;
    @FXML private TableColumn<Historique, String> idEnregistrementCol;
    @FXML private TableColumn<Historique, String> dateCol;
    @FXML private TableColumn<Historique, String> detailsCol;

    @FXML private TextField recherche;
    @FXML private ComboBox<String> filtreAction;
    @FXML private ComboBox<String> filtreTable;

    private HistoriqueDAO historiqueDAO;
    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Historique> historiques;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        historiqueDAO  = new HistoriqueDAO();
        utilisateurDAO = new UtilisateurDAO();
        historiques    = FXCollections.observableArrayList();

        configurerColonnes();
        configurerFiltres();
        chargerHistorique();
    }

    private void configurerColonnes() {
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_historique())));

        // Résolution du nom de l'utilisateur via UtilisateurDAO
        utilisateurCol.setCellValueFactory(cellData -> {
            Utilisateur u = utilisateurDAO.findById(cellData.getValue().getId_utilisateur());
            String texte = u != null
                    ? u.getNom() + " " + u.getPrenom() + " (" + u.getRole() + ")"
                    : "#" + cellData.getValue().getId_utilisateur();
            return new SimpleStringProperty(texte);
        });

        actionCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAction() != null
                        ? cellData.getValue().getAction() : ""));

        // Colorisation de l'action
        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "CREATION":
                        case "INSERT":
                            setStyle("-fx-text-fill: #155724; -fx-font-weight: bold;");
                            break;
                        case "MODIFICATION":
                        case "UPDATE":
                            setStyle("-fx-text-fill: #856404; -fx-font-weight: bold;");
                            break;
                        case "SUPPRESSION":
                        case "DELETE":
                            setStyle("-fx-text-fill: #721c24; -fx-font-weight: bold;");
                            break;
                        case "VALIDATION":
                            setStyle("-fx-text-fill: #1a73e8; -fx-font-weight: bold;");
                            break;
                        case "REFUS":
                            setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #555;");
                    }
                }
            }
        });

        tableCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTable_concernee() != null
                        ? cellData.getValue().getTable_concernee() : ""));

        idEnregistrementCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_enregistrement())));

        dateCol.setCellValueFactory(cellData -> {
            String texte = cellData.getValue().getDate_action() != null
                    ? cellData.getValue().getDate_action().toString().replace("T", " ")
                    : "N/A";
            return new SimpleStringProperty(texte);
        });

        detailsCol.setCellValueFactory(cellData -> {
            String details = cellData.getValue().getDetails();
            if (details != null && details.length() > 60) {
                details = details.substring(0, 57) + "...";
            }
            return new SimpleStringProperty(details != null ? details : "");
        });

        table.setItems(historiques);
    }

    private void configurerFiltres() {
        if (filtreAction != null) {
            filtreAction.setItems(FXCollections.observableArrayList(
                    "Toutes", "CREATION", "MODIFICATION", "SUPPRESSION", "VALIDATION", "REFUS"));
            filtreAction.setValue("Toutes");
            filtreAction.setOnAction(event -> appliquerFiltres());
        }

        if (filtreTable != null) {
            filtreTable.setItems(FXCollections.observableArrayList(
                    "Toutes",
                    "utilisateur", "patient", "dossier",
                    "hospitalisation", "ordonnance",
                    "produit", "fournisseur", "demande_produit",
                    "reapprovisionnement", "produit_fournisseur"));
            filtreTable.setValue("Toutes");
            filtreTable.setOnAction(event -> appliquerFiltres());
        }
    }

    private void chargerHistorique() {
        List<Historique> liste = historiqueDAO.findAll();
        historiques.clear();
        historiques.addAll(liste);
    }

    @FXML
    private void rechercher() {
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        String texte  = recherche    != null ? recherche.getText().toLowerCase().trim() : "";
        String action = filtreAction != null ? filtreAction.getValue() : "Toutes";
        String table  = filtreTable  != null ? filtreTable.getValue()  : "Toutes";

        List<Historique> tous = historiqueDAO.findAll();
        historiques.clear();

        for (Historique h : tous) {
            // Filtre action
            if (!"Toutes".equals(action) && !action.equalsIgnoreCase(h.getAction())) continue;

            // Filtre table
            if (!"Toutes".equals(table) && !table.equals(h.getTable_concernee())) continue;

            // Filtre texte libre
            if (!texte.isEmpty()) {
                boolean correspond = false;

                Utilisateur u = utilisateurDAO.findById(h.getId_utilisateur());
                if (u != null) {
                    String nomComplet = (u.getNom() + " " + u.getPrenom()).toLowerCase();
                    if (nomComplet.contains(texte)) correspond = true;
                }
                if (h.getAction() != null && h.getAction().toLowerCase().contains(texte))             correspond = true;
                if (h.getTable_concernee() != null && h.getTable_concernee().toLowerCase().contains(texte)) correspond = true;
                if (h.getDetails() != null && h.getDetails().toLowerCase().contains(texte))           correspond = true;
                if (h.getDate_action() != null && h.getDate_action().toString().contains(texte))      correspond = true;

                if (!correspond) continue;
            }

            historiques.add(h);
        }
    }

    @FXML
    private void rafraichir() {
        if (recherche    != null) recherche.clear();
        if (filtreAction != null) filtreAction.setValue("Toutes");
        if (filtreTable  != null) filtreTable.setValue("Toutes");
        chargerHistorique();
    }
}