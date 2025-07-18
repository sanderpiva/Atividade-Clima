package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Cidade;
import model.ListaCidades;
import model.Previsao;
import model.PrevisaoCidade;
import model.parse.XStreamParser;
import model.service.WeatherForecastService;

public class PrevisaoServlet extends HttpServlet {

	// Mapeamentos de dados movidos para o Servlet
	private static final Map<String, String> descriptionMappings = new HashMap<>();
	private static final Map<String, String> iconMappings = new HashMap<>();
	private static final Map<String, String> svgIcons = new HashMap<>();
	private static final Map<String, String> svgForecastIcons = new HashMap<>();

	// Bloco estático para inicializar os mapas uma única vez
	static {
		initializeMappings();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");

		PrintWriter out = resp.getWriter();
		String cidadeNome = req.getParameter("cidade");

		if (cidadeNome == null || cidadeNome.trim().isEmpty()) {
			resp.sendRedirect("index.html"); // Redireciona se não houver cidade
			return;
		}

		try {
			String cidadesXML = WeatherForecastService.cidades(cidadeNome);
			XStreamParser<PrevisaoCidade, ListaCidades> xspCidades = new XStreamParser<>();
			ListaCidades listaCidades = xspCidades.cidades(cidadesXML);

			if (listaCidades.getCidades() == null || listaCidades.getCidades().isEmpty()) {
				renderErrorPage(out, "Cidade não encontrada", "Não foi possível encontrar dados para a cidade '"
						+ cidadeNome + "'. Por favor, verifique o nome e tente novamente.");
				return;
			}

			// Pega o ID da primeira cidade correspondente (a API pode retornar várias)
			Cidade c = listaCidades.getCidades().get(0);
			String previsaoXML = WeatherForecastService.previsoesParaSeteDias(c.getId());
			XStreamParser<PrevisaoCidade, ListaCidades> xspPrevisoes = new XStreamParser<>();
			PrevisaoCidade pc = xspPrevisoes.previsao(previsaoXML);

			// Renderiza a página de clima com os dados obtidos
			renderWeatherPage(out, pc);

		} catch (Exception e) {
			e.printStackTrace(out);
			renderErrorPage(out, "Erro na Aplicação", "Ocorreu um erro ao processar sua solicitação.");
		}
	}

