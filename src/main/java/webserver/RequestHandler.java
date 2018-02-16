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

            log.debug("read request...");
            String line = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            do {
                line = reader.readLine();
                sb.append(line).append("\r\n");
                if (line == null)
                    break;
            } while (!"".equals(line));

            log.debug("read request finish");

            String request = sb.toString();
            String[] reqArr = request.split("\r\n");

            String[] header = reqArr[0].split(" ");

            String method = header[0];
            String path = header[1];
            String protocol = header[2];

            byte[] body = null;

            if ("/index.html".equals(path)) {
                File file = new File("./webapp/index.html");
                Scanner scanner = new Scanner(file);
                StringBuffer html = new StringBuffer();
                while (scanner.hasNext()) {
                    html.append(scanner.nextLine());
                }
                body = html.toString().getBytes();
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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
