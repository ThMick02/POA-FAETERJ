package web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import model.Pedido;
import model.Produto;
import util.CardapioRepositorio;
import util.Formato;
import util.PedidoRepositorio;

/** Servidor web em Java puro (sem Tomcat, sem Jetty). Sobe em http://localhost:8080/ */
public class Servidor {

	public static void main(String[] args) throws IOException {
		HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

		servidor.createContext("/", exchange -> {
			if (!exchange.getRequestURI().getPath().equals("/") && !exchange.getRequestURI().getPath().equals("/index.jsp")) {
				exchange.sendResponseHeaders(404, -1);
				exchange.close();
				return;
			}
			String sessionId = Http.pegarOuCriarSessao(exchange);
			Http.responderHtml(exchange, Paginas.index(Sessao.lerERemoverMsg(sessionId)));
		});

		servidor.createContext("/index.jsp", exchange -> {
			String sessionId = Http.pegarOuCriarSessao(exchange);
			Http.responderHtml(exchange, Paginas.index(Sessao.lerERemoverMsg(sessionId)));
		});

		servidor.createContext("/css/estilo.css", exchange -> Http.responderCss(exchange, CssEstilo.CSS));

		servidor.createContext("/paginas/painel.jsp", exchange -> {
			String sessionId = Http.pegarOuCriarSessao(exchange);
			Http.responderHtml(exchange, Paginas.painel(Sessao.lerERemoverMsg(sessionId)));
		});

		servidor.createContext("/paginas/cardapio.jsp", exchange -> {
			String sessionId = Http.pegarOuCriarSessao(exchange);
			Map<String, String> q = Http.parseQuery(exchange.getRequestURI().getQuery());
			Integer editar = null;
			try {
				editar = Integer.parseInt(q.get("editar"));
			} catch (Exception e) {
				// sem item em edicao
			}
			Http.responderHtml(exchange, Paginas.cardapio(Sessao.lerERemoverMsg(sessionId), editar));
		});

		servidor.createContext("/PedidoServlet", exchange -> {
			String sessionId = Http.pegarOuCriarSessao(exchange);
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				Http.redirecionar(exchange, "/index.jsp");
				return;
			}
			Map<String, String> form = Http.lerCorpo(exchange);

			Map<String, Integer> itens = new LinkedHashMap<String, Integer>();
			for (Produto produto : CardapioRepositorio.getInstancia().listar()) {
				int qtd = lerQuantidade(form.get("qtd_" + produto.getId()));
				if (qtd > 0) {
					itens.put(produto.getNome(), qtd);
				}
			}

			if (itens.isEmpty()) {
				Sessao.setMsg(sessionId, "Adicione ao menos um item ao pedido.");
				Http.redirecionar(exchange, "/index.jsp");
				return;
			}

			String cliente = limpar(form.get("cliente"), 30);
			if (cliente.isEmpty()) {
				cliente = "Sem identificacao";
			}
			String obs = limpar(form.get("obs"), 80);

			Pedido p = PedidoRepositorio.getInstancia().registrarEntrada(cliente, obs, itens);
			Sessao.setMsg(sessionId, "Entrada registrada: pedido " + Formato.numero(p.getNumero()) + " de " + cliente
					+ " (" + Formato.moeda(p.getTotal()) + ").");
			Http.redirecionar(exchange, "/paginas/painel.jsp");
		});

		servidor.createContext("/StatusServlet", exchange -> {
			String sessionId = Http.pegarOuCriarSessao(exchange);
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				Http.redirecionar(exchange, "/paginas/painel.jsp");
				return;
			}
			Map<String, String> form = Http.lerCorpo(exchange);
			PedidoRepositorio repo = PedidoRepositorio.getInstancia();
			try {
				int numero = Integer.parseInt(form.get("numero"));
				boolean cancelar = "cancelar".equals(form.get("acao"));
				boolean ok = cancelar ? repo.cancelar(numero) : repo.avancar(numero);
				Sessao.setMsg(sessionId, ok
						? "Pedido " + Formato.numero(numero) + (cancelar ? " cancelado." : " atualizado.")
						: "Pedido " + Formato.numero(numero) + " nao esta mais em aberto.");
			} catch (NumberFormatException e) {
				Sessao.setMsg(sessionId, "Numero de pedido invalido.");
			}
			Http.redirecionar(exchange, "/paginas/painel.jsp");
		});

		servidor.createContext("/CardapioServlet", exchange -> {
			String sessionId = Http.pegarOuCriarSessao(exchange);
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				Http.redirecionar(exchange, "/paginas/cardapio.jsp");
				return;
			}
			Map<String, String> form = Http.lerCorpo(exchange);
			CardapioRepositorio repo = CardapioRepositorio.getInstancia();
			String acao = form.get("acao");
			String msg = "Acao invalida.";
			try {
				if ("criar".equals(acao)) {
					Produto p = repo.criar(form.get("nome"), CardapioRepositorio.lerPreco(form.get("preco")));
					msg = "Item cadastrado: " + p.getNome() + ".";
				} else if ("atualizar".equals(acao)) {
					int id = Integer.parseInt(form.get("id"));
					repo.atualizar(id, form.get("nome"), CardapioRepositorio.lerPreco(form.get("preco")));
					msg = "Item atualizado.";
				} else if ("excluir".equals(acao)) {
					int id = Integer.parseInt(form.get("id"));
					msg = repo.excluir(id) ? "Item excluido." : "Item nao encontrado.";
				}
			} catch (NumberFormatException e) {
				msg = "Codigo de item invalido.";
			} catch (IllegalArgumentException e) {
				msg = e.getMessage();
			}
			Sessao.setMsg(sessionId, msg);
			Http.redirecionar(exchange, "/paginas/cardapio.jsp");
		});

		servidor.setExecutor(null);
		servidor.start();
		System.out.println("Servidor rodando em http://localhost:8080/");
	}

	private static int lerQuantidade(String valor) {
		try {
			int q = Integer.parseInt(valor.trim());
			return Math.max(0, Math.min(q, 50));
		} catch (Exception e) {
			return 0;
		}
	}

	private static String limpar(String valor, int max) {
		if (valor == null) {
			return "";
		}
		valor = valor.trim();
		return valor.length() > max ? valor.substring(0, max) : valor;
	}
}