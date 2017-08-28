package de.flywieeinairplane.drivingcar;

import java.util.ArrayList;

/**
 * Created by cornelius on 28.08.17.
 */
public class Generation {
    int number;
    float maxDistanceTraveled = 0; //number of updates
    float maxScore = 0; //x-value
    float maxFitnes = 0;
    float averageDistanceTraveled = 0;
    float averageScore = 0;
    float averageFitnes = 0;
    int numberOfCars = 0;

    public Generation(int number) {
        this.number = number;
    }

    public void extractInformation(ArrayList<Car> carList) {
        float scoreSum = 0;
        float distanceTraveledSum = 0;
        float fitnesSum = 0;
        numberOfCars = carList.size();
        for (Car car : carList) {
            if (car.distanceTraveled > this.maxDistanceTraveled) {
                this.maxDistanceTraveled = car.distanceTraveled;
            }
            if (car.position.x > this.maxScore) {
                this.maxScore = car.position.x;
            }
            if (car.fitnes > this.maxFitnes) {
                this.maxFitnes = car.fitnes;
            }
            distanceTraveledSum +=car.distanceTraveled;
            scoreSum += car.position.x;
            fitnesSum += car.fitnes;
        }
        averageDistanceTraveled = distanceTraveledSum / numberOfCars;
        averageScore = scoreSum / numberOfCars;
        averageFitnes = fitnesSum / numberOfCars;
    }

    public void printStats() {
        System.out.println("Generation number "+this.number + " with " + numberOfCars + " cars");
        System.out.println("Maximum distance traveled "+maxDistanceTraveled);
        System.out.println("Average distance traveled "+averageDistanceTraveled);
        System.out.println("Maximum score "+maxScore);
        System.out.println("Average score "+averageScore);
        System.out.println("Maximum fitnes "+maxFitnes);
        System.out.println("Average fitnes "+averageFitnes);
    }
}
