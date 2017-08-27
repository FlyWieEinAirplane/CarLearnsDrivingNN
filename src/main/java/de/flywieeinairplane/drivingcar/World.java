package de.flywieeinairplane.drivingcar;

import processing.core.PApplet;
import processing.core.PVector;

import java.io.*;
import java.util.ArrayList;

public class World extends PApplet {
    int heigt = 900;
    int width = 1600;
    ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
    ArrayList<Car> carList = new ArrayList<Car>();
    PVector startPosition = new PVector(10, 75);

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

//        create Car
        carList.add(new Car(startPosition.get(), 5, this));
    }

    public void draw() {
        background(50);
        noStroke();
//        ellipse(startPosition.x, startPosition.y, 5, 5);

//        Draw Obstacles
        fill(255);
        for (Obstacle obstacle :
                obstacleList) {
            ellipse(obstacle.position.x, obstacle.position.y, obstacle.radius * 2, obstacle.radius * 2);
        }

//        Draw Cars
        fill(0, 255, 0);
        ArrayList<Car> carRemoveList = new ArrayList<Car>();
        for (Car car : carList) {
            car.updatePosition();

            if (!car.crashed) {
//            draw sensors
                stroke(0, 255, 0);
                PVector vector = car.velocity.get();
                vector.setMag(car.sensorFront);
                vector.add(car.position);
                line(car.position.x, car.position.y, vector.x, vector.y);

                vector.rotate(-QUARTER_PI);
                vector.setMag(car.sensorLeft);
                vector.add(car.position);
                line(car.position.x, car.position.y, vector.x, vector.y);

                vector.rotate(QUARTER_PI);
                vector.setMag(car.sensorRight);
                vector.add(car.position);
                line(car.position.x, car.position.y, vector.x, vector.y);

                noStroke();

//            Draw Car body
                float theta = car.velocity.heading() + PI / 2;
                translate(car.position.x, car.position.y);
                rotate(theta);
                beginShape();
                vertex(0, -car.radius * 2);
                vertex(-car.radius, car.radius * 2);
                vertex(car.radius, car.radius * 2);
                endShape(CLOSE);
            } else {
                carRemoveList.add(car);
            }
        }
        for (Car car : carRemoveList) {
            carList.remove(car);
            carList.add(new Car(startPosition.get(), 5, this));
        }
    }

    public void mouseClicked() {
//        this.obstacleList.add(new Obstacle(new PVector(mouseX, mouseY), 30));
    }
}
