package Multithread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    static final String FILE_PATH = ".\\src\\ClientStorage";

    private static SocketChannel openSocketChannel(InetAddress serverIP, int serverPort) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(serverIP, serverPort));
        return channel;
    }

    private static void closeSocketChannel(SocketChannel channel) throws IOException {
        channel.close();
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 2) {
            System.out.println("Please specify server IP and server port");
            return;
        }

        InetAddress serverIP = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);


        System.out.println("Available Commands:\n" +
                "DEL <filename> - Delete a file\n" +
                "DWN <filename> - Download a file\n" +
                "UPL <filename> - Upload a file\n" +
                "REN <old_filename> <new_filename> - Rename a file\n" +
                "DIR - List all files\n" +
                "Q - Quit");

        while(true){
            Scanner keyboard = new Scanner(System.in);
            System.out.print("Enter command: ");
            String command = keyboard.nextLine();

            // split line for byte buffer filenames
            String[] cmdArray = command.split(" ");
            String request = "";

            switch (cmdArray[0].toUpperCase()) {
                case "DEL":
                    SocketChannel delChannel = openSocketChannel(serverIP, serverPort);

                    request = "X~" + cmdArray[1];
                    delChannel.write(ByteBuffer.wrap(request.getBytes()));
                    // Receive and print server response
                    ByteBuffer replyBuffer = ByteBuffer.allocate(1024);
                    int bytesRead = delChannel.read(replyBuffer);
                    replyBuffer.flip();
                    byte[] replyArray = new byte[bytesRead];
                    replyBuffer.get(replyArray);
                    System.out.println(new String(replyArray));

                    closeSocketChannel(delChannel);
                    break;
                case "DWN":
                    SocketChannel dwnChannel = openSocketChannel(serverIP, serverPort);

                    request = "D~" + cmdArray[1];
                    dwnChannel.write(ByteBuffer.wrap(request.getBytes()));

                    FileOutputStream fs = new FileOutputStream(FILE_PATH + cmdArray[1], true);
                    FileChannel fc = fs.getChannel();
                    ByteBuffer reply = ByteBuffer.allocate(1000);

                    while (dwnChannel.read(reply) >= 0) {
                        reply.flip();
                        fc.write(reply);
                        reply.clear();
                    }

                    System.out.println("Downloaded : " + cmdArray[1]);
                    closeSocketChannel(dwnChannel);
                    break;
                case "UPL":
                    SocketChannel uplChannel = openSocketChannel(serverIP, serverPort);


                    String fileName = cmdArray[1];
                    File fileToUpload = new File(FILE_PATH + fileName);

                    if (!fileToUpload.exists()) {
                        System.out.println("Error: File '" + fileName + "' does not exist.");
                        break;
                    }

                    request = "U~" + fileName;
                    uplChannel.write(ByteBuffer.wrap(request.getBytes()));

                    FileInputStream fis = new FileInputStream(fileToUpload);
                    FileChannel fileChannel = fis.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(1000);

                    try {
                        while (fileChannel.read(buffer) >= 0) {
                            buffer.flip();
                            uplChannel.write(buffer);
                            buffer.clear();
                        }
                        uplChannel.shutdownOutput(); // Signal end of output
                        System.out.println("Uploaded : " + fileName);
                    } catch (IOException e) {
                        System.out.println("Error uploading file: " + e.getMessage());
                    } finally {
                        fis.close();
                        fileChannel.close();
                        closeSocketChannel(uplChannel);
                    }
                    break;
                case "REN":
                    SocketChannel renChannel = openSocketChannel(serverIP, serverPort);


                    request = "R~" + cmdArray[1] + "~" + cmdArray[2];
                    renChannel.write(ByteBuffer.wrap(request.getBytes()));

                    // Receive and print server response
                    ByteBuffer renameReplyBuffer = ByteBuffer.allocate(1024);
                    int renameBytesRead = renChannel.read(renameReplyBuffer);
                    renameReplyBuffer.flip();
                    byte[] renameReplyArray = new byte[renameBytesRead];
                    renameReplyBuffer.get(renameReplyArray);
                    System.out.println(new String(renameReplyArray));
                    closeSocketChannel(renChannel);
                    break;
                case "DIR":
                    SocketChannel dirChannel = openSocketChannel(serverIP, serverPort);

                    request = "L";
                    dirChannel.write(ByteBuffer.wrap(request.getBytes()));

                    // receive and print server response
                    ByteBuffer dirReplyBuffer = ByteBuffer.allocate(4096); // Adjust size as needed
                    int dirBytesRead = dirChannel.read(dirReplyBuffer);
                    dirReplyBuffer.flip();
                    byte[] dirReplyArray = new byte[dirBytesRead];
                    dirReplyBuffer.get(dirReplyArray);
                    System.out.println(new String(dirReplyArray));

                    dirChannel.close();
                    break;
                case "Q":
                    System.out.println("Exited");
                    return;
                default:
                    System.out.println("Invalid command.");
                    break;
            }
        }
    }
}
