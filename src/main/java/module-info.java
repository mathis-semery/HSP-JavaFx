module com.hsp.hsp_urgences {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;
    requires jbcrypt;

    opens com.hsp to javafx.graphics;
    opens com.hsp.controller.patient to javafx.fxml;
    opens com.hsp.controller.dossier to javafx.fxml;
    opens com.hsp.controller.hospitalisation to javafx.fxml;
    opens com.hsp.controller.stock to javafx.fxml;

    exports com.hsp;
}