package team.woo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import team.woo.domain.Schedule;
import team.woo.domain.ScheduleRepository;
import team.woo.member.Member;
import team.woo.session.SessionManager;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CheckController {

    private final ScheduleRepository scheduleRepository;
    private final SessionManager sessionManager;

    @GetMapping("/check")
    public String checkMenu(Model model, HttpServletRequest request){
        Member member = (Member) sessionManager.getSession(request, "member");

        if (member == null) {
            log.info("로그인 정보가 없습니다.");
            return "redirect:/"; // 로그인 페이지로 리다이렉트
        }

        List<Schedule> schedules = scheduleRepository.findByMemberId(member.getId());

        for (Schedule schedule : schedules) {
            // taskType 이름을 직접 설정
            if (schedule.getTaskType() != null) {
                schedule.setTaskTypeName(schedule.getTaskType().getName());
            } else {
                schedule.setTaskTypeName("Unknown");
            }
        }

        log.info("schedules={}", schedules);
        model.addAttribute("schedules", schedules);
        return "check";
    }

}
