package team.woo.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class CalendarDTO {
    private List<String> selectedDates;// 체크 상태를 포함한 날짜 리스트
    private String scheduleName;
    private Long scheduleId;  // 스케줄 ID 추가
}