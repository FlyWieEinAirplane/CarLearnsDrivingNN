package de.flywieeinairplane.drivingcar;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.nnet.MultiLayerPerceptron;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;

import static de.flywieeinairplane.drivingcar.World.GLOBAL_TRANSFER_FUNCTION;
import static de.flywieeinairplane.drivingcar.World.NEURON_LAYER_DEFINITION;
import static processing.core.PConstants.QUARTER_PI;

public class Car implements Comparable {

    PVector acceleration = new PVector();
    PVector position;
    PVector velocity = new PVector();
    int radius;
    float maxspeed = 4;
    float maxAcceleration = 1f;
    float sensorRange = 100;

    float sensorFront;
    float sensorLeft;
    float sensorRight;
    PVector vectorFront;
    PVector vectorLeft;
    PVector vectorRight;

    NeuralNetwork nn;
    int distanceTraveled = 0;
    float fitnes = 0;

    World world;
    boolean crashed;

    public Car(PVector position, int radius, World world) {
        this.position = position;
        this.radius = radius;
        this.world = world;
        nn = new MultiLayerPerceptron(Arrays.asList(NEURON_LAYER_DEFINITION), GLOBAL_TRANSFER_FUNCTION);
    }

    public Car(PVector position, int radius, World world, NeuralNetwork nn) {
        this.position = position;
        this.radius = radius;
        this.world = world;

        this.nn = nn;

    }

    public void updatePosition() {

        if (!crashed) {
//            decide acceleration
            nn.setInput(getInputLayerFromCar());
            nn.calculate();
            double[] outputs = nn.getOutput();

//            apply NN output to vectors
            PVector frontAcceleration = new PVector((float) outputs[0], 0);
            PVector leftAcceleration = new PVector(0, -(float) outputs[1]);
            PVector rightAcceleration = new PVector(0, (float) outputs[2]);


            this.acceleration.add(frontAcceleration);
            this.acceleration.add(leftAcceleration);
            this.acceleration.add(rightAcceleration);
            float theta = this.velocity.heading();
//            float theta = PVector.angleBetween(frontAcceleration, velocity);
            this.acceleration.rotate(theta);
            this.acceleration.limit(maxAcceleration);

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
            this.distanceTraveled++;
            this.fitnes = this.position.x + this.distanceTraveled * 3;
        }
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

        vectorFront = frontDistance.get();
        vectorFront.setMag(sensorFront);
        vectorLeft = leftDistance.get();
        vectorLeft.setMag(sensorLeft);
        vectorRight = rightDistance.get();
        vectorRight.setMag(sensorRight);

    }


    public double[] getInputLayerFromCar() {
        double[] res = {this.velocity.mag(), this.sensorFront, this.sensorLeft, this.sensorRight};
        return res;
    }

    public Car clone(PVector startPosition) {
        return new Car(startPosition, this.radius, this.world);
    }

    public Car clone(PVector startPosition, boolean nn) {
        return new Car(startPosition, this.radius, this.world, cloneMLPNN(this.nn));
    }

    public int compareTo(Object o) {
        Car car2 = (Car) o;
        if (this.fitnes == car2.fitnes)
            return 0;
        if (this.fitnes > car2.fitnes)
            return 1;
        if (this.fitnes < car2.fitnes)
            return -1;
        return 0;
    }

    /**
     * create a copy of a Neural Network by creating copies of layers, neurons, weights
     */
    public NeuralNetwork cloneMLPNN(NeuralNetwork oldNN) {
        ArrayList<Integer> neurons = new ArrayList<Integer>();
        Layer[] layers = oldNN.getLayers();
        for (int i = 0; i < layers.length; i++) {
            Layer layer = layers[i];
            if (i != layers.length - 1) {
//                when cloning we need to exclude the bias neuron
                neurons.add(layer.getNeurons().length - 1);
            } else {
                neurons.add(layer.getNeurons().length);
            }
        }
        NeuralNetwork newNetwork = new MultiLayerPerceptron(neurons, GLOBAL_TRANSFER_FUNCTION);

        for (int i = 0; i < oldNN.getLayers().length; i++) {
            Layer layer = oldNN.getLayerAt(i);
            for (int j = 0; j < layer.getNeurons().length; j++) {
                Neuron neuron = layer.getNeuronAt(j);
                for (int k = 0; k < neuron.getWeights().length; k++) {
                    Weight weight = neuron.getWeights()[k];
                    newNetwork.getLayerAt(i).getNeuronAt(j).getWeights()[k].setValue(weight.value);
                }
            }
        }
        return newNetwork;
    }


}
