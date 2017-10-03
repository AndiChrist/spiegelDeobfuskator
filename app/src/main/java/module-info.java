module de.avwc.app {
    exports de.avwc.app;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.web;

    requires de.avwc.decoder;
    uses de.avwc.decoder.SpiegelDecoder;

}