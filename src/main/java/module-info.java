module com.oscarrtorres.openbridgefx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    // requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.json;
    requires io.github.cdimascio.dotenv.java;
    requires jtokkit;

    opens com.oscarrtorres.openbridgefx to javafx.fxml;
    exports com.oscarrtorres.openbridgefx;
}