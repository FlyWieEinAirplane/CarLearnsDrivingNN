package de.flywieeinairplane.drivingcar;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.util.TransferFunctionType;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.event.MouseWheelEvent;
import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class World extends PApplet {

    private ArrayList<Generation> generationList = new ArrayList<>();
    Random random = new Random();
    int heigt = 900;
    int width = 1600;
    private int currentObstacleRadius = 30;

    ArrayList<Obstacle> obstacleList = new ArrayList<>();
    ArrayList<Checkpoint> checkpointList = new ArrayList<>();
    ArrayList<Car> carList = new ArrayList<Car>();
    PVector carStartPosition = new PVector(150, 185);


    //    EDIT

    private int generationSize = 30; // change the number of cars per generation
    private boolean drawCourseMode = false; // enable/disable creating new world
    private boolean drawCar = true; // enable/disable drawing the car
    private boolean drawCheckpoints = true; // enable/disable drawing the checkpoints used for training
    private boolean drawObstacles = true; // enable/disable drawing the obstacles making the
    private boolean drawSensors = true; // enable/disable drawing the sensors of the car
    // transfer function used by the neuralNet to calculate weights
    static final TransferFunctionType GLOBAL_TRANSFER_FUNCTION = TransferFunctionType.SIGMOID;
    // Number of input, hidden and output layers of the neuralNet
    static final Integer[] NEURON_LAYER_DEFINITION = new Integer[]{4, 12, 12, 3};
    // filename to load/save environment from (obstacles and checkpoints)
    public static final String ENVIRONMENT_FILENAME = "env01";
    // number of car updates calculated per frame (used to speed up the progress)
    private final int updateSpeedMultiplier = 2;

    // edit to load trained models for cars:
    private final boolean loadModelFromFile = false; // enable/disable loading a pretrained model
    private final String modelFileName = "_NN_1000000.nnet"; // file to save/load NN  model from/to
    private final boolean keepTrainingModel = true; // enable/disable training of the model



    public World() {
        if (drawCourseMode) {
            System.out.println("Click on the world to create custom obstacles and create a racing track");
//          Routine to save changes of Obstacles
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ENVIRONMENT_FILENAME))) {
                    oos.writeObject(new Environment(obstacleList, checkpointList));
                    System.out.println("Obstacles saved to file: " + obstacleList.size() + " Obstacles");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }));
        } else {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Instant.now() + "_save.nnet"))) {
                    oos.writeObject(carList.stream().max(Car::compareTo).orElseThrow(IllegalArgumentException::new).nn);
                    System.out.println("Obstacles saved to file: " + obstacleList.size() + " Obstacles");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }));
        }
    }

    public void setup() {
//        Load Obstacle List
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ENVIRONMENT_FILENAME))) {
            Environment env = (Environment) ois.readObject();
            this.obstacleList = env.obstacleList;
            this.checkpointList = env.checkpointList;
            System.out.println("Obstacles loaded from file: " + obstacleList.size());
            System.out.println("Checkpoints loaded from file: " + checkpointList.size());
        } catch (FileNotFoundException e) {
            System.out.println("No File found: start with a blank map");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }

//        setup frame
        size(this.width, this.heigt);
        frameRate(30);

