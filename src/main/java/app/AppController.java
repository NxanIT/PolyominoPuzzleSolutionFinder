package app;

import SolutionFinder.CalculateSolutions;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import zoomInSolution.ZoomInSolution;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class AppController {

    private int numberOfBricks = 0;
    private String ProjectPath;

    public void setupApp(String InitialProjectPath){
        ProjectPath = InitialProjectPath;
        numberOfBricks = getNumberOfStoredBricks();
        setupInfoProjectIsOpen();
        numberOfBricks = getNumberOfStoredBricks();
    }

    private void setupInfoProjectIsOpen(){
        DisplayCurrentlyOpen.setText(ProjectPath);
        TooltipCurrentlyOpen.setText(ProjectPath);
        setupCoordinateModes();
        setupSolutionFinderTab();
        setupViewSolutionsTab();
    }

    private void setupCoordinateModes(){
        String[] modes = {"2D","3D-square","3d-pyramid"};
        SelectPuzzleMode.getItems().clear();
        SelectPuzzleMode.getItems().addAll(modes);
        SelectPuzzleMode.getSelectionModel().selectFirst();
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
        Pattern brickPattern = Pattern.compile("(Brick:)");
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
    private void createNewPuzzle(ActionEvent event) {
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
    private void openPuzzle(ActionEvent event) {
        DirectoryChooser chooseDir = new DirectoryChooser();
        if(ProjectPath!=null){//set initial directory to parent of currentProject, if not null
            int ParentDirEnding = Integer.max(ProjectPath.lastIndexOf("\\"), ProjectPath.lastIndexOf("/"));//this is not save
            chooseDir.setInitialDirectory(new File(ProjectPath.substring(0,ParentDirEnding)));
        }
        File selectedDir = chooseDir.showDialog(new Stage());
        if(!selectedDir.exists()){
            return;
        }
        ProjectPath = selectedDir.toPath().toString();
        setupInfoProjectIsOpen();
        numberOfBricks = getNumberOfStoredBricks();
    }
    
    @FXML
    private void editMap(ActionEvent event) {
        LayeredBrick Map = loadMapFromFile();
        LayeredBrick editedMap = openBrickWindow(0,0,Map,true).getKey();
        if(editedMap != null){//-> store Map in file
            try {
                FileWriter MapFile = new FileWriter(ProjectPath + "/PuzzleData/map.txt");
                MapFile.write(editedMap.repr(true));
                MapFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private LayeredBrick loadMapFromFile(){
        //TODO: outsource to extra class with added benefit of needed at other instances.
        //TODO: type-checking
        Hashtable<Integer, Vector<Pair<Integer, Integer>>> loadedBrick = new Hashtable<>();
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
                loadedBrick.put(layerIndex,CoordinateVectorOfLayer);
            }
            BRMap.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new LayeredBrick(loadedBrick);
    }

    private Pair<LayeredBrick,Integer> openBrickWindow(int brickID,
                                                       int defaultBrickColor,
                                                       LayeredBrick Brick,
                                                       boolean BrickIsMap){
        boolean CoordinateModeIs2D = SelectPuzzleMode.getValue().equals("2D");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Brick-view.fxml"));

        try {
            Parent root1 = fxmlLoader.load();
            BrickViewController controller = fxmlLoader.getController();
            controller.setBrickViewVariables(brickID, defaultBrickColor, Brick, CoordinateModeIs2D, BrickIsMap);

            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.showAndWait();
            if(controller.was_successful){
                LayeredBrick newBrick = controller.getBrick();
                int newBrickColor = controller.getColor();
                return new Pair<>(newBrick, newBrickColor);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Pair<>(null,null);
    }


    @FXML
    private void viewBricks(ActionEvent event) {
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
                text.setOnMouseClicked(new BrickMouseClick(i));
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

    class BrickMouseClick implements EventHandler<MouseEvent> {
        private final int brickIndex;

        BrickMouseClick(int brickIndex){
            this.brickIndex = brickIndex;
        }

        @Override
        public void handle(MouseEvent event){
            if (event.getButton() == MouseButton.PRIMARY) {
                int BrickColor = loadColorFromFile(brickIndex);
                LayeredBrick Brick = loadBrickFromFile(brickIndex);

                Pair<LayeredBrick,Integer> edited = openBrickWindow(brickIndex,BrickColor,Brick,false);
                LayeredBrick editedBrick = edited.getKey();
                int editedBrickColor = edited.getValue();
                try {
                    replace_nth_rowInFile(ProjectPath + "/PuzzleData/bricks.txt",brickIndex,editedBrick.repr(false));
                    replace_nth_rowInFile(ProjectPath + "/PuzzleData/colors.txt",brickIndex,intToHexString(editedBrickColor));
                } catch (RuntimeException e) {//brick contains no data
                    System.out.println("was empty");
                    //TODO: log instance of empty Brick
                }
            }
        }
    }

    private int loadColorFromFile(int brickIndex){
        try {
            BufferedReader BRMap = new BufferedReader(new FileReader(ProjectPath + "/PuzzleData/colors.txt"));
            String colorAsString = BRMap.readAllLines().get(brickIndex);
            BRMap.close();
            return Integer.parseInt(colorAsString.substring(1),16);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private LayeredBrick loadBrickFromFile(int brickIndex){
        Hashtable<Integer, Vector<Pair<Integer, Integer>>> loadedBrick = new Hashtable<>();
        File mapfile = new File(ProjectPath + "/PuzzleData/bricks.txt");

        String thisBrickText;
        try {
            FileReader FRMap = new FileReader(mapfile);
            thisBrickText = FRMap.readAllLines().get(brickIndex);
            FRMap.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Coordinates AllCoordinates = new Coordinates();
        String CoordinatesString = thisBrickText.substring(thisBrickText.indexOf(":")+1);
        String[] Coordinates = CoordinatesString.split(",");

        for(String Coordinate : Coordinates){
            int index_first_slash = Coordinate.indexOf("/");
            int index_second_slash = Coordinate.indexOf("/",index_first_slash+1);
            int x = Integer.parseInt(Coordinate.substring(Coordinate.indexOf("(")+1,index_first_slash));
            int y = Integer.parseInt(Coordinate.substring(index_first_slash+1,index_second_slash));
            int z = Integer.parseInt(Coordinate.substring(index_second_slash+1,Coordinate.indexOf(")")));
            AllCoordinates.add(new Coordinate(x,y,z));
        }

        for(int z_value : AllCoordinates.getZLayers()){
            loadedBrick.put(z_value,AllCoordinates.getFiber(z_value));
        }

        return new LayeredBrick(loadedBrick);
    }



    @FXML
    private void addBrick(ActionEvent event) {
        Pair<LayeredBrick,Integer> added = openBrickWindow(numberOfBricks,
                getDefaultBrickColor(numberOfBricks),
                new LayeredBrick(),
                false);
        LayeredBrick addedBrick = added.getKey();
        int addedBrickColor = added.getValue();

        if(addedBrick != null){
            try {
                replace_nth_rowInFile(ProjectPath + "/PuzzleData/bricks.txt",numberOfBricks,addedBrick.repr(false));
                replace_nth_rowInFile(ProjectPath + "/PuzzleData/colors.txt",numberOfBricks,intToHexString(addedBrickColor));
            } catch (RuntimeException e) {//brick contains no data
                System.out.println("was empty");
                return;//TODO: log instance of empty Brick
            }
            numberOfBricks++;
        }
    }

    private String intToHexString(int value){
        return String.format("#%02x%02x%02x",(value>>16)%256,(value>>8)%256,value % 256);
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
                    lineNumber++;
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

    Thread ThreadCalculateSolutions;

    @FXML
    private CheckBox SFCreateImages;

    @FXML
    private CheckBox SFCreateStatisticData;
    
    @FXML
    private ChoiceBox<String> SFChoiceNextLayerRule;
    
    @FXML
    private ChoiceBox<String> SFChoiceNextLayerSecondaryRule;

    @FXML
    private GridPane SFGridPaneMatrixMostConstrained;
    
    @FXML
    private Button buttonStartSolutionFinder;
    
    @FXML
    private ProgressBar SFProgressBar;
    
    @FXML
    private Button buttonCancelSolutionFinder;
    
    @FXML
    private Pane SFStatisticsPane;
    private LineChart<Number,Number> StatisticsLineChart;

    private void setupSolutionFinderTab(){
        initChoiceNextLayerRule();
        initSolutionFinderStatisticChart();
    }

    private void initChoiceNextLayerRule(){
        //gets called by opening or creating Project
        String[] nextLayerRules = {"most constrained", "max-x, max-y",
                "max-y, max-x","max-x, min-y","max-y, min-x", "min-x, max-y",
                "min-y, max-x","min-x, min-y","min-y, min-x"};
        SFChoiceNextLayerRule.getItems().clear();
        SFChoiceNextLayerRule.getItems().addAll(nextLayerRules);
        SFChoiceNextLayerRule.getSelectionModel().selectFirst();

        String[] nextLayerSecondaryRules = {"none",">= 40% filled -> most constrained",
                "most constrained >= 50% filled","most constrained >= 60% filled","most constrained >= 80% filled"};
        SFChoiceNextLayerSecondaryRule.getItems().clear();
        SFChoiceNextLayerSecondaryRule.getItems().addAll(nextLayerSecondaryRules);
        SFChoiceNextLayerSecondaryRule.getSelectionModel().selectFirst();
    }
    
    private void initSolutionFinderStatisticChart(){
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Layer");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("iterations in this Layer");
        yAxis.setAutoRanging(true);

        StatisticsLineChart = new LineChart<>(xAxis,yAxis);
        StatisticsLineChart.autosize();
        StatisticsLineChart.setLegendVisible(false);
        SFStatisticsPane.getChildren().clear();
        SFStatisticsPane.getChildren().add(StatisticsLineChart);
    }

    public class SolutionFinderConfiguration extends SolutionFinderConfig{

        public SolutionFinderConfiguration(SynchronousQueue<Vector<Long>> queue) {
            super(queue);
        }

        @Override
        public String getPrimaryNextLayerRule(){
            return SFChoiceNextLayerRule.getValue();
        }
        @Override
        public String getSecondaryNextLayerRule(){
            return SFChoiceNextLayerSecondaryRule.getValue();
        }

        @Override
        public boolean shouldCreateImages(){
            return SFCreateImages.isSelected();
        }

        @Override
        public boolean shouldCreateStatistics(){
            return SFCreateStatisticData.isSelected();
        }

        @Override
        public Vector<Coordinate> getMostConstrainedMatrix(){
            ObservableList<Node> matrix_nodes = SFGridPaneMatrixMostConstrained.getChildren();
            //ObservableList<Button> button_nodes = matrix_nodes.stream().map(n -> (Button) n).collect();
            Vector<Coordinate> MostConstrained = new Vector<>();
            for(Node n : matrix_nodes){
                TextField b = (TextField) n;
                for(int i = -1; i<2;i++){
                    for(int j = -1; j<2;j++){
                        MostConstrained.add(new Coordinate(i,j,Integer.parseInt(b.getText())));
                    }
                }
            }
            return MostConstrained;
        }

        @Override
        public String getCoordinateMode(){
            return SelectPuzzleMode.getValue();
        }
    }
    
    @FXML
    private void calculateSolutions(ActionEvent event){
        //TODO: add coordinate mode capability - current solver only works in 2d mode
        //TODO: make new thread that runs this
        //TODO: add warning if this project has already started computation once
        SynchronousQueue<Vector<Long>> queue = new SynchronousQueue<>();
        SolutionFinderConfiguration Config = new SolutionFinderConfiguration(queue);

        CalculateSolutions CS = new CalculateSolutions(ProjectPath, Config);
        int firstLayerMatches = CS.getFirstLayerMatches();
        System.out.println("firstlayermatches: " + firstLayerMatches);
        ThreadCalculateSolutions = new Thread(CS);

        UpdateSolutionFinderProgress updateProgressbar
                = new UpdateSolutionFinderProgress(SFProgressBar,
                                                queue,
                                                ThreadCalculateSolutions,
                                                buttonStartSolutionFinder,
                                                buttonCancelSolutionFinder,
                                                StatisticsLineChart,
                                                firstLayerMatches);
        Thread ThreadUpdateProgressBar = new Thread(updateProgressbar);
        ThreadCalculateSolutions.setDaemon(true);
        ThreadUpdateProgressBar.setDaemon(true);
        ThreadCalculateSolutions.start();
        ThreadUpdateProgressBar.start();
        buttonStartSolutionFinder.setDisable(true);
        buttonCancelSolutionFinder.setDisable(false);
    }

    @FXML
    private void cancelCalculateSolutions(ActionEvent event){
        if(ThreadCalculateSolutions == null){
            return;
        }
        if(ThreadCalculateSolutions.isAlive()){

            ThreadCalculateSolutions.interrupt();
            buttonStartSolutionFinder.setDisable(false);
        }
    }

    //-----VIEW-SOLUTIONS-TAB-----
    @FXML
    private Pagination PaginationSolutions;
    
    @FXML
    private TextField textFieldShowImageSolutionID;

    @FXML
    private TextField textFieldShowImageIndex;
    
    private void setupViewSolutionsTab(){
        setupPagination();
    }

    @FXML
    private void initPagination(ActionEvent e){
        setupPagination();
    }

    private void setupPagination(){
        //TODO: use lazy loading
        File imageDirectory = new File(ProjectPath + "/SolutionImages");
        int numberOfImages = 0;
        try{
            numberOfImages = (int) Arrays.stream(Objects.requireNonNull(imageDirectory.listFiles())).count();
        } catch (NullPointerException e){
            //do nothing - no files to display
            return;
        }
        String[] indexedSolutionIds;
        try {
            FileReader SolutionIDFile = new FileReader(ProjectPath + "/Solutions/_solution_ids.txt");
            List<String> solutionsList = SolutionIDFile.readAllLines();
            SolutionIDFile.close();
            indexedSolutionIds = new String[solutionsList.size()];
            solutionsList.toArray(indexedSolutionIds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PaginationSolutions.setPageCount(numberOfImages);

        final int finalNumberOfImages = numberOfImages;
        PaginationSolutions.setPageFactory((Integer pageIndex) -> {
            String imagePath = "file:" + ProjectPath + "\\SolutionImages\\_Reference.png";
            if(pageIndex+1< finalNumberOfImages){
                imagePath = "file:" + ProjectPath + "\\SolutionImages\\" + indexedSolutionIds[pageIndex] + ".png";
            }
            //System.out.println(ProjectPath + "\\SolutionImages\\" + indexedSolutionIds[pageIndex] + ".png");

            Image image = new Image(imagePath,900,350,true,false);
            return new ImageView(image);
        });
    }

    @FXML
    private void showImagePerSolutionID(ActionEvent event) {
        
    }

    @FXML
    private void showImagePerIndex(ActionEvent event) {
        PaginationSolutions.setPageCount(Integer.parseInt(textFieldShowImageIndex.getText()));
    }


    //-----ZOOM-IN-SOLUTIONS-TAB-----
    
    Thread ThreadZoomIn;

    @FXML
    private TextField textFieldSolutionIndexOrID;

    @FXML
    private RadioButton buttonSelectSolutionIsIndex;

    @FXML
    private RadioButton buttonSelectSolutionIsSolutionID;

    @FXML
    private CheckBox checkBoxSelectRestrictionTo;

    @FXML
    private TextField textFieldRestrictTo;

    @FXML
    private RadioButton buttonRestrictToIsIndex;

    @FXML
    private RadioButton buttonRestrictToIsID;

    @FXML
    private TextField textFieldRestrictToModulo;

    @FXML
    private TextField textFieldModuloLeq;

    @FXML
    private TextField textFieldConnectedLeq;

    @FXML
    private TextField textFieldDecreaseByPerIteration;

    @FXML
    private Button buttonStartZoomIn;

    @FXML
    private ProgressBar progressBarZoomIn;

    @FXML
    private Button buttonCancelZoomIn;

    @FXML
    private AnchorPane anchorPaneZoomIn;

    @FXML
    private void toggleSolutionIndexOrID(ActionEvent event){
        if(event.getSource().equals(buttonSelectSolutionIsIndex)){//event source is buttonSelectSolutionIsIndex
            buttonSelectSolutionIsSolutionID.setSelected(!buttonSelectSolutionIsIndex.isSelected());
            return;
        }
        buttonSelectSolutionIsIndex.setSelected(!buttonSelectSolutionIsSolutionID.isSelected());
    }

    @FXML
    private void toggleRestrictToIndexOrID(ActionEvent event){
        if(event.getSource().equals(buttonRestrictToIsID)){//event source is buttonRestrictToIsID
            buttonRestrictToIsIndex.setSelected(!buttonRestrictToIsID.isSelected());
            return;
        }
        buttonRestrictToIsID.setSelected(!buttonRestrictToIsIndex.isSelected());
    }

    @FXML
    private void startZoomIn(ActionEvent event){
        String ZoomIn = getZoomInTargetID();
        String RestrictTo = null;
        int RestrictionLeq = -1;
        if(checkBoxSelectRestrictionTo.isSelected()){
            RestrictTo = getRestrictedZoomID();
            RestrictionLeq = Integer.parseInt(textFieldRestrictToModulo.getText());
        }

        int startingModuloLeq = Integer.parseInt(textFieldModuloLeq.getText());
        int startingConnectedEdgesLeq = Integer.parseInt(textFieldConnectedLeq.getText());
        int decrementPerIteration = Integer.parseInt(textFieldDecreaseByPerIteration.getText());

        //TODO: call function
        ZoomInSolution Zoom = new ZoomInSolution(ProjectPath,
                                                ZoomIn,
                                                RestrictTo,
                                                startingModuloLeq,
                                                startingConnectedEdgesLeq,
                                                decrementPerIteration,
                                                RestrictionLeq);
        ThreadZoomIn = new Thread(Zoom);
        ThreadZoomIn.start();
        buttonStartZoomIn.setDisable(true);
    }

    private String getZoomInTargetID(){
        String SolutionID = textFieldSolutionIndexOrID.getText();
        if(buttonSelectSolutionIsIndex.isSelected()){
            SolutionID = getSolutionIdFromIndex(Integer.parseInt(SolutionID));
        }
        return SolutionID;
    }

    private String getRestrictedZoomID(){
        String SolutionID = textFieldRestrictTo.getText();
        if(buttonRestrictToIsIndex.isSelected()){
            SolutionID = getSolutionIdFromIndex(Integer.parseInt(SolutionID));
        }
        return SolutionID;
    }

    private String getSolutionIdFromIndex(int index){
        //inspired from: https://www.educative.io/answers/reading-the-nth-line-from-a-file-in-java
        //BufferedReader file = new BufferedReader(new FileReader(ProjectPath + "/Solutions/_solution_ids.txt"));
        String SolutionId = null;
        try (Stream<String> lines = Files.lines(new File(ProjectPath + "/Solutions/_solution_ids.txt").toPath())) {
            SolutionId = lines.skip(index).findFirst().get();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return SolutionId;
    }

    @FXML
    private void cancelZoomIn(ActionEvent event){
        //TODO: interrupt computation
        buttonStartZoomIn.setDisable(false);
    }

}
