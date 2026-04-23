module com.example.astroids {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens com.example.astroids to javafx.fxml;
    exports com.example.astroids;
}