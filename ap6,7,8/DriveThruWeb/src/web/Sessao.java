package web;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Guarda a mensagem de status por sessao (substitui HttpSession do servlet). */
public class Sessao {

	private static final Map<String, String> MENSAGENS = new ConcurrentHashMap<String, String>();

	public static String novoId() {
		return UUID.randomUUID().toString();
	}

	public static void setMsg(String sessionId, String msg) {
		if (sessionId != null) {
			MENSAGENS.put(sessionId, msg);
		}
	}

	public static String lerERemoverMsg(String sessionId) {
		return sessionId == null ? null : MENSAGENS.remove(sessionId);
	}
}