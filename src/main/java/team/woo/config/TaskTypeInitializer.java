package team.woo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import team.woo.algorithm.TaskType;
import team.woo.algorithm.TaskTypeRepository;
import team.woo.algorithm.Weight;

@Component
public class TaskTypeInitializer implements CommandLineRunner {

    @Autowired
    private TaskTypeRepository taskTypeRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeTaskType("회의");
        initializeTaskType("학습");
        initializeTaskType("운동");
        initializeTaskType("보고서 작성");
        initializeTaskType("가사일");
        initializeTaskType("창의적인 작업");
    }

    private void initializeTaskType(String name) {
        if (taskTypeRepository.findByName(name).isEmpty()) {
            // TaskType 생성
            TaskType taskType = new TaskType(name);

            // TaskType에 기본 Weight 설정
            Weight weight = Weight.createDefaultWeight(name);
            taskType.setWeight(weight);
            weight.setTaskType(taskType);

            // TaskType과 연관된 Weight를 함께 저장
            taskTypeRepository.save(taskType);
        }
    }
}