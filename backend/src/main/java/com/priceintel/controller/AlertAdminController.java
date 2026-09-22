package com.priceintel.controller;

import com.priceintel.service.AlertEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/alerts")
public class AlertAdminController {
    private final AlertEvaluationService evaluation;
    public AlertAdminController(AlertEvaluationService evaluation) { this.evaluation = evaluation; }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate active price alerts against current landed prices")
    public AlertEvaluationService.EvaluationReport evaluate() {
        return evaluation.evaluateActiveAlerts();
    }
}
