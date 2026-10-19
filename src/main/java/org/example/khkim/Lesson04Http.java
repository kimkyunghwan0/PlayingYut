package org.example.khkim;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * [4강] HTTP
 *
 * 1. HTTP 란
 *    - 클라이언트(브라우저, 앱)가 서버에 "요청(request)"을 보내면 서버가 "응답(response)"을 돌려주는 규칙.
 *    - 요청 한 번에 응답 한 번. 서버는 이전 요청을 기억하지 않는다(stateless).
 *      그래서 로그인 유지에는 쿠키/세션/토큰을 따로 쓴다.
 *
 * 2. 요청(Request) 구성
 *    시작줄   GET /users/1 HTTP/1.1        메서드 + 경로 + 버전
 *    헤더     Host: localhost:8080          부가정보 (Content-Type, Authorization, Cookie ...)
 *    (빈 줄)
 *    본문     {"name":"김철수"}             POST/PUT 등에서 보내는 데이터
 *
 * 3. 메서드 (무엇을 하고 싶은가)
 *    GET     조회.  본문 없음. 값은 URL 쿼리스트링으로 (?page=1&size=10)
 *    POST    생성.  본문에 데이터
 *    PUT     전체 수정
 *    PATCH   일부 수정
 *    DELETE  삭제
 *
 * 4. 응답(Response) 구성
 *    상태줄   HTTP/1.1 200 OK
 *    헤더     Content-Type: application/json
 *    본문     실제 데이터
 *
 * 5. 상태코드 (결과가 어떻게 됐나)
 *    2xx 성공      200 OK, 201 Created, 204 No Content
 *    3xx 이동      301/302 리다이렉트
 *    4xx 클라이언트 잘못   400 잘못된 요청, 401 인증 안됨, 403 권한 없음, 404 없음
 *    5xx 서버 잘못         500 서버 내부 오류
 *
 * 6. URL 분해
 *    http://localhost:8080/users/1?lang=ko
 *    프로토콜 / 호스트 / 포트 / 경로(path) / 쿼리스트링
 *    http 는 기본 포트 80, https(암호화된 http)는 443.
 *
 * 7. 스프링과의 연결 (이 프로젝트가 Spring Boot)
 *    @RestController + @GetMapping("/hello") 가 바로 "GET /hello 요청이 오면 이 메서드 실행"이다.
 *    아래 예제는 스프링 없이 자바 기본 기능만으로 그 원리를 보여준다.
 *    (서버와 클라이언트를 모두 내 PC(localhost) 안에서 띄우므로 외부 통신은 없다)
 */
public class Lesson04Http {

    public static void main(String[] args) throws IOException, InterruptedException {
        // ---- 서버: 경로별로 요청을 처리하는 코드를 등록 ----
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0); // 포트 0 = 빈 포트 자동 선택

        server.createContext("/hello", exchange -> {
            String method = exchange.getRequestMethod();                 // 요청의 메서드
            String query = exchange.getRequestURI().getQuery();          // ?뒤의 쿼리스트링
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            String result = "method=" + method + ", query=" + query + ", body=" + body;
            byte[] bytes = result.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);             // 상태코드 + 본문 길이
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        // "/hello" 이외의 경로는 등록 안 했으므로 서버가 404 를 돌려준다
        server.start();
        int port = server.getAddress().getPort();
        System.out.println("서버 시작 port=" + port);

        // ---- 클라이언트: 요청을 만들어 보내고 응답을 받는다 ----
        HttpClient client = HttpClient.newHttpClient();
        String base = "http://localhost:" + port;

        // 1) GET : 쿼리스트링으로 값 전달
        HttpRequest get = HttpRequest.newBuilder(URI.create(base + "/hello?name=kim")).GET().build();
        print("GET", client.send(get, HttpResponse.BodyHandlers.ofString()));

        // 2) POST : 본문에 데이터 전달
        HttpRequest post = HttpRequest.newBuilder(URI.create(base + "/hello"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"김철수\"}"))
                .build();
        print("POST", client.send(post, HttpResponse.BodyHandlers.ofString()));

        // 3) 없는 경로 : 404
        HttpRequest notFound = HttpRequest.newBuilder(URI.create(base + "/nothing")).GET().build();
        print("GET /nothing", client.send(notFound, HttpResponse.BodyHandlers.ofString()));

        server.stop(0);
    }

    private static void print(String title, HttpResponse<String> res) {
        System.out.println("[" + title + "] 상태코드=" + res.statusCode() + ", 본문=" + res.body());
    }
}
