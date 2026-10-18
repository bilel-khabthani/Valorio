package com.valorio.objets.service;

import com.valorio.objets.audit.AuditLog;
import com.valorio.objets.domain.DiagnosticObjet;
import com.valorio.objets.domain.Objet;
import com.valorio.objets.domain.TypeEvenement;
import com.valorio.objets.dto.DiagnosticRequest;
import com.valorio.objets.repo.DiagnosticRepository;
import com.valorio.objets.repo.HistoriqueRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiagnosticService {

    private final DiagnosticRepository diagnostics;
    private final HistoriqueRepository historique;
    private final ObjetService objets;
    private final AuditLog audit;

    public DiagnosticService(DiagnosticRepository diagnostics,
                             HistoriqueRepository historique,
                             ObjetService objets,
                             AuditLog audit) {
        this.diagnostics = diagnostics;
        this.historique = historique;
        this.objets = objets;
        this.audit = audit;
    }

    @Transactional
    public DiagnosticObjet creer(long objetId, DiagnosticRequest request) {
        Objet objet = objets.detail(objetId);
        List<String> problemes = request.problemes() == null ? List.of() : request.problemes();
        String analyse = AnalyseDiagnostic.analyser(objet, request.panneDeclaree(), problemes);
        String diagnostic = AnalyseDiagnostic.diagnosticPreliminaire(objet.etat(), problemes);

        long id = diagnostics.creer(objetId, request.panneDeclaree(), problemes, analyse, diagnostic);
        historique.ajouter(objetId, TypeEvenement.DIAGNOSTIC, "Diagnostic préliminaire : " + diagnostic,
                objet.proprietaireId(), null);
        audit.enregistrer("OBJET_DIAGNOSTIQUE", "objet", objetId, "problemes=" + problemes.size());
        return diagnostics.findDiagnostics(objetId).stream()
                .filter(d -> d.id() == id)
                .findFirst()
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<DiagnosticObjet> lister(long objetId) {
        objets.detail(objetId);
        return diagnostics.findDiagnostics(objetId);
    }

    @Transactional(readOnly = true)
    public Optional<DiagnosticObjet> dernier(long objetId) {
        return diagnostics.findDiagnostics(objetId).stream().findFirst();
    }
}
