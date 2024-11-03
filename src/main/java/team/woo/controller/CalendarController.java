package team.woo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import team.woo.domain.Schedule;
import team.woo.domain.ScheduleRepository;
import team.woo.domain.ScheduleService;
import team.woo.dto.CalendarDTO;
import team.woo.member.Member;
import team.woo.member.MemberRepository;
import team.woo.session.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final SessionManager sessionManager;
    private final ScheduleService scheduleService;
    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;

    @PostMapping("/calendar")
    @ResponseBody
    public String saveSelectedDates(@RequestBody CalendarDTO calendarDTO, HttpServletRequest request) {
        HttpSession session = request.getSession();

        // 세션에서 멤버 ID 가져오기
        Long memberId = (Long) session.getAttribute("memberId");

        // 세션에서 scheduleId 가져오기
        Long scheduleId = (Long) session.getAttribute("scheduleId");
        if (scheduleId == null) {
            throw new IllegalArgumentException("Schedule ID not found in session.");
        }

        // CalendarDTO와 scheduleId를 ScheduleService로 전달하여 저장
        scheduleService.saveScheduleFromCalendarDTO(calendarDTO, scheduleId, memberId);

        // 세션에 CalendarDTO 저장
        session.setAttribute("calendarDTO", calendarDTO);

        log.info("Selected Dates and Schedule saved: {}", calendarDTO);

        return "저장 완료";
    }

    @GetMapping("/calendar")
    public String calendar(Model model, HttpServletRequest request) throws JsonProcessingException {
        Member loginMember = (Member) sessionManager.getSession(request, "member");

        if (loginMember == null) {
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        Long memberId = loginMember.getId();
        List<Schedule> schedules = scheduleRepository.findByMemberId(memberId);

        List<Map<String, Object>> scheduleList = new ArrayList<>();
        for (Schedule schedule : schedules) {
            Map<String, Object> scheduleMap = new HashMap<>();
            scheduleMap.put("title", schedule.getName());
            scheduleMap.put("selectedDates", schedule.getSelectedDates()); // selectedDates를 추가

            scheduleList.add(scheduleMap);  // 하나의 JSON으로 각 스케줄 정보 추가
        }

        // scheduleList를 JSON으로 변환
        String schedulesJson = new ObjectMapper().writeValueAsString(scheduleList);
        model.addAttribute("schedules", schedulesJson);

        return "calendar";
    }

    @GetMapping("/calendarCheck/{loginId}/{id}")
    public String calendarCheck(
            @PathVariable String loginId,
            @PathVariable Long id,
            Model model) {

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 loginId입니다: " + loginId));

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 일정 Id입니다: " + id));

        schedule.setAdjustDays(30);
        // 받은 값을 Schedule 객체에 저장
//        schedule.setRestTime(restTime);
//        schedule.setPreferenceTime(preferenceTime);

        // 스케줄 저장
        scheduleRepository.save(schedule);

        model.addAttribute("schedule", schedule);
        model.addAttribute("loginId", member.getLoginId());
        model.addAttribute("adjustDays", schedule.getAdjustDays());
        model.addAttribute("adjustTime", schedule.getAdjustTime());
        model.addAttribute("id", id);
        model.addAttribute("scheduleName", schedule.getName());

        return "calendarCheck";
    }

    @GetMapping("/calendarChecked/{loginId}/{checkedId}")
    public String calendarChecked(@PathVariable String loginId, @PathVariable Long checkedId, Model model) throws JsonProcessingException {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loginId: " + loginId));

        List<Schedule> schedules = scheduleRepository.findByMemberId(member.getId());
        Schedule checkedSchedule = scheduleRepository.findById(checkedId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id: " + checkedId));

        // ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 모듈 등록

        // Schedule 객체에서 startTime과 endTime을 제외한 데이터 생성
        List<Map<String, Object>> scheduleList = new ArrayList<>();
        for (Schedule schedule : schedules) {
            Map<String, Object> scheduleMap = new HashMap<>();
            scheduleMap.put("title", schedule.getName());
            scheduleMap.put("selectedDates", schedule.getSelectedDates()); // 필요한 데이터만 추가
            scheduleList.add(scheduleMap);
        }

        Map<String, Object> checkedScheduleMap = new HashMap<>();
        checkedScheduleMap.put("title", checkedSchedule.getName());
        checkedScheduleMap.put("selectedDates", checkedSchedule.getSelectedDates()); // 필요한 데이터만 추가

        // JSON으로 변환
        String schedulesJson = objectMapper.writeValueAsString(scheduleList);
        String checkedScheduleJson = objectMapper.writeValueAsString(checkedScheduleMap);

        model.addAttribute("schedules", schedulesJson);
        model.addAttribute("checkedSchedule", checkedScheduleJson);

        return "calendarChecked";
    }
}