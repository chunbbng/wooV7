package team.woo.dto;

import lombok.Data;

@Data
public class ScheduleDTO {
//    private Long id;
    private String name;
    private int difficulty;
    private int urgency;
    private int importance;
    private double restTime;
    private int stress;

    private String preferenceTime; // 선호 시간대 (아침, 점심 등)
    private double adjustTime; // 스케줄에 할당된 adjustTime
}
