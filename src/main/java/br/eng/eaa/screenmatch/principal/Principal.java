package br.eng.eaa.screenmatch.principal;

import br.eng.eaa.screenmatch.model.*;
import br.eng.eaa.screenmatch.repository.SerieRepository;
import br.eng.eaa.screenmatch.service.ConsumoApi;
import br.eng.eaa.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository serieRepository;
    private List<Serie> series = new ArrayList<>();


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
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
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
        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);
        if (serie.isPresent()) {
            System.out.println("Dados da serie: " + serie.get());
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