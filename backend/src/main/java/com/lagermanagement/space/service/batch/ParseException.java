package com.lagermanagement.space.service.batch;

/**
 * Wird geworfen, wenn eine Import-Datei nicht lesbar ist oder Pflicht-Spalten fehlen.
 */
public class ParseException extends Exception {

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
