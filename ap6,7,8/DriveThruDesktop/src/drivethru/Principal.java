package drivethru;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import model.Pedido;
import model.Produto;
import model.Situacao;
import util.CardapioRepositorio;
import util.Formato;
import util.PedidoRepositorio;

public class Principal {

	private static final PedidoRepositorio repo = PedidoRepositorio.getInstancia();

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		int opcao;
		do {
			System.out.println();
			System.out.println("=== Drive-thru Mexicano ===");
			System.out.println("1 - Registrar entrada de pedido");
			System.out.println("2 - Painel de pedidos");
			System.out.println("3 - Avancar pedido (preparo / pronto / saida)");
			System.out.println("4 - Cancelar pedido");
			System.out.println("5 - Historico de saidas");
			System.out.println("6 - Cardapio (cadastrar, alterar, excluir)");
			System.out.println("0 - Sair");
			opcao = lerInteiro(in, "Opcao: ");

			switch (opcao) {
			case 1:
				novoPedido(in);
				break;
			case 2:
				painel();
				break;
			case 3:
				atualizar(in, false);
				break;
			case 4:
				atualizar(in, true);
				break;
			case 5:
				historico();
				break;
			case 6:
				CardapioMenu.abrir(in);
				break;
			case 0:
				System.out.println("Encerrando.");
				break;
			default:
				System.out.println("Opcao invalida.");
			}
		} while (opcao != 0);
		in.close();
	}

	private static void novoPedido(Scanner in) {
		System.out.print("Placa ou nome do cliente: ");
		String cliente = in.nextLine().trim();
		if (cliente.isEmpty()) {
			cliente = "Sem identificacao";
		}

		CardapioMenu.listar();

		Map<String, Integer> itens = new LinkedHashMap<String, Integer>();
		while (true) {
			int id = lerInteiro(in, "Codigo do item (0 para concluir): ");
			if (id == 0) {
				break;
			}
			Produto produto = CardapioRepositorio.getInstancia().buscar(id);
			if (produto == null) {
				System.out.println("Item invalido.");
				continue;
			}
			int qtd = lerInteiro(in, "Quantidade: ");
			if (qtd > 0) {
				Integer atual = itens.get(produto.getNome());
				itens.put(produto.getNome(), (atual == null ? 0 : atual) + qtd);
			}
		}

		if (itens.isEmpty()) {
			System.out.println("Pedido sem itens, nada foi registrado.");
			return;
		}

		System.out.print("Observacoes (opcional): ");
		String obs = in.nextLine().trim();

		Pedido p = repo.registrarEntrada(cliente, obs, itens);
		System.out.println("Entrada registrada: pedido " + Formato.numero(p.getNumero()) + " - " + Formato.moeda(p.getTotal()));
	}

	private static void painel() {
		System.out.println();
		System.out.println("Em aberto: " + repo.totalEmAberto() + " | Tempo medio de saida: "
				+ Formato.duracao(repo.tempoMedio()) + " | Vendido: " + Formato.moeda(repo.totalVendido()));
		Situacao[] etapas = { Situacao.NA_FILA, Situacao.EM_PREPARO, Situacao.PRONTO };
		for (Situacao etapa : etapas) {
			List<Pedido> lista = repo.listar(etapa);
			System.out.println();
			System.out.println("--- " + etapa.getRotulo() + " (" + lista.size() + ") ---");
			for (Pedido p : lista) {
				System.out.println(Formato.numero(p.getNumero()) + " " + p.getCliente() + " | " + Formato.itens(p)
						+ " | entrada " + Formato.hora(p.getEntrada()) + " (" + Formato.duracao(p.tempoTotal()) + ")"
						+ (p.getObservacao().isEmpty() ? "" : " | obs: " + p.getObservacao()));
			}
		}
	}

	private static void atualizar(Scanner in, boolean cancelar) {
		int numero = lerInteiro(in, "Numero do pedido: ");
		boolean ok = cancelar ? repo.cancelar(numero) : repo.avancar(numero);
		if (!ok) {
			System.out.println("Pedido " + Formato.numero(numero) + " nao encontrado ou ja finalizado.");
			return;
		}
		Pedido p = repo.buscar(numero);
		System.out.println("Pedido " + Formato.numero(numero) + " agora esta: " + p.getSituacao().getRotulo());
	}

	private static void historico() {
		List<Pedido> lista = repo.historico(20);
		System.out.println();
		if (lista.isEmpty()) {
			System.out.println("Nenhuma saida registrada ainda.");
			return;
		}
		for (Pedido p : lista) {
			System.out.println(Formato.numero(p.getNumero()) + " " + p.getCliente() + " | " + Formato.itens(p)
					+ " | entrada " + Formato.hora(p.getEntrada()) + " | saida " + Formato.hora(p.getSaida())
					+ " | tempo " + Formato.duracao(p.tempoTotal()) + " | " + Formato.moeda(p.getTotal()) + " | "
					+ p.getSituacao().getRotulo());
		}
	}

	static int lerInteiro(Scanner in, String texto) {
		System.out.print(texto);
		try {
			return Integer.parseInt(in.nextLine().trim());
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
