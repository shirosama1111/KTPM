package com.example.demo4.models;

import javafx.beans.property.*;

public class assets {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty type;
    private final IntegerProperty quantity;
    private final StringProperty status;

    public assets(int id, String name, String type, int quantity, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.status = new SimpleStringProperty(status);
    }

    @Override
    public String toString() {
        return name + " (" + quantity + ")";
    }


    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Name
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    // Type
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public StringProperty typeProperty() { return type; }

    // Quantity
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }

    // Status
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
}
