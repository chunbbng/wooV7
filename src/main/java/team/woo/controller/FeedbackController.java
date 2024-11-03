package team.woo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.woo.algorithm.FeedbackService;

import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/{taskTypeName}/feedback")
    public ResponseEntity<Map<String, Map<String, Double>>> submitFeedback(
            @PathVariable String taskTypeName,
            @RequestBody Map<String, Object> feedbackData) {

        double feedbackSum = ((Number) feedbackData.get("feedbackSum")).doubleValue();
        Map<String, Map<String, Double>> differences = feedbackService.processFeedback(feedbackSum, taskTypeName);

        return ResponseEntity.ok(differences);
    }
}


