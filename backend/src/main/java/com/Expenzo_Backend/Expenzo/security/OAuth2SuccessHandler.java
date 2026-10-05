package com.Expenzo_Backend.Expenzo.security;

import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        assert oAuth2User != null;
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendUrl + "/login?error=email_not_found");
            return;
        }

        // ✅ Find or create the user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setUsername(generateUniqueUsername(email));
            newUser.setImage(picture);
            newUser.setCreatedAt(LocalDateTime.now());

            // ✅ OAuth-specific flags
            newUser.setProvider("GOOGLE");
            newUser.setEmailVerified(true);       // Google already verified
            newUser.setPassword(null);            // No local password
            newUser.setMustChangePassword(false);
            newUser.setOtp(null);
            newUser.setOtpExpiry(null);

            System.out.println("✅ Created new GOOGLE user: " + email);
            return userRepository.save(newUser);
        });

        // ✅ Existing user — refresh flags in case they were created wrong
        boolean needsUpdate = false;

        if (!"GOOGLE".equals(user.getProvider())) {
            user.setProvider("GOOGLE");
            needsUpdate = true;
        }
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            needsUpdate = true;
        }
        if (user.isMustChangePassword()) {
            user.setMustChangePassword(false);
            needsUpdate = true;
        }
        if (user.getOtp() != null) {
            user.setOtp(null);
            user.setOtpExpiry(null);
            needsUpdate = true;
        }
        if (user.getImage() == null && picture != null) {
            user.setImage(picture);
            needsUpdate = true;
        }

        if (needsUpdate) {
            userRepository.save(user);
            System.out.println("🔁 Refreshed GOOGLE user flags for: " + email);
        }

        // ✅ Build UserDetails and generate JWT
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String token = jwtService.generateToken(userDetails);

        response.sendRedirect(frontendUrl + "/oauth-success?token=" + token);
    }

    /**
     * Generate a unique username from the email's local part,
     * appending a numeric suffix if the base is already taken.
     */
    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        if (base.length() < 3) base = "user" + base;

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}