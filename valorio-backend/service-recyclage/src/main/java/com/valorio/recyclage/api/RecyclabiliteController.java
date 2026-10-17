package com.valorio.recyclage.api;

import com.valorio.recyclage.domain.AnalyseRecyclabilite;
import com.valorio.recyclage.service.RecyclageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/objets")
public class RecyclabiliteController {

    private final RecyclageService service;

    public RecyclabiliteController(RecyclageService service) {
        this.service = service;
    }

    @GetMapping("/{objetId}/recyclabilite")
    public AnalyseRecyclabilite analyser(@PathVariable long objetId) {
        return service.analyser(objetId);
    }
}
