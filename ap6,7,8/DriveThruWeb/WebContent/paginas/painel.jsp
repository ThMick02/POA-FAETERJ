<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, model.Pedido, model.Situacao, util.Formato, util.PedidoRepositorio"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta http-equiv="refresh" content="15">
<title>Drive-thru - Painel de pedidos</title>
<link rel="stylesheet" href="../css/estilo.css">
</head>
<body>
<h1>Painel de pedidos</h1>
<nav>
	<a href="../index.jsp">Novo pedido</a>
	<a href="painel.jsp">Atualizar</a>
	<a href="cardapio.jsp">Cardápio</a>
</nav>

<%
PedidoRepositorio repo = PedidoRepositorio.getInstancia();
String msg = (String) session.getAttribute("msg");
session.removeAttribute("msg");
if (msg != null) {
%>
<p class="msg"><%=Formato.html(msg)%></p>
<%
}
%>

<div class="resumo">
	<span>Em aberto: <b><%=repo.totalEmAberto()%></b></span>
	<span>Tempo médio de saída: <b><%=Formato.duracao(repo.tempoMedio())%></b></span>
	<span>Vendido (entregues): <b><%=Formato.moeda(repo.totalVendido())%></b></span>
</div>

<div class="quadro">
<%
Situacao[] etapas = { Situacao.NA_FILA, Situacao.EM_PREPARO, Situacao.PRONTO };
for (Situacao etapa : etapas) {
	List<Pedido> lista = repo.listar(etapa);
%>
	<section>
		<h2><%=etapa.getRotulo()%> (<%=lista.size()%>)</h2>
<%
	if (lista.isEmpty()) {
%>
		<p>Nenhum pedido aqui.</p>
<%
	}
	for (Pedido p : lista) {
%>
		<div class="ticket">
			<b><%=Formato.numero(p.getNumero())%></b> - <%=Formato.html(p.getCliente())%><br/>
			Entrada às <%=Formato.hora(p.getEntrada())%> (há <%=Formato.duracao(p.tempoTotal())%>)<br/>
			<%=Formato.html(Formato.itens(p))%>
<%
		if (!p.getObservacao().isEmpty()) {
%>
			<br/>Obs.: <%=Formato.html(p.getObservacao())%>
<%
		}
%>
			<form action="../StatusServlet" method="post">
				<input type="hidden" name="numero" value="<%=p.getNumero()%>"/>
				<button type="submit" name="acao" value="avancar"><%=etapa.getAcao()%></button>
				<button type="submit" name="acao" value="cancelar">Cancelar</button>
			</form>
		</div>
<%
	}
%>
	</section>
<%
}
%>
</div>

<h2>Histórico de saídas</h2>
<table>
	<tr><th>Pedido</th><th>Cliente</th><th>Itens</th><th>Entrada</th><th>Saída</th><th>Tempo</th><th>Valor</th><th>Situação</th></tr>
<%
List<Pedido> historico = repo.historico(20);
if (historico.isEmpty()) {
%>
	<tr><td colspan="8">As saídas aparecem aqui com horário de entrada, saída e tempo total.</td></tr>
<%
}
for (Pedido p : historico) {
%>
	<tr>
		<td><%=Formato.numero(p.getNumero())%></td>
		<td><%=Formato.html(p.getCliente())%></td>
		<td><%=Formato.html(Formato.itens(p))%></td>
		<td><%=Formato.hora(p.getEntrada())%></td>
		<td><%=Formato.hora(p.getSaida())%></td>
		<td><%=Formato.duracao(p.tempoTotal())%></td>
		<td><%=Formato.moeda(p.getTotal())%></td>
		<td class="<%=p.getSituacao() == Situacao.CANCELADO ? "cancelado" : ""%>"><%=p.getSituacao().getRotulo()%></td>
	</tr>
<%
}
%>
</table>
</body>
</html>
