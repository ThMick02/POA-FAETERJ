package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Pedido {
	private final int numero;
	private final String cliente;
	private final String observacao;
	private final Map<String, Integer> itens;
	private final double total;
	private final LocalDateTime entrada;
	private volatile LocalDateTime saida;
	private volatile Situacao situacao = Situacao.NA_FILA;

	public Pedido(int numero, String cliente, String observacao, Map<String, Integer> itens, double total) {
		this.numero = numero;
		this.cliente = cliente;
		this.observacao = observacao;
		this.itens = new LinkedHashMap<String, Integer>(itens);
		this.total = total;
		this.entrada = LocalDateTime.now();
	}

	public void avancar() {
		if (!situacao.estaEmAberto()) {
			return;
		}
		situacao = situacao.proxima();
		if (situacao == Situacao.ENTREGUE) {
			saida = LocalDateTime.now();
		}
	}

	public void cancelar() {
		if (!situacao.estaEmAberto()) {
			return;
		}
		situacao = Situacao.CANCELADO;
		saida = LocalDateTime.now();
	}

	/** Tempo desde a entrada ate a saida (ou ate agora, se ainda esta em aberto). */
	public Duration tempoTotal() {
		LocalDateTime fim = saida != null ? saida : LocalDateTime.now();
		return Duration.between(entrada, fim);
	}

	public int getNumero() {
		return numero;
	}

	public String getCliente() {
		return cliente;
	}

	public String getObservacao() {
		return observacao;
	}

	public Map<String, Integer> getItens() {
		return itens;
	}

	public double getTotal() {
		return total;
	}

	public LocalDateTime getEntrada() {
		return entrada;
	}

	public LocalDateTime getSaida() {
		return saida;
	}

	public Situacao getSituacao() {
		return situacao;
	}
}
