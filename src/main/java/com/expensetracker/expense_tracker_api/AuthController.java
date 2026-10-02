package com.expensetracker.expense_tracker_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        userRepository.save(user);

        // Simulam trimiterea emailului: afisam tokenul in consola
        System.out.println("=== EMAIL SIMULAT ===");
        System.out.println("Catre: " + user.getEmail());
        System.out.println("Link de verificare: http://localhost:5173/verify-email?token=" + token);
        System.out.println("======================");

        return "Cont creat! Verifica consola backend-ului pentru link-ul de confirmare (simulat).";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalid"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return "Email confirmat cu succes! Te poti autentifica acum.";
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email neconfirmat. Verifica-ti inbox-ul.");
        }

        return jwtUtil.generateToken(request.getEmail());
    }
}