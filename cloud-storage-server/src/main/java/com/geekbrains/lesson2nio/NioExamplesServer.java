package com.geekbrains.lesson2nio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class NioExamplesServer {
    public static void main(String[] args) throws IOException {
        Path parent = Paths.get("");
        Path path = Paths.get("cloud-storage-server", "server", "root");
        for (Path p:path){
            Path current = parent.resolve(p);
            System.out.println(current);
            if(!Files.exists(current)) {
                Files.createDirectory(current);

            }
            parent = current;
        }
        //WatchService watchService = path.getFileSystem().newWatchService(); variant create
//        WatchService watchService = FileSystems.getDefault().newWatchService();
//        runAsync(watchService);
//        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        Path helloPath = path.resolve("Hello.txt");
        //Files.write(helloPath, "gt".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND); pishem in file
        Files.copy(helloPath, path.resolve("copy.txt"), StandardCopyOption.REPLACE_EXISTING);
        Path root = Paths.get("");

//        Files.walkFileTree(root, new HashSet<>(), 2,new SimpleFileVisitor<Path>(){
//            @Override
//            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                System.out.println(file);
//                    return super.visitFile(file, attrs);
//            }
//        });
        Files.walk(root,2).forEach(System.out::println);
    }
    private static void runAsync(WatchService watchService){
        new Thread(() -> {
            try {
                while (true) {
                    WatchKey watchKey = watchService.take();
                   List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                   for (WatchEvent<?> event : watchEvents){
                       System.out.println(event.kind() + " "+ event.context());
                   }
                    watchKey.reset();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}
