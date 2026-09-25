package drivethru;

import java.util.List;
import java.util.Scanner;

import model.Produto;
import util.CardapioRepositorio;
import util.Formato;

/** Menu de console do CRUD do cardapio. */
public class CardapioMenu {

	private static final CardapioRepositorio repo = CardapioRepositorio.getInstancia();

	static void abrir(Scanner in) {
		int opcao;
		do {
			System.out.println();
			System.out.println("--- Cardapio ---");
			System.out.println("1 - Listar itens");
			System.out.println("2 - Cadastrar item");
			System.out.println("3 - Alterar item");
			System.out.println("4 - Excluir item");
			System.out.println("0 - Voltar");
			opcao = Principal.lerInteiro(in, "Opcao: ");

			switch (opcao) {
			case 1:
				listar();
				break;
			case 2:
				cadastrar(in);
				break;
			case 3:
				alterar(in);
				break;
			case 4:
				excluir(in);
				break;
			case 0:
				break;
			default:
				System.out.println("Opcao invalida.");
			}
		} while (opcao != 0);
	}

	static void listar() {
		List<Produto> lista = repo.listar();
		System.out.println();
		if (lista.isEmpty()) {
			System.out.println("Cardapio vazio.");
			return;
		}
		for (Produto p : lista) {
			System.out.println(p.getId() + " - " + p.getNome() + " (" + Formato.moeda(p.getPreco()) + ")");
		}
	}

	private static void cadastrar(Scanner in) {
		System.out.print("Nome do item: ");
		String nome = in.nextLine().trim();
		System.out.print("Preco (ex.: 32,50): ");
		String preco = in.nextLine();
		try {
			Produto p = repo.criar(nome, CardapioRepositorio.lerPreco(preco));
			System.out.println("Item cadastrado com o codigo " + p.getId() + ".");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void alterar(Scanner in) {
		int id = Principal.lerInteiro(in, "Codigo do item: ");
		Produto p = repo.buscar(id);
		if (p == null) {
			System.out.println("Item nao encontrado.");
			return;
		}
		System.out.print("Novo nome (Enter mantem \"" + p.getNome() + "\"): ");
		String nome = in.nextLine().trim();
		System.out.print("Novo preco (Enter mantem " + Formato.moeda(p.getPreco()) + "): ");
		String preco = in.nextLine().trim();
		try {
			repo.atualizar(id, nome.isEmpty() ? p.getNome() : nome,
					preco.isEmpty() ? p.getPreco() : CardapioRepositorio.lerPreco(preco));
			System.out.println("Item atualizado.");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void excluir(Scanner in) {
		int id = Principal.lerInteiro(in, "Codigo do item: ");
		Produto p = repo.buscar(id);
		if (p == null) {
			System.out.println("Item nao encontrado.");
			return;
		}
		System.out.print("Excluir \"" + p.getNome() + "\"? (s/n): ");
		if (in.nextLine().trim().equalsIgnoreCase("s")) {
			repo.excluir(id);
			System.out.println("Item excluido.");
		} else {
			System.out.println("Nada foi excluido.");
		}
	}
}
