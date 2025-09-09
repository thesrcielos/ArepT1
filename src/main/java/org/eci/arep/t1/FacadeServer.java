package org.eci.arep.t1;

import java.io.*;
import java.net.*;

public class FacadeServer {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GET_URL = "http://localhost:36000";
    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(40000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 40000.");
            System.exit(1);
        }
        boolean running = true;
        while(running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
             boolean firstLine = true;
            URI requestUri = null;
            while ((inputLine = in.readLine()) != null) {
                if (firstLine){
                    String path = inputLine.split(" ")[1];
                    requestUri = new URI(path);
                    firstLine = false;
                }
                System.out.println("Recibí: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }
            handleRequest(requestUri, out);
            out.close();
            in.close();
            clientSocket.close();

        }
        serverSocket.close();
    }

    public static void handleRequest(URI uri, PrintWriter out) throws IOException {
        if (uri == null){
            return;
        }
        String path = uri.getPath();
        if (path.startsWith("/cliente")){
            String outputLine = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n"
                    + "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "\n" +
                    "<head>\n" +
                    "    <title>Form Example</title>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "</head>\n" +
                    "\n" +
                    "<body>\n" +
                    "    <h1>GET Value</h1>\n" +
                    "    <form action=\"/hello\">\n" +
                    "        <label for=\"name\">Name:</label><br>\n" +
                    "        <input type=\"text\" id=\"key\" name=\"key\" value=\"John\"><br><br>\n" +
                    "        <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                    "    </form>\n" +
                    "    <div id=\"getrespmsg\"></div>\n" +
                    "    <h1>SET Value</h1>\n" +
                    "        <form action=\"/hello\">\n" +
                    "        <label for=\"name\">Name:</label><br>\n" +
                    "        <input type=\"text\" id=\"keyset\" name=\"key\" value=\"John\"><br><br>\n" +
                    "        <input type=\"text\" id=\"value\" name=\"value\" value=\"Smith\"><br><br>\n" +
                    "        <input type=\"button\" value=\"Submit\" onclick=\"loadSetMsg()\">\n" +
                    "    </form>\n" +
                    "    <div id=\"setrespmsg\"></div>\n" +
                    "\n" +
                    "    <script>\n" +
                    "        function loadGetMsg() {\n" +
                    "            let keyName = document.getElementById(\"key\").value;\n" +
                    "            const xhttp = new XMLHttpRequest();\n" +
                    "            xhttp.onload = function () {\n" +
                    "                document.getElementById(\"getrespmsg\").innerHTML =\n" +
                    "                    this.responseText;\n" +
                    "            }\n" +
                    "            xhttp.open(\"GET\", \"/getkv?key=\" + keyName);\n" +
                    "            xhttp.send();\n" +
                    "        }\n" +
                    "        function loadSetMsg() {\n" +
                    "            let keyName = document.getElementById(\"keyset\").value;\n" +
                    "            let valueVar = document.getElementById(\"value\").value;\n" +
                    "            const xhttp = new XMLHttpRequest();\n" +
                    "            xhttp.onload = function () {\n" +
                    "                document.getElementById(\"setrespmsg\").innerHTML =\n" +
                    "                    this.responseText;\n" +
                    "            }\n" +
                    "            xhttp.open(\"GET\", \"/setkv?key=\" + keyName + \"&value=\" + valueVar);\n" +
                    "            xhttp.send();\n" +
                    "        }\n" +
                    "    </script>\n" +
                    "\n" +
                    "</body>\n" +
                    "\n" +
                    "</html>\n";
                    out.println(outputLine);
        }else if(path.startsWith("/getkv") || path.startsWith("/setkv") ){
            handleKeyValueOperation(uri.toString(), out);
        }
    }

    public static void handleKeyValueOperation(String path, PrintWriter out) throws IOException {
        URL obj = new URL(GET_URL + path);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.getResponseMessage();
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String outputLine = "HTTP/1.1 " + responseCode + " " + HttpServer.codeMessage(responseCode) + "\r\n"
                        + "Content-Type: application/json; charset=utf-8\r\n"
                        + "\r\n"
                        + response.toString();
                out.println(outputLine);
            } else {
                System.out.println("GET request not worked");
                String outputLine = "HTTP/1.1 " + responseCode + " " + HttpServer.codeMessage(responseCode) + "\r\n"
                        + "Content-Type: application/json; charset=utf-8\r\n"
                        + "\r\n"
                        + "{\"error\":" + "\"" + con.getResponseMessage() + "\""
                        + ", \"key\":" + "\"" + getKey(path) + "\""
                        + "}";
                out.println(outputLine);
            }

        System.out.println("GET DONE");
    }

    public static String getKey(String path){
        String[] keys = path.split("key=");
        if (keys.length < 2){
            return "";
        }
        return keys[1].split("&")[0];
    }
}


