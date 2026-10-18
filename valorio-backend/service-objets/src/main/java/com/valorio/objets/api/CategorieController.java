package com.valorio.objets.api;

import com.valorio.objets.domain.Categorie;
import com.valorio.objets.dto.CategorieRequest;
import com.valorio.objets.service.CategorieService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    private final CategorieService service;

    public CategorieController(CategorieService service) {
        this.service = service;
    }

    @GetMapping
    public List<Categorie> lister() {
        return service.lister();
    }

    @GetMapping("/{categorieId}")
    public Categorie consulter(@PathVariable long categorieId) {
        return service.detail(categorieId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categorie creer(@Valid @RequestBody CategorieRequest request) {
        return service.creer(request);
    }
}