	private void renderWeatherPage(PrintWriter out, PrevisaoCidade data) {
		Previsao today = data.getPrevisoes().get(0);
		String iconKey = iconMappings.getOrDefault(today.getTempo(), "cloud");
		int avgTemp = Math.round((Float.parseFloat(today.getMinima()) + Float.parseFloat(today.getMaxima())) / 2);

		// Início do HTML
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"pt-BR\">");
		out.println("<head>");
		//out.println("<meta charset=\"UTF-8\">");
		out.println("<meta charset='UTF-8'>");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<title>Clima para " + data.getNome() + "</title>");
		out.println("<script src=\"https://cdn.tailwindcss.com\"></script>");
		out.println("<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">");
		out.println("<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>");
		out.println(
				"<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">");
		out.println("<style>");
		out.println("body { font-family: 'Inter', sans-serif; }");
		out.println(
				".weather-card { background: linear-gradient(135deg, #6B73FF 0%, #000DFF 100%); box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25); }");
		out.println(".forecast-card { background: rgba(255, 255, 255, 0.1); backdrop-filter: blur(10px); }");
		out.println("</style>");
		out.println("</head>");
		out.println("<body class=\"bg-gray-100 dark:bg-gray-900 flex items-center justify-center min-h-screen p-4\">");

		// Card Principal
		out.println("<div class=\"w-full max-w-sm mx-auto\">");
		out.println("<div class=\"weather-card text-white rounded-3xl p-6 sm:p-8\">");

		// Conteúdo do tempo
		out.println("<div class=\"text-center\">");

		// Clima atual
		out.println("<div class=\"mb-8\">");
		out.println(
				"<h2 class=\"text-3xl font-bold tracking-wide\">" + data.getNome() + " - " + data.getUf() + "</h2>");
		out.println("<p class=\"text-white/80\">" + formatDate(today.getDia()) + "</p>");
		out.println("<div class=\"my-6\">" + svgIcons.get(iconKey) + "</div>");
		out.println("<p class=\"text-6xl font-extrabold\">" + avgTemp + "°C</p>");
		out.println("<p class=\"text-xl font-medium capitalize\">"
				+ descriptionMappings.getOrDefault(today.getTempo(), "Não Definido") + "</p>");
		out.println("</div>");

		// Detalhes (Min/Max)
		out.println("<div class=\"grid grid-cols-2 gap-4 text-center mb-8\">");
		out.println("<div><p class=\"font-semibold\">" + today.getMinima()
				+ "°C</p><p class=\"text-xs text-white/70\">Mínima</p></div>");
		out.println("<div><p class=\"font-semibold\">" + today.getMaxima()
				+ "°C</p><p class=\"text-xs text-white/70\">Máxima</p></div>");
		out.println("</div>");

		// Previsão para os próximos dias
		out.println("<div>");
		out.println("<h3 class=\"text-left font-bold mb-4\">Próximos dias</h3>");
		out.println("<div class=\"flex justify-between space-x-2\">");

		for (int i = 1; i < Math.min(6, data.getPrevisoes().size()); i++) {
			Previsao day = data.getPrevisoes().get(i);
			String forecastIconKey = iconMappings.getOrDefault(day.getTempo(), "cloud");
			out.println("<div class=\"forecast-card rounded-2xl p-3 text-center flex-1\">");
			out.println("<p class=\"font-semibold text-sm\">" + getDayAbbreviation(day.getDia()) + "</p>");
			out.println("<div class=\"my-1\">" + svgForecastIcons.get(forecastIconKey) + "</div>");
			out.println("<p class=\"font-bold\">" + day.getMaxima() + "°</p>");
			out.println("</div>");
		}

		out.println("</div>"); // Fim do forecast-container
		out.println("</div>"); // Fim da div de previsão
		out.println("</div>"); // Fim do text-center
		out.println("</div>"); // Fim do weather-card
		out.println("</div>"); // Fim do container principal

		// Link para voltar
		out.println("<div class='fixed flex justify-center bottom-4'>");
		out.println(
				"<a href='index.jsp' class='bg-white text-indigo-600 font-semibold py-2 px-4 rounded-full shadow-lg hover:bg-gray-100 transition'>Nova Busca</a>");
		out.println("</div>");

		out.println("</body>");
		out.println("</html>");
	}

	private void renderErrorPage(PrintWriter out, String title, String message) {
		out.println(
				"<!DOCTYPE html><html lang='pt-BR'><head><title>Erro</title><script src='https://cdn.tailwindcss.com'></script></head>");
		out.println("<body class='bg-gray-100 flex items-center justify-center h-screen'>");
		out.println("<div class='text-center bg-white p-10 rounded-lg shadow-xl'>");
		out.println("<h1 class='text-2xl font-bold text-red-600 mb-4'>" + title + "</h1>");
		out.println("<p class='text-gray-700'>" + message + "</p>");
		out.println(
				"<a href='index.html' class='mt-6 inline-block bg-indigo-600 text-white font-bold py-2 px-4 rounded hover:bg-indigo-700'>Tentar Novamente</a>");
		out.println("</div></body></html>");
	}

	// --- Funções Utilitárias ---
	private String formatDate(String dateString) {
		try {
			LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", new Locale("pt", "BR"));
			String formatted = date.format(formatter);
			return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
		} catch (Exception e) {
			return dateString;
		}
	}

	private String getDayAbbreviation(String dateString) {
		try {
			LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String day = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
			return day.substring(0, 1).toUpperCase() + day.substring(1, 3);
		} catch (Exception e) {
			return "Dia";
		}
	}

	// --- Inicialização dos Mapas ---
	private static void initializeMappings() {
		// Mapeamento de descrições
		descriptionMappings.put("ec", "Encoberto com Chuvas Isoladas");
		descriptionMappings.put("ci", "Chuvas Isoladas");
		descriptionMappings.put("c", "Chuva");
		descriptionMappings.put("in", "Instável");
		descriptionMappings.put("pp", "Poss. de Pancadas de Chuva");
		descriptionMappings.put("cm", "Chuva pela Manhã");
		descriptionMappings.put("cn", "Chuva a Noite");
		descriptionMappings.put("pt", "Pancadas de Chuva a Tarde");
		descriptionMappings.put("pm", "Pancadas de Chuva pela Manhã");
		descriptionMappings.put("np", "Nublado e Pancadas de Chuva");
		descriptionMappings.put("pc", "Pancadas de Chuva");
		descriptionMappings.put("pn", "Parcialmente Nublado");
		descriptionMappings.put("cv", "Chuvisco");
		descriptionMappings.put("ch", "Chuvoso");
		descriptionMappings.put("t", "Tempestade");
		descriptionMappings.put("ps", "Predomínio de Sol");
		descriptionMappings.put("e", "Encoberto");
		descriptionMappings.put("n", "Nublado");
		descriptionMappings.put("cl", "Céu Claro");
		descriptionMappings.put("nv", "Nevoeiro");
		descriptionMappings.put("g", "Geada");
		descriptionMappings.put("ne", "Neve");
		descriptionMappings.put("nd", "Não Definido");
		descriptionMappings.put("pnt", "Pancadas de Chuva a Noite");
		descriptionMappings.put("psc", "Possibilidade de Chuva");
		descriptionMappings.put("pcm", "Poss. de Chuva pela Manhã");
		descriptionMappings.put("pct", "Poss. de Chuva a Tarde");
		descriptionMappings.put("pcn", "Poss. de Chuva a Noite");
		descriptionMappings.put("npt", "Nublado com Pancadas a Tarde");
		descriptionMappings.put("npn", "Nublado com Pancadas a Noite");
		descriptionMappings.put("ncn", "Nublado com Poss. de Chuva a Noite");
		descriptionMappings.put("nct", "Nublado com Poss. de Chuva a Tarde");
		descriptionMappings.put("ncm", "Nublado c/ Poss. de Chuva pela Manhã");
		descriptionMappings.put("npm", "Nublado com Pancadas pela Manhã");
		descriptionMappings.put("npp", "Nublado com Possibilidade de Chuva");
		descriptionMappings.put("vn", "Variação de Nebulosidade");
		descriptionMappings.put("ppt", "Poss. de Pancadas de Chuva a Tarde");
		descriptionMappings.put("ppm", "Poss. de Pancadas de Chuva pela Manhã");

		// Mapeamento de siglas para nomes de ícones
		iconMappings.put("ec", "cloud-rain");
		iconMappings.put("ci", "cloud-rain");
		iconMappings.put("c", "cloud-rain");
		iconMappings.put("in", "cloud-rain");
		iconMappings.put("pp", "cloud-rain");
		iconMappings.put("cm", "cloud-rain");
		iconMappings.put("cn", "cloud-rain");
		iconMappings.put("pt", "cloud-rain");
		iconMappings.put("pm", "cloud-rain");
		iconMappings.put("np", "cloud-rain");
		iconMappings.put("pc", "cloud-rain");
		iconMappings.put("pnt", "cloud-rain");
		iconMappings.put("psc", "cloud-rain");
		iconMappings.put("pcm", "cloud-rain");
		iconMappings.put("pct", "cloud-rain");
		iconMappings.put("pcn", "cloud-rain");
		iconMappings.put("npt", "cloud-rain");
		iconMappings.put("npn", "cloud-rain");
		iconMappings.put("ncn", "cloud-rain");
		iconMappings.put("nct", "cloud-rain");
		iconMappings.put("ncm", "cloud-rain");
		iconMappings.put("npm", "cloud-rain");
		iconMappings.put("npp", "cloud-rain");
		iconMappings.put("ppt", "cloud-rain");
		iconMappings.put("ppm", "cloud-rain");
		iconMappings.put("ps", "sun");
		iconMappings.put("e", "cloud");
		iconMappings.put("n", "cloud");
		iconMappings.put("cl", "sun");
		iconMappings.put("nv", "cloud");
		iconMappings.put("g", "cloud");
		iconMappings.put("ne", "cloud");
		iconMappings.put("nd", "cloud");
		iconMappings.put("pn", "cloud-sun");
		iconMappings.put("cv", "cloud-rain");
		iconMappings.put("ch", "cloud-rain");
		iconMappings.put("t", "cloud-lightning");
		iconMappings.put("vn", "cloud-sun");

		// Ícones SVG
		svgIcons.put("sun",
				"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"mx-auto feather feather-sun text-yellow-300\"><circle cx=\"12\" cy=\"12\" r=\"5\"></circle><line x1=\"12\" y1=\"1\" x2=\"12\" y2=\"3\"></line><line x1=\"12\" y1=\"21\" x2=\"12\" y2=\"23\"></line><line x1=\"4.22\" y1=\"4.22\" x2=\"5.64\" y2=\"5.64\"></line><line x1=\"18.36\" y1=\"18.36\" x2=\"19.78\" y2=\"19.78\"></line><line x1=\"1\" y1=\"12\" x2=\"3\" y2=\"12\"></line><line x1=\"21\" y1=\"12\" x2=\"23\" y2=\"12\"></line><line x1=\"4.22\" y1=\"19.78\" x2=\"5.64\" y2=\"18.36\"></line><line x1=\"18.36\" y1=\"5.64\" x2=\"19.78\" y2=\"4.22\"></line></svg>");
		svgIcons.put("cloud",
				"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"mx-auto feather feather-cloud text-gray-300\"><path d=\"M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z\"></path></svg>");
		svgIcons.put("cloud-rain",
				"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"mx-auto feather feather-cloud-rain text-blue-300\"><line x1=\"16\" y1=\"13\" x2=\"16\" y2=\"21\"></line><line x1=\"8\" y1=\"13\" x2=\"8\" y2=\"21\"></line><line x1=\"12\" y1=\"15\" x2=\"12\" y2=\"23\"></line><path d=\"M20 16.58A5 5 0 0 0 18 7h-1.26A8 8 0 1 0 4 15.25\"></path></svg>");
		svgIcons.put("cloud-sun",
				"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"mx-auto feather feather-cloud-sun text-yellow-200\"><path d=\"M12 16.5A4.5 4.5 0 1 0 7.5 12h9A4.5 4.5 0 1 0 12 16.5z\"/><path d=\"M12 7V6.5A4.5 4.5 0 1 0 7.5 11H12\"/><path d=\"M12 2v1\"/><path d=\"M12 21v-1\"/><path d=\"M16.24 7.76l.71-.71\"/><path d=\"M7.05 18.95l-.71.71\"/><path d=\"M22 12h-1\"/><path d=\"M3 12H2\"/><path d=\"M18.95 7.05l-.71-.71\"/><path d=\"M7.76 16.24l-.71.71\"/></svg>");
		svgIcons.put("cloud-lightning",
				"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\" class=\"mx-auto feather feather-cloud-lightning text-yellow-400\"><path d=\"M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z\"></path><polyline points=\"13 11 9 17 15 17 11 23\"></polyline></svg>");

		// Ícones SVG para a previsão (menores)
		svgIcons.forEach((key, value) -> svgForecastIcons.put(key,
				value.replace("width=\"100\"", "width=\"24\"").replace("height=\"100\"", "height=\"24\"")));
	}
}