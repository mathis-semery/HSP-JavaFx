package com.hsp;

import com.hsp.config.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        String fxml = adminExiste() ? "/view/auth/Login.fxml" : "/view/auth/SetupAdmin.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Scene scene = new Scene(loader.load(), 800, 600);
        stage.setTitle("HSP Urgences");
        stage.setScene(scene);
        stage.show();
    }

    private boolean adminExiste() {
        try (Connection conn = Database.getConnexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM utilisateur WHERE role = 'Admin'")) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
