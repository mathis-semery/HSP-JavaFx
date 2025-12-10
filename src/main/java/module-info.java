module com.hsp.hsp_urgences {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.hsp.hsp_urgences to javafx.fxml;
    exports com.hsp.hsp_urgences;
}