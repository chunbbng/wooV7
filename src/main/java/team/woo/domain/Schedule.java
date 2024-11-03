package team.woo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import team.woo.algorithm.TaskType;
import team.woo.algorithm.Weight;
import team.woo.member.Member;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

//    private LocalDate startTime;
//    private LocalDate deadLine;
    private int difficulty;
    private int importance;
    private int urgency;
    private String restTime;
    private int stress;
    private String preferenceTime;
    private String option;

    @ElementCollection
    private List<String> selectedDates;

    private int adjustDays = 0; // 기본 값 설정
    private double adjustTime = 0.0; // 기본 값 설정

    // Member와의 연관관계 대신 단순히 memberId를 저장
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "existing_foreign_key_column_name")
    @JsonIgnore
    @ToString.Exclude
    private Member member;

    // TaskType과의 ManyToOne 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_type_id")  // 외래 키 설정
    @JsonIgnore
    private TaskType taskType;

    // 예상 소요 시간 저장 필드
    private double estimatedTime;

    @Transient // JPA에서 관리하지 않을 필드
    private String taskTypeName;

    public void setTaskTypeName() {
        if (this.taskType != null) {
            this.taskTypeName = this.taskType.getName();
        } else {
            this.taskTypeName = "Unknown";
        }
    }

    public Schedule() {
        // 기본 생성자
    }

    // 로그인한 사용자의 Schedule 생성 시 사용
    public Schedule(String name, int difficulty,
                    int importance, int urgency, int stress, Long memberId, TaskType taskType, double estimatedTime) {
        this.name = name;
        this.difficulty = difficulty;
        this.importance = importance;
        this.urgency = urgency;
        this.stress = stress;
        this.memberId = memberId;
        this.adjustDays = 0;
        this.adjustTime = 0.0;
        this.taskType = taskType;
        this.estimatedTime = estimatedTime;  // 예상 소요 시간
    }

    // 비로그인 사용자의 Schedule 생성 시 사용
    public Schedule(String name, int difficulty,
                    int importance, int urgency, int stress, TaskType taskType, double estimatedTime) {
        this.name = name;
        this.difficulty = difficulty;
        this.importance = importance;
        this.urgency = urgency;
        this.stress = stress;
        this.memberId = null;
        this.adjustDays = 0;
        this.adjustTime = 0.0;
        this.taskType = taskType;
        this.estimatedTime = estimatedTime;  // 예상 소요 시간
    }

    public void calculateAndSetAdjustTime() {
        if (taskType != null) {
            // TaskType의 메서드를 호출하여 조정된 시간을 계산
            this.adjustTime = taskType.calculateAdjustedTime(this.estimatedTime, this.difficulty, this.stress, this.urgency, this.importance);  // 계산된 값을 adjustTime에 설정
        }
    }
}