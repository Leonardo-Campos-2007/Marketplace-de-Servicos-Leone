package com.br.leone.controller;

import com.br.leone.entity.CategoriaServico;
import com.br.leone.service.CategoriaServicoService;
import com.br.leone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaServicoController {

    private final CategoriaServicoService categoriaServicoService;
    private final UserService userService;

    public CategoriaServicoController(CategoriaServicoService categoriaServicoService, UserService userService) {
        this.categoriaServicoService = categoriaServicoService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaServico> criar(@Valid @RequestBody CategoriaServico categoria) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaServicoService.criar(categoria));
    }

    @PostMapping("/sugerir")
    public ResponseEntity<CategoriaServico> sugerir(@Valid @RequestBody CategoriaServico categoria, Authentication authentication) {
        Long usuarioId = obterUsuarioId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaServicoService.sugerir(categoria, usuarioId));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<CategoriaServico> aprovar(@PathVariable Long id, Authentication authentication) {
        exigirAdmin(authentication);
        return ResponseEntity.ok(categoriaServicoService.aprovarSugestao(id));
    }

    @DeleteMapping("/{id}/rejeitar")
    public ResponseEntity<Void> rejeitar(@PathVariable Long id, Authentication authentication) {
        exigirAdmin(authentication);
        categoriaServicoService.rejeitarSugestao(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<CategoriaServico>> listarPendentes(Authentication authentication) {
        exigirAdmin(authentication);
        return ResponseEntity.ok(categoriaServicoService.listarPendentes());
    }

    @GetMapping
    public ResponseEntity<List<CategoriaServico>> listarTodos() {
        return ResponseEntity.ok(categoriaServicoService.listarTodos());
    }

    @GetMapping("/raizes")
    public ResponseEntity<List<CategoriaServico>> listarRaizes() {
        return ResponseEntity.ok(categoriaServicoService.listarRaizes());
    }

    @GetMapping("/{id}/subcategorias")
    public ResponseEntity<List<CategoriaServico>> listarSubcategorias(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaServicoService.listarSubcategorias(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaServico> buscarPorId(@PathVariable Long id) {
        return categoriaServicoService.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaServico> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaServico categoria, Authentication authentication) {
        exigirAdmin(authentication);
        return ResponseEntity.ok(categoriaServicoService.atualizar(id, categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication authentication) {
        exigirAdmin(authentication);
        categoriaServicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private void exigirAdmin(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Apenas administradores podem acessar este recurso.");
        }
    }

    private Long obterUsuarioId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.buscarPorEmail(userDetails.getUsername()).orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado.")).getId();
    }
}