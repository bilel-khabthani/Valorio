package com.valorio.auth.service;

import com.valorio.auth.domain.CibleSignalement;
import com.valorio.auth.domain.Signalement;
import com.valorio.auth.domain.StatutSignalement;
import com.valorio.auth.dto.SignalementRequest;
import com.valorio.auth.dto.SignalementStats;
import com.valorio.auth.repo.SignalementRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Traitement des signalements (module 1, sous-module 1.3 « signaler un comportement »). */
@Service
public class SignalementService {

    private final SignalementRepository repository;

    public SignalementService(SignalementRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Signalement creer(SignalementRequest request, Long signaleurParDefaut) {
        Long signaleurId = request.signaleurId() != null ? request.signaleurId() : signaleurParDefaut;
        if (signaleurId == null) {
            throw new IllegalStateException("Signaleur inconnu : impossible d'enregistrer le signalement");
        }
        CibleSignalement cible = CibleSignalement.depuis(request.cibleType());
        long id = repository.creer(signaleurId, cible, request.cibleId(), request.motif().trim(),
                request.description());
        return detail(id);
    }

    @Transactional(readOnly = true)
    public Signalement detail(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Signalement " + id + " introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Signalement> lister(StatutSignalement statut, CibleSignalement cibleType) {
        return repository.lister(statut, cibleType);
    }

    @Transactional
    public Signalement traiter(long id, StatutSignalement statut, Long traitePar, String note) {
        if (statut == null || statut == StatutSignalement.OUVERT) {
            throw new IllegalArgumentException("Le statut de traitement doit être EN_COURS, TRAITE ou REJETE");
        }
        detail(id);
        repository.traiter(id, statut, traitePar, note);
        return detail(id);
    }

    @Transactional(readOnly = true)
    public SignalementStats statistiques() {
        List<Signalement> tous = repository.lister(null, null);
        long ouverts = tous.stream().filter(s -> s.statut() == StatutSignalement.OUVERT).count();
        long enCours = tous.stream().filter(s -> s.statut() == StatutSignalement.EN_COURS).count();
        long traites = tous.stream().filter(s -> s.statut() == StatutSignalement.TRAITE).count();
        long rejetes = tous.stream().filter(s -> s.statut() == StatutSignalement.REJETE).count();

        TreeMap<String, Long> parCible = new TreeMap<>();
        for (CibleSignalement cible : CibleSignalement.values()) {
            parCible.put(cible.name(), 0L);
        }
        for (Signalement s : tous) {
            parCible.merge(s.cibleType().name(), 1L, Long::sum);
        }
        return new SignalementStats(tous.size(), ouverts, enCours, traites, rejetes, parCible);
    }
}