//        create Cars
        for (int i = 0; i < generationSize; i++) {
            if (loadModelFromFile) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFileName))) {
                    NeuralNetwork nn = (NeuralNetwork) ois.readObject();
                    System.out.println("Loading NN: " + nn.toString());
                    carList.add(new Car(carStartPosition.get(), 5, this, nn));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                carList.add(new Car(carStartPosition.get(), 5, this));
            }
        }
    }

    public void draw() {
        background(50);
        noStroke();

//        Draw Obstacles
        fill(255);
        for (Obstacle obstacle : obstacleList) {
            ellipse(obstacle.position.x, obstacle.position.y, obstacle.radius * 2, obstacle.radius * 2);
        }
        stroke(255);
        for (Checkpoint checkpoint : checkpointList) {
            line(checkpoint.start.x, checkpoint.start.y, checkpoint.end.x, checkpoint.end.y);
        }
        noStroke();

        if (drawCourseMode) {
//            draw preview of objects that can be created
            fill(150);
            if (drawObstacles) {
                ellipse(mouseX, mouseY, currentObstacleRadius * 2, currentObstacleRadius * 2);
            } else if (drawCheckpoints) {
                stroke(255);
                if (checkpointStartVector != null) {
                    rect(checkpointStartVector.x, checkpointStartVector.y, 5, 5);
                    line(checkpointStartVector.x, checkpointStartVector.y,mouseX, mouseY);
                }
                rect(mouseX, mouseY, 5, 5);
            }
            fill(255,0,0);
            ellipse(carStartPosition.x, carStartPosition.y, 5, 5);
//            do not continue drawing anything else if we just want to create a new course
            return;
        }
//        Draw Cars
        fill(0, 255, 0);
        noStroke();
        ArrayList<Car> carFinishedList = new ArrayList<Car>();
        for (Car car : carList) {
            for (int i = 0; i < updateSpeedMultiplier; i++) {
                car.updatePosition();
            }

            if (!car.crashed) {
//            draw sensors
                if (drawSensors) {
                    stroke(0, 255, 0);
                    PVector vector = car.vectorFront.get();
                    vector.add(car.position);
                    line(car.position.x, car.position.y, vector.x, vector.y);

                    vector = car.vectorLeft.get();
                    vector.add(car.position);
                    line(car.position.x, car.position.y, vector.x, vector.y);

                    vector = car.vectorRight.get();
                    vector.add(car.position);
                    line(car.position.x, car.position.y, vector.x, vector.y);

                    noStroke();
                }

//            Draw Car body
                if (drawCar) {
                    float theta = car.velocity.heading() + PI / 2;
                    pushMatrix();
                    translate(car.position.x, car.position.y);
                    rotate(theta);
                    beginShape();
                    vertex(0, -car.radius * 2);
                    vertex(-car.radius, car.radius * 2);
                    vertex(car.radius, car.radius * 2);
                    endShape(CLOSE);
                    popMatrix();
                }
            } else {
                carFinishedList.add(car);
            }
        }
//        for (Car car : carFinishedList) {
//            carList.remove(car);
//        }

        if (carList.size() == carFinishedList.size()) {
//            Generation has finished
            Generation gen = new Generation(generationList.size() + 1);
            gen.extractInformation(carFinishedList);
            gen.printStats();
            System.out.println();
            generationList.add(gen);

            carList.clear();
            carList.addAll(fps(carFinishedList));
            mutation(carList, 0.05f);
            carFinishedList.clear();
//
//            for (Car car : carFinishedList) {
//                Layer layer = car.nn.getLayers()[0];
//                Neuron neuron = layer.getNeuronAt(0);
//                Weight weight = neuron.getWeights()[0];
//
//            }

//            System.exit(0);

        }

    }

    /**
     * Create a new obstacle at the cursor position when in drawCourseMode
     */
    @Override
    public void mouseClicked() {
        if (drawCourseMode) {
            if (drawObstacles) {
                this.obstacleList.add(new Obstacle(new PVector(mouseX, mouseY), currentObstacleRadius));
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        if (e.getWheelRotation() < 0) {
            if (currentObstacleRadius < 500)
                currentObstacleRadius++;
        } else {
            if (currentObstacleRadius > 2)
                currentObstacleRadius--;
        }
    }

    private PVector checkpointStartVector = null;

    @Override
    public void mousePressed(MouseEvent event) {
        super.mousePressed(event);
        if (drawCourseMode & drawCheckpoints) {
//            set start of checkpoint
            checkpointStartVector = new PVector(mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        super.mouseReleased(event);
        if (drawCourseMode) {
            if (drawObstacles) {
//                add obstacle at mouse position
                this.obstacleList.add(new Obstacle(new PVector(mouseX, mouseY), currentObstacleRadius));
            } else if (drawCheckpoints) {
//                add checkpoint at mouse position
                this.checkpointList.add(new Checkpoint(checkpointStartVector, new PVector(mouseX, mouseY)));
                checkpointStartVector = null;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
        super.keyTyped(event);
        //        change mode of creating obstacles or checkpoints
        if (event.getKey() == 'o') {
            drawCheckpoints = false;
            drawObstacles = true;
        } else if (event.getKey() == 'c') {
            drawCheckpoints = true;
            drawObstacles = false;
        }
    }


    /**
     * Create a new list of cars from an existing
     * list of cars using fitness proportional selection
     */
    public ArrayList<Car> fps(ArrayList<Car> carList) {
        ArrayList<Car> newCars = new ArrayList<Car>();
        Collections.sort(carList); //0 is the worst, carList.size()-1 is the best
        float fitnesSum = 0;
        for (Car car : carList) {
            fitnesSum += car.fitness;
        }
        for (int i = 0; i < carList.size(); i++) {
            float randomFloat = random.nextInt((int) fitnesSum);
            for (Car car : carList) {
                if (randomFloat < car.fitness) {
                    newCars.add(car.clone(this.carStartPosition.get(), true));
                    break;
                } else {
                    randomFloat -= car.fitness;
                }
            }
        }
        if (newCars.size() != carList.size()) {
            System.exit(999);
        }

        return newCars;
    }

    /**
     * randomize a portion of all the weights of the NN
     */
    public void mutation(ArrayList<Car> carList, float mutationRate) {
        for (Car car : carList) {
            for (Layer layer : car.nn.getLayers()) {
                for (Neuron neuron : layer.getNeurons()) {
                    for (Weight weight : neuron.getWeights()) {
                        if (random.nextFloat() < mutationRate) {
                            weight.randomize();
                        }
                    }
                }
            }
        }
    }


}
