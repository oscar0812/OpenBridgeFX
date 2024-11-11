module com.oscarrtorres.openbridgefx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires org.jsoup;
    requires org.json;
    requires org.yaml.snakeyaml;
    requires jtokkit;
    requires org.commonmark;
    requires java.desktop;
    requires vosk;
    requires com.fasterxml.jackson.databind;
    requires annotations;

    opens com.oscarrtorres.openbridgefx to javafx.fxml;
    exports com.oscarrtorres.openbridgefx;

    exports com.oscarrtorres.openbridgefx.dialogs;
    opens com.oscarrtorres.openbridgefx.dialogs to javafx.fxml;
    exports com.oscarrtorres.openbridgefx.models;
    opens com.oscarrtorres.openbridgefx.models to javafx.fxml;
    exports com.oscarrtorres.openbridgefx.services;
    opens com.oscarrtorres.openbridgefx.services to javafx.fxml;
    exports com.oscarrtorres.openbridgefx.utils;
    opens com.oscarrtorres.openbridgefx.utils to javafx.fxml;
}