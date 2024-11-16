module com.oscarrtorres.openmodelfx {
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
    requires lombok;

    opens com.oscarrtorres.openmodelfx to javafx.fxml;
    exports com.oscarrtorres.openmodelfx;

    exports com.oscarrtorres.openmodelfx.dialogs;
    opens com.oscarrtorres.openmodelfx.dialogs to javafx.fxml;
    exports com.oscarrtorres.openmodelfx.models;
    opens com.oscarrtorres.openmodelfx.models to javafx.fxml;
    exports com.oscarrtorres.openmodelfx.services;
    opens com.oscarrtorres.openmodelfx.services to javafx.fxml;
    exports com.oscarrtorres.openmodelfx.utils;
    opens com.oscarrtorres.openmodelfx.utils to javafx.fxml;
}