package de.flywieeinairplane.drivingcar;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.nnet.MultiLayerPerceptron;
import processing.core.PVector;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import static de.flywieeinairplane.drivingcar.World.GLOBAL_TRANSFER_FUNCTION;
import static de.flywieeinairplane.drivingcar.World.NEURON_LAYER_DEFINITION;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static processing.core.PConstants.QUARTER_PI;

public class Car implements Comparable {

    PVector acceleration = new PVector();
    PVector position;
    PVector velocity = new PVector(0.05f,0.05f);
    int radius;

    float MAX_SPEED = 400;
    float MAX_ACCELLERATION = 1f;
    float SENSOR_RANGE = 100;
    float DRAG = 0.1f;

    float sensorFront;
    float sensorLeft;
    float sensorRight;
    PVector vectorFront;
    PVector vectorLeft;
    PVector vectorRight;

    NeuralNetwork nn;
    int distanceTraveled = 0;
    float distanceSinceLastCheckpoint = 0;
    float fitness = 0;
    int latestCheckpointCrossedIndex = -1;

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
            this.acceleration.limit(MAX_ACCELLERATION);

            // Update velocity
            this.velocity.add(this.acceleration);
            this.velocity.mult(1-DRAG);
            // Limit speed
            this.velocity.limit(this.MAX_SPEED);
            //update position
            PVector oldPos = new PVector(this.position.x, this.position.y);
            this.position.add(this.velocity);

            int checkpointBonus = calculateCheckpointBonus(oldPos, position);

            if (checkpointBonus <= 0) {
                distanceSinceLastCheckpoint += this.velocity.mag();
            } else {
                distanceSinceLastCheckpoint = 0;
            }
            if (collidesWithObstacles(this.position)
                    || collidesWithBoundary()
                    || distanceSinceLastCheckpoint > 1500 // to avoid circling
                    || distanceTraveled > 10000) { // to avoid endless generations when a first collision avoidance is learned
                this.crashed = true;
            }

            this.updateSensors();

            this.acceleration.mult(0);
            this.distanceTraveled += this.velocity.mag();
            this.fitness += this.velocity.mag() + checkpointBonus;
        }
    }

    private boolean collidesWithBoundary() {
        return (position.x <= 0 || position.y <= 0 || position.x >= this.world.width || position.y >= this.world.heigt);
    }

    private int calculateCheckpointBonus(PVector oldPos, PVector newPos) {
        Checkpoint nextCheckpoint;
        if (latestCheckpointCrossedIndex + 1 >= this.world.checkpointList.size()) {
            latestCheckpointCrossedIndex = -1;
        }
        nextCheckpoint = this.world.checkpointList.get(latestCheckpointCrossedIndex + 1);
        if (doIntersect(nextCheckpoint.start, nextCheckpoint.end, oldPos, newPos)) {
            return 6000*(++latestCheckpointCrossedIndex+1); // add arbitrary bonus for crossed checkpoints
        }
        return 0;
    }

    // https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/
    // Given three colinear points p, q, r, the function checks if
    // point q lies on line segment 'pr'
    static boolean onSegment(PVector p, PVector q, PVector r)
    {
        if (q.x <= max(p.x, r.x) && q.x >= min(p.x, r.x) &&
                q.y <= max(p.y, r.y) && q.y >= min(p.y, r.y))
            return true;

        return false;
    }

    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are colinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    static int orientation(PVector p, PVector q, PVector r)
    {
        // See https://www.geeksforgeeks.org/orientation-3-ordered-points/
        // for details of below formula.
        float val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0;  // colinear

        return (val > 0)? 1: 2; // clock or counterclock wise
    }

    // The main function that returns true if line segment 'p1q1'
    // and 'p2q2' intersect.
    public static boolean doIntersect(PVector p1, PVector q1, PVector p2, PVector q2)
    {
        // Find the four orientations needed for general and
        // special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;

        // Special Cases
        // p1, q1 and p2 are colinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1)) return true;

        // p1, q1 and q2 are colinear and q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;

        // p2, q2 and p1 are colinear and p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;

        // p2, q2 and q1 are colinear and q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false; // Doesn't fall in any of the above cases
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
        frontDistance.limit(2.0f);
        PVector leftDistance = frontDistance.get();
        leftDistance.rotate(-QUARTER_PI);
        PVector rightDistance = frontDistance.get();
        rightDistance.rotate(QUARTER_PI);
        sensorFront = 0;
        sensorRight = 0;
        sensorLeft = 0;
        PVector checkerPosition = this.position.get();
        while (!(collidesWithObstacles(checkerPosition) || collidesWithBoundary()) && this.sensorFront < this.SENSOR_RANGE) {
            checkerPosition.add(frontDistance);
            this.sensorFront = checkerPosition.dist(this.position);
        }
        PVector frontLine = this.position.get();
        frontLine.sub(checkerPosition);

        checkerPosition = this.position.get();
        while (!(collidesWithObstacles(checkerPosition) || collidesWithBoundary()) && this.sensorLeft < this.SENSOR_RANGE) {
            checkerPosition.add(leftDistance);
            this.sensorLeft = checkerPosition.dist(this.position);
        }
        checkerPosition = this.position.get();
        while (!(collidesWithObstacles(checkerPosition) || collidesWithBoundary()) && this.sensorRight < this.SENSOR_RANGE) {
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
        double[] res = {this.velocity.mag()/MAX_SPEED, this.sensorFront/SENSOR_RANGE, this.sensorLeft/SENSOR_RANGE, this.sensorRight/SENSOR_RANGE};
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
        if (this.fitness == car2.fitness)
            return 0;
        if (this.fitness > car2.fitness)
            return 1;
        if (this.fitness < car2.fitness)
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
