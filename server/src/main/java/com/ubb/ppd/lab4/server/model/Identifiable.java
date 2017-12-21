package com.ubb.ppd.lab4.server.model;

/**
 * @author Marius Adam
 */
public interface Identifiable<Id> {
    Id getId();

    void setId(Id id);
}
