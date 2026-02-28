package app;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.Vector;
import java.util.concurrent.SynchronousQueue;

public abstract class SolutionFinderConfig {
    public SynchronousQueue<Vector<Long>> queue;
    SolutionFinderConfig(SynchronousQueue<Vector<Long>> queue){
        this.queue = queue;
    }
    public abstract String getPrimaryNextLayerRule();

    public abstract String getSecondaryNextLayerRule();

    public abstract boolean shouldCreateImages();

    public abstract boolean shouldCreateStatistics();

    public abstract Vector<Coordinate> getMostConstrainedMatrix();

    public abstract String getCoordinateMode();
}
