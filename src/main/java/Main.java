import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept(); // Wait for connection from client.

            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            String[] HttpRequest = line.split(" ", 0);

            OutputStream output = clientSocket.getOutputStream();

            if (HttpRequest[1].equals("/")) {
                output.write(("HTTP/1.1 200 OK\r\n\r\n".getBytes()));
            } else if (HttpRequest[1].startsWith("/echo/")) {
                String message = HttpRequest[1].substring(6);
                String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s" +
                        message.length() + message;
                output.write(response.getBytes());
            } else if (HttpRequest[1].equals("/user-agent")) {
                String[] userAgent = new String[2];
                for (String s : HttpRequest) {
                    if (s.startsWith("User-Agent"))
                        userAgent = s.split(": ");
                }

                String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" +
                        userAgent[1].length() + "\r\n\r\n" + userAgent[1];
                output.write(response.getBytes());
            } else {
                output.write(("HTTP/1.1 404 Not Found\r\n\r\n".getBytes()));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}