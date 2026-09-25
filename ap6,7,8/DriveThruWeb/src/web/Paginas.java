package web;

import java.util.List;

import model.Pedido;
import model.Produto;
import model.Situacao;
import util.CardapioRepositorio;
import util.Formato;
import util.PedidoRepositorio;

/** Gera o HTML das paginas (substitui os .jsp, que nao rodam sem servidor de aplicacao). */
public class Paginas {

	public static String index(String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n<html lang=\"pt-BR\">\n<head>\n<meta charset=\"UTF-8\">\n")
			.append("<title>Drive-thru - Novo pedido</title>\n<link rel=\"stylesheet\" href=\"css/estilo.css\">\n</head>\n<body>\n")
			.append("<h1>Drive-thru Mexicano</h1>\n<nav>\n\t<a href=\"index.jsp\">Novo pedido</a>\n")
			.append("\t<a href=\"paginas/painel.jsp\">Painel de pedidos</a>\n\t<a href=\"paginas/cardapio.jsp\">Cardápio</a>\n</nav>\n");

		if (msg != null) {
			sb.append("<p class=\"msg\">").append(Formato.html(msg)).append("</p>\n");
		}

		sb.append("<h2>Novo pedido ").append(Formato.numero(PedidoRepositorio.getInstancia().getProximoNumero())).append("</h2>\n")
			.append("<form action=\"PedidoServlet\" method=\"post\">\nPlaca ou nome do cliente:\n")
			.append("<input type=\"text\" id=\"cliente\" name=\"cliente\" maxlength=\"30\"/>\n<table>\n")
			.append("\t<tr><th>Item</th><th>Preço</th><th>Quantidade</th></tr>\n");

		for (Produto item : CardapioRepositorio.getInstancia().listar()) {
			sb.append("\t<tr>\n\t\t<td>").append(Formato.html(item.getNome())).append("</td>\n")
				.append("\t\t<td>").append(Formato.moeda(item.getPreco())).append("</td>\n")
				.append("\t\t<td><input type=\"number\" name=\"qtd_").append(item.getId())
				.append("\" value=\"0\" min=\"0\" max=\"50\"/></td>\n\t</tr>\n");
		}

