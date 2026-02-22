package app;

import SolutionFinder.CalculateSolutions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Pattern;


public class AppController {

    private int numberOfBricks = 0;
    private String ProjectPath;

    public void setupApp(String InitialProjectPath){
        ProjectPath = InitialProjectPath;
        numberOfBricks = getNumberOfStoredBricks();
        setupInfoProjectIsOpen();
    }

    private void setupInfoProjectIsOpen(){
        DisplayCurrentlyOpen.setText(ProjectPath);
        TooltipCurrentlyOpen.setText(ProjectPath);
        setupCoordinateModes();
        initChoiceNextLayerRule();
    }

    private void setupCoordinateModes(){
        String[] modes = {"2D","3D-square","3d-pyramid"};
        SelectPuzzleMode.getItems().clear();
        SelectPuzzleMode.getItems().addAll(modes);
    }

    private void initChoiceNextLayerRule(){
        //gets called by opening or creating Project
        String[] nextLayerRules = {"most constrained", "max-x, max-y",
                "max-y, max-x","max-x, min-y","max-y, min-x", "min-x, max-y",
                "min-y, max-x","min-x, min-y","min-y, min-x"};
        SFChoiceNextLayerRule.getItems().clear();
        SFChoiceNextLayerRule.getItems().addAll(nextLayerRules);

        String[] nextLayerSecondaryRules = {"none","most constrained >= 40% filled",
                "most constrained >= 50% filled","most constrained >= 60% filled","most constrained >= 80% filled"};
        SFChoiceNextLayerSecondaryRule.getItems().clear();
        SFChoiceNextLayerSecondaryRule.getItems().addAll(nextLayerSecondaryRules);
    }

