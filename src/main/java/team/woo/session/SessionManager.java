package team.woo.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private final Map<String, HttpSession> sessionStore = new ConcurrentHashMap<>();

    public void createSession(HttpServletRequest request, Object value, String key) {
        HttpSession session = request.getSession(true);
        session.setAttribute(key, value);
        sessionStore.put(session.getId(), session);
        logger.info("새로운 세션 생성: 세션 ID = {}", session.getId());
    }

    public Object getSession(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        if (session == null || !sessionStore.containsKey(session.getId())) {
            logger.warn("세션 저장소에서 세션을 찾을 수 없습니다. 세션 ID = {}", (session != null ? session.getId() : "null"));
            return null;
        }
        return session.getAttribute(key);
    }

    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            sessionStore.remove(session.getId());
            session.invalidate();
            logger.info("세션 무효화: 세션 ID = {}", session.getId());
        }
    }
}