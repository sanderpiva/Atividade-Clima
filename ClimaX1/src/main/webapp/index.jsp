<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Consulta de Clima</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
        }
        .card-gradient {
             /* Gradiente azul vibrante da imagem, aplicado ao card */
            background: linear-gradient(135deg, #6B73FF 0%, #000DFF 100%);
        }
    </style>
</head>
<!-- Fundo escuro, como na imagem de referência -->
<body class="bg-gray-900 flex items-center justify-center min-h-screen p-4">
    
    <!-- Card com o gradiente azul -->
    <div class="card-gradient p-8 rounded-2xl shadow-2xl w-full max-w-md">
        <h1 class="text-3xl font-bold text-white text-center mb-2">Previsao do Tempo</h1>
        <p class="text-white/70 text-center mb-8">Insira uma cidade para comecar.</p>
        
        <!-- Formulário com design atualizado -->
        <form action="PrevisaoServlet" method="get" class="space-y-6">
            <div class="relative">
                 <!-- Ícone de busca dentro do input -->
                 <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-300" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
                    </svg>
                </div>
                <!-- Input com fundo escuro semi-transparente -->
                <input type="text" name="cidade" id="cidade" required 
                       placeholder="Ex: Machado"
                       class="block w-full pl-12 pr-4 py-3 bg-black/20 text-white border border-transparent rounded-full placeholder-white/60 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-blue-900 focus:ring-white sm:text-sm transition duration-300">
            </div>
            <!-- Botão com bordas totalmente circulares -->
            <button type="submit" 
                    class="w-full flex justify-center py-3 px-4 border border-transparent rounded-full shadow-lg text-sm font-medium text-blue-700 bg-white hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-blue-900 focus:ring-white transition duration-300 transform hover:scale-105">
                Buscar Previsao
            </button>
        </form>
    </div>
</body>
</html>