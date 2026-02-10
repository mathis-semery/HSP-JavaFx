package com.hsp.controller.fournisseur;

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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FournisseurListController implements Initializable {

    @FXML
    private TableView<Fournisseur> table;

    @FXML
    private TableColumn<Fournisseur, String> idCol;

    @FXML
    private TableColumn<Fournisseur, String> nomCol;

    @FXML
    private TableColumn<Fournisseur, String> contactCol;

    @FXML
    private TableColumn<Fournisseur, String> emailCol;

    @FXML
    private TableColumn<Fournisseur, String> telephoneCol;

    @FXML
    private TableColumn<Fournisseur, String> adresseCol;

    @FXML
    private TableColumn<Fournisseur, String> dateCreationCol;

    @FXML
    private TextField recherche;

    @FXML
    private Button ajouter;

    @FXML
    private Button modifier;

    @FXML
    private Button supprimer;

    private FournisseurDAO fournisseurDAO;
    private ObservableList<Fournisseur> fournisseurs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fournisseurDAO = new FournisseurDAO();
        fournisseurs = FXCollections.observableArrayList();

        configurerColonnes();
        chargerFournisseurs();

        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        supprimer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                modifierFournisseur();
            }
        });
    }

    private void configurerColonnes() {
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId_fournisseur())));

        nomCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom()));

        contactCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getContact() != null ?
                        cellData.getValue().getContact() : ""));

        emailCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail() != null ?
                        cellData.getValue().getEmail() : ""));

        telephoneCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTelephone() != null ?
                        cellData.getValue().getTelephone() : ""));

        adresseCol.setCellValueFactory(cellData -> {
            String adresse = cellData.getValue().getAdresse();
            if (adresse != null && adresse.length() > 40) {
                adresse = adresse.substring(0, 37) + "...";
            }
            return new SimpleStringProperty(adresse != null ? adresse : "");
        });

        dateCreationCol.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getDate_creation() != null ?
                    cellData.getValue().getDate_creation().toString() : "N/A";
            return new SimpleStringProperty(date);
        });

        table.setItems(fournisseurs);
    }

    private void chargerFournisseurs() {
        List<Fournisseur> liste = fournisseurDAO.findAll();
        fournisseurs.clear();
        fournisseurs.addAll(liste);
    }

    @FXML
    private void rechercher() {
        String texte = recherche.getText().toLowerCase().trim();
        List<Fournisseur> tous = fournisseurDAO.findAll();

        fournisseurs.clear();

        if (texte.isEmpty()) {
            fournisseurs.addAll(tous);
        } else {
            for (Fournisseur fournisseur : tous) {
                boolean correspond = false;

                if (String.valueOf(fournisseur.getId_fournisseur()).contains(texte)) {
                    correspond = true;
                }

                if (fournisseur.getNom() != null && fournisseur.getNom().toLowerCase().contains(texte)) {
                    correspond = true;
                }

                if (fournisseur.getContact() != null && fournisseur.getContact().toLowerCase().contains(texte)) {
                    correspond = true;
                }

                if (fournisseur.getEmail() != null && fournisseur.getEmail().toLowerCase().contains(texte)) {
                    correspond = true;
                }

                if (fournisseur.getTelephone() != null && fournisseur.getTelephone().contains(texte)) {
                    correspond = true;
                }

                if (correspond) {
                    fournisseurs.add(fournisseur);
                }
            }
        }
    }

    @FXML
    private void ajouterFournisseur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/FournisseurForm.fxml"));
            Parent root = loader.load();

            FournisseurFormController controller = loader.getController();
            controller.setMode(FournisseurFormController.Mode.CREATION);

            Stage stage = new Stage();
            stage.setTitle("Nouveau fournisseur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerFournisseurs();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void modifierFournisseur() {
        Fournisseur selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stock/FournisseurForm.fxml"));
            Parent root = loader.load();

            FournisseurFormController controller = loader.getController();
            controller.setMode(FournisseurFormController.Mode.MODIFICATION);
            controller.setFournisseur(selection);

            Stage stage = new Stage();
            stage.setTitle("Modifier le fournisseur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerFournisseurs();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void supprimerFournisseur() {
        Fournisseur selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le fournisseur \"" + selection.getNom() + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = fournisseurDAO.delete(selection.getId_fournisseur());
            if (succes) {
                chargerFournisseurs();
                afficherInfo("Succès", "Le fournisseur a été supprimé.");
            } else {
                afficherErreur("Erreur", "Impossible de supprimer le fournisseur.");
            }
        }
    }

    @FXML
    private void rafraichir() {
        recherche.clear();
        chargerFournisseurs();
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
