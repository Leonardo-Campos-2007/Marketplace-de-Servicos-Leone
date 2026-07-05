package com.br.leone.controller;

import com.br.leone.dto.CategoriaRequestDTO;
import com.br.leone.dto.CategoriaResponseDTO;
import com.br.leone.service.CategoriaServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaServicoController {

    private final CategoriaServicoService categoriaServicoService;
    private final com.br.leone.service.UserService userService; // Para buscar o ID do usuário logado

    public CategoriaServicoController(CategoriaServicoService categoriaServicoService, com.br.leone.service.UserService userService) {
        this.categoriaServicoService = categoriaServicoService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponseDTO> criar(@Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaServicoService.criar(dto));
    }

    @PostMapping("/sugerir")
    @PreAuthorize("isAuthenticated()") // Garante que qualquer user logado possa sugerir
    public ResponseEntity<CategoriaResponseDTO> sugerir(@Valid @RequestBody CategoriaRequestDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        // Captura o usuário logado de forma limpa usando @AuthenticationPrincipal do Spring Security
        Long usuarioId = userService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."))
                .getId();

        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaServicoService.sugerir(dto, usuarioId));
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponseDTO> aprovar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaServicoService.aprovarSugestao(id));
    }

    @DeleteMapping("/{id}/rejeitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejeitar(@PathVariable Long id) {
        categoriaServicoService.rejeitarSugestao(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoriaResponseDTO>> listarPendentes() {
        return ResponseEntity.ok(categoriaServicoService.listarPendentes());
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodos() {
        return ResponseEntity.ok(categoriaServicoService.listarTodos());
    }

    @GetMapping("/raizes")
    public ResponseEntity<List<CategoriaResponseDTO>> listarRaizes() {
        return ResponseEntity.ok(categoriaServicoService.listarRaizes());
    }

    @GetMapping("/{id}/subcategorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listarSubcategorias(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaServicoService.listarSubcategorias(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaServicoService.buscarPorIdCompleto(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponseDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.ok(categoriaServicoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        categoriaServicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}