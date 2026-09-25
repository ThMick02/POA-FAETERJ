package util;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import model.Pedido;

public class Formato {

	private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
	private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

	public static synchronized String moeda(double valor) {
		return MOEDA.format(valor).replace('\u00A0', ' ');
	}

	/** Preco no formato de campo de formulario, ex.: 32,50 */
	public static String decimal(double valor) {
		return String.format(new Locale("pt", "BR"), "%.2f", valor);
	}

	public static String hora(LocalDateTime t) {
		return t == null ? "-" : t.format(HORA);
	}

	public static String duracao(Duration d) {
		if (d == null) {
			return "--:--";
		}
		long s = d.getSeconds();
		return String.format("%02d:%02d", s / 60, s % 60);
	}

	public static String numero(int n) {
		return String.format("#%03d", n);
	}

	public static String itens(Pedido p) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> e : p.getItens().entrySet()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(e.getValue()).append("x ").append(e.getKey());
		}
		return sb.toString();
	}

	/** Escapa texto digitado pelo usuario antes de escrever na pagina. */
	public static String html(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}
}
