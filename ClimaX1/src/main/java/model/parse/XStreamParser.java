package model.parse;

import com.thoughtworks.xstream.XStream;

import model.Cidade;
import model.ListaCidades;
import model.Previsao;
import model.PrevisaoCidade;

public class XStreamParser<P, C> {
	
	private XStream xstream = new XStream();

	public XStreamParser() {
		xstream = new XStream();

		generalSetup();
	}

	private void generalSetup() {
		// Ajuste de segurança do XStream
		Class<?>[] classes = new Class[] {PrevisaoCidade.class, Previsao.class, 
				Cidade.class, ListaCidades.class};
		
		xstream.allowTypes(classes);
	}

	private void previsaoSetup() {
		// Aliases para consulta de clima
		xstream.alias("cidade", PrevisaoCidade.class);
		xstream.alias("previsao", Previsao.class);
		
		// Para a coleção previsões, que é uma coleção implicita 
		// (não vem em uma tag) no XML
		xstream.addImplicitCollection(PrevisaoCidade.class, "previsoes");
	}
	
	private void cidadesSetup() {
		// Aliases para consulta cidades
		xstream.alias("cidades", ListaCidades.class);
		xstream.alias("cidade", Cidade.class);

		// Para a coleção cidades, que é uma coleção implicita 
		// (não vem em uma tag) no XML
		xstream.addImplicitCollection(ListaCidades.class, "cidades");
	}

	public P previsao(String xml) {
		previsaoSetup();
		return (P) xstream.fromXML(xml);
	}
	
	public C cidades(String xml) {
		cidadesSetup();
		return (C) xstream.fromXML(xml);
	}
}
