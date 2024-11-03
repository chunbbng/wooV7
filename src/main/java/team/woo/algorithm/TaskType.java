package team.woo.algorithm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import team.woo.domain.Schedule;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class TaskType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "weight_id")
    private Weight weight;

    @OneToMany(mappedBy = "taskType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeightHistory> weightHistories = new ArrayList<>();

    @OneToMany(mappedBy = "taskType", cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    public TaskType() {
        // 기본 생성자
    }

    public TaskType(String name) {
        this.name = name;
        this.weight = Weight.createDefaultWeight(name); // 자동으로 기본 Weight 할당
        this.weight.setTaskType(this); // 양방향 관계 설정
    }

    public double calculateAdjustedTime(double estimatedTime, int difficulty, int stress, int urgency, int importance) {
        double adjustedTime = estimatedTime * (1 + (difficulty * weight.getDifficultyWeight()))
                * (1 + (stress * weight.getStressWeight()))
                * (1 - (urgency * weight.getUrgencyWeight()))
                * (1 + (importance * weight.getImportanceWeight()));

        // 결과를 반올림하여 정수로 변환
        return Math.round(adjustedTime);
    }

    // WeightHistory 추가 메서드
    public void addWeightHistory(WeightHistory weightHistory) {
        this.weightHistories.add(weightHistory);
        weightHistory.setTaskType(this);
    }

    // WeightHistory 리스트 반환
    public List<WeightHistory> getWeightHistories() {
        return this.weightHistories;
    }
}