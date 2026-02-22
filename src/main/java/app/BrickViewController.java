package app;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;


import java.util.*;

public class BrickViewController {
    /* Controller for adding/editing bricks, creating/editing map

     */

    private int defaultBrickColor;
    private int brickColor;
    public boolean was_successful = false;

    private Hashtable<Integer, Vector<Pair<Integer,Integer>>> Brick;
    Rectangle[][] cell_array;
    Rectangle backColorRectangle;

    void setBrickViewVariables(int brickID,
                               int defaultBrickColor,
                               Hashtable<Integer, Vector<Pair<Integer,Integer>>> Brick,
                               boolean CoordinateModeIs2D,
                               boolean BrickIsMap){
        
        this.defaultBrickColor = defaultBrickColor;
        this.brickColor = defaultBrickColor;
        this.Brick = Brick;

        TextBrickID.setText(Integer.toString(brickID));
        customColorPicker.setValue(intToColor(defaultBrickColor));
        setupGrid();

        if(CoordinateModeIs2D){
            setup2DMode();
        }
        if(BrickIsMap){ //disables custom color option and changes Text
            setupMapView();
        }
    }

    private void setupMapView(){
        TextConfigureBrick.setText("Configure Map");
        TextBeforeBrickID.setVisible(false);
        TextBrickID.setVisible(false);
        selectCustomColor.setVisible(false);
        customColorPicker.setVisible(false);
    }

    private void setup2DMode(){
        buttonLayerUp.setDisable(true);
        buttonLayerDown.setDisable(true);
        brickLayerViewed.setDisable(true);
    }

    private void setupGrid(){

        //get maximal dimensions as per Hashtable - cell_array-dimension is max of this with default values
        Pair<Integer,Integer> MapDimensions = getMapDimensions();
        int width = Integer.max(Integer.parseInt(brickWidth.getText()),MapDimensions.getKey());
        int height = Integer.max(Integer.parseInt(brickHeight.getText()),MapDimensions.getValue());
        brickWidth.setText(Integer.toString(width));
        brickHeight.setText(Integer.toString(height));

        backColorRectangle = new Rectangle(0, 0, intToPaint(brickColor)); //resizing is done in makeGrid
        makeGrid(width,height);

    }

    private Pair<Integer,Integer> getMapDimensions() {
        if(cell_array!=null){//initialisation process is finished
            storeCurrentLayer();
        }
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

    private void makeGrid(int width, int height){
        //inspired from: https://github.com/RonenLes/Minesweeper/
        int cellSpacing = 2;
        int rectangleSize = getCellSize(width, height, cellSpacing);

        int GridPaneTotalWidth = width*rectangleSize + (width+1)*cellSpacing;
        int GridPaneTotalHeight = height*rectangleSize + (height+1)*cellSpacing;
        GridPane board = new GridPane(GridPaneTotalWidth,GridPaneTotalHeight);
        board.setPadding(new Insets(cellSpacing));

        board.setHgap(cellSpacing);
        board.setVgap(cellSpacing);

        Rectangle[][] cell_array = new Rectangle[width][height];

        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++) {
                Rectangle cell = new Rectangle(rectangleSize,rectangleSize,Paint.valueOf("0xffffff"));
                cell_array[i][j] = cell;
                cell.setOnMouseClicked(new mouseClick(cell));
                board.add(cell, i, j);

            }
        }
        this.cell_array = cell_array;
        StackPane.setAlignment(board, Pos.CENTER_LEFT);//TODO: this seems to do nothing

        brickStackPane.getChildren().clear();  //delete previous board
        backColorRectangle.setFill(intToPaint(brickColor));
        backColorRectangle.setWidth(GridPaneTotalWidth);
        backColorRectangle.setHeight(GridPaneTotalHeight);

        StackPane.setAlignment(backColorRectangle, Pos.CENTER_LEFT);
        brickStackPane.getChildren().add(backColorRectangle);
        brickStackPane.getChildren().add(board); //add the new board
        
