package com.fred4106.defaultMinimapZoom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MinimapZoomLevel {
    Zoom200("2.00"),
    Zoom225("2.25"),
    Zoom250("2.50"),
    Zoom275("2.75"),
    Zoom300("3.00"),
    Zoom325("3.25"),
    Zoom350("3.50"),
    Zoom375("3.75"),
    Zoom400("4.00"),
    Zoom425("4.25"),
    Zoom450("4.50"),
    Zoom475("4.75"),
    Zoom500("5.00"),
    Zoom525("5.25"),
    Zoom550("5.50"),
    Zoom575("5.75"),
    Zoom600("6.00"),
    Zoom625("6.25"),
    Zoom650("6.50"),
    Zoom675("6.75"),
    Zoom700("7.00"),
    Zoom725("7.25"),
    Zoom750("7.50"),
    Zoom775("7.75"),
    Zoom800("8.00");

    private final String option;

    @Override
    public String toString() {
        return option;
    }

    public double toZoomLevel() {
        return Double.parseDouble(option);
    }
}