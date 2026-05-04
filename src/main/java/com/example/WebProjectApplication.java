package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SpringBootApplication
@RestController
public class WebProjectApplication {

    @Value("${database.url:jdbc:mysql://localhost:3306/test}")
    private String databaseUrl;

    @Value("${database.user:root}")
    private String databaseUser;

    @Value("${database.password:password}")
    private String databasePassword;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        SpringApplication.run(WebProjectApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "<html>" +
                "<head><title>CloudFolks HUB</title></head>" +
                "<body style='text-align:center; background-color:#f0f8ff;'>" +
                "<h1 style='color: #4CAF50;'>Welcome to <span style='color: #00008B;'>CloudFolks HUB</span>!</h1>" +
                "<p style='font-size:20px; color: #555;'>Empowering Your DevOps Journey</p>" +
                "<form method='post' action='/submit' style='margin-top:20px;'>" +
                "<label>Name:</label><br>" +
                "<input type='text' name='name' required><br><br>" +
                "<label>Email:</label><br>" +
                "<input type='email' name='email' required><br><br>" +
                "<button type='submit'>Submit</button>" +
                "</form>" +
                "</body>" +
                "</html>";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam String name, @RequestParam String email) {
        return "<html>" +
                "<head><title>Form Submitted</title></head>" +
                "<body style='text-align:center; background-color:#f0f8ff;'>" +
                "<h1 style='color: #4CAF50;'>Thank You, " + escapeHtml(name) + "!</h1>" +
                "<p style='font-size:20px; color: #555;'>Your email (" + escapeHtml(email) + ") submitted successfully.</p>" +
                "</body>" +
                "</html>";
    }

    @GetMapping("/redirect")
    public String redirect(@RequestParam String url) {
        if (!isValidRedirect(url)) {
            return "Invalid Redirect URL!";
        }
        return "<script>window.location.href='" + escapeHtml(url) + "'</script>";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        String storedHashedPassword = getUserPassword(username);

        if (storedHashedPassword != null && passwordEncoder.matches(password, storedHashedPassword)) {
            return "Login Successful!";
        }
        return "Invalid Credentials!";
    }

    @GetMapping("/fetchUser")
    public String fetchUser(@RequestParam String username) {
        try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "User found: " + escapeHtml(rs.getString("username"));
            }

        } catch (Exception e) {
            e.printStackTrace(); // helpful for debugging
            return "Database Error!";
        }
        return "User not found";
    }

    // Escape HTML (basic protection)
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("<", "&lt;").replace(">", "&gt;");
    }

    // Restrict redirect URLs
    private boolean isValidRedirect(String url) {
        return url != null && url.startsWith("https://trustedsite.com");
    }

    // Simulated password fetch (IMPORTANT FIX)
    private String getUserPassword(String username) {
        // Always return SAME hash (not re-encode every time)
        return "$2a$10$7QJv1Q8Q7vYl6u2zj1G9UuJrY7j0zJ9Z6W6kFQ5Yl5eK9xkX1Zc1G"; 
        // password = "securepassword"
    }
}
