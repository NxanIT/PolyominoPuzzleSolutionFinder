package app;

import javafx.scene.control.ProgressBar;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class LayeredBrick {
    private Hashtable<Integer, Vector<Pair<Integer, Integer>>> Brick;
    
    LayeredBrick(){
        Brick = new Hashtable<>();
    }
    
    LayeredBrick(Hashtable<Integer, Vector<Pair<Integer, Integer>>> Brick){
        this.Brick = Brick;
    }
    
    boolean containsLayer(int layer){
        return Brick.containsKey(layer);
    }
    
    void storeLayer(int layer, Vector<Pair<Integer, Integer>> Data){
        Brick.put(layer, Data);
    }

    Vector<Pair<Integer, Integer>> getLayer(int layer){
        return Brick.get(layer);
    }
    
    String repr(boolean BrickIsMap){
        if(BrickIsMap){
            return getRepresentationAsMap();
        }
        return getRepresentationAsBrick();
    }
    
    private String getRepresentationAsMap(){
        StringBuilder MapFileText = new StringBuilder();
        Enumeration<Integer> layers = Brick.keys();
        while (layers.hasMoreElements()){
            int layer = layers.nextElement();
            MapFileText.append("Layer").append(layer).append("-");
            for(Pair<Integer,Integer> coordinate : Brick.get(layer)){
                MapFileText.append("(").append(coordinate.getKey())
                        .append("/").append(coordinate.getValue()).append("),");
            }
            MapFileText.deleteCharAt(MapFileText.lastIndexOf(","));
            MapFileText.append("\n");
        }
        return MapFileText.toString();
    }

    private String getRepresentationAsBrick() throws RuntimeException{
        StringBuilder inputBuffer = new StringBuilder();
        inputBuffer.append("Brick:");

        boolean flag_brickEmpty = true;
        Enumeration<Integer> layers = Brick.keys();
        while (layers.hasMoreElements()){
            int layer = layers.nextElement();
            Vector<Pair<Integer, Integer>> xy_coordinates = Brick.get(layer);
            if(xy_coordinates.isEmpty()){
                continue;
            }
            flag_brickEmpty = false;
            for(Pair<Integer,Integer> xy_coordinate : xy_coordinates){
                inputBuffer.append("(").append(xy_coordinate.getKey())
                        .append("/").append(xy_coordinate.getValue())
                        .append("/").append(layer).append("),");
            }
            inputBuffer.deleteCharAt(inputBuffer.lastIndexOf(","));

        }
        if(flag_brickEmpty){
            throw new RuntimeException("Brick contains no data.");
        }
        return inputBuffer.toString();
    }

    Pair<Integer,Integer> getDimensions() {
        
        int xm = 0;
        int ym = 0;
        Enumeration<Integer> keys = Brick.keys();
        while (keys.hasMoreElements()){
            Vector<Pair<Integer, Integer>> layer = Brick.get(keys.nextElement());

            Optional<Pair<Integer, Integer>> max_x = layer.stream().max(Comparator.comparingInt(Pair::getKey));
            Optional<Pair<Integer, Integer>> max_y = layer.stream().max(Comparator.comparingInt(Pair::getValue));
            if(max_x.isPresent()){
                xm = Integer.max(xm,max_x.get().getKey()+1);
            }
            if(max_y.isPresent()){
                ym = Integer.max(ym,max_y.get().getValue()+1);
            }
        }
        return new Pair<>(xm,ym);
    }
    
    void printLayer(int layer) {
        Vector<Pair<Integer,Integer>> Layer = Brick.get(layer);
        System.out.print("Layer:" + layer + " - ");
        for(Pair<Integer,Integer> coordinate : Layer){
            System.out.print("(" + coordinate.getKey() + ", " + coordinate.getValue() + "), ");
        }
    }
    
    
}
