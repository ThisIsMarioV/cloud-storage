package com.geekbrains;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@Slf4j
public class CloudStorageWindowController implements Initializable {

    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField input;
    private Path clientDir;
    private byte[] buff ;
    private static final int BUFFER_SIZE = 1024;


    private DataInputStream inStream;
    private DataOutputStream outStream;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            buff = new byte[BUFFER_SIZE];
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
                String command = inStream.readUTF();
                log.debug("Received: {}", command);
                if(command.equals("list")){
                    int count = inStream.readInt();
                    log.debug("Process list files, count = {}", count);
                    List<String> listFiles = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        String fileName = inStream.readUTF();
                        listFiles.add(fileName);
                    }
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().addAll(listFiles);
                    });
                    log.debug("Files on server: {}", listFiles);

                }
                if(command.equals("file")) {
                    String fileName = inStream.readUTF();
                    log.debug("Download file: {}", fileName );
                    long size = inStream.readLong(); // kolichestvo baytov
                    log.debug("Read {} bytes", size);
                    long butchCount = (size + BUFFER_SIZE - 1) / BUFFER_SIZE; // koll zohodov
                    Path file = clientDir.resolve(fileName);
                    try (OutputStream ous = new FileOutputStream(file.toFile())) {
                        for (int i = 0; i < butchCount; i++) {
                            int read = inStream.read(buff);
                            ous.write(buff,0,read);
                        }
                    }
                    List<String> list = getFiles(clientDir);
                    Platform.runLater(() ->{
                        clientView.getItems().clear();
                        clientView.getItems().addAll(list);
                    });

                }

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

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        outStream.writeUTF("file");
        outStream.writeUTF(fileName);
        long size = Files.size(clientDir.resolve(fileName));
        outStream.writeLong(size);
        Path file = clientDir.resolve(fileName);
        try (FileInputStream fis = new FileInputStream(file.toFile())){
            while (fis.available()>0){
                int read = fis.read(buff);
                outStream.write(buff,0,read);
            }
        }
        outStream.flush();
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileName =  serverView.getSelectionModel().getSelectedItem();
        outStream.writeUTF("fileRequest");
        outStream.writeUTF(fileName);
        outStream.flush();
    }
}

