package app;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.util.Pair;

import java.util.Vector;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UpdateSolutionFinderProgress extends Thread {
    private ProgressBar progressBar;
    private Button StartButton;
    private Button CancelButton;
    private SynchronousQueue<Vector<Long>> queue;
    private Thread CS_thread;
    private LineChart<Number,Number> lineChart;
    private int firstLayerMatches;
    UpdateSolutionFinderProgress(ProgressBar progressBar, 
                                 SynchronousQueue<Vector<Long>> queue, 
                                 Thread CS_thread,
                                 Button StartButton,
                                 Button CancelButton,
                                 LineChart<Number,Number> lineChart,
                                 int firstLayerMatches){
        this.progressBar = progressBar;
        this.queue = queue;
        this.CS_thread = CS_thread;
        this.StartButton = StartButton;
        this.CancelButton = CancelButton;
        this.lineChart = lineChart;
        this.firstLayerMatches = firstLayerMatches;
    }
    
    @Override
    public void run(){
        try {
            if(queue!=null){
                Vector<Long> CurrentLayerStats;
                do{
                        CurrentLayerStats = queue.poll(1, TimeUnit.SECONDS);
                        if(CurrentLayerStats!=null){
                            progressBar.setProgress((double) CurrentLayerStats.get(1)/firstLayerMatches);
                            Vector<Long> finalCurrentLayerStats = CurrentLayerStats;
                            Platform.runLater(() -> updateLineChart(finalCurrentLayerStats));
                        }
                        
                } while (CS_thread.isAlive());
            }
       
            
        } catch (InterruptedException e) {
            //is interrupted when cancel button is pressed
        }
        StartButton.setDisable(false);
        CancelButton.setDisable(true);
        progressBar.setProgress(0);
        System.out.println("progressbar thread finished.");
    }

    private void updateLineChart(Vector<Long> CurrentLayerStats){
        XYChart.Series<Number,Number> currentData = new XYChart.Series<>();
        for(int i=0;i<CurrentLayerStats.size();i++){
            currentData.getData().add(new XYChart.Data<>(i,CurrentLayerStats.get(i)));
        }
        ObservableList<XYChart.Series<Number, Number>> lineData = lineChart.getData();
        lineData.add(currentData);
        if(lineData.size()>5){
            lineData.removeFirst();
        }
        

        
        
    }
}
