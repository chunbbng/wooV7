package team.woo.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.woo.algorithm.TaskType;
import team.woo.algorithm.TaskTypeRepository;
import team.woo.algorithm.Weight;
import team.woo.dto.CalendarDTO;
import team.woo.member.Member;
import team.woo.member.MemberRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final TaskTypeRepository taskTypeRepository;

    // 스케줄을 저장하고 adjustTime을 계산하는 메서드
    @Transactional
    public Schedule saveSchedule(Schedule schedule, Long memberId) {
        // TaskType에서 관련 가중치를 가져와 adjustTime 계산
        TaskType taskType = schedule.getTaskType();
        if (taskType != null) {
            double adjustedTime = taskType.calculateAdjustedTime(
                    schedule.getEstimatedTime(),
                    schedule.getDifficulty(),
                    schedule.getStress(),
                    schedule.getUrgency(),
                    schedule.getImportance()
            );
            schedule.setAdjustTime(adjustedTime);
        }

        // Member 정보와 연관 설정
        if (memberId != null) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid member Id: " + memberId));
            schedule.setMember(member);
        }

        return scheduleRepository.save(schedule);
    }

    // TaskType을 포함하여 Schedule 객체를 생성하고 저장하는 메서드
    @Transactional
    public Schedule saveSchedule(String name, int difficulty, int urgency, int importance, String restTime, int stress, String preferenceTime, String taskTypeName, Long memberId, double estimatedTime) {
        // taskTypeName으로 TaskType 엔티티 조회
        TaskType taskType = taskTypeRepository.findByName(taskTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task type: " + taskTypeName));

        Schedule schedule = new Schedule(name, difficulty, importance, urgency, stress, taskType, estimatedTime);
        return saveSchedule(schedule, memberId);
    }

    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        // 스케줄 업데이트 시에도 adjustTime 계산
        TaskType taskType = schedule.getTaskType();
        if (taskType != null) {
            double adjustedTime = taskType.calculateAdjustedTime(
                    schedule.getEstimatedTime(),
                    schedule.getDifficulty(),
                    schedule.getStress(),
                    schedule.getUrgency(),
                    schedule.getImportance()
            );
            schedule.setAdjustTime(adjustedTime);
        }

        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public Schedule getSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByMemberId(Long memberId) {
        return scheduleRepository.findByMemberId(memberId);
    }

    /**
     * CalendarDTO로부터 선택된 날짜 및 스케줄 정보를 저장하는 메서드 (스케줄 ID 사용)
     */
    @Transactional
    public Schedule saveScheduleFromCalendarDTO(CalendarDTO calendarDTO, Long scheduleId, Long memberId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule ID: " + scheduleId));

        // 선택된 날짜를 Schedule의 selectedDates로 설정
        schedule.setSelectedDates(calendarDTO.getSelectedDates());

        // 필요에 따라 Member 정보도 업데이트
        if (memberId != null) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid member Id: " + memberId));
            schedule.setMember(member);
        }

        // adjustTime 다시 계산
        TaskType taskType = schedule.getTaskType();
        if (taskType != null) {
            double adjustedTime = taskType.calculateAdjustedTime(
                    schedule.getEstimatedTime(),
                    schedule.getDifficulty(),
                    schedule.getStress(),
                    schedule.getUrgency(),
                    schedule.getImportance()
            );
            schedule.setAdjustTime(adjustedTime);
        }

        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with ID: " + scheduleId));
    }

    public Map<String, Object> calculateWeightMinMax(double estimatedTime, int difficulty, int stress, int urgency, int importance, Long taskTypeId) {
        // TaskType ID로 Weight 가져오기
        TaskType taskType = taskTypeRepository.findById(taskTypeId).orElse(null);
        if (taskType == null || taskType.getWeight() == null) {
            throw new IllegalArgumentException("Invalid task type or weight not found.");
        }

        Weight weight = taskType.getWeight();

        // 가중치 조정 결과를 저장할 맵 생성
        Map<String, Double> results = new HashMap<>();

        double difficultyAdjustment = difficulty * weight.getDifficultyWeight();
        double stressAdjustment = stress * weight.getStressWeight();
        double urgencyAdjustment = urgency * weight.getUrgencyWeight(); // 부호 변경
        double importanceAdjustment = importance * weight.getImportanceWeight();

        // 결과를 맵에 추가
        results.put("난이도", difficultyAdjustment);
        results.put("스트레스", stressAdjustment);
        results.put("긴급도", urgencyAdjustment);
        results.put("중요도", importanceAdjustment);

        // 조정된 시간 계산
        double adjustedTime = estimatedTime * (1 + difficultyAdjustment) * (1 + stressAdjustment) * (1 + urgencyAdjustment) * (1 + importanceAdjustment);

        // 결과를 반올림하여 정수로 변환
        double roundedAdjustedTime = Math.round(adjustedTime);

        // 최대값과 최소값 찾기
        String maxTask = null;
        String minTask = null;
        double maxAdjustment = Double.NEGATIVE_INFINITY; // 초기값 설정
        double minAdjustment = Double.POSITIVE_INFINITY; // 초기값 설정

        for (Map.Entry<String, Double> entry : results.entrySet()) {
            if (entry.getValue() > maxAdjustment) {
                maxAdjustment = entry.getValue();
                maxTask = entry.getKey(); // 최대값의 키 저장
            }
            if (entry.getValue() < minAdjustment) {
                minAdjustment = entry.getValue();
                minTask = entry.getKey(); // 최소값의 키 저장
            }
        }

        // 결과를 맵으로 반환
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("adjustedTime", roundedAdjustedTime);
        resultMap.put("maxAdjustment", maxAdjustment); // 최대 조정값
        resultMap.put("maxTask", maxTask); // 최대 조정값에 대한 태스크 이름
        resultMap.put("minAdjustment", minAdjustment); // 최소 조정값
        resultMap.put("minTask", minTask); // 최소 조정값에 대한 태스크 이름

        return resultMap;
    }
}