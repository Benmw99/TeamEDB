package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;

public class Controller {
    //Global for the file, shut up about globals
    File globalFile = null;
    @FXML
    TextField StreetText;
    @FXML
    TextField CityText;
    @FXML
    TextField StateText;
    @FXML
    TextField ZipText;
    @FXML
    Button submitAddress;
    @FXML
    TextField citySearch;
    @FXML
    Button resetButton;
    @FXML
    Label textUpdates;
    @FXML
    TextArea resultAddress;
    @FXML
    Button chooseButton;
    @FXML
    TextField fileText;
    @FXML
    Button submitImage;
    @FXML
    Button viewAddress;
    @FXML
    Button viewImage;
    @FXML
    ImageView imageDisplay;
    @FXML
    Label imageName;

    //Function to return a connection to the DB
    public Connection connect() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error in setting up class");
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            String dbName = "./DBProto.db";
            connection = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true");
        } catch (SQLException e) {
            System.out.println("Connection failed. Check output console.");
            e.printStackTrace();
        }
        return connection;
    }

    @FXML
    //Function for resetting the DB
    public void reset() {
        Connection connection = connect();
        try {
            String drop = "drop table Labels";
            Statement stmt = connection.createStatement();
            stmt.execute(drop);
        } catch (SQLException e) {}
        try {
            String drop = "drop table Address";
            Statement stmt = connection.createStatement();
            stmt.execute(drop);
        } catch (SQLException e) {}
        try {
            String create = "create table Labels (image blob(1M), imageName varchar(64), Constraint labels_PK Primary Key (imageName))";
            Statement stmt = connection.createStatement();
            stmt.execute(create);
        } catch (SQLException e) {
            System.out.println("Error creating Labels");
            System.out.println("SQL State: " + e.getErrorCode());
            System.out.println("Error Code: " + e.getSQLState());
            System.out.println("Message: " + e.getMessage());
        }
        try {
            String create = "create table Address (street varchar(32), city varchar(32), zipcode INT, state varchar(32), CONSTRAINT Address_PK Primary Key (street, city, zipcode, state))";
            Statement stmt = connection.createStatement();
            stmt.execute(create);
        } catch (SQLException e) {
            System.out.println("Error creating Address");
            System.out.println("SQL State: " + e.getErrorCode());
            System.out.println("Error Code: " + e.getSQLState());
            System.out.println("Message: " + e.getMessage());
        }
        //TODO insert data into Address
        resetButton.setText("Reset!");
    }

    @FXML
    //Function for choosing the actual file
    public void choose() {
        //Make a new file chooser to select the file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select label");
        //Opening a file chooser without passing the direct stage
        globalFile = fileChooser.showOpenDialog(chooseButton.getScene().getWindow());
        //If they actually select something
        if (globalFile != null) {
            //Show the path in the label
            submitImage.setDisable(false);
        } else {
        }
    }

    @FXML
    public void submitAddressData() {
        Connection connection = connect();
        String insert = "insert into Address values (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(insert);
            pstmt.setString(1, StreetText.getText());
            pstmt.setString(2, CityText.getText());
            pstmt.setInt(3, Integer.parseInt(ZipText.getText()));
            pstmt.setString(4, StateText.getText());
            pstmt.execute();
            StreetText.setText("");
            CityText.setText("");
            ZipText.setText("");
            StateText.setText("");
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e);
        }
    }

    @FXML
    public void viewAddresses() {
        Connection connection = connect();
        String query = "select street from Address where city = ?";
        String searchResults = "";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, citySearch.getText());
            ResultSet rset = pstmt.executeQuery();
            while (rset.next()) {
                searchResults = searchResults + rset.getString("street") + "\n";
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e);
        }
        resultAddress.setText(searchResults);
    }

    @FXML
    //Function for uploading to the DB
    public void submit() {
        //Get the connection from the function
        Connection connection = connect();
        //Make what the insert statement will look like
        String insert = "insert into Labels values (?, ?)";
        try {
            //Make a prepared statement of the connection with the insert string
            PreparedStatement pstmt = connection.prepareStatement(insert);
            //Make a null FileInputStream and then set it in the try in case of non-existence
            FileInputStream input = null;
            try {
                input = new FileInputStream(globalFile);
            } catch (FileNotFoundException e) {
                System.out.println("Error: " + e);
            }
            //Set the parameters and the blob will be as a binary stream
            pstmt.setBinaryStream(1, input);
            pstmt.setString(2, globalFile.getName());
            System.out.println(globalFile.getName());
            pstmt.execute();
            textUpdates.setText("Submitted");
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e);
        }
    }

    @FXML
    //Function for viewing the file that's on the DB
    public void view() {
        //Get the connection from the function
        Connection connection = connect();
        try {
            //Autcommit must be turned off to work with blobs
            connection.setAutoCommit(false);
            //Select the entry searched for in the DB
            String imageSearch = fileText.getText();
            String str = "select * from Labels where imageName = '" + imageSearch + "'";
            //Create the statement and execute the query
            Statement stmt = connection.createStatement();
            ResultSet rset = stmt.executeQuery(str);
            //Must all be set to something or else java gets angry
            Blob image = null;
            String imageFileName = null;
            //Get everything from the ResultSet
            while (rset.next()) {
                image = rset.getBlob("image");
                imageFileName = rset.getString("imageName");
            }
            //Make an inputstream from the blobs binary stream
            InputStream in = image.getBinaryStream();
            //Make an image from the input stream
            Image displayImage = new Image(in);
            //Display that image in the imageView
            imageDisplay.setImage(displayImage);
            //Output the rest of the info about that image
            imageName.setText(imageFileName);
            //Turn autocommit back on to kill the blob and make sure its on
            connection.setAutoCommit(true);
            //Might not be necessary but also might be preventing memory leaks. I don't really know
            image.free();
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e);
        }
    }
}
