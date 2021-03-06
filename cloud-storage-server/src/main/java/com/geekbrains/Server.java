package com.geekbrains;





import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class Server {
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(8189)) {
            log.debug("Server started...");
            while (true){
                Socket socket = serverSocket.accept();
                log.debug("Client accepted...");
                Handler handler = new Handler(socket);
                new Thread(handler).start();
            }
        }catch (Exception e){
            log.error("", e);
        }
    }
}
