package Multithread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {

    static final String FILE_PATH = ".\\src\\ServerStorage";

    private static void handleClient(SocketChannel serverChannel) throws IOException {
        ByteBuffer request = ByteBuffer.allocate(1000);
        int numBytes = serverChannel.read(request);
        request.flip();

        byte[] clientRequest = new byte[numBytes];
        request.get(clientRequest);

        String command = new String(clientRequest);
        System.out.println(command);

        String[] cmdArray = command.split("~");
        System.out.println(Arrays.toString(cmdArray));

        String replyArray = "";
        System.out.println(Arrays.toString(cmdArray));

        switch (cmdArray[0]){
            case "X":
                // DELETE
                File fileToDelete = new File(FILE_PATH + File.separator + cmdArray[1]);
                System.out.println("Deleting file: " + fileToDelete.getAbsolutePath());
                if (fileToDelete.exists()) {
                    if (fileToDelete.delete()) {
                        replyArray = "S";
                    } else {
                        replyArray = "F";
                    }
                } else {
                    replyArray = "F";
                }
                serverChannel.write(ByteBuffer.wrap(replyArray.getBytes()));
                break;
            case "D":
                // DOWNLOAD
                File fileToDownload = new File(FILE_PATH + cmdArray[1]);
                if (fileToDownload.exists()) {
                    FileInputStream fs = new FileInputStream(fileToDownload);
                    FileChannel fc = fs.getChannel();
                    ByteBuffer content = ByteBuffer.allocate(1000);

                    while(fc.read(content) >= 0){
                        content.flip();
                        serverChannel.write(content);
                        content.clear();
                    }
                    serverChannel.shutdownOutput();
                    break;
                }
            case "U":
                // UPLOAD
                String fileName = cmdArray[1];

                File uploadedFile = new File(FILE_PATH + fileName);
                FileOutputStream fos = new FileOutputStream(uploadedFile);
                FileChannel fileChannel = fos.getChannel();

                ByteBuffer buffer = ByteBuffer.allocate(1000);
                while (serverChannel.read(buffer) >= 0) {
                    buffer.flip();
                    fileChannel.write(buffer);
                    buffer.clear();
                }
                serverChannel.shutdownInput(); // Signal end of input
                fos.close();
                fileChannel.close();
                break;

            case "R":
                // RENAME
                File oldFile = new File(FILE_PATH + File.separator + cmdArray[1]);
                File newFile = new File(FILE_PATH + File.separator + cmdArray[2]);

                if (oldFile.exists() && !newFile.exists()) {
                    if (oldFile.renameTo(newFile)) {
                        replyArray = "S";
                    } else {
                        replyArray = "F";
                    }
                } else {
                    replyArray = "F";
                }

                serverChannel.write(ByteBuffer.wrap(replyArray.getBytes()));
//                    serverChannel.close();
                break;
            case "L":
                // LIST
                File directory = new File(FILE_PATH);
                File[] files = directory.listFiles();

                if (files != null) {
                    List<String> fileNames = new ArrayList<>();
                    for (File file : files) {
                        fileNames.add(file.getName());
                    }
                    replyArray = String.join(", ", fileNames);
                } else {
                    replyArray = "F"; // Indicate failure in case of directory read error
                }
                serverChannel.write(ByteBuffer.wrap(replyArray.getBytes()));
//                    serverChannel.close();
                break;
            default:
                if (!command.equals("Q")){
                    // send back error message (would there ever be a bad request)
                    System.out.println("Invalid command.");
                }
        }
        serverChannel.close(); // Close the channel after handling the request
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1){
            System.out.println("Please specify the server port.");
            return;
        }
        int port = Integer.parseInt(args[0]);

        ServerSocketChannel welcomeChannel = ServerSocketChannel.open();
        welcomeChannel.socket().bind(new InetSocketAddress(port));

        while (true){
            SocketChannel serverChannel = welcomeChannel.accept();
            handleClient(serverChannel);
        }
    }
}
