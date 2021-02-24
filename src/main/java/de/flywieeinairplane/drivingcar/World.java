package de.flywieeinairplane.drivingcar;

import org.neuroph.core.Layer;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.util.TransferFunctionType;
import processing.core.PApplet;
import processing.core.PVector;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class World extends PApplet {
    private ArrayList<Generation> generationList = new ArrayList<Generation>();
    Random random = new Random();
    int heigt = 900;
    int width = 1600;

    ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
    ArrayList<Car> carList = new ArrayList<Car>();
    PVector carStartPosition = new PVector(30, 85);


    //    EDIT
    private int generationSize = 10;
    private boolean drawCar = true;
    private boolean drawSensors = false;
    private boolean drawCourseMode = false;
    static final TransferFunctionType GLOBAL_TRANSFER_FUNCTION = TransferFunctionType.SIGMOID;
    static final Integer[] NEURON_LAYER_DEFINITION = new Integer[]{4, 12, 12, 3};


    public World() {
        if (drawCourseMode) {
            System.out.println("Click on the world to create custom obstacles and create a racing track");
//          Routine to save changes of Obstacles
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("obstalcleList"))) {
                    oos.writeObject(obstacleList);
                    System.out.println("Obstacles saved to file: " + obstacleList.size() + " Obstacles");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }));
        }
    }

    public void setup() {
        if (!drawCourseMode) {
//        Load Obstacle List
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("obstalcleList"))) {
                this.obstacleList = (ArrayList) ois.readObject();
                System.out.println("Obstacles loaded from file: " + obstacleList.size() + " Obstacles");
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
        }

//        setup frame
        size(this.width, this.heigt);
        frameRate(60);

//        create Cars
        for (int i = 0; i < generationSize; i++) {
            carList.add(new Car(carStartPosition.get(), 5, this));
        }
    }

    public void draw() {
        background(50);
        noStroke();

//        Draw Obstacles
        fill(255);
        for (Obstacle obstacle :
                obstacleList) {
            ellipse(obstacle.position.x, obstacle.position.y, obstacle.radius * 2, obstacle.radius * 2);
        }

        if (drawCourseMode) {
//            do not continue drawing anything else if we just want to create a new course
            return;
        }
//        Draw Cars
        fill(0, 255, 0);
        ArrayList<Car> carFinishedList = new ArrayList<Car>();
        for (Car car : carList) {
            car.updatePosition();

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
    public void mouseClicked() {
        if (drawCourseMode) {
            this.obstacleList.add(new Obstacle(new PVector(mouseX, mouseY), 30));
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
            fitnesSum += car.fitnes;
        }
        for (int i = 0; i < carList.size(); i++) {
            float randomFloat = random.nextInt((int) fitnesSum);
            for (Car car : carList) {
                if (randomFloat < car.fitnes) {
                    newCars.add(car.clone(this.carStartPosition.get(), true));
                    break;
                } else {
                    randomFloat -= car.fitnes;
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
        ArrayList<Car> newCars = new ArrayList<Car>();
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
