package br.eng.eaa.screenmatch.principal;

import br.eng.eaa.screenmatch.model.*;
import br.eng.eaa.screenmatch.repository.SerieRepository;
import br.eng.eaa.screenmatch.service.ConsumoApi;
import br.eng.eaa.screenmatch.service.ConverteDados;

import java.util.*;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository serieRepository;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries
                    4 - Buscar série por título
                    5 - Buscar série por ator
                    6 - Buscar Top 5 series
                    7 - Buscar série por categoria
                    8 - Buscar séries por temporada e avaliação
                    9 - Buscar episódios por temporada
                    10 - Top episódios por série
                    11 - Buscar episódios a partir de uma data
                                        
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    buscarSeriePorTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpísodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarEpísodiosDepoisDeUmaData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lançamento");
            var anoLancamento = leitura.nextInt();
            List<Episodio> episodiosAno = serieRepository.episodioPorSerieEAno(serie, anoLancamento);
            episodiosAno.forEach(e -> System.out.println(
                    "Série: " + e.getSerie().getTitulo() + " # Episódio: " + e.getTitulo() + " # Temporada: " + e.getTemporada() + " # Data: " + e.getDataLancamento()
            ));
        }
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = serieRepository.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e -> System.out.println(
                    "Série: " + e.getSerie().getTitulo() + " # Episódio: " + e.getTitulo() + " # Temporada: " + e.getTemporada() + " # Avaliação: " + e.getAvaliacao()
            ));

        }

    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Digite o nome do episordio para busca");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodios = serieRepository.episodioPorTrecho(trechoEpisodio);
        if (episodios.isEmpty()) {
            System.out.println("Episódio não encontrado");
        } else {
            episodios.forEach(e -> System.out.println(
                    "Série: " + e.getSerie().getTitulo() + " # Episódio: " + e.getTitulo() + " # Temporada: " + e.getTemporada()
            ));
        }
    }

    private void buscarSeriePorTemporadaEAvaliacao() {
        List<Serie> series = serieRepository.seriesPorTemporadaEAvaliacao(10, 8.0);
        series.forEach(s -> System.out.println(s.getTitulo() + " - " + s.getTotalTemporadas() + " - " + s.getAvaliacao()));
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Digite o nome da categoria para busca");
        var nomeCategoria = leitura.nextLine();
        var categoria = Categoria.fromPortugues(nomeCategoria);
        List<Serie> series = serieRepository.findByGenero(categoria);
        if (series.isEmpty()) {
            System.out.println("Série não encontrada");
        } else {
            series.forEach(s -> System.out.println(s.getTitulo() + " - " + s.getGenero()));
        }
    }

    private void buscarTop5Series() {
        List<Serie> series = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        series.forEach(s -> System.out.println(s.getTitulo() + " - " + s.getAvaliacao()));
    }

    private void buscarSeriePorAtor() {
        System.out.println("Digite o nome do ator para busca");
        var nomeAtor = leitura.nextLine();
        System.out.println("Digite a avaliação mínima");
        var avaliacao = leitura.nextDouble();
        List<Serie> series = serieRepository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        if (series.isEmpty()) {
            System.out.println("Série não encontrada");
        } else {
            series.forEach(s -> System.out.println(s.getTitulo()+ " - " + s.getAvaliacao()) );
        }
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        serieBusca = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);
        if (serieBusca.isPresent()) {
            System.out.println("Dados da serie: " + serieBusca.get());
        } else {
            System.out.println("Série não encontrada");
        }

    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        dadosSeries.add(dados);
//        System.out.println(dados);
        Serie serie = new Serie(dados);
        serieRepository.save(serie);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream()
                    .map(e -> new Episodio(t.numero(), e)))
                    .toList();
            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada");
        }
    }
    private void listarSeriesBuscadas() {
//        List<Serie> series = new ArrayList<>();
//        series = dadosSeries.stream()
//                .map(d -> new Serie(d))
//                .collect(Collectors.toList());
//        series.stream()
//                .sorted(Comparator.comparing(Serie::getGenero))
//                .forEach(System.out::println);
        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }
}