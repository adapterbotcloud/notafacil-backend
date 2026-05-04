package br.com.notafacil.controller;

import br.com.notafacil.entity.*;
import br.com.notafacil.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lookup")
public class LookupController {

    private final NbsRepository nbsRepo;
    private final CstIbsCbsRepository cstRepo;
    private final ClassificacaoTributariaRepository classRepo;
    private final IndicadorOperacaoRepository indOpRepo;
    private final CorrelacaoLc116NbsRepository correlacaoRepo;

    public LookupController(NbsRepository nbsRepo,
                             CstIbsCbsRepository cstRepo,
                             ClassificacaoTributariaRepository classRepo,
                             IndicadorOperacaoRepository indOpRepo,
                             CorrelacaoLc116NbsRepository correlacaoRepo) {
        this.nbsRepo = nbsRepo;
        this.cstRepo = cstRepo;
        this.classRepo = classRepo;
        this.indOpRepo = indOpRepo;
        this.correlacaoRepo = correlacaoRepo;
    }

    // ─── NBS ─────────────────────────────────────────────────────────────────────

    @GetMapping("/nbs")
    public List<NbsEntity> listarNbs() {
        return nbsRepo.findByHabilitadoTrue();
    }

    @GetMapping("/nbs/{codigo}")
    public ResponseEntity<NbsEntity> buscarNbs(@PathVariable String codigo) {
        return nbsRepo.findByCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nbs/busca")
    public List<NbsEntity> buscarNbsPorTermo(@RequestParam String q) {
        return nbsRepo.findByDescricaoContainingIgnoreCase(q);
    }

    // ─── CST IBS/CBS ─────────────────────────────────────────────────────────────

    @GetMapping("/cst")
    public List<CstIbsCbsEntity> listarCst() {
        return cstRepo.findByHabilitadoTrue();
    }

    @GetMapping("/cst/{codigo}")
    public ResponseEntity<CstIbsCbsEntity> buscarCst(@PathVariable String codigo) {
        return cstRepo.findByCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Classificação Tributária ────────────────────────────────────────────────

    @GetMapping("/classificacao-tributaria")
    public List<ClassificacaoTributariaEntity> listarClassificacao() {
        return classRepo.findByHabilitadoTrue();
    }

    @GetMapping("/classificacao-tributaria/{codigo}")
    public ResponseEntity<ClassificacaoTributariaEntity> buscarClassificacao(@PathVariable String codigo) {
        return classRepo.findByCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Indicador de Operação ───────────────────────────────────────────────────

    @GetMapping("/indicador-operacao")
    public List<IndicadorOperacaoEntity> listarIndicadorOperacao() {
        return indOpRepo.findByHabilitadoTrue();
    }

    @GetMapping("/indicador-operacao/{codigo}")
    public ResponseEntity<IndicadorOperacaoEntity> buscarIndicadorOperacao(@PathVariable String codigo) {
        return indOpRepo.findByCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Correlação LC 116 → NBS/IndOp/ClassTrib/CST ────────────────────────────

    /**
     * Dado um item da lista de serviços (LC 116), retorna a correlação com
     * NBS, Indicador de Operação, Classificação Tributária e CST sugeridos.
     * Útil para autocompletar no cadastro de empresa.
     */
    @GetMapping("/correlacao/{itemListaServico}")
    public ResponseEntity<?> buscarCorrelacao(@PathVariable String itemListaServico) {
        var correlacao = correlacaoRepo.findFirstByItemListaServicoAndHabilitadoTrue(itemListaServico);
        if (correlacao.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var c = correlacao.get();
        return ResponseEntity.ok(Map.of(
                "itemListaServico", c.getItemListaServico(),
                "descricaoLc116", c.getDescricaoLc116() != null ? c.getDescricaoLc116() : "",
                "nbsCodigo", c.getNbsCodigo(),
                "indicadorOperacaoCodigo", c.getIndicadorOperacaoCodigo() != null ? c.getIndicadorOperacaoCodigo() : "",
                "classificacaoTributariaCodigo", c.getClassificacaoTributariaCodigo() != null ? c.getClassificacaoTributariaCodigo() : "",
                "cstPadrao", c.getCstPadrao() != null ? c.getCstPadrao() : ""
        ));
    }

    /**
     * Lista todas as correlações disponíveis (para popular um select de itens LC 116).
     */
    @GetMapping("/correlacao")
    public List<CorrelacaoLc116NbsEntity> listarCorrelacoes() {
        return correlacaoRepo.findByHabilitadoTrue();
    }
}
