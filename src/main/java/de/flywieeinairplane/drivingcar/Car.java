package de.flywieeinairplane.drivingcar;

import processing.core.PApplet;
import processing.core.PVector;

import static processing.core.PConstants.QUARTER_PI;

public class Car {
    PVector acceleration = new PVector(1, 1);
    PVector position;
    PVector velocity = new PVector();
    int radius;
    float maxspeed = 3;
    float maxforce = 0.5f;
    float sensorRange = 100;

    float sensorFront;
    float sensorLeft;
    float sensorRight;

    World world;
    boolean crashed;

    public Car(PVector position, int radius, World world) {

        this.position = position;
        this.radius = radius;
        this.world = world;
    }

    public void updatePosition() {

//        NN-stuff to decide acceleration



        // Update velocity
        this.velocity.add(this.acceleration);
        // Limit speed
        this.velocity.limit(this.maxspeed);
        //update position
        this.position.add(this.velocity);

        if (collidesWithObstacles(this.position)) {
            this.crashed = true;
        }

        this.updateSensors();

        this.acceleration.mult(0);
    }

    public boolean collidesWithObstacles(PVector position) {
        for (Obstacle obst : this.world.obstacleList) {
            if (Math.abs(PVector.dist(position, obst.position)) < obst.radius) {
                return true;
            }
        }
        return false;
    }

    public void updateSensors() {
        PVector frontDistance = velocity.get();
        frontDistance.limit(0.1f);
        PVector leftDistance = frontDistance.get();
        leftDistance.rotate(-QUARTER_PI);
        PVector rightDistance = frontDistance.get();
        rightDistance.rotate(QUARTER_PI);
        sensorFront = 0;
        sensorRight = 0;
        sensorLeft = 0;
        PVector checkerPosition = this.position.get();
        while (!collidesWithObstacles(checkerPosition) && this.sensorFront < this.sensorRange) {
            checkerPosition.add(frontDistance);
            this.sensorFront = checkerPosition.dist(this.position);
        }
        PVector frontLine = this.position.get();
        frontLine.sub(checkerPosition);

        checkerPosition = this.position.get();
        while (!collidesWithObstacles(checkerPosition) && this.sensorLeft < this.sensorRange) {
            checkerPosition.add(leftDistance);
            this.sensorLeft = checkerPosition.dist(this.position);
        }
        checkerPosition = this.position.get();
        while (!collidesWithObstacles(checkerPosition) && this.sensorRight < this.sensorRange) {
            checkerPosition.add(rightDistance);
            this.sensorRight = checkerPosition.dist(this.position);
        }


    }


    public void reset(Car car) {

    }
}
