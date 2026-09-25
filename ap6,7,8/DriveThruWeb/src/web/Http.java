package web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

/** Utilidades para ler formularios e cookies, sem depender de nenhuma biblioteca externa. */
public class Http {

	private Http() {
	}

	public static Map<String, String> parseQuery(String query) {
		return parseParams(query);
	}

	public static Map<String, String> lerCorpo(HttpExchange ex) throws IOException {
		InputStream in = ex.getRequestBody();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] dados = new byte[1024];
		int lido;
		while ((lido = in.read(dados)) != -1) {
			buffer.write(dados, 0, lido);
		}
		return parseParams(buffer.toString("UTF-8"));
	}

	private static Map<String, String> parseParams(String texto) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		if (texto == null || texto.isEmpty()) {
			return params;
		}
		for (String par : texto.split("&")) {
			if (par.isEmpty()) {
				continue;
			}
			int i = par.indexOf('=');
			try {
				String chave = i >= 0 ? URLDecoder.decode(par.substring(0, i), "UTF-8") : URLDecoder.decode(par, "UTF-8");
				String valor = i >= 0 ? URLDecoder.decode(par.substring(i + 1), "UTF-8") : "";
				params.put(chave, valor);
			} catch (Exception e) {
				// ignora parametro malformado
			}
		}
		return params;
	}

	public static String pegarOuCriarSessao(HttpExchange ex) {
		String cookies = ex.getRequestHeaders().getFirst("Cookie");
		if (cookies != null) {
			for (String parte : cookies.split(";")) {
				String[] kv = parte.trim().split("=", 2);
				if (kv.length == 2 && kv[0].equals("SESSIONID")) {
					return kv[1];
				}
			}
		}
		String novo = Sessao.novoId();
		ex.getResponseHeaders().add("Set-Cookie", "SESSIONID=" + novo + "; Path=/");
		return novo;
	}

	public static void responderHtml(HttpExchange ex, String html) throws IOException {
		byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
		ex.sendResponseHeaders(200, bytes.length);
		OutputStream os = ex.getResponseBody();
		os.write(bytes);
		os.close();
	}

	public static void responderCss(HttpExchange ex, String css) throws IOException {
		byte[] bytes = css.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
		ex.sendResponseHeaders(200, bytes.length);
		OutputStream os = ex.getResponseBody();
		os.write(bytes);
		os.close();
	}

	public static void redirecionar(HttpExchange ex, String local) throws IOException {
		ex.getResponseHeaders().set("Location", local);
		ex.sendResponseHeaders(302, -1);
		ex.close();
	}
}