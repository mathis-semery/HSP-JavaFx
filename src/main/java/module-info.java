module com.hsp.hsp_urgences {
    // Modules JavaFX requis
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Module pour la base de données MySQL
    requires java.sql;
    requires jbcrypt;

    // Exporter les packages de contrôleurs pour que JavaFX FXML puisse y accéder
    opens com.hsp.controller.login to javafx.fxml;
    opens com.hsp.controller.dashboard to javafx.fxml;
    opens com.hsp.controller.stock to javafx.fxml;
    opens com.hsp.controller.fournisseur to javafx.fxml;
    opens com.hsp.controller.produit to javafx.fxml;
    opens com.hsp.controller.reappro to javafx.fxml;
    opens com.hsp.controller.demande to javafx.fxml;


    // Si vous avez d'autres contrôleurs, ajoutez-les ici
    // opens com.hsp.controller.auth to javafx.fxml;
    // opens com.hsp.controller.admin to javafx.fxml;

    // Exporter le package principal (si nécessaire)
    exports com.hsp;

    // Si vous avez des modèles/entités utilisés dans les vues
    // opens com.hsp.model to javafx.base;
}