		sb.append("</table>\n<br/>\nObservações: <input type=\"text\" id=\"obs\" name=\"obs\" maxlength=\"80\"/>\n<br/>\n")
			.append("<input type=\"submit\" value=\"Registrar entrada\"/>\n</form>\n</body>\n</html>\n");
		return sb.toString();
	}

	public static String painel(String msg) {
		PedidoRepositorio repo = PedidoRepositorio.getInstancia();
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n<html lang=\"pt-BR\">\n<head>\n<meta charset=\"UTF-8\">\n")
			.append("<meta http-equiv=\"refresh\" content=\"15\">\n<title>Drive-thru - Painel de pedidos</title>\n")
			.append("<link rel=\"stylesheet\" href=\"../css/estilo.css\">\n</head>\n<body>\n<h1>Painel de pedidos</h1>\n<nav>\n")
			.append("\t<a href=\"../index.jsp\">Novo pedido</a>\n\t<a href=\"painel.jsp\">Atualizar</a>\n")
			.append("\t<a href=\"cardapio.jsp\">Cardápio</a>\n</nav>\n");

		if (msg != null) {
			sb.append("<p class=\"msg\">").append(Formato.html(msg)).append("</p>\n");
		}

		sb.append("<div class=\"resumo\">\n\t<span>Em aberto: <b>").append(repo.totalEmAberto()).append("</b></span>\n")
			.append("\t<span>Tempo médio de saída: <b>").append(Formato.duracao(repo.tempoMedio())).append("</b></span>\n")
			.append("\t<span>Vendido (entregues): <b>").append(Formato.moeda(repo.totalVendido())).append("</b></span>\n</div>\n");

		sb.append("<div class=\"quadro\">\n");
		Situacao[] etapas = { Situacao.NA_FILA, Situacao.EM_PREPARO, Situacao.PRONTO };
		for (Situacao etapa : etapas) {
			List<Pedido> lista = repo.listar(etapa);
			sb.append("\t<section>\n\t\t<h2>").append(etapa.getRotulo()).append(" (").append(lista.size()).append(")</h2>\n");
			if (lista.isEmpty()) {
				sb.append("\t\t<p>Nenhum pedido aqui.</p>\n");
			}
			for (Pedido p : lista) {
				sb.append("\t\t<div class=\"ticket\">\n\t\t\t<b>").append(Formato.numero(p.getNumero())).append("</b> - ")
					.append(Formato.html(p.getCliente())).append("<br/>\n\t\t\tEntrada às ").append(Formato.hora(p.getEntrada()))
					.append(" (há ").append(Formato.duracao(p.tempoTotal())).append(")<br/>\n\t\t\t")
					.append(Formato.html(Formato.itens(p))).append("\n");
				if (!p.getObservacao().isEmpty()) {
					sb.append("\t\t\t<br/>Obs.: ").append(Formato.html(p.getObservacao())).append("\n");
				}
				sb.append("\t\t\t<form action=\"../StatusServlet\" method=\"post\">\n")
					.append("\t\t\t\t<input type=\"hidden\" name=\"numero\" value=\"").append(p.getNumero()).append("\"/>\n")
					.append("\t\t\t\t<button type=\"submit\" name=\"acao\" value=\"avancar\">").append(etapa.getAcao()).append("</button>\n")
					.append("\t\t\t\t<button type=\"submit\" name=\"acao\" value=\"cancelar\">Cancelar</button>\n\t\t\t</form>\n\t\t</div>\n");
			}
			sb.append("\t</section>\n");
		}
		sb.append("</div>\n");

		sb.append("<h2>Histórico de saídas</h2>\n<table>\n")
			.append("\t<tr><th>Pedido</th><th>Cliente</th><th>Itens</th><th>Entrada</th><th>Saída</th><th>Tempo</th><th>Valor</th><th>Situação</th></tr>\n");
		List<Pedido> historico = repo.historico(20);
		if (historico.isEmpty()) {
			sb.append("\t<tr><td colspan=\"8\">As saídas aparecem aqui com horário de entrada, saída e tempo total.</td></tr>\n");
		}
		for (Pedido p : historico) {
			String classe = p.getSituacao() == Situacao.CANCELADO ? "cancelado" : "";
			sb.append("\t<tr>\n\t\t<td>").append(Formato.numero(p.getNumero())).append("</td>\n")
				.append("\t\t<td>").append(Formato.html(p.getCliente())).append("</td>\n")
				.append("\t\t<td>").append(Formato.html(Formato.itens(p))).append("</td>\n")
				.append("\t\t<td>").append(Formato.hora(p.getEntrada())).append("</td>\n")
				.append("\t\t<td>").append(Formato.hora(p.getSaida())).append("</td>\n")
				.append("\t\t<td>").append(Formato.duracao(p.tempoTotal())).append("</td>\n")
				.append("\t\t<td>").append(Formato.moeda(p.getTotal())).append("</td>\n")
				.append("\t\t<td class=\"").append(classe).append("\">").append(p.getSituacao().getRotulo()).append("</td>\n\t</tr>\n");
		}
		sb.append("</table>\n</body>\n</html>\n");
		return sb.toString();
	}

	public static String cardapio(String msg, Integer editarId) {
		CardapioRepositorio repo = CardapioRepositorio.getInstancia();
		Produto editando = editarId == null ? null : repo.buscar(editarId);

		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n<html lang=\"pt-BR\">\n<head>\n<meta charset=\"UTF-8\">\n")
			.append("<title>Drive-thru - Cardápio</title>\n<link rel=\"stylesheet\" href=\"../css/estilo.css\">\n</head>\n<body>\n")
			.append("<h1>Cardápio</h1>\n<nav>\n\t<a href=\"../index.jsp\">Novo pedido</a>\n\t<a href=\"painel.jsp\">Painel de pedidos</a>\n")
			.append("\t<a href=\"cardapio.jsp\">Cardápio</a>\n</nav>\n");

		if (msg != null) {
			sb.append("<p class=\"msg\">").append(Formato.html(msg)).append("</p>\n");
		}

		sb.append("<h2>").append(editando == null ? "Novo item" : "Alterar item").append("</h2>\n")
			.append("<form action=\"../CardapioServlet\" method=\"post\">\n")
			.append("\t<input type=\"hidden\" name=\"acao\" value=\"").append(editando == null ? "criar" : "atualizar").append("\"/>\n");
		if (editando != null) {
			sb.append("\t<input type=\"hidden\" name=\"id\" value=\"").append(editando.getId()).append("\"/>\n");
		}
		sb.append("\tNome: <input type=\"text\" name=\"nome\" maxlength=\"40\" value=\"")
			.append(editando == null ? "" : Formato.html(editando.getNome())).append("\"/>\n")
			.append("\tPreço (R$): <input type=\"text\" name=\"preco\" size=\"8\" value=\"")
			.append(editando == null ? "" : Formato.decimal(editando.getPreco())).append("\"/>\n")
			.append("\t<input type=\"submit\" value=\"").append(editando == null ? "Cadastrar item" : "Salvar alterações").append("\"/>\n");
		if (editando != null) {
			sb.append("\t<a href=\"cardapio.jsp\">Cancelar</a>\n");
		}
		sb.append("</form>\n\n<h2>Itens cadastrados</h2>\n<table>\n")
			.append("\t<tr><th>Código</th><th>Item</th><th>Preço</th><th>Ações</th></tr>\n");

		List<Produto> produtos = repo.listar();
		if (produtos.isEmpty()) {
			sb.append("\t<tr><td colspan=\"4\">O cardápio está vazio. Cadastre o primeiro item acima.</td></tr>\n");
		}
		for (Produto p : produtos) {
			sb.append("\t<tr>\n\t\t<td>").append(p.getId()).append("</td>\n")
				.append("\t\t<td>").append(Formato.html(p.getNome())).append("</td>\n")
				.append("\t\t<td>").append(Formato.moeda(p.getPreco())).append("</td>\n\t\t<td>\n")
				.append("\t\t\t<a href=\"cardapio.jsp?editar=").append(p.getId()).append("\">Alterar</a>\n")
				.append("\t\t\t<form action=\"../CardapioServlet\" method=\"post\" style=\"display:inline\" onsubmit=\"return confirm('Excluir este item do cardápio? Pedidos já registrados não mudam.');\">\n")
				.append("\t\t\t\t<input type=\"hidden\" name=\"acao\" value=\"excluir\"/>\n")
				.append("\t\t\t\t<input type=\"hidden\" name=\"id\" value=\"").append(p.getId()).append("\"/>\n")
				.append("\t\t\t\t<button type=\"submit\">Excluir</button>\n\t\t\t</form>\n\t\t</td>\n\t</tr>\n");
		}
		sb.append("</table>\n</body>\n</html>\n");
		return sb.toString();
	}
}