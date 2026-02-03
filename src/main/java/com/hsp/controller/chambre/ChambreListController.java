package com.hsp.controller.chambre;

import com.hsp.dao.ChambreDAO;
import com.hsp.model.Chambre;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChambreListController implements Initializable {

    @FXML
    private TableView<Chambre> table;

    @FXML
    private TableColumn<Chambre, String> numero;

    @FXML
    private TableColumn<Chambre, String> etage;

    @FXML
    private TableColumn<Chambre, String> nbLits;

    @FXML
    private TableColumn<Chambre, String> disponible;

    @FXML
    private TextField recherche;

    @FXML
    private Button ajouter;

    @FXML
    private Button modifier;

    @FXML
    private Button supprimer;

    @FXML
    private Button details;

    @FXML
    private ComboBox<String> filtre;

    private ChambreDAO chambreDAO;
    private ObservableList<Chambre> chambres;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chambreDAO = new ChambreDAO();
        chambres = FXCollections.observableArrayList();

        configurerColonnes();
        configurerFiltre();
        chargerChambres();

        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        supprimer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        details.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                afficherDetails();
            }
        });
    }

    private void configurerColonnes() {
        numero.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNumero()));
        etage.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEtage()));
        nbLits.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNb_lits()));
        disponible.setCellValueFactory(cellData -> {
            String dispo = cellData.getValue().getDisponible();
            String texte = ("1".equals(dispo) || "true".equalsIgnoreCase(dispo)) ? "Oui" : "Non";
            return new SimpleStringProperty(texte);
        });

        table.setItems(chambres);
    }

    private void configurerFiltre() {
        if (filtre != null) {
            filtre.setItems(FXCollections.observableArrayList("Toutes", "Disponibles", "Occupees"));
            filtre.setValue("Toutes");
            filtre.setOnAction(event -> filtrerChambres());
        }
    }

    private void chargerChambres() {
        List<Chambre> liste = chambreDAO.findAll();
        chambres.clear();
        chambres.addAll(liste);
    }

    @FXML
    private void rechercher() {
        String texte = recherche.getText().toLowerCase().trim();
        List<Chambre> toutes = chambreDAO.findAll();

        chambres.clear();

        if (texte.isEmpty()) {
            chambres.addAll(toutes);
        } else {
            for (Chambre chambre : toutes) {
                boolean correspond = false;
                if (chambre.getNumero() != null && chambre.getNumero().toLowerCase().contains(texte)) {
                    correspond = true;
                }
                if (chambre.getEtage() != null && chambre.getEtage().toLowerCase().contains(texte)) {
                    correspond = true;
                }
                if (correspond) {
                    chambres.add(chambre);
                }
            }
        }

        appliquerFiltre();
    }

    @FXML
    private void filtrerChambres() {
        rechercher();
    }

    private void appliquerFiltre() {
        if (filtre == null || "Toutes".equals(filtre.getValue())) {
            return;
        }

        String valeur = filtre.getValue();
        chambres.removeIf(chambre -> {
            boolean estDisponible = "1".equals(chambre.getDisponible()) || "true".equalsIgnoreCase(chambre.getDisponible());
            if ("Disponibles".equals(valeur)) {
                return !estDisponible;
            } else if ("Occupees".equals(valeur)) {
                return estDisponible;
            }
            return false;
        });
    }

    @FXML
    private void ajouterChambre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hsp/view/chambre/chambre-form.fxml"));
            Parent root = loader.load();

            ChambreFormController controller = loader.getController();
            controller.setMode(ChambreFormController.Mode.CREATION);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle chambre");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerChambres();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void modifierChambre() {
        Chambre selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hsp/view/chambre/chambre-form.fxml"));
            Parent root = loader.load();

            ChambreFormController controller = loader.getController();
            controller.setMode(ChambreFormController.Mode.MODIFICATION);
            controller.setChambre(selection);

            Stage stage = new Stage();
            stage.setTitle("Modifier la chambre");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerChambres();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void supprimerChambre() {
        Chambre selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la chambre " + selection.getNumero() + " ?");
        confirmation.setContentText("Cette action est irreversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = chambreDAO.delete(selection.getId_chambre());
            if (succes) {
                chargerChambres();
                afficherInfo("Succes", "La chambre a ete supprimee.");
            } else {
                afficherErreur("Erreur", "Impossible de supprimer la chambre.");
            }
        }
    }

    @FXML
    private void afficherDetails() {
        Chambre selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hsp/view/chambre/chambre-detail.fxml"));
            Parent root = loader.load();

            ChambreDetailController controller = loader.getController();
            controller.setChambre(selection);

            Stage stage = new Stage();
            stage.setTitle("Details de la chambre " + selection.getNumero());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture des details", e.getMessage());
        }
    }

    @FXML
    private void rafraichir() {
        recherche.clear();
        if (filtre != null) {
            filtre.setValue("Toutes");
        }
        chargerChambres();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
