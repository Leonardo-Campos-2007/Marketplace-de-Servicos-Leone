package com.br.leone.service;

import com.br.leone.dto.CategoriaRequestDTO;
import com.br.leone.dto.CategoriaResponseDTO;
import com.br.leone.entity.CategoriaServico;
import com.br.leone.enums.StatusAprovacao;
import com.br.leone.exception.CategoriaJaCadastradaException;
import com.br.leone.exception.CategoriaNaoEncontradaException;
import com.br.leone.exception.CategoriaPossuiVinculosException;
import com.br.leone.exception.HierarquiaCategoriaInvalidaException;
import com.br.leone.repository.CategoriaServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServicoService {

    private final CategoriaServicoRepository categoriaServicoRepository;

    public CategoriaServicoService(CategoriaServicoRepository categoriaServicoRepository) {
        this.categoriaServicoRepository = categoriaServicoRepository;
    }

    // 1. Criar Categoria (ADMIN) - Agora aceita DTO e retorna DTO
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        validarRegrasCategoria(dto.nome(), dto.categoriaPaiId(), null);

        CategoriaServico categoria = new CategoriaServico();
        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        categoria.setCategoriaPaiId(dto.categoriaPaiId());
        categoria.setStatusAprovacao(StatusAprovacao.APROVADO);
        categoria.setCriadoPorUsuarioId(null);

        return new CategoriaResponseDTO(categoriaServicoRepository.save(categoria));
    }

    // 2. Sugerir Categoria (USER) - Agora aceita DTO e retorna DTO
    public CategoriaResponseDTO sugerir(CategoriaRequestDTO dto, Long usuarioId) {
        validarRegrasCategoria(dto.nome(), dto.categoriaPaiId(), null);

        CategoriaServico categoria = new CategoriaServico();
        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        categoria.setCategoriaPaiId(dto.categoriaPaiId());
        categoria.setStatusAprovacao(StatusAprovacao.PENDENTE);
        categoria.setCriadoPorUsuarioId(usuarioId);

        return new CategoriaResponseDTO(categoriaServicoRepository.save(categoria));
    }

    // 3. Atualizar Categoria (ADMIN) - Evita auto-referência e valida colisão de nomes
    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        CategoriaServico categoriaExistente = categoriaServicoRepository.findById(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException(id));

        // Impedir auto-referência (Loop Infinito)
        if (dto.categoriaPaiId() != null && dto.categoriaPaiId().equals(id)) {
            throw new HierarquiaCategoriaInvalidaException("Uma categoria não pode ser pai de si mesma.");
        }

        validarRegrasCategoria(dto.nome(), dto.categoriaPaiId(), id);

        categoriaExistente.setNome(dto.nome());
        categoriaExistente.setDescricao(dto.descricao());
        categoriaExistente.setCategoriaPaiId(dto.categoriaPaiId());

        return new CategoriaResponseDTO(categoriaServicoRepository.save(categoriaExistente));
    }

    public CategoriaResponseDTO aprovarSugestao(Long id) {
        CategoriaServico categoria = categoriaServicoRepository.findById(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException(id));

        // Se tiver pai, garante que o pai também está aprovado antes de aprovar o filho
        if (categoria.getCategoriaPaiId() != null) {
            CategoriaServico pai = categoriaServicoRepository.findById(categoria.getCategoriaPaiId())
                    .orElseThrow(() -> new CategoriaNaoEncontradaException(categoria.getCategoriaPaiId()));
            if (pai.getStatusAprovacao() != StatusAprovacao.APROVADO) {
                throw new HierarquiaCategoriaInvalidaException("Não é possível aprovar esta subcategoria porque a categoria pai ainda está pendente.");

            }
        }

        categoria.setStatusAprovacao(StatusAprovacao.APROVADO);
        return new CategoriaResponseDTO(categoriaServicoRepository.save(categoria));
    }

    public void rejeitarSugestao(Long id) {
        if (!categoriaServicoRepository.existsById(id)) {
            throw new CategoriaNaoEncontradaException(id);
        }
        categoriaServicoRepository.deleteById(id);
    }

    // 4. Buscar por ID Sem Optional - Lança exceção direto no Service
    public CategoriaResponseDTO buscarPorIdCompleto(Long id) {
        return categoriaServicoRepository.findById(id)
                .map(CategoriaResponseDTO::new)
                .orElseThrow(() -> new CategoriaNaoEncontradaException(id));
    }

    public void deletar(Long id) {
        // 1. Verifica se a categoria existe
        CategoriaServico categoria = categoriaServicoRepository.findById(id)
                .orElseThrow(() -> new CategoriaNaoEncontradaException(id));

        // 2. Impede a deleção se houver subcategorias filhas vinculadas a ela
        boolean possuiFilhos = !categoriaServicoRepository.findByCategoriaPaiIdAndStatusAprovacao(id, StatusAprovacao.APROVADO).isEmpty()
                || !categoriaServicoRepository.findByCategoriaPaiIdAndStatusAprovacao(id, StatusAprovacao.PENDENTE).isEmpty();

        if (possuiFilhos) {
            throw new CategoriaPossuiVinculosException("Não é possível deletar esta categoria pois ela possui subcategorias vinculadas.");
        }

        categoriaServicoRepository.deleteById(id);
    }

    // LISTAGENS (Atualizadas na Etapa 2 para retornar DTO)
    public List<CategoriaResponseDTO> listarPendentes() {
        return categoriaServicoRepository.findByStatusAprovacao(StatusAprovacao.PENDENTE)
                .stream().map(CategoriaResponseDTO::new).toList();
    }

    public List<CategoriaResponseDTO> listarTodos() {
        return categoriaServicoRepository.findByStatusAprovacao(StatusAprovacao.APROVADO)
                .stream().map(CategoriaResponseDTO::new).toList();
    }

    public List<CategoriaResponseDTO> listarRaizes() {
        return categoriaServicoRepository.findByStatusAprovacao(StatusAprovacao.APROVADO)
                .stream().filter(c -> c.getCategoriaPaiId() == null)
                .map(CategoriaResponseDTO::new).toList();
    }

    public List<CategoriaResponseDTO> listarSubcategorias(Long categoriaPaiId) {
        return categoriaServicoRepository.findByCategoriaPaiIdAndStatusAprovacao(categoriaPaiId, StatusAprovacao.APROVADO)
                .stream().map(CategoriaResponseDTO::new).toList();
    }

    /**
     * Concentra as validações complexas de hierarquia, nível máximo e duplicidade.
     */
    private void validarRegrasCategoria(String nome, Long categoriaPaiId, Long categoriaAtualId) {
        if (categoriaPaiId != null) {
            // 1. Validar se a categoria pai realmente existe
            CategoriaServico pai = categoriaServicoRepository.findById(categoriaPaiId)
                    .orElseThrow(() -> new CategoriaNaoEncontradaException(categoriaPaiId));

            // 2. Validar limite máximo de profundidade (3 níveis)
            int nivel = calcularProfundidade(pai) + 1;
            if (nivel > 3) {
                throw new HierarquiaCategoriaInvalidaException("Profundidade máxima permitida é de 3 níveis (Raiz -> Subcategoria -> Especialidade).");
            
            }
        }

        // 3. Validar duplicidade de nome sob o mesmo nível (ignora a própria categoria se for atualização)
        boolean jaExiste = categoriaServicoRepository.existsByNomeAndCategoriaPaiId(nome, categoriaPaiId);

        if (jaExiste && categoriaAtualId == null) {
            throw new CategoriaJaCadastradaException();
        }

        if (jaExiste && categoriaAtualId != null) {
            boolean conflitaComOutraCategoria = categoriaServicoRepository.findByCategoriaPaiIdAndStatusAprovacao(categoriaPaiId, StatusAprovacao.APROVADO)
                    .stream()
                    .anyMatch(c -> c.getNome().equalsIgnoreCase(nome) && !c.getId().equals(categoriaAtualId));

            if (conflitaComOutraCategoria) {
                throw new CategoriaJaCadastradaException();
            }
        }
    }

    /**
     * Calcula recursivamente quantos níveis acima o pai possui para validar o limite.
     */
    private int calcularProfundidade(CategoriaServico categoria) {
        int profundidade = 1;
        Long paiId = categoria.getCategoriaPaiId();
        while (paiId != null) {
            profundidade++;
            CategoriaServico pai = categoriaServicoRepository.findById(paiId).orElse(null);
            paiId = (pai != null) ? pai.getCategoriaPaiId() : null;
        }
        return profundidade;
    }
}