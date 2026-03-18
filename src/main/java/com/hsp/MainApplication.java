package com.hsp;

import com.hsp.config.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        String fxmlPath = adminExists() ? "/view/auth/Login.fxml" : "/view/auth/SetupAdmin.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = new Scene(loader.load(), 800, 600);
        stage.setTitle("HSP Urgences");
        stage.setScene(scene);
        stage.show();
    }

    private boolean adminExists() {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role = 'Admin'";
        try (Connection cnx = Database.getConnexion();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
