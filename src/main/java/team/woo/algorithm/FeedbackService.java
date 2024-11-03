package team.woo.algorithm;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
public class FeedbackService {

    @Autowired
    private TaskTypeRepository taskTypeRepository;

    @Transactional
    public Map<String, Map<String, Double>> processFeedback(double feedbackSum, String taskTypeName) {
        TaskType taskType = taskTypeRepository.findByName(taskTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task type: " + taskTypeName));

        Weight weight = taskType.getWeight();

        int updateCount = taskType.getWeightHistories().size() + 1;
        WeightHistory weightHistory = new WeightHistory(weight, taskType, updateCount);
        taskType.addWeightHistory(weightHistory);

        double baseLearningRate = 0.05;
        double weightChange = baseLearningRate * Math.abs(feedbackSum);

        // 변경된 가중치 적용
        if (feedbackSum > 0) {
            weight.setUrgencyWeight(weight.getUrgencyWeight() + weightChange);
            weight.setDifficultyWeight(weight.getDifficultyWeight() + weightChange);
        } else if (feedbackSum < 0) {
            weight.setStressWeight(weight.getStressWeight() - weightChange);
            weight.setImportanceWeight(weight.getImportanceWeight() - weightChange);
        }

        taskTypeRepository.save(taskType);

        // 변경 전과 후의 차이 및 값을 반환
        Map<String, Map<String, Double>> differences = new HashMap<>();
        WeightHistory lastHistory = taskType.getWeightHistories().stream()
                .max(Comparator.comparing(WeightHistory::getUpdateCount))
                .orElse(null);

        if (feedbackSum > 0) {
            differences.put("urgencyWeight", Map.of("current", weight.getUrgencyWeight(),
                    "previous", lastHistory.getUrgencyWeight(),
                    "difference", weight.getUrgencyWeight() - lastHistory.getUrgencyWeight()));
            differences.put("difficultyWeight", Map.of("current", weight.getDifficultyWeight(),
                    "previous", lastHistory.getDifficultyWeight(),
                    "difference", weight.getDifficultyWeight() - lastHistory.getDifficultyWeight()));
        } else if (feedbackSum < 0) {
            differences.put("stressWeight", Map.of("current", weight.getStressWeight(),
                    "previous", lastHistory.getStressWeight(),
                    "difference", weight.getStressWeight() - lastHistory.getStressWeight()));
            differences.put("importanceWeight", Map.of("current", weight.getImportanceWeight(),
                    "previous", lastHistory.getImportanceWeight(),
                    "difference", weight.getImportanceWeight() - lastHistory.getImportanceWeight()));
        }

        return differences;
    }
}


