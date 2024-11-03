package team.woo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import team.woo.domain.Schedule;
import team.woo.domain.ScheduleRepository;
import team.woo.dto.ScheduleDTO;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/charts")
public class ChartController {
    private final ScheduleRepository scheduleRepository;

    @Autowired
    public ChartController(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    // restTime을 소수점 시간 단위로 변환하는 함수
    private double convertRestTimeToHours(String restTimeStr) {
        try {
            double hours = 0.0;

            // '시간'이 포함된 경우 처리
            if (restTimeStr.contains("시간")) {
                String[] parts = restTimeStr.split("시간");
                int hourPart = Integer.parseInt(parts[0].trim()); // 시간 부분
                hours += hourPart; // 시간은 그대로 더함

                // '분'이 포함된 경우 처리
                if (parts.length > 1 && parts[1].contains("분")) {
                    int minutePart = Integer.parseInt(parts[1].replace("분", "").trim()); // 분 부분
                    hours += minutePart / 60.0; // 분을 시간으로 변환하여 더함
                }
            }
            // '분'만 있는 경우 처리
            else if (restTimeStr.contains("분")) {
                int minutes = Integer.parseInt(restTimeStr.replace("분", "").trim()); // 분만 있는 경우
                hours += minutes / 60.0; // 분을 시간으로 변환하여 더함
            }

            return hours; // 최종 시간 반환
        } catch (NumberFormatException e) {
            // 변환 실패 시 기본값 0.0시간 반환
            log.error("Invalid format for restTime: {}", restTimeStr, e);
            return 0.0;
        }
    }

    @GetMapping("/example")
    public String show(Model model, @ModelAttribute("selectedIds") List<Long> selectedIds) {
        List<Schedule> schedules = scheduleRepository.findAllById(selectedIds);

        List<ScheduleDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> {
                    ScheduleDTO dto = new ScheduleDTO();
                    dto.setName(schedule.getName());
                    dto.setDifficulty(schedule.getDifficulty());
                    dto.setUrgency(schedule.getUrgency());
                    dto.setImportance(schedule.getImportance());

                    // restTime을 소수점 시간 단위로 변환하여 DTO에 설정
                    double restTimeInHours = convertRestTimeToHours(schedule.getRestTime());
                    dto.setRestTime(restTimeInHours); // restTime을 double로 설정

                    dto.setAdjustTime(schedule.getAdjustTime());
                    dto.setStress(schedule.getStress());

                    return dto;
                })
                .toList();

        model.addAttribute("schedules", scheduleDTOs);
        return "charts/example";
    }
}
