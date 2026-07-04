package com.br.leone.service;

import com.br.leone.dto.UserRequestDTO;
import com.br.leone.dto.UserResponseDTO;
import com.br.leone.dto.UserUpdateRequestDTO;
import com.br.leone.entity.User;
import com.br.leone.enums.Role;
import com.br.leone.enums.TipoConta;
import com.br.leone.exception.DadosJaCadastradosException;
import com.br.leone.exception.UsuarioNaoEncontradoException;
import com.br.leone.repository.UserRepository;
import com.br.leone.repository.PerfilPrestadorRepository; // Injetado para a trava de deleção
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PerfilPrestadorRepository perfilPrestadorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Atualizado o construtor para receber o novo repositório dependente
    public UserService(UserRepository userRepository,
                       PerfilPrestadorRepository perfilPrestadorRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.perfilPrestadorRepository = perfilPrestadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO criar(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DadosJaCadastradosException();
        }
        if (userRepository.existsByTelefone(dto.telefone())) {
            throw new DadosJaCadastradosException();
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setSenha(passwordEncoder.encode(dto.senha()));
        user.setTelefone(dto.telefone());
        user.setTipoConta(dto.tipoConta());
        user.setRole(Role.COMPRADOR);
        user.setDataCadastro(LocalDateTime.now());

        configurarEDocumentarConta(user, dto);

        return toDTO(userRepository.save(user));
    }

    public List<UserResponseDTO> listarTodos() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // 1. REFATORADO: Retorna diretamente o DTO ou lança a exceção tratada
    public UserResponseDTO buscarPorId(Long id) {
        return userRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    // Mantido para uso interno de autenticação do Spring Security
    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserResponseDTO atualizar(Long id, UserUpdateRequestDTO dto) { // Alterado o DTO na assinatura
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        // Valida se o telefone novo já pertence a OUTRO usuário antes de atualizar
        if (!user.getTelefone().equals(dto.telefone()) && userRepository.existsByTelefone(dto.telefone())) {
            throw new DadosJaCadastradosException();
        }

        user.setName(dto.name());
        user.setTelefone(dto.telefone());

        // Se a senha foi preenchida (não está nula nem vazia), nós a criptografamos e atualizamos
        if (dto.senha() != null && !dto.senha().isBlank()) {
            user.setSenha(passwordEncoder.encode(dto.senha()));
        }

        return toDTO(userRepository.save(user));
    }

    // 2. REFATORADO: Adicionada a trava de integridade relacional
    public void deletar(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UsuarioNaoEncontradoException(id);
        }

        // Checa se o usuário possui um perfil de prestador vinculado
        boolean possuiPerfilPrestador = perfilPrestadorRepository.findByUsuarioId(id).isPresent();
        if (possuiPerfilPrestador) {
            throw new IllegalStateException("Não é possível deletar o usuário porque ele possui um perfil de prestador vinculado.");
        }

        userRepository.deleteById(id);
    }

    private void configurarEDocumentarConta(User user, UserRequestDTO dto) {
        if (dto.tipoConta() == TipoConta.PESSOA_FISICA) {
            if (dto.cpf() == null || dto.cpf().isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para Pessoa Física.");
            }
            if (userRepository.existsByCpf(dto.cpf())) {
                throw new DadosJaCadastradosException();
            }
            user.setCpf(dto.cpf());
            user.setCnpj(null);
        }
        else if (dto.tipoConta() == TipoConta.PESSOA_JURIDICA) {
            if (dto.cnpj() == null || dto.cnpj().isBlank()) {
                throw new IllegalArgumentException("CNPJ é obrigatório para Pessoa Jurídica.");
            }
            if (userRepository.existsByCnpj(dto.cnpj())) {
                throw new DadosJaCadastradosException();
            }
            user.setCnpj(dto.cnpj());
            user.setCpf(null);
        }
    }

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTelefone(),
                user.getCpf(),
                user.getCnpj(),
                user.getTipoConta(),
                user.getRole(),
                user.getDataCadastro()
        );
    }
}