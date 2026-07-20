package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.CompraProdutoPeriodoDTO;
import com.jubo.cameriere.api.dto.ItemPedidoRequestDTO;
import com.jubo.cameriere.api.dto.PedidoFiltroResumoDTO;
import com.jubo.cameriere.api.dto.PedidoRequestDTO;
import com.jubo.cameriere.api.dto.PedidoResponseDTO;
import com.jubo.cameriere.api.dto.PedidosPorClienteDTO;
import com.jubo.cameriere.api.dto.ValorTotalPeriodoDTO;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.model.Empresa;
import com.jubo.cameriere.domain.model.Endereco;
import com.jubo.cameriere.domain.model.ItemPedido;
import com.jubo.cameriere.domain.model.Pedido;
import com.jubo.cameriere.domain.model.Produto;
import com.jubo.cameriere.domain.service.comando.CompraProdutoPeriodo;
import com.jubo.cameriere.domain.service.comando.ItemPedidoCommand;
import com.jubo.cameriere.domain.service.comando.PedidoCreateCommand;
import com.jubo.cameriere.domain.service.comando.PedidoFiltroResumo;
import com.jubo.cameriere.domain.service.comando.PedidosPorCliente;
import com.jubo.cameriere.domain.service.comando.ValorTotalPeriodo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class PedidoAssembler {

    public PedidoCreateCommand toCreateCommand(PedidoRequestDTO request) {
        List<ItemPedidoCommand> itens = request.itens().stream()
                .map(this::toItemCommand)
                .toList();
        return new PedidoCreateCommand(request.clienteId(), request.observacao(), itens);
    }

    public PedidoResponseDTO toResponseDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getDataCriacao(),
                pedido.getDataConfirmacao(),
                pedido.getDataPrevisaoEntrega(),
                pedido.getDataCancelamento(),
                pedido.getDataEntrega(),
                pedido.getValorTotal(),
                pedido.getEntregue(),
                pedido.getObservacao(),
                pedido.getObsCancelamento(),
                pedido.getFinalizado(),
                toClienteResumoDTO(pedido.getCliente()),
                toEmpresaResumoDTO(pedido.getEmpresaNota()),
                toItensResponseDTO(pedido.getItens())
        );
    }

    public ValorTotalPeriodoDTO toValorTotalPeriodoDTO(ValorTotalPeriodo valor) {
        return new ValorTotalPeriodoDTO(valor.inicio(), valor.fim(), valor.valorTotal());
    }

    public PedidosPorClienteDTO toPedidosPorClienteDTO(PedidosPorCliente relatorio) {
        return new PedidosPorClienteDTO(
                relatorio.clienteId(),
                relatorio.clienteNome(),
                relatorio.totalPedidos(),
                relatorio.valorTotal()
        );
    }

    public PedidoFiltroResumoDTO toPedidoFiltroResumoDTO(PedidoFiltroResumo resumo) {
        return new PedidoFiltroResumoDTO(resumo.quantidadePedidos(), resumo.valorTotal());
    }

    public CompraProdutoPeriodoDTO toCompraProdutoPeriodoDTO(CompraProdutoPeriodo compra) {
        return new CompraProdutoPeriodoDTO(
                compra.produtoId(),
                compra.produtoNome(),
                compra.unidadeMedida(),
                compra.quantidadeTotal(),
                compra.valorTotal(),
                compra.quantidadePedidos()
        );
    }

    private ItemPedidoCommand toItemCommand(ItemPedidoRequestDTO item) {
        return new ItemPedidoCommand(
                item.produtoId(),
                item.estanteId(),
                item.quantidade(),
                item.observacao()
        );
    }

    private PedidoResponseDTO.ClienteResumoDTO toClienteResumoDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        return new PedidoResponseDTO.ClienteResumoDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCnpj(),
                toEnderecoDTO(cliente.getEndereco()),
                cliente.getTelefone(),
                cliente.getEmpresa() != null ? cliente.getEmpresa().getId() : null,
                cliente.getEmpresa() != null ? cliente.getEmpresa().getNome() : null
        );
    }

    private PedidoResponseDTO.EmpresaResumoDTO toEmpresaResumoDTO(Empresa empresa) {
        if (empresa == null) {
            return null;
        }

        return new PedidoResponseDTO.EmpresaResumoDTO(
                empresa.getId(),
                empresa.getNome(),
                empresa.getCnpj(),
                toEnderecoDTO(empresa.getEndereco()),
                empresa.getTelefone()
        );
    }

    private com.jubo.cameriere.api.dto.EnderecoDTO toEnderecoDTO(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new com.jubo.cameriere.api.dto.EnderecoDTO(
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCep(),
                endereco.getCidade(),
                endereco.getEstado()
        );
    }

    private List<PedidoResponseDTO.ItemPedidoResponseDTO> toItensResponseDTO(List<ItemPedido> itens) {
        if (itens == null) {
            return List.of();
        }

        return itens.stream()
                .map(this::toItemResponseDTO)
                .toList();
    }

    private PedidoResponseDTO.ItemPedidoResponseDTO toItemResponseDTO(ItemPedido item) {
        if (item == null) {
            return null;
        }

        BigDecimal precoUnitario = BigDecimal.ZERO;
        if (item.getQuantidade() != null && item.getQuantidade().compareTo(BigDecimal.ZERO) != 0
                && item.getPrecoTotal() != null) {
            precoUnitario = item.getPrecoTotal()
                    .divide(item.getQuantidade(), 2, RoundingMode.HALF_UP);
        }

        return new PedidoResponseDTO.ItemPedidoResponseDTO(
                item.getId(),
                item.getQuantidade(),
                item.getPrecoTotal(),
                precoUnitario,
                item.getObservacao(),
                toProdutoResumoDTO(item.getProduto())
        );
    }

    private PedidoResponseDTO.ItemPedidoResponseDTO.ProdutoResumoDTO toProdutoResumoDTO(Produto produto) {
        if (produto == null) {
            return null;
        }

        String unidadeMedida = produto.getUnidadeMedida() != null
                ? produto.getUnidadeMedida().getSigla()
                : "UN";

        return new PedidoResponseDTO.ItemPedidoResponseDTO.ProdutoResumoDTO(
                produto.getId(),
                produto.getNome(),
                unidadeMedida
        );
    }
}
