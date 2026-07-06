package com.br.leone.controller;

import com.br.leone.dto.AtualizarQuantidadeRequestDTO;
import com.br.leone.dto.CarrinhoResponseDTO;
import com.br.leone.dto.ItemCarrinhoRequestDTO;
import com.br.leone.service.CarrinhoService;
import com.br.leone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final UserService userService;

    public CarrinhoController(CarrinhoService carrinhoService, UserService userService) {
        this.carrinhoService = carrinhoService;
        this.userService = userService;
    }

    // 1. BUSCAR CARRINHO ATIVO
    @GetMapping
    public ResponseEntity<CarrinhoResponseDTO> obterCarrinho(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        return ResponseEntity.ok(carrinhoService.obterCarrinhoResponse(usuarioId));
    }

    // 2. ADICIONAR ITEM AO CARRINHO
    @PostMapping("/itens")
    public ResponseEntity<CarrinhoResponseDTO> adicionarItem(
            @Valid @RequestBody ItemCarrinhoRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        return ResponseEntity.ok(carrinhoService.adicionarItem(usuarioId, dto));
    }

    // 3. ATUALIZAR QUANTIDADE DE UM ITEM
    @PutMapping("/itens/{itemId}")
    public ResponseEntity<CarrinhoResponseDTO> atualizarQuantidade(
            @PathVariable Long itemId,
            @Valid @RequestBody AtualizarQuantidadeRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        return ResponseEntity.ok(carrinhoService.atualizarQuantidade(usuarioId, itemId, dto.quantidade()));
    }

    // 4. REMOVER ITEM DO CARRINHO
    @DeleteMapping("/itens/{itemId}")
    public ResponseEntity<CarrinhoResponseDTO> removerItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        return ResponseEntity.ok(carrinhoService.removerItem(usuarioId, itemId));
    }

    // 5. LIMPAR CARRINHO
    @DeleteMapping
    public ResponseEntity<CarrinhoResponseDTO> limparCarrinho(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        return ResponseEntity.ok(carrinhoService.limparCarrinho(usuarioId));
    }

    private Long obterUsuarioId(UserDetails userDetails) {
        return userService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."))
                .getId();
    }
}
