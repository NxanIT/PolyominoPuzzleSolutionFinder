package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class CreatePuzzleController {

    private String pathSelected;
    public boolean was_successful = false;
    
    void setupInitialPath(String p){
        pathSelected = p;
    }

    @FXML
    private TextField PuzzleName;

    @FXML
    private Text TextPathSelect;

    @FXML
    void cancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void save(ActionEvent event) {
        try {
            createFileStructure();
            was_successful = true;
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO add logging
        }
        closeWindow();
    }

    private void closeWindow(){
        //inspired from https://stackoverflow.com/questions/13567019/close-fxml-window-by-code-javafx
        Stage s = (Stage) PuzzleName.getScene().getWindow();
        s.close();
    }

    private void createFileStructure() throws IOException{
        /*
        inspired from https://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java#3634879
                      https://stackoverflow.com/questions/35017393/how-to-create-file-in-a-project-folder-in-java
                      TODO: add logging warning if file creation was not successful
         */

        String ProjectName = PuzzleName.getText();
        //TODO: check input
        File SolutionsDirectory = new File(pathSelected + "/" + ProjectName + "/Solutions");
        SolutionsDirectory.mkdirs();
        
        File ImageDirectory = new File(pathSelected + "/" + ProjectName + "/SolutionImages");
        ImageDirectory.mkdirs();

        String ProjectDataString = pathSelected + "/" + ProjectName;
        File ProjectDirectory = new File(ProjectDataString+ "/PuzzleData");
        ProjectDirectory.mkdirs(); 
        
        //create files for storing puzzle data
        String[] fileNames = {"/PuzzleData/map.txt","/PuzzleData/bricks.txt","/PuzzleData/colors.txt","/Solutions/_solution_ids.txt"};

        File[] files = Arrays.stream(fileNames)
                .map(name -> new File(ProjectDataString  + name))
                .toArray(File[]::new);
        for(File file : files){
            file.createNewFile();
        }
        
    }

    @FXML
    void selectPath(ActionEvent event) {
        DirectoryChooser chooseDir = new DirectoryChooser();
        if(pathSelected!=null){//set initial directory to parent of currentProject, if not null
            int ParentDirEnding = Integer.max(pathSelected.lastIndexOf("\\"), pathSelected.lastIndexOf("/"));
            chooseDir.setInitialDirectory(new File(pathSelected.substring(0,ParentDirEnding)));
        }
        File selectedDir = chooseDir.showDialog(new Stage());
        if(selectedDir!=null){
            TextPathSelect.setText(selectedDir.getAbsolutePath());
            pathSelected = selectedDir.toPath().toString();
        }
    }
    
    String getPathSelected(){
        return pathSelected + "\\" + PuzzleName.getText();
    }

}
