package de.flywieeinairplane.drivingcar;

import processing.core.PVector;

import java.io.Serializable;

public class Checkpoint implements Serializable {
    public final PVector start;
    public final PVector end;

    public Checkpoint(PVector start, PVector end) {
        this.start = start;
        this.end = end;
    }
}
