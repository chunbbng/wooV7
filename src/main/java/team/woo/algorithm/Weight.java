package team.woo.algorithm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Weight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double difficultyWeight;
    private double stressWeight;
    private double urgencyWeight;
    private double importanceWeight;

    @OneToOne(mappedBy = "weight", cascade = CascadeType.ALL)
    private TaskType taskType;

    public Weight() {
        // 기본 생성자
    }

    public Weight(double difficultyWeight, double stressWeight, double urgencyWeight, double importanceWeight) {
        this.difficultyWeight = difficultyWeight;
        this.stressWeight = stressWeight;
        this.urgencyWeight = urgencyWeight;
        this.importanceWeight = importanceWeight;
    }

    // 자동으로 기본 가중치를 설정하는 메서드
    public static Weight createDefaultWeight(String taskTypeName) {
        Weight weight;
        switch (taskTypeName) {
            case "회의":
                weight = new Weight(0.04, 0.05, 0.025, 0.08);
                break;
            case "학습":
                weight = new Weight(0.05, 0.07, 0.03, 0.06); // 학습의 난이도를 조금 낮게 설정
                break;
            case "운동":
                weight = new Weight(0.06, 0.04, 0.035, 0.07); // 운동은 스트레스보다 긴급도를 조금 높임
                break;
            case "보고서 작성":
                weight = new Weight(0.08, 0.09, 0.05, 0.1); // 보고서 작성의 중요도를 더 강조
                break;
            case "가사일":
                weight = new Weight(0.03, 0.02, 0.02, 0.03); // 가사일의 긴급도와 중요도를 낮춤
                break;
            case "창의적인 작업":
                weight = new Weight(0.07, 0.1, 0.04, 0.09); // 창의적인 작업은 스트레스와 중요도를 높임
                break;
            default:
                throw new IllegalArgumentException("Invalid task type: " + taskTypeName);
        }
        return weight;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }
}