package org.eci.arep.t1;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;

public class HttpServer {
    private static Map<String, String> keyValueStore = new HashMap<>();
    private static final String LENGTH_MSG = "MAX LENGTH FOR KEY OR VALUE IS 50 CHARACTERS AND MIN LENGTH IS 1 CHARACTER";
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 36000.");
            System.exit(1);
        }
        boolean running = true;
        Socket clientSocket = null;
        while(running) {
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
            String path = null;
            while ((inputLine = in.readLine()) != null) {
                if (path == null){
                    path = inputLine.split(" ")[1];
                }
                if (!in.ready()) {
                    break;
                }
            }
            handleHttpRequest(path, out);
            out.close();
            in.close();
            clientSocket.close();

        }
        serverSocket.close();
    }

    public static void handleHttpRequest(String path, PrintWriter out){
        if (path == null){
            return;
        }
        if(path.startsWith("/getkv")){
            String res = handleGetKey(path);
            if (res == null){
                sendMessage("Key was not found", "error", 404, out);
                return;
            }
            sendMessage(res, "msg",200, out);
        } else if (path.startsWith("/setkv")) {
            String res = handleSetKeyValue(path);
            if (res == null){
                sendMessage(LENGTH_MSG, "error", 400, out);
                return;
            }
            sendMessage(res, "msg",200, out);
        }
    }

    public static void sendMessage(String msg, String fieldName,int code,PrintWriter out){
        if(code >= 400){
            String outputLine = "HTTP/1.1 " + code + " " + msg + "\r\n"
                    + "Content-Type: application/json; charset=utf-8\r\n";
            out.println(outputLine);
            return;
        }
        String codeMessage = codeMessage(code);
        String outputLine = "HTTP/1.1 " + code + " " + codeMessage + "\r\n"
                + "Content-Type: application/json; charset=utf-8\r\n"
                + "\r\n"
                +"{\""+ fieldName + "\":" + "\"" + msg + "\"" + "}";
        System.out.println("outputLine = " + outputLine);
        out.println(outputLine);
    }

    public static String codeMessage(int code){
        switch (code){
            case 200:
                return "OK";
            case 201:
                return "CREATED";
            case 400:
                return "BAD REQUEST";
            case 404:
                return "NOT FOUND";
            default:
                return "INTERNAL SERVER ERROR";
        }
    }
    public static String handleGetKey(String path){
        String[] keys = path.split("key=");
        if (keys.length < 2){
            return null;
        }
        String key = keys[1].split("&")[0];
        return keyValueStore.get(key);
    }

    public static String handleSetKeyValue(String path){
        String[] keys = path.split("key=");
        String[] values = path.split("value=");
        if (keys.length < 2 || values.length < 2){
            return null;
        }
        String key = keys[1].split("&")[0].trim();
        String value = values[1].split("&")[0].trim();
        if (key.length() > 50 || key.isEmpty() || value.length() > 50 || value.isEmpty()){
            return null;
        }
        keyValueStore.put(key, value);

        return "The key " + key + " with value " + value
                + " was stored successfully";
    }

}