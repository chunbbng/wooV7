package team.woo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import team.woo.session.SessionManager;

import java.util.Map;

@RestController
public class AuthController {

    private final SessionManager sessionManager;

    public AuthController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @GetMapping("/check-login")
    public Map<String, Boolean> checkLogin(HttpServletRequest request) {
        Object member = sessionManager.getSession(request, "member"); // 세션에서 사용자 정보를 가져옴
        boolean isLoggedIn = (member != null);
        return Map.of("isLoggedIn", isLoggedIn);
    }
}
