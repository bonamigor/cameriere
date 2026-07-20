package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.exception.ProdutoNaoEncontradoException;
import com.jubo.cameriere.domain.exception.UnidadeMedidaNaoEncontradaException;
import com.jubo.cameriere.domain.model.Produto;
import com.jubo.cameriere.domain.model.UnidadeMedida;
import com.jubo.cameriere.domain.repository.ProdutoRepository;
import com.jubo.cameriere.domain.repository.UnidadeMedidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;

    public List<Produto> listarTodosProdutos() {
        return produtoRepository.findAllWithUnidadeMedida();
    }

    public Produto buscarProdutoPorId(Long id) {
        return buscarOuFalhar(id);
    }

    @Transactional
    public Produto criarProduto(Produto produto, String unidadeMedidaSigla) {
        aplicarUnidadeMedida(produto, unidadeMedidaSigla);
        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }
        Produto saved = produtoRepository.save(produto);
        return buscarOuFalhar(saved.getId());
    }

    @Transactional
    public Produto atualizarProduto(Long id, Produto dados, String unidadeMedidaSigla) {
        Produto produto = buscarOuFalhar(id);
        produto.setNome(dados.getNome());
        produto.setPrecoCusto(dados.getPrecoCusto());
        if (dados.getAtivo() != null) {
            produto.setAtivo(dados.getAtivo());
        }
        aplicarUnidadeMedida(produto, unidadeMedidaSigla);
        produtoRepository.save(produto);
        return buscarOuFalhar(id);
    }

    @Transactional
    public void deletarProduto(Long id) {
        Produto produto = buscarOuFalhar(id);
        produtoRepository.delete(produto);
    }

    public Produto buscarOuFalhar(Long id) {
        return produtoRepository.findByIdWithUnidadeMedida(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado: " + id));
    }

    private void aplicarUnidadeMedida(Produto produto, String unidadeMedidaSigla) {
        UnidadeMedida unidadeMedida = unidadeMedidaRepository.findBySigla(unidadeMedidaSigla)
                .orElseThrow(() -> new UnidadeMedidaNaoEncontradaException(
                        "Unidade de medida não encontrada: " + unidadeMedidaSigla));
        produto.setUnidadeMedida(unidadeMedida);
    }
}