        loadLayer(getCurrentLayerIndex());
    }

    private int getCellSize(int width, int height, int cellSpacing) {
        int StackPaneTotalWidth = (int) brickStackPane.getWidth();
        int StackPaneTotalHeight = (int) brickStackPane.getHeight();
        //At initialization the brickStackPane (width, height) are still (0,0) -> use pref dimensions
        StackPaneTotalWidth = StackPaneTotalWidth==0 ? (int) brickStackPane.getPrefWidth() : StackPaneTotalWidth;
        StackPaneTotalHeight = StackPaneTotalHeight==0 ? (int) brickStackPane.getPrefHeight() : StackPaneTotalHeight;

        return Math.min((StackPaneTotalWidth- cellSpacing *(width -1))/ width,
                (StackPaneTotalHeight- cellSpacing *(height -1))/ height);
    }

    @FXML
    private Label TextConfigureBrick;

    @FXML
    private Label TextBeforeBrickID;

    @FXML
    private Text TextBrickID;

    @FXML
    private TextField brickHeight;

    @FXML
    private TextField brickLayerViewed;

    @FXML
    private TextField brickWidth;

    @FXML
    private ColorPicker customColorPicker;

    @FXML
    private CheckBox selectCustomColor;

    @FXML
    private StackPane brickStackPane;

    @FXML
    private Button buttonLayerUp;

    @FXML
    private Button buttonLayerDown;
    
    @FXML
    void updateCustomColorEnabled(ActionEvent event) {
        boolean isDisabled = !selectCustomColor.isSelected();
        customColorPicker.setDisable(isDisabled);
        if(isDisabled){
            brickColor = defaultBrickColor;
            Color defaultColor = intToColor(defaultBrickColor);
            customColorPicker.setValue(defaultColor);
            updateBrickColors(defaultColor);
        }
    }

    @FXML
    void increaseLayerViewed(ActionEvent e){
        storeCurrentLayer();
        //switch to new Layer
        int layer = getCurrentLayerIndex();
        brickLayerViewed.setText(Integer.toString(++layer));
        loadLayer(layer);
    }

    @FXML
    void decreaseLayerViewed(ActionEvent e){
        storeCurrentLayer();
        //switch to new layer
        int layer = getCurrentLayerIndex();
        brickLayerViewed.setText(Integer.toString(--layer));
        loadLayer(layer);
    }
    
    private void storeCurrentLayer(){
        int layer = getCurrentLayerIndex();
        //store data in Brick
        Brick.put(layer, getCellsFilledCurrentLayer());
    }
    
    private int getCurrentLayerIndex(){
        return Integer.parseInt(brickLayerViewed.getText());
    }

    private Vector<Pair<Integer,Integer>> getCellsFilledCurrentLayer(){
        Vector<Pair<Integer,Integer>> cellsFilled = new Vector<>();
        for(int i=0;i<cell_array.length;i++){
            for(int j=0;j<cell_array[i].length;j++){
                if(Cell_isFilled(cell_array[i][j])){
                    cellsFilled.add(new Pair<>(i,j));
                }
            }
        }
        return cellsFilled;
    }

    private boolean Cell_isFilled(Rectangle cell){
        return !cell.getFill().equals(Paint.valueOf("0xffffff"));
    }

    private void loadLayer(int Layer){
        //updates Fill-value of cells corresponding to entries saved for the specified layer
        clearGrid();
        if(!Brick.containsKey(Layer)){//no data yet
            return;
        }
        Vector<Pair<Integer,Integer>> LayerMap = Brick.get(Layer);
        for(Pair<Integer,Integer> Coordinate : LayerMap){//fill cells whose indices are in LayerMap with brick-color
            cell_array[Coordinate.getKey()][Coordinate.getValue()].setFill(intToPaint(brickColor));
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
    int getBrickHeight() {
        int min_height = getMapDimensions().getValue();
        return Integer.max(min_height,Integer.parseInt(brickHeight.getText()));
    }

    @FXML
    int getBrickWidth() {
        int min_width = getMapDimensions().getKey();
        return Integer.max(min_width,Integer.parseInt(brickWidth.getText()));
    }

    @FXML
    void updateColor(ActionEvent event){
        Color color = customColorPicker.getValue();
        updateBrickColors(color);
    }

    private void updateBrickColors(Color color){
        int r = (int) (color.getRed()*255);
        int g = (int) (color.getGreen()*255);
        int b = (int) (color.getBlue()*255);
        String hex_code = String.format("0x%02x%02x%02x",r, g, b);
        
        brickColor = Integer.valueOf(hex_code.substring(2),16);
        Paint paint = Paint.valueOf(hex_code);
        backColorRectangle.setFill(paint);

        for (Rectangle[] rectangles : cell_array) {
            for (Rectangle cell : rectangles) {
                if (Cell_isFilled(cell)) {
                    cell.setFill(paint);
                }
            }
        }
    }

    @FXML
    void updateGridDimensions(ActionEvent event) {
        int width = getBrickWidth();
        int height = getBrickHeight();
        brickWidth.setText(Integer.toString(width));
        brickHeight.setText(Integer.toString(height));
        
        storeCurrentLayer();
        makeGrid(width,height);
    }
    
    @FXML
    void cancelConfiguration(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void saveConfiguration(ActionEvent event) {
        //update Hashtable
        //TODO add 3D compatibility
        Vector<Pair<Integer,Integer>> currentLayer = getCellsFilledCurrentLayer();
        
        Brick.put(getCurrentLayerIndex(),currentLayer);
        was_successful = true;
        closeWindow();
    }

    private void closeWindow(){
        Stage s = (Stage) TextBrickID.getScene().getWindow();
        s.close();
    }



    

    private Color intToColor(int value){
        return Color.rgb((value>>16)%256,(value>>8)%256,value % 256);
    }

    private Paint intToPaint(int value) {
        return Paint.valueOf("0x" + String.format("%06x",value));
    }

    class mouseClick implements EventHandler<MouseEvent>{
        //inspired from: https://github.com/RonenLes/Minesweeper/
        private final Rectangle cell;


        public mouseClick(Rectangle cell) {
            this.cell= cell;
        }

        @Override
        public void handle(MouseEvent e) {

            //handle left click
            if (e.getButton() == MouseButton.PRIMARY) {
                cell.setFill(intToPaint(brickColor));
            //handle right click
            } else if (e.getButton() == MouseButton.SECONDARY) {
                cell.setFill(Paint.valueOf("0xffffff"));
            }
        }
    }

    public Integer getColor(){
        return brickColor;
    }

    public Hashtable<Integer,Vector<Pair<Integer,Integer>>> getBrick(){
        return Brick;
    }

}
