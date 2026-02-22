package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Vector;

public class MapViewController {

    private Hashtable<Integer, Vector<Pair<Integer,Integer>>> Map;
    private Rectangle[][] cell_array;
    
    public boolean was_successful = false;
    
    public void initMapView(Hashtable<Integer, Vector<Pair<Integer,Integer>>> Map,boolean CoordinateMode2d){
        this.Map = Map;
        buttonLayerUp.setDisable(CoordinateMode2d);
        buttonLayerDown.setDisable(CoordinateMode2d);
    }
    
    public Hashtable<Integer, Vector<Pair<Integer,Integer>>> getMap(){
        return Map;
    }

    @FXML
    private TextField brickHeight;

    @FXML
    private TextField brickLayerViewed;

    @FXML
    private StackPane brickStackPane;

    @FXML
    private TextField brickWidth;
    
    @FXML
    private Button buttonLayerUp;
    
    @FXML
    private Button buttonLayerDown;

    @FXML
    void cancelConfiguration(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void decreaseLayerViewed(ActionEvent event) {
        int layer = Integer.parseInt(brickLayerViewed.getText());
        //store data in Map
        Map.put(layer,getCellsFilledLayer());
        //switch to new Layer
        brickLayerViewed.setText(Integer.toString(--layer));
        updateGrid(layer);
    }

    @FXML
    void increaseLayerViewed(ActionEvent event) {
        int layer = Integer.parseInt(brickLayerViewed.getText());
        //store data in Map
        Map.put(layer,getCellsFilledLayer());
        //switch to new Layer
        brickLayerViewed.setText(Integer.toString(++layer));
        updateGrid(layer);
    }
    
    private Vector<Pair<Integer,Integer>> getCellsFilledLayer(){
        Vector<Pair<Integer,Integer>> cellsFilled = new Vector<>();
        for(int i=0;i<cell_array.length;i++){
            for(int j=0;j<cell_array[i].length;j++){
                if(cell_array[i][j].getFill().equals(Paint.valueOf("0x000000"))){
                    cellsFilled.add(new Pair<>(i,j));
                }
            }
        }
        return cellsFilled;
    }
    
    private void initGrid(){
        //get maximal dimensions as per Hashtable - cell_array-dimension is max of this with default values
        Pair<Integer,Integer> MapDimensions = getMapDimensions();
        int width = Integer.max(Integer.parseInt(brickWidth.getText()),MapDimensions.getKey());
        int height = Integer.max(Integer.parseInt(brickHeight.getText()),MapDimensions.getValue());
        brickWidth.setText(Integer.toString(width));
        brickHeight.setText(Integer.toString(height));
        
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                
            }
        }
    }
    
    private Pair<Integer,Integer> getMapDimensions() {
        int xm = 0;
        int ym = 0;
        Enumeration<Integer> keys = Map.keys();
        while (keys.hasMoreElements()){
            Vector<Pair<Integer, Integer>> layer = Map.get(keys.nextElement());
            
            Optional<Pair<Integer, Integer>> max_x = layer.stream().max((p1, p2) -> p1.getKey()-p2.getKey());
            Optional<Pair<Integer, Integer>> max_y = layer.stream().max((p1, p2) -> p1.getValue()-p2.getValue());
            if(max_x.isPresent()){
                xm = Integer.max(xm,max_x.get().getKey());
            }
            if(max_y.isPresent()){
                ym = Integer.max(ym,max_y.get().getValue());
            }
        }
        return new Pair<>(xm,ym);
    }
    
    private void updateGrid(int Layer){
        //updates Fill-value of cells corresponding to entries saved for the specified layer
        clearGrid();
        if(!Map.containsKey(Layer)){
            return;
        }
        Vector<Pair<Integer,Integer>> LayerMap = Map.get(Layer);
        clearGrid();
        for(Pair<Integer,Integer> Coord : LayerMap){
            cell_array[Coord.getKey()][Coord.getValue()].setFill(Paint.valueOf("0x000000"));
        }
        
    }
    
    private void clearGrid(){
        for(Rectangle[] row : cell_array){
            for(Rectangle cell : row){
                cell.setFill(Paint.valueOf("0xffffff"));
            }
        }
    }

    @FXML
    void saveConfiguration(ActionEvent event) {
        was_successful = true;
        closeWindow();
    }

    @FXML
    void updateGrid(ActionEvent event) {

    }
    
    private void closeWindow(){
        Stage s = (Stage) brickHeight.getScene().getWindow();
        s.close();
    }



}
