// src/main/java/com/registro/model/Site.java
package com.registro.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Site {
    BUENOS_AIRES("Buenos Aires"),
    CORDOBA("Córdoba"),
    TUCUMAN("Tucumán"),
    MAR_DEL_PLATA("Mar del Plata"),
    PERU("Perú");

    private final String label;

    Site(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
