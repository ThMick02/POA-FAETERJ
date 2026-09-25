package model;

public enum Situacao {
	NA_FILA("Na fila"), EM_PREPARO("Em preparo"), PRONTO("Pronto para sair"), ENTREGUE("Entregue"), CANCELADO("Cancelado");

	private final String rotulo;

	Situacao(String rotulo) {
		this.rotulo = rotulo;
	}

	public String getRotulo() {
		return rotulo;
	}

	public boolean estaEmAberto() {
		return this == NA_FILA || this == EM_PREPARO || this == PRONTO;
	}

	public Situacao proxima() {
		switch (this) {
		case NA_FILA:
			return EM_PREPARO;
		case EM_PREPARO:
			return PRONTO;
		case PRONTO:
			return ENTREGUE;
		default:
			return this;
		}
	}

	/** Texto do botao que leva o pedido para a proxima etapa. */
	public String getAcao() {
		switch (this) {
		case NA_FILA:
			return "Iniciar preparo";
		case EM_PREPARO:
			return "Marcar como pronto";
		case PRONTO:
			return "Entregar pedido";
		default:
			return "";
		}
	}
}
