package com.geekbrains;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@Slf4j
public class CloudStorageWindowController implements Initializable {

    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField input;
    private Path clientDir;


    private DataInputStream inStream;
    private DataOutputStream outStream;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            clientDir = Paths.get("cloud-storage-client", "Client");
            if(!Files.exists(clientDir)){
                Files.createDirectory(clientDir);
            }
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(clientDir));
            clientView.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2){
                    String item = clientView.getSelectionModel().getSelectedItem();
                    input.setText(item);
                }
            });
            Socket socket = new Socket("localhost", 8189);
            inStream = new DataInputStream(socket.getInputStream());
            outStream = new DataOutputStream(socket.getOutputStream());
            Thread threadRead = new Thread(this::read);
            threadRead.setDaemon(true); // servisniy potok
            threadRead.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void read(){
        try {
            while (true) {
                String message = inStream.readUTF();
                log.debug("Received: {}", message);
                Platform.runLater(() -> clientView.getItems().add(message));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<String> getFiles(Path path) throws IOException {
       return Files.list(path).map(p -> p.getFileName().toString()).collect(Collectors.toList());
    }

    public void sendMessage(javafx.event.ActionEvent actionEvent) throws IOException {
        String text = input.getText();
        outStream.writeUTF(text);
        outStream.flush();
        input.clear();
    }
}

