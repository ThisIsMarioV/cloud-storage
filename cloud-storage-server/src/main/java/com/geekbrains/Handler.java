package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class Handler implements Runnable{
//    private final DataInputStream inputStream;
//    private final DataOutputStream outputStream;
    private static int counter = 0;
    private final String nameUser;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private boolean isRunning;

//    public Handler(Socket socket) throws IOException {
//        inputStream = new DataInputStream(socket.getInputStream());
//        outputStream = new DataOutputStream(socket.getOutputStream());
//        counter++;
//        nameUser = "User#"+ counter;
//        log.debug("Set nick:{} for new client", nameUser);
//        isRunning = true;
//
//    }
public Handler(ServerSocketChannel serverSocketChannel) throws IOException {

    counter++;
    nameUser = "User#"+ counter;
    log.debug("Set nick:{} for new client", nameUser);
    isRunning = true;

}

    private String getDate(){
        return dateTimeFormatter.format(LocalDateTime.now());
    }


    @Override
    public void run() {
        try {
            while (isRunning){
                String message = inputStream.readUTF();
                log.debug("received: {}", message);
                String response = String.format("%s %s: %s" , getDate(), nameUser, message); // privedenie k formatu time name message
                log.debug("Message for response: {}",response);
                outputStream.writeUTF(response);
                outputStream.flush();
            }

        }catch (Exception e){
            log.error("", e);
        }
    }

}
