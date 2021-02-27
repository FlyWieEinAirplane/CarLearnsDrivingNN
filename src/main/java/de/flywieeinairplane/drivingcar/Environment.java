package de.flywieeinairplane.drivingcar;

import java.io.Serializable;
import java.util.ArrayList;

public class Environment implements Serializable {
    public ArrayList<Obstacle> obstacleList;
    public ArrayList<Checkpoint> checkpointList;

    public Environment(ArrayList<Obstacle> obstacleList, ArrayList<Checkpoint> checkpointList) {
        this.obstacleList = obstacleList;
        this.checkpointList = checkpointList;
    }
}
