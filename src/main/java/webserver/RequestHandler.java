package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            int i = 0;
            StringBuffer sb = new StringBuffer();

            String line = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            do {
                line = reader.readLine();
                sb.append(line).append("\r\n");
                if (line == null)
                    break;
            } while (!"".equals(line));

            String request = sb.toString();
            String[] reqArr = request.split("\r\n");

            if (reqArr.length < 0)
                return;

            String[] header = reqArr[0].split(" ");

            String method = header[0];
            String path = header[1];
            String protocol = header[2];
            String accept = reqArr[2];

            log.debug(method + " " + path);

            byte[] body = null;

            if (path != null) {
                File file = new File("./webapp" + path);
                Scanner scanner = new Scanner(file);
                StringBuffer html = new StringBuffer();
                while (scanner.hasNext()) {
                    html.append(scanner.nextLine());
                }
                body = html.toString().getBytes();
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length, accept);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String accept) {
        try {
            String contentType = "text/html";
            if (accept.indexOf("text/css") != -1) {
                contentType = "text/css";
            }
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
