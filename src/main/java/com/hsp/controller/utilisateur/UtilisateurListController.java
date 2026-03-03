package com.hsp.controller.utilisateur;

import com.hsp.dao.UtilisateurDAO;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UtilisateurListController implements Initializable {

    @FXML private TableView<Utilisateur> table;
    @FXML private TableColumn<Utilisateur, String> idCol;
    @FXML private TableColumn<Utilisateur, String> nomCol;
    @FXML private TableColumn<Utilisateur, String> prenomCol;
    @FXML private TableColumn<Utilisateur, String> emailCol;
    @FXML private TableColumn<Utilisateur, String> roleCol;
    @FXML private TableColumn<Utilisateur, String> dateCreationCol;
    @FXML private TextField recherche;
    @FXML private Button ajouter;
    @FXML private Button modifier;
    @FXML private Button supprimer;

    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Utilisateur> utilisateurs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        utilisateurDAO = new UtilisateurDAO();
        utilisateurs = FXCollections.observableArrayList();

        configurerColonnes();
        chargerUtilisateurs();

        modifier.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        supprimer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                modifierUtilisateur();
            }
        });
    }

    private void configurerColonnes() {
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));

        nomCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom() != null ? cellData.getValue().getNom() : ""));

        prenomCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrenom() != null ? cellData.getValue().getPrenom() : ""));

        emailCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : ""));

        roleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole() != null ? cellData.getValue().getRole() : ""));

        dateCreationCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate_creation() != null ?
                        cellData.getValue().getDate_creation() : ""));

        table.setItems(utilisateurs);
    }

    private void chargerUtilisateurs() {
        List<Utilisateur> liste = utilisateurDAO.findAll();
        utilisateurs.clear();
        utilisateurs.addAll(liste);
    }

    @FXML
    private void rechercher() {
        String texte = recherche.getText().toLowerCase().trim();
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

                if (correspond) {
                    utilisateurs.add(u);
                }
            }
        }
    }

    @FXML
    private void ajouterUtilisateur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateur/UtilisateurForm.fxml"));
            Parent root = loader.load();

            UtilisateurFormController controller = loader.getController();
            controller.setMode(UtilisateurFormController.Mode.CREATION);

            Stage stage = new Stage();
            stage.setTitle("Nouvel utilisateur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerUtilisateurs();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void modifierUtilisateur() {
        Utilisateur selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateur/UtilisateurForm.fxml"));
            Parent root = loader.load();

            UtilisateurFormController controller = loader.getController();
            controller.setMode(UtilisateurFormController.Mode.MODIFICATION);
            controller.setUtilisateur(selection);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerUtilisateurs();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }

    @FXML
    private void supprimerUtilisateur() {
        Utilisateur selection = table.getSelectionModel().getSelectedItem();
        if (selection == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'utilisateur \"" + selection.getPrenom() + " " + selection.getNom() + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            boolean succes = utilisateurDAO.delete(selection.getId());
            if (succes) {
                chargerUtilisateurs();
                afficherInfo("Succès", "L'utilisateur a été supprimé.");
            } else {
                afficherErreur("Erreur", "Impossible de supprimer l'utilisateur.");
            }
        }
    }

    @FXML
    private void rafraichir() {
        recherche.clear();
        chargerUtilisateurs();
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
