<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="model.Produto, util.CardapioRepositorio, util.Formato, util.PedidoRepositorio"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>Drive-thru - Novo pedido</title>
<link rel="stylesheet" href="css/estilo.css">
</head>
<body>
<h1>Drive-thru Mexicano</h1>
<nav>
	<a href="index.jsp">Novo pedido</a>
	<a href="paginas/painel.jsp">Painel de pedidos</a>
	<a href="paginas/cardapio.jsp">Cardápio</a>
</nav>

<%
String msg = (String) session.getAttribute("msg");
session.removeAttribute("msg");
if (msg != null) {
%>
<p class="msg"><%=Formato.html(msg)%></p>
<%
}
%>

<h2>Novo pedido <%=Formato.numero(PedidoRepositorio.getInstancia().getProximoNumero())%></h2>
<form action="PedidoServlet" method="post">
Placa ou nome do cliente:
<input type="text" id="cliente" name="cliente" maxlength="30"/>
<table>
	<tr><th>Item</th><th>Preço</th><th>Quantidade</th></tr>
<%
for (Produto item : CardapioRepositorio.getInstancia().listar()) {
%>
	<tr>
		<td><%=Formato.html(item.getNome())%></td>
		<td><%=Formato.moeda(item.getPreco())%></td>
		<td><input type="number" name="qtd_<%=item.getId()%>" value="0" min="0" max="50"/></td>
	</tr>
<%
}
%>
</table>
<br/>
Observações: <input type="text" id="obs" name="obs" maxlength="80"/>
<br/>
<input type="submit" value="Registrar entrada"/>
</form>
</body>
</html>
