module com.example.aes {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.aes to javafx.fxml;
    exports com.example.aes;
}