module org.zakariafarih.copyspider {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.zakariafarih.copyspider to javafx.fxml;
    exports org.zakariafarih.copyspider;
}