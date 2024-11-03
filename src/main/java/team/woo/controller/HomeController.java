package team.woo.controller;

import jakarta.servlet.http.HttpServletRequest;  // HttpServletRequest import
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;  // Model import
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import team.woo.domain.Schedule;
import team.woo.domain.ScheduleRepository;
import team.woo.domain.ScheduleService;
import team.woo.member.Member;
import team.woo.member.MemberRepository;
import team.woo.session.SessionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final ScheduleService scheduleService;
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    private final SessionManager sessionManager;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "메인 페이지 입니다.");
        return "index";
    }

    @GetMapping("/cabinet")
    public String cabinet(Model model,HttpServletRequest request, RedirectAttributes redirectAttributes) {

        Member loginMember = (Member) sessionManager.getSession(request, "member");

        if (loginMember == null) {
            log.info("로그인 정보가 없습니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }
        String loginId = loginMember.getLoginId();
        log.info("loginMember={}", loginId);
        redirectAttributes.addAttribute("loginMember", loginMember);
        return "redirect:/cabinet/" + loginId;
    }

    @GetMapping("/cabinet/{loginID}")
    public String cabinet(@PathVariable String loginID,
                          HttpServletRequest request,
                          Model model) {

        Member loginMember = (Member) sessionManager.getSession(request, "member");

        if (loginMember == null) {
            log.info("로그인 정보가 없습니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        Member member = memberRepository.findByLoginId(loginID)
                .orElseThrow(() -> new IllegalArgumentException("Invalid login Id:" + loginID));

        List<Schedule> schedules = scheduleRepository.findByMemberId(loginMember.getId());

        // 모델에 추가합니다
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("member", member);
        model.addAttribute("schedules", schedules);
        String loginId = member.getLoginId();
        model.addAttribute("loginId", loginId);
        log.info("loginMember={}", loginId);

        return "cabinet";
    }

    @PostMapping("/cabinet")
    public String processSelectedSchedules(@RequestBody SelectionRequest request,
                                           RedirectAttributes redirectAttributes) {
        try {
            List<Long> selectedIds = request.getSelectedSchedules();

            for (Long selectedId : selectedIds) {
                log.info("selectedId={}", selectedId);
            }
            redirectAttributes.addAttribute("selectedIds", selectedIds);

            // 성공 시 리다이렉트
            return "redirect:/charts/example";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/errorPage"; // 에러 시 리다이렉트
        }
    }

    @Getter
    public static class SelectionRequest {
        private List<Long> selectedSchedules;

        public void setSelectedSchedules(List<Long> selectedSchedules) {
            this.selectedSchedules = selectedSchedules;
        }
    }

    @PostMapping("/cabinet/delete")
    public String deleteSelectedSchedules(@RequestBody SelectionRequest request, RedirectAttributes redirectAttributes) {
        try {
            List<Long> selectedIds = request.getSelectedSchedules();

            // 선택된 스케줄을 삭제합니다.
            for (Long selectedId : selectedIds) {
                scheduleRepository.deleteById(selectedId);
            }

            // 삭제 후 다시 Cabinet 페이지로 리다이렉트
            return "redirect:/cabinet";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/errorPage"; // 에러 시 리다이렉트
        }
    }
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About 페이지 입니다.");
        return "about";
    }

    @GetMapping("/result_ts/{id}")
    public String resultTs(@PathVariable Long id, Model model, HttpServletRequest request) {
        Schedule schedule = scheduleService.getSchedule(id);
        model.addAttribute("schedule", schedule);
        model.addAttribute("adjustTime", schedule.getAdjustTime());

        // TaskType ID를 schedule로부터 가져오기
        Long taskTypeId = schedule.getTaskType().getId();

        // Calculate adjusted time and get max/min adjustments
        Map<String, Object> adjustmentResults = scheduleService.calculateWeightMinMax(
                schedule.getEstimatedTime(), // 추정 시간
                schedule.getDifficulty(), // 난이도
                schedule.getStress(), // 스트레스
                schedule.getUrgency(), // 긴급도
                schedule.getImportance(), // 중요도
                taskTypeId // TaskType ID 전달
        );

        BigDecimal maxAdjustment = new BigDecimal(adjustmentResults.get("maxAdjustment").toString());
        BigDecimal minAdjustment = new BigDecimal(adjustmentResults.get("minAdjustment").toString());

        // 모델에 조정된 시간 및 최대/최소 조정값과 그 항목 이름 추가
        model.addAttribute("adjustedTime", adjustmentResults.get("adjustedTime"));
        model.addAttribute("maxAdjustment", maxAdjustment.setScale(3, RoundingMode.HALF_UP));
        model.addAttribute("minAdjustment", minAdjustment.setScale(3, RoundingMode.HALF_UP));
        model.addAttribute("maxTask", adjustmentResults.get("maxTask")); // 최대 조정값 항목 이름 추가
        model.addAttribute("minTask", adjustmentResults.get("minTask")); // 최소 조정값 항목 이름 추가

        System.out.println("adjustmentResults = " + adjustmentResults);

        // Fetch user from session, if available
        Member loginMember = (Member) sessionManager.getSession(request, "member");

        if (loginMember == null) {
            logger.warn("세션에 LOGIN_MEMBER 정보가 없음.");
            model.addAttribute("loginMember", null); // Ensure null is passed
        } else {
            model.addAttribute("loginMember", loginMember);
        }

        return "result_ts";
    }

    @PostMapping("/result_ts/{id}")
    public String updatePreference(
            @PathVariable Long id,
            @RequestParam String restTime,
            @RequestParam String preferenceTime,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        // Schedule 객체를 찾음
        Schedule schedule = scheduleService.getSchedule(id);

        // 받은 값을 Schedule 객체에 저장
        schedule.setRestTime(restTime);
        schedule.setPreferenceTime(preferenceTime);

        // 스케줄 저장
        scheduleRepository.save(schedule);

        // 모델에 필요한 데이터 추가
        model.addAttribute("schedule", schedule);
        model.addAttribute("adjustTime", schedule.getAdjustTime());

        // Fetch user from session, if available
        Member loginMember = (Member) sessionManager.getSession(request, "member");

        if (loginMember == null) {
            logger.warn("세션에 LOGIN_MEMBER 정보가 없음.");
            model.addAttribute("loginMember", null); // Ensure null is passed
        } else {
            model.addAttribute("loginMember", loginMember);
            redirectAttributes.addAttribute("loginId", loginMember.getLoginId());
            redirectAttributes.addAttribute("id",schedule.getId());
        }

        return "redirect:/calendarCheck/{loginId}/{id}"; // 업데이트 후 이동할 페이지 설정
    }
}