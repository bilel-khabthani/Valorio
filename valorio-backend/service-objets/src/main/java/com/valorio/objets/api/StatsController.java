package com.valorio.objets.api;

import com.valorio.objets.dto.DashboardStats;
import com.valorio.objets.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardStats stats() {
        return service.getStats();
    }
}