    private int getNumberOfStoredBricks(){
        String content;
        try {
            FileReader BricksFile = new FileReader(ProjectPath + "/PuzzleData/bricks.txt");
            content = BricksFile.readAllAsString();
            BricksFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Pattern brickPattern = Pattern.compile("(^Brick)");

        return (int) brickPattern.matcher(content).results().count();
    }


    @FXML
    private ChoiceBox<String> SelectPuzzleMode;

    @FXML
    private Label DisplayCurrentlyOpen;
    
    @FXML
    private Tooltip TooltipCurrentlyOpen;

    @FXML
    private VBox listOfBricks;


    @FXML
    void createNewPuzzle(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CreatePuzzle-view.fxml"));
        try {
            Parent root1 = fxmlLoader.load();
            CreatePuzzleController controller = fxmlLoader.getController();
            controller.setupInitialPath(ProjectPath);
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.showAndWait();
            if(controller.was_successful){
                ProjectPath = controller.getPathSelected();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        setupInfoProjectIsOpen();
    }
    
    @FXML
    void openPuzzle(ActionEvent event) {
        DirectoryChooser chooseDir = new DirectoryChooser();
        if(ProjectPath!=null){//set initial directory to parent of currentProject, if not null
            int ParentDirEnding = Integer.max(ProjectPath.lastIndexOf("\\"), ProjectPath.lastIndexOf("/"));//this is not save
            chooseDir.setInitialDirectory(new File(ProjectPath.substring(0,ParentDirEnding)));
        }
        File selectedDir = chooseDir.showDialog(new Stage());
        ProjectPath = selectedDir.toPath().toString();
        setupInfoProjectIsOpen();
        numberOfBricks = getNumberOfStoredBricks();
    }
    
    @FXML
    void editMap(ActionEvent event) {
        //TODO: outsource to extra class with added benefit of needed at other instances.
        //TODO: type-checking
        Hashtable<Integer,Vector<Pair<Integer,Integer>>> Map = new Hashtable<>();
        File mapfile = new File(ProjectPath + "/PuzzleData/map.txt");
        try {
            BufferedReader BRMap = new BufferedReader(new FileReader(mapfile));
            String Layer;
            while((Layer = BRMap.readLine()) != null){
                String[] LayerAndCoordinates = Layer.split("-");
                int layerIndex = Integer.parseInt(LayerAndCoordinates[0].substring(5)); //implicitly asserts that start of line is "Layer"
                String[] Coordinates = LayerAndCoordinates[1].split(",");
                Vector<Pair<Integer,Integer>> CoordinateVectorOfLayer = new Vector<>(Coordinates.length);
                for(String Coordinate : Coordinates){
                    int index_of_slash = Coordinate.indexOf("/");
                    int x = Integer.parseInt(Coordinate.substring(Coordinate.indexOf("(")+1,index_of_slash));
                    int y = Integer.parseInt(Coordinate.substring(index_of_slash+1,Coordinate.indexOf(")")));
                    CoordinateVectorOfLayer.add(new Pair<>(x,y));
                }
                Map.put(layerIndex,CoordinateVectorOfLayer);
            }
            BRMap.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } 
        
        //open Map-window, init Controller with Map
        boolean CoordinateModeIs2D = SelectPuzzleMode.getValue().equals("2D");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Brick-view.fxml"));
            Parent root1 = fxmlLoader.load();
            
            BrickViewController controller = fxmlLoader.getController();
            controller.setBrickViewVariables(0,0,Map,CoordinateModeIs2D,true);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.showAndWait();

            if(controller.was_successful){//-> store Map in file
                Hashtable<Integer, Vector<Pair<Integer,Integer>>> NewMap = controller.getBrick();
                
                FileWriter MapFile = new FileWriter(ProjectPath + "/PuzzleData/map.txt");
                StringBuilder MapFileText = new StringBuilder();
                Enumeration<Integer> layers = NewMap.keys();
                while (layers.hasMoreElements()){
                    int layer = layers.nextElement();
                    MapFileText.append("Layer").append(layer).append("-");
                    for(Pair<Integer,Integer> coordinate : NewMap.get(layer)){
                        MapFileText.append("(").append(coordinate.getKey())
                                .append("/").append(coordinate.getValue()).append("),");
                    }
                    MapFileText.deleteCharAt(MapFileText.lastIndexOf(","));
                    MapFileText.append("\n");
                }
                MapFile.write(MapFileText.toString());
                MapFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void viewBricks(ActionEvent event) {
        //open colors.txt, extract data and visualize as list
        Text[] bricks = new Text[numberOfBricks];
        try {
            BufferedReader colorFileBR = new BufferedReader(new FileReader(ProjectPath + "/PuzzleData/colors.txt"));
            String newLine;
            for(int i=0;i<numberOfBricks;i++){
                newLine = colorFileBR.readLine();
                Paint TextColor = Paint.valueOf(newLine);
                Text text = new Text();
                text.setText("Brick " + i);
                text.setFill(TextColor);
                bricks[i] = text;
            }
            colorFileBR.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        listOfBricks.getChildren().clear();
        listOfBricks.getChildren().addAll(bricks);
    }


    private int getDefaultBrickColor(int brickID){
        //sets default colors to bricks - they should never equal 0xffffff
        int[] defaultColors = {0xff0000, 0xffff00, 0x00ff00, 0x00ffff, 0x0000ff, 0xff00ff,
                0xe67300, 0x73e600, 0x00e673, 0x0073e6, 0x7300e6, 0xe60073, 0x5cd6b8, 0x5cd67a, 0x7ad65c,
                0xb8d65c, 0xd6b85c, 0xd67a5c, 0x5cb8d6, 0x5c7ad6, 0x7a5cd6, 0xb85cd6, 0xd65cb8, 0xd65c7a};
        return defaultColors[brickID % defaultColors.length];
    }

    @FXML
    private void addBrick(ActionEvent event) {
        boolean CoordinateModeIs2D = SelectPuzzleMode.getValue().equals("2D");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Brick-view.fxml"));
            Parent root1 = fxmlLoader.load();
            
            BrickViewController controller = fxmlLoader.getController();
            controller.setBrickViewVariables(numberOfBricks,
                    getDefaultBrickColor(numberOfBricks), 
                    new Hashtable<>(),
                    CoordinateModeIs2D,
                    false);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.showAndWait();
            System.out.println(controller.was_successful);
            if(controller.was_successful){
                Hashtable<Integer, Vector<Pair<Integer, Integer>>> newBrick = controller.getBrick();
                int newBrickColor = controller.getColor();
                
                try {
                    replace_nth_rowInFile(ProjectPath + "/PuzzleData/bricks.txt",numberOfBricks,StringRepresentationOfBrick(newBrick));
                    replace_nth_rowInFile(ProjectPath + "/PuzzleData/colors.txt",numberOfBricks,intToHexString(newBrickColor));
                } catch (RuntimeException e) {//brick contains no data
                    System.out.println("was empty");
                    return;//TODO: log instance of empty Brick
                }
                numberOfBricks++;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String intToHexString(int value){
        return String.format("#%02x%02x%02x",(value>>16)%256,(value>>8)%256,value % 256);
    }

    private String StringRepresentationOfBrick(Hashtable<Integer, Vector<Pair<Integer, Integer>>> Brick) throws RuntimeException{
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

    private boolean replace_nth_rowInFile(String Filename,int n,String newLine){
        //inspired from: https://stackoverflow.com/questions/20039980/java-replace-line-in-text-file
        boolean flag_successful = false;
        try {
            // input the (modified) file content to the StringBuffer "input"
            BufferedReader file = new BufferedReader(new FileReader(Filename));
            StringBuilder inputBuffer = new StringBuilder();
            String line;
            int lineNumber = 0;
            while ((line = file.readLine()) != null) {
                if(lineNumber==n){
                    inputBuffer.append(newLine).append("\n");
                    flag_successful = true;
                    continue;
                }
                inputBuffer.append(line).append("\n");
                lineNumber++;
            }
            file.close();
            //we allow for n to be one longer than # of lines in file:
            if(lineNumber==n){
                inputBuffer.append(newLine).append("\n");
                flag_successful = true;
            }

            // write the new string with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream(Filename);
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return flag_successful;
    }

    //-----SOLUTION-FINDER-TAB-----

    @FXML
    private CheckBox SFCreateImages;

    @FXML
    private CheckBox SFCreateStatisticData;
    
    @FXML
    private ChoiceBox<String> SFChoiceNextLayerRule;
    
    @FXML
    private ChoiceBox<String> SFChoiceNextLayerSecondaryRule;
    
    @FXML
    private ProgressBar SFProgressBar;
    
    @FXML
    private void calculateSolutions(ActionEvent event){
        boolean createImages = SFCreateImages.isSelected();
        boolean createStats = SFCreateStatisticData.isSelected();
        String coordinateMode = SelectPuzzleMode.getValue();
        assert coordinateMode.equals("2D") : "Not implemented";
        System.out.println(coordinateMode);
        //TODO: add coordinate mode capability - current solver only works in 2d mode
        //TODO: make new thread that runs this
        //TODO: add warning if this project has already started computation once
        CalculateSolutions.start(ProjectPath,createImages,createStats);
    }
    
    @FXML
    private void cancelCalculateSolutions(ActionEvent event){
        
    }
}
