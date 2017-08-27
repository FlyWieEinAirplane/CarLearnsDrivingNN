package de.flywieeinairplane.drivingcar;

import processing.core.PVector;

import java.io.Serializable;

public class Obstacle implements Serializable{
    PVector position;
    float radius;

    public Obstacle(PVector position, float radius) {
        this.position = position;
        this.radius = radius;
    }
}
