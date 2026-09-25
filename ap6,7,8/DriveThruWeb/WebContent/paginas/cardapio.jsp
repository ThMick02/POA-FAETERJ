<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List, model.Produto, util.CardapioRepositorio, util.Formato"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Drive-thru - Cardápio</title>
<link rel="stylesheet" href="../css/estilo.css">
</head>
<body>
<h1>Cardápio</h1>
<nav>
	<a href="../index.jsp">Novo pedido</a>
	<a href="painel.jsp">Painel de pedidos</a>
	<a href="cardapio.jsp">Cardápio</a>
</nav>

<%
CardapioRepositorio repo = CardapioRepositorio.getInstancia();
String msg = (String) session.getAttribute("msg");
session.removeAttribute("msg");
if (msg != null) {
%>
<p class="msg"><%=Formato.html(msg)%></p>
<%
}

Produto editando = null;
try {
	editando = repo.buscar(Integer.parseInt(request.getParameter("editar")));
} catch (Exception e) {
	// nenhum item em edicao
}
%>

<h2><%=editando == null ? "Novo item" : "Alterar item"%></h2>
<form action="../CardapioServlet" method="post">
	<input type="hidden" name="acao" value="<%=editando == null ? "criar" : "atualizar"%>"/>
<%
if (editando != null) {
%>
	<input type="hidden" name="id" value="<%=editando.getId()%>"/>
<%
}
%>
	Nome: <input type="text" name="nome" maxlength="40" value="<%=editando == null ? "" : Formato.html(editando.getNome())%>"/>
	Preço (R$): <input type="text" name="preco" size="8" value="<%=editando == null ? "" : Formato.decimal(editando.getPreco())%>"/>
	<input type="submit" value="<%=editando == null ? "Cadastrar item" : "Salvar alterações"%>"/>
<%
if (editando != null) {
%>
	<a href="cardapio.jsp">Cancelar</a>
<%
}
%>
</form>

<h2>Itens cadastrados</h2>
<table>
	<tr><th>Código</th><th>Item</th><th>Preço</th><th>Ações</th></tr>
<%
List<Produto> produtos = repo.listar();
if (produtos.isEmpty()) {
%>
	<tr><td colspan="4">O cardápio está vazio. Cadastre o primeiro item acima.</td></tr>
<%
}
for (Produto p : produtos) {
%>
	<tr>
		<td><%=p.getId()%></td>
		<td><%=Formato.html(p.getNome())%></td>
		<td><%=Formato.moeda(p.getPreco())%></td>
		<td>
			<a href="cardapio.jsp?editar=<%=p.getId()%>">Alterar</a>
			<form action="../CardapioServlet" method="post" style="display:inline" onsubmit="return confirm('Excluir este item do cardápio? Pedidos já registrados não mudam.');">
				<input type="hidden" name="acao" value="excluir"/>
				<input type="hidden" name="id" value="<%=p.getId()%>"/>
				<button type="submit">Excluir</button>
			</form>
		</td>
	</tr>
<%
}
%>
</table>
</body>
</html>
