package com.simple.httpserver;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sun.misc.IOUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
//import java.util.logging.Logger;

public class MyHttpHandler implements HttpHandler {
    static final Logger log = Logger.getLogger(String.valueOf(MyHttpHandler.class));

    String url = "jdbc:mysql://localhost:3306/Account?useSSL=false";
    String userName = "root";
    String psw = "B@0783369391m1";

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        String requestParamValue = null;
        log.info(httpExchange.getRequestMethod());
        if ("GET".equals(httpExchange.getRequestMethod())) {

            requestParamValue = handleGetRequest(httpExchange);

        } else if ("POST".equals(httpExchange.getRequestMethod())) {

            requestParamValue = handlePostRequest(httpExchange);
        }
        handleResponse(httpExchange, requestParamValue);
    }
    private String handlePostRequest(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        byte[] result = IOUtils.readAllBytes(inputStream);
        String jsonString = new String(result, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(jsonString, User.class);
//        log.info(httpExchange.getRequestHeaders().toString());

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, userName, psw);
            String query = "INSERT INTO details (EMAIL,FIRSTNAME,SURNAME,DATEOFBIRTH,PHONENUMBER,ADDRESS,PASSWORD )"
                    + "VALUES(?,?,?,?,?,?,?)";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            preparedStmt.setString(1, user.getEmail());
            preparedStmt.setString(2, user.getFirstName());
            preparedStmt.setString(3, user.getSurname());
            preparedStmt.setString(4, user.getDateOfBirth());
            preparedStmt.setString(5, user.getPhoneNumber());
            preparedStmt.setString(6, user.getAddress());
            preparedStmt.setString(7, user.getPassword());
            preparedStmt.execute();
            Statement st = con.createStatement();
            String sql= "SELECT * FROM details WHERE EMAIL='"+user.getEmail()+"';";
            ResultSet rs = st.executeQuery(sql);
            rs.next();
            user.setAccountID(rs.getLong("accountID"));
            con.close();
            String userString= mapper.writeValueAsString(user);
            return userString;
        } catch (Exception e) {

            log.severe(e.toString());

        }

return " {" + "\"error\":\"error adding user\"\n" +"}";

    }

    private String handleGetRequest(HttpExchange httpExchange) throws IOException {
//get users from db

        List<User> users = new ArrayList<>();

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, userName, psw);
            Statement st = con.createStatement();
            String query = "SELECT * FROM details";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                User user = new User();
                user.setEmail(rs.getString("email"));
                user.setAccountID(rs.getLong("accountID"));
                user.setSurname(rs.getString("surname"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setAddress(rs.getString("address"));
                user.setFirstName(rs.getString("firstName"));
                user.setDateOfBirth(rs.getString("dateOfBirth"));
                users.add(user);
            }
        } catch (Exception e) {
            log.severe("got exception");
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, users);
        final byte[] data = out.toByteArray();
        String result = new String(data);
        return result;
    }
    private void handleResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(requestParamValue);
        String htmlResponse = htmlBuilder.toString();
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.getResponseHeaders().add("Status", "200");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST");
        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
