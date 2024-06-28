import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        String CRLF = "\r\n";

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            while (true) {
                clientSocket = serverSocket.accept(); // Wait for connection from client.
                // client side conversion of bytes into data.
                ArrayList<String> HttpRequest = getHttpRequest(clientSocket);

                System.out.println(HttpRequest);

                // Striping URL from the HTTP req
                String[] URL = HttpRequest.get(0).split(" ", 0);

                if (URL[1].equals("/")) {
                    String response = "HTTP/1.1 200 OK" + CRLF + CRLF;
                    clientSocket.getOutputStream().write(response.getBytes());
                } else if (URL[1].startsWith("/echo/")) {
                    String[] path = URL[1].split("/", 0);
                    String response =
                            "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Content-Length:" + path[2].length() + CRLF + CRLF + path[2];
                    clientSocket.getOutputStream().write(response.getBytes());
                } else if (URL[1].startsWith("/user-agent")) {
                    String[] user_agent = new String[2];
                    for (String s : HttpRequest) {
                        if (s.startsWith("User-Agent"))
                            user_agent = s.split(": ");
                    }
                    String response =
                            "HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF + "Content-Length:" +
                                    user_agent[1].length() + CRLF + CRLF + user_agent[1];
                    clientSocket.getOutputStream().write(response.getBytes());
                } else {
                    String response = "HTTP/1.1 404 Not Found" + CRLF + CRLF;
                    clientSocket.getOutputStream().write(response.getBytes());
                    clientSocket.close();
                    System.out.println("accepted new connection");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<String> getHttpRequest(Socket clientSocket) throws IOException {
        BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // Read the request
        String req;
        ArrayList<String> HttpRequest = new ArrayList<>();
        // read request completely HTTP requests don't end with EOF but with blank line.
        while (!(req = clientIn.readLine()).isEmpty())
            HttpRequest.add(req);

        return HttpRequest;
    }
}