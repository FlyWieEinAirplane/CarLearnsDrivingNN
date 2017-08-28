package de.flywieeinairplane.drivingcar;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;
import processing.core.PApplet;
import processing.core.PVector;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

public class World extends PApplet {
    int heigt = 900;
    int width = 1600;
    ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
    ArrayList<Car> carList = new ArrayList<Car>();
    PVector startPosition = new PVector(30, 85);

    ArrayList<Generation> generationList = new ArrayList<Generation>();
    private int numberOfCars = 10;
    Random random = new Random();


    public World() {
//        Routine to save changes of Obstacles
//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            public void run() {
//                try {
//                    FileOutputStream fos = new FileOutputStream("obstalcleList");
//                    ObjectOutputStream oos = new ObjectOutputStream(fos);
//                    oos.writeObject(obstacleList);
//                    oos.close();
//                    fos.close();
//                    System.out.println("Obstacles saved to file: " + obstacleList.size() + " Obstacles");
//                } catch (IOException ioe) {
//                    ioe.printStackTrace();
//                }
//            }
//        }));
    }

    public void setup() {
//        Load Obstacle List
        try {
            FileInputStream fis = new FileInputStream("obstalcleList");
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.obstacleList = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
            System.out.println("Obstacles loaded from file: " + obstacleList.size() + " Obstacles");
        } catch (FileNotFoundException e) {
            System.out.println("No File found: start with a blanc map");
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
        frameRate(100);

//        create Cars
        for (int i = 0; i < numberOfCars; i++) {
            carList.add(new Car(startPosition.get(), 5, this));
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

//        Draw Cars
        fill(0, 255, 0);
        ArrayList<Car> carFinishedList = new ArrayList<Car>();
        for (Car car : carList) {
            car.updatePosition();

            if (!car.crashed) {
//            draw sensors
                /*
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

//            Draw Car body
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
                */
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

    public void mouseClicked() {
//        this.obstacleList.add(new Obstacle(new PVector(mouseX, mouseY), 30));
    }


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
                    newCars.add(car.clone(this.startPosition.get(), true));
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
