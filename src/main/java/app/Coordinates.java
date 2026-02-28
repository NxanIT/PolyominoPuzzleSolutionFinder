package app;

import javafx.util.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Coordinates {
    private Vector<Coordinate> Coordinates;
    
    Coordinates(){
        Coordinates = new Vector<>();
    }
    
    void printCoords(){
        for(Coordinate coordinate : Coordinates) {
            System.out.print("C(" + coordinate.getX() + "," + coordinate.getY() + "," + coordinate.getZ() + "),");
        }
        System.out.println();
    }
    
    Vector<Pair<Integer,Integer>> getFiber(int z_value){
        Vector<Pair<Integer,Integer>> Fiber = new Vector<>();
//        Coordinates.stream().map( coordinate -> {
//            if(coordinate.z_equals(z_value)){
//                Fiber.add(new Pair(coordinate.getX(),coordinate.getY()));
//            }
//            System.out.println("fiber at.." + coordinate.getZ());
//            return Fiber;
//        });
        for(Coordinate coordinate : Coordinates){
            if(coordinate.z_equals(z_value)){
                Fiber.add(new Pair<>(coordinate.getX(),coordinate.getY()));
            }
        }
        return Fiber;
    }
    
    void add(Coordinate coordinate){
        Coordinates.add(coordinate);
    }
    
    HashSet<Integer> getZLayers() {
        HashSet<Integer> z_values = new HashSet<>();
        for(Coordinate coordinate : Coordinates){
            z_values.add(coordinate.getZ());
        }
        return z_values;
    }
}


