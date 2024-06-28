import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        String directory = "";
        if (args.length > 1 && args[0].equals("--directory")) {
            directory = args[1];
            System.out.println(args[1]);
        }

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            while (true) {
                clientSocket = serverSocket.accept(); // Wait for connection from client.
                //client side conversion of bytes into data.
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream();
                String request; // Read the request
                ArrayList<String> HttpRequest = new ArrayList<>();

                //read request completely HTTP requests don't end with EOF but with blank line.
                while (!(request = clientIn.readLine()).equals(""))
                    HttpRequest.add(request);

                System.out.println(HttpRequest);
                //Striping URL from the HTTP req
                String[] URL = HttpRequest.get(0).split(" ", 0);
                if (URL[0].equals("POST")) {
                    StringBuffer data = new StringBuffer();
                    while (clientIn.ready())
                        data.append((char) clientIn.read());

                    String body = data.toString();
                    Path path = Paths.get(directory, URL[1].split("/")[2]);
                    Files.write(path, body.getBytes());
                    outputStream.write("HTTP/1.1 201 Created\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                } else {
                    if (URL[1].equals("/")) {
                        String response = "HTTP/1.1 200 OK\r\n\r\n";
                        outputStream.write(response.getBytes(StandardCharsets.UTF_8));

                    } else if (URL[1].startsWith("/echo/")) {
                        String[] path = URL[1].split("/", 0);
                        String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + path[2].length() + "\r\n\r\n" + path[2];
                        outputStream.write(response.getBytes());
                    } else if (URL[1].startsWith("/user-agent")) {
                        String[] userAgent = new String[2];
                        for (String s : HttpRequest)
                            if (s.startsWith("User-Agent"))
                                userAgent = s.split(": ");

                        String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + userAgent[1].length() + "\r\n\r\n" + userAgent[1];
                        outputStream.write(response.getBytes());
                    } else if (URL[1].startsWith("/files")) {
                        String filename = URL[1].split("/", 0)[2];
                        File file = new File(directory, filename);
                        System.out.println(file.toPath());
                        if (file.exists()) {
                            //reading byte content
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            String httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " +
                                            fileContent.length + "\r\n\r\n" + new String(fileContent);
                            outputStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
                        } else {
                            String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                            outputStream.write(response.getBytes());
                        }
                    } else {
                        String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                        outputStream.write(response.getBytes());
                    }
                    clientSocket.close();
                    System.out.println("accepted new connection");
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}