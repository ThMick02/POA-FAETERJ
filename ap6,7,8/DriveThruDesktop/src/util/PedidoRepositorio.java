package util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Pedido;
import model.Situacao;

/** Guarda os pedidos em memoria. Uma unica instancia e compartilhada por todos os usuarios. */
public class PedidoRepositorio {

	private static final PedidoRepositorio INSTANCIA = new PedidoRepositorio();

	private final List<Pedido> pedidos = new ArrayList<Pedido>();
	private int proximoNumero = 1;

	private PedidoRepositorio() {
	}

	public static PedidoRepositorio getInstancia() {
		return INSTANCIA;
	}

	public synchronized Pedido registrarEntrada(String cliente, String observacao, Map<String, Integer> itens) {
		Pedido p = new Pedido(proximoNumero++, cliente, observacao, itens, CardapioRepositorio.getInstancia().total(itens));
		pedidos.add(p);
		return p;
	}

	public synchronized boolean avancar(int numero) {
		Pedido p = buscar(numero);
		if (p == null || !p.getSituacao().estaEmAberto()) {
			return false;
		}
		p.avancar();
		return true;
	}

	public synchronized boolean cancelar(int numero) {
		Pedido p = buscar(numero);
		if (p == null || !p.getSituacao().estaEmAberto()) {
			return false;
		}
		p.cancelar();
		return true;
	}

	public synchronized Pedido buscar(int numero) {
		for (Pedido p : pedidos) {
			if (p.getNumero() == numero) {
				return p;
			}
		}
		return null;
	}

	public synchronized List<Pedido> listar(Situacao situacao) {
		List<Pedido> r = new ArrayList<Pedido>();
		for (Pedido p : pedidos) {
			if (p.getSituacao() == situacao) {
				r.add(p);
			}
		}
		return r;
	}

	/** Pedidos ja finalizados (entregues ou cancelados), do mais recente para o mais antigo. */
	public synchronized List<Pedido> historico(int limite) {
		List<Pedido> r = new ArrayList<Pedido>();
		for (int i = pedidos.size() - 1; i >= 0 && r.size() < limite; i--) {
			if (!pedidos.get(i).getSituacao().estaEmAberto()) {
				r.add(pedidos.get(i));
			}
		}
		return r;
	}

	public synchronized int totalEmAberto() {
		int n = 0;
		for (Pedido p : pedidos) {
			if (p.getSituacao().estaEmAberto()) {
				n++;
			}
		}
		return n;
	}

	/** Tempo medio entre entrada e saida dos pedidos entregues; null se nao houver nenhum. */
	public synchronized Duration tempoMedio() {
		long soma = 0;
		int n = 0;
		for (Pedido p : pedidos) {
			if (p.getSituacao() == Situacao.ENTREGUE) {
				soma += p.tempoTotal().getSeconds();
				n++;
			}
		}
		return n == 0 ? null : Duration.ofSeconds(soma / n);
	}

	public synchronized double totalVendido() {
		double soma = 0;
		for (Pedido p : pedidos) {
			if (p.getSituacao() == Situacao.ENTREGUE) {
				soma += p.getTotal();
			}
		}
		return soma;
	}

	public synchronized int getProximoNumero() {
		return proximoNumero;
	}
}
