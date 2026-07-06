package com.br.leone.controller;

import com.br.leone.dto.ServicoRequestDTO;
import com.br.leone.dto.ServicoResponseDTO;
import com.br.leone.entity.Servico;
import com.br.leone.service.ServicoService;
import com.br.leone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;
    private final UserService userService;

    public ServicoController(ServicoService servicoService, UserService userService) {
        this.servicoService = servicoService;
        this.userService = userService;
    }

    // 1. CREATE
    @PostMapping
    public ResponseEntity<ServicoResponseDTO> criar(@Valid @RequestBody ServicoRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioAutenticadoId = obterUsuarioId(userDetails);


        Servico servico = new Servico(
                dto.perfilPrestadorId(),
                dto.categoriaServicoId(),
                dto.nome(),
                dto.descricao(),
                dto.precoBase(),
                dto.tempoEstimado(),
                null
        );

        Servico novoServico = servicoService.criar(servico, usuarioAutenticadoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ServicoResponseDTO(novoServico));
    }

    // 2. READ (Listar Ativos)
    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> listarAtivos() {
        List<ServicoResponseDTO> servicos = servicoService.listarAtivos().stream()
                .map(ServicoResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(servicos);
    }

    // 3. READ (Buscar por ID)
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return servicoService.buscarPorId(id)
                .map(servico -> ResponseEntity.ok(new ServicoResponseDTO(servico)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. READ (Por Prestador)
    @GetMapping("/prestador/{perfilPrestadorId}")
    public ResponseEntity<List<ServicoResponseDTO>> listarPorPrestador(@PathVariable Long perfilPrestadorId) {
        List<ServicoResponseDTO> servicos = servicoService.listarPorPrestador(perfilPrestadorId).stream()
                .map(ServicoResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(servicos);
    }

    // 5. READ (Por Categoria)
    @GetMapping("/categoria/{categoriaServicoId}")
    public ResponseEntity<List<ServicoResponseDTO>> listarPorCategoria(@PathVariable Long categoriaServicoId) {
        List<ServicoResponseDTO> servicos = servicoService.listarPorCategoria(categoriaServicoId).stream()
                .map(ServicoResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(servicos);
    }

    // 6. UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ServicoRequestDTO dto,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioAutenticadoId = obterUsuarioId(userDetails);

        
        Servico dadosNovos = new Servico(
                dto.perfilPrestadorId(),
                dto.categoriaServicoId(),
                dto.nome(),
                dto.descricao(),
                dto.precoBase(),
                dto.tempoEstimado(),
                null
        );

        Servico servicoAtualizado = servicoService.atualizar(id, dadosNovos, usuarioAutenticadoId);
        return ResponseEntity.ok(new ServicoResponseDTO(servicoAtualizado));
    }

    // 7. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioAutenticadoId = obterUsuarioId(userDetails);
        servicoService.deletar(id, usuarioAutenticadoId);
        return ResponseEntity.noContent().build();
    }

    private Long obterUsuarioId(UserDetails userDetails) {
        return userService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."))
                .getId();
    }
}