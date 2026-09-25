package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Produto;

/** CRUD dos itens do cardapio, em memoria. Uma unica instancia e compartilhada. */
public class CardapioRepositorio {

	private static final CardapioRepositorio INSTANCIA = new CardapioRepositorio();

	private final List<Produto> produtos = new ArrayList<Produto>();
	private int proximoId = 1;

	private CardapioRepositorio() {
		criar("Burrito", 32.0);
		criar("Tacos (3 un.)", 29.0);
		criar("Quesadilla", 27.0);
		criar("Nachos", 34.0);
		criar("Enchilada", 36.0);
		criar("Guacamole", 18.0);
		criar("Horchata", 12.0);
		criar("Refrigerante", 8.0);
	}

	public static CardapioRepositorio getInstancia() {
		return INSTANCIA;
	}

	// CREATE
	public synchronized Produto criar(String nome, double preco) {
		validar(nome, preco, 0);
		Produto p = new Produto(proximoId++, nome.trim(), preco);
		produtos.add(p);
		return p;
	}

	// READ
	public synchronized List<Produto> listar() {
		return new ArrayList<Produto>(produtos);
	}

	public synchronized Produto buscar(int id) {
		for (Produto p : produtos) {
			if (p.getId() == id) {
				return p;
			}
		}
		return null;
	}

	// UPDATE
	public synchronized void atualizar(int id, String nome, double preco) {
		Produto p = buscar(id);
		if (p == null) {
			throw new IllegalArgumentException("Item nao encontrado.");
		}
		validar(nome, preco, id);
		p.alterar(nome.trim(), preco);
	}

	// DELETE
	public synchronized boolean excluir(int id) {
		Produto p = buscar(id);
		return p != null && produtos.remove(p);
	}

	/** Soma o valor de um pedido a partir do nome dos itens e das quantidades. */
	public synchronized double total(Map<String, Integer> itens) {
		double soma = 0;
		for (Produto p : produtos) {
			Integer qtd = itens.get(p.getNome());
			if (qtd != null) {
				soma += p.getPreco() * qtd;
			}
		}
		return soma;
	}

	/** Aceita "32,50" ou "32.50". */
	public static double lerPreco(String texto) {
		try {
			return Double.parseDouble(texto.trim().replace(',', '.'));
		} catch (Exception e) {
			throw new IllegalArgumentException("Preco invalido (use um valor entre 0,01 e 9999).");
		}
	}

	private void validar(String nome, double preco, int idAtual) {
		if (nome == null || nome.trim().isEmpty()) {
			throw new IllegalArgumentException("Informe o nome do item.");
		}
		if (nome.trim().length() > 40) {
			throw new IllegalArgumentException("O nome deve ter ate 40 caracteres.");
		}
		if (Double.isNaN(preco) || Double.isInfinite(preco) || preco <= 0 || preco > 9999) {
			throw new IllegalArgumentException("Preco invalido (use um valor entre 0,01 e 9999).");
		}
		for (Produto p : produtos) {
			if (p.getId() != idAtual && p.getNome().equalsIgnoreCase(nome.trim())) {
				throw new IllegalArgumentException("Ja existe um item com esse nome.");
			}
		}
	}
}
