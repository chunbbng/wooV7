package team.woo.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;  // 올바른 Model 클래스 임포트
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import team.woo.domain.Schedule;
import team.woo.domain.ScheduleService; // ScheduleService 임포트 추가
import team.woo.member.Member;
import team.woo.member.MemberRepository;
import team.woo.session.SessionManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;
    private final ScheduleService scheduleService;  // ScheduleService 주입 추가

    public MemberController(MemberRepository memberRepository, SessionManager sessionManager, ScheduleService scheduleService) {
        this.memberRepository = memberRepository;
        this.sessionManager = sessionManager;
        this.scheduleService = scheduleService;
    }

    @PostMapping("/login")
    @ResponseBody
    public LoginResponse login(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        Optional<Member> member = memberRepository.findByLoginId(username);

        if (member.isPresent() && member.get().getPassword().equals(password)) {
            sessionManager.createSession(request, member.get(), "member");  // 세션에 "member" 키로 저장
            logger.info("로그인 성공: 사용자 ID = {}, 세션 ID = {}", member.get().getId(), request.getSession().getId());
            return new LoginResponse(true, member.get().getName());
        } else {
            logger.warn("로그인 실패: 잘못된 로그인 시도. 사용자명 = {}", username);
            return new LoginResponse(false, "Invalid username or password.");
        }
    }

    @PostMapping("/signup")
    @ResponseBody
    public SignupResponse signup(@RequestParam String username, @RequestParam String name, @RequestParam String password) {
        Optional<Member> existingMember = memberRepository.findByLoginId(username);
        if (existingMember.isPresent()) {
            logger.warn("회원가입 실패: 사용자명 이미 존재 = {}", username);
            return new SignupResponse(false, "Username already exists.");
        }

        Member newMember = new Member();
        newMember.setLoginId(username);
        newMember.setName(name);
        newMember.setPassword(password);

        memberRepository.save(newMember);
        logger.info("회원가입 성공: 사용자 ID = {}", newMember.getId());

        return new SignupResponse(true, name);
    }

    @PostMapping("/logout")
    @ResponseBody
    public void logout(HttpServletRequest request) {
        sessionManager.invalidateSession(request);  // SessionManager를 사용해 세션 무효화
        logger.info("로그아웃: 세션 무효화");
    }

    // 내부 클래스: 로그인 응답을 위한 클래스
    static class LoginResponse {
        private boolean success;
        private String message;

        public LoginResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    @GetMapping("/status")
    @ResponseBody
    public LoginStatusResponse checkLoginStatus(HttpServletRequest request) {
        Object sessionObject = sessionManager.getSession(request, "member");

        if (sessionObject instanceof Member) {  // Object 타입을 Member로 캐스팅
            Member loggedInMember = (Member) sessionObject;
            return new LoginStatusResponse(true, loggedInMember.getName());
        } else {
            return new LoginStatusResponse(false, "Not logged in");
        }
    }


    @Getter
    static class LoginStatusResponse {
        private final boolean loggedIn;
        private final String name;

        public LoginStatusResponse(boolean loggedIn, String name) {
            this.loggedIn = loggedIn;
            this.name = name;
        }

    }


    // 내부 클래스: 회원가입 응답을 위한 클래스
    static class SignupResponse {
        private boolean success;
        private String message;

        public SignupResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}