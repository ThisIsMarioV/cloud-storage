package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Handler implements Runnable{
    private static final int BUFFER_SIZE = 1024;
    private byte[] buff;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private static int counter = 0;
    private final String nameUser;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private boolean isRunning;
    private Path serverDir;

    public Handler(Socket socket) throws IOException {
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        counter++;
        nameUser = "User#"+ counter;
        log.debug("Set nick:{} for new client", nameUser);
        isRunning = true;
        buff = new byte[BUFFER_SIZE];
        serverDir = Paths.get("cloud-storage-server", "server");
        sendListFiles();
    }
//public Handler(Socket socket) {
//
//    counter++;
//    nameUser = "User#"+ counter;
//    log.debug("Set nick:{} for new client", nameUser);
//    isRunning = true;
//
//}

    private String getDate(){
        return dateTimeFormatter.format(LocalDateTime.now());
    }


    @Override
    public void run() {
        try {
            while (isRunning){
                String command = inputStream.readUTF();
                log.debug("received: {}", command);
                //String response = String.format("%s %s: %s" , getDate(), nameUser, message); // privedenie k formatu time name message
                if(command.equals("file")){
                    processFileMessage();
                }
                if(command.equals("fileRequest")){
                    processFileRequest();
                }

            }

        }catch (Exception e){
            log.error("", e);
        }
    }

    private void processFileRequest() throws IOException {
        String fileName = inputStream.readUTF();
        outputStream.writeUTF("file");
        outputStream.writeUTF(fileName);
        long size = Files.size(serverDir.resolve(fileName));
        outputStream.writeLong(size);
        Path file = serverDir.resolve(fileName);
        try (FileInputStream fis = new FileInputStream(file.toFile())){
            while (fis.available()>0){
                int read = fis.read(buff);
                outputStream.write(buff,0,read);
            }
        }
        outputStream.flush();
    }

    private void processFileMessage() throws IOException {
        String fileName = inputStream.readUTF();
        log.debug("Download file: {}", fileName );
        long size = inputStream.readLong(); // kolichestvo baytov
        log.debug("Read {} bytes", size);
        long butchCount = (size + BUFFER_SIZE - 1) / BUFFER_SIZE; // koll zohodov
        Path file = serverDir.resolve(fileName);
        try (OutputStream ous = new FileOutputStream(file.toFile())) {
            for (int i = 0; i < butchCount; i++) {
                int read = inputStream.read(buff);
                ous.write(buff,0,read);
            }
        }
        sendListFiles();
    }
    private void sendListFiles() throws IOException {
        List<String>  fileList = Files.list(serverDir).map(p -> p.getFileName().toString()).collect(Collectors.toList());
        outputStream.writeUTF("list");
        outputStream.writeInt(fileList.size());
        for (String s : fileList) {
            outputStream.writeUTF(s);
        }
        outputStream.flush();
    }
}
