import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        String CRLF = "\r\n";

        String directory = "";
        if (args.length > 1 && args[0].equals("--directory")) {
            directory = args[1];
            System.out.println(directory);
        }

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            while (true) {
                clientSocket = serverSocket.accept(); // Wait for connection from client.
                // client side conversion of bytes into data.
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ArrayList<String> HttpRequest = getHttpRequest(clientSocket);

                System.out.println(HttpRequest);

                OutputStream outputStream = clientSocket.getOutputStream();

                // Striping URL from the HTTP req
                String[] URL = HttpRequest.get(0).split(" ", 0);

                if (URL[0].equals("POST")) {
                    StringBuffer data = new StringBuffer();
                    while (bufferedReader.ready())
                        data.append(bufferedReader.read());

                    String body = data.toString();
                    Path path = Paths.get(directory, URL[1].split("/")[2]);
                    Files.write(path, body.getBytes());
                    outputStream.write(("HTTP/1.1 201 Created" + CRLF + CRLF).getBytes());

                } else {
                    if (URL[1].equals("/")) {
                        String response = "HTTP/1.1 200 OK" + CRLF + CRLF;
                        outputStream.write(response.getBytes());
                    } else if (URL[1].startsWith("/echo/")) {
                        String[] path = URL[1].split("/", 0);
                        String response = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Content-Length:" +
                                path[2].length() + CRLF + CRLF + path[2];
                        outputStream.write(response.getBytes());
                    } else if (URL[1].startsWith("/user-agent")) {
                        String[] user_agent = new String[2];
                        for (String s : HttpRequest)
                            if (s.startsWith("User-Agent"))
                                user_agent = s.split(": ");

                        String response = "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Content-Length:" +
                                user_agent[1].length() + CRLF + CRLF + user_agent[1];
                        outputStream.write(response.getBytes());
                    } else if (URL[1].startsWith("/files")) {
                        String filename = URL[1].split("/", 0)[2];
                        File file = new File(directory, filename);
                        System.out.println(file.toPath());

                        if (file.exists()) {
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            String response = "HTTP/1.1 200 OK" + CRLF + "Content-Type: application/octet-stream" + CRLF +
                                    "Content-Length: " + fileContent.length + CRLF + CRLF + new String(fileContent);
                            outputStream.write(response.getBytes());
                        } else {
                            String response = "HTTP/1.1 404 Not Found" + CRLF + CRLF;
                            outputStream.write(response.getBytes());
                        }
                    } else {
                        String response = "HTTP/1.1 404 Not Found" + CRLF + CRLF;
                        outputStream.write(response.getBytes());
                        clientSocket.close();
                        System.out.println("accepted new connection");
                    }
                }
            }
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<String> getHttpRequest(Socket clientSocket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // Read the request
        String request;
        ArrayList<String> HttpRequest = new ArrayList<>();
        // read request completely HTTP requests don't end with EOF but with blank line.
        while (!(request = bufferedReader.readLine()).isEmpty())
            HttpRequest.add(request);

        return HttpRequest;
    }
}