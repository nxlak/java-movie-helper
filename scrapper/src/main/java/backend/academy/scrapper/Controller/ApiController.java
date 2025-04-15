package backend.academy.scrapper.Controller;

import backend.academy.scrapper.DTO.Country;
import backend.academy.scrapper.DTO.Genre;
import backend.academy.scrapper.DTO.MovieDoc;
import backend.academy.scrapper.DTO.MovieInfo;
import backend.academy.scrapper.DTO.MovieResponse;
import backend.academy.scrapper.ScrapperConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ApiController {

    private final String TOKEN;

    @Autowired
    public ApiController(ScrapperConfig scrapperConfig) {
        this.TOKEN = scrapperConfig.kinopoiskToken();
    }

    private List<MovieInfo> proccessData(MovieResponse movieResponse) {
        List<MovieInfo> result = new ArrayList<>();
        for (MovieDoc doc : movieResponse.getDocs()) {
            Double kpRating = doc.getRating() != null ? doc.getRating().getKp() : null;
            String posterUrl = doc.getPoster() != null ? doc.getPoster().getUrl() : null;

            List<String> genres = new ArrayList<>();
            if (doc.getGenres() != null) {
                for (Genre genre : doc.getGenres()) {
                    genres.add(genre.getName());
                }
            }

            List<String> countries = new ArrayList<>();
            if (doc.getCountries() != null) {
                for (Country country : doc.getCountries()) {
                    countries.add(country.getName());
                }
            }

            result.add(new MovieInfo(
                doc.getName(),
                doc.getYear(),
                doc.getDescription(),
                kpRating,
                posterUrl,
                genres,
                countries
            ));
        }

        return result;
    }

    //поиск по имени
    @GetMapping("/films/{name}")
    public List<MovieInfo> findByName(@PathVariable String name) {
        try {
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
            String req = "https://api.kinopoisk.dev/v1.4/movie/search?page=1&limit=3&query=" + encodedName;

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(req))
                .header("accept", "application/json")
                .header("X-API-KEY", TOKEN)
                .GET()
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();

            return proccessData(mapper.readValue(response.body(), MovieResponse.class));

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private MovieInfo convertToMovieInfo(MovieDoc doc) {
        Double kpRating = doc.getRating() != null ? doc.getRating().getKp() : null;
        String posterUrl = doc.getPoster() != null ? doc.getPoster().getUrl() : null;

        List<String> genres = doc.getGenres() != null
            ? doc.getGenres().stream().map(Genre::getName).toList()
            : List.of();

        List<String> countries = doc.getCountries() != null
            ? doc.getCountries().stream().map(Country::getName).toList()
            : List.of();

        return new MovieInfo(
            doc.getName(),
            doc.getYear(),
            doc.getDescription(),
            kpRating,
            posterUrl,
            genres,
            countries
        );
    }

    public String switchType(String type) {
        type = type.toUpperCase();

        switch (type) {
            case "СЕРИАЛ" -> {
                return "tv-series";
            }
            case "АНИМЕ" -> {
                return "!anime";
            }
            default -> {
                return "movie";
            }
        }
    }

    //получить рандомный фильм и тд
    @GetMapping("/random")
    public MovieInfo getRandom(
                               @RequestParam(required = false) String genre,
                               @RequestParam(required = false) String year,
                               @RequestParam(required = false) String rating,
                               @RequestParam(required = false) String type) {

        try {

            String baseUrl = "https://api.kinopoisk.dev/v1.4/movie/random?notNullFields=name";
            StringBuilder urlBuilder = new StringBuilder(baseUrl);

            if (type != null && !type.isEmpty()) {

                urlBuilder.append("&type="+switchType(type));
            }
            if (year != null && !year.isEmpty()) {
                urlBuilder.append("&year="+year);
            }
            if (rating != null && !rating.isEmpty()) {
                urlBuilder.append("&rating.kp="+rating);
            }
            urlBuilder.append("&votes.kp=30000-1000000");
            if (genre != null && !genre.isEmpty()) {
                String encodedGenre = URLEncoder.encode(genre, StandardCharsets.UTF_8);
                urlBuilder.append("&genres.name="+encodedGenre);
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.valueOf(urlBuilder)))
                .header("accept", "application/json")
                .header("X-API-KEY", TOKEN)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            MovieDoc movieDoc = mapper.readValue(response.body(), MovieDoc.class);

            return convertToMovieInfo(movieDoc);

        } catch (Exception e) {
            e.printStackTrace();
            return new MovieInfo();
        }

    }

    @GetMapping("/filter")
    public List<MovieInfo> findFromFilters(
        @RequestParam(required = false) String genre,
        @RequestParam(required = false) String year,
        @RequestParam(required = false) String rating,
        @RequestParam(required = false) String type) {

        try {

            String baseUrl = "https://api.kinopoisk.dev/v1.4/movie?page=1&limit=5&notNullFields=name";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);

            if (type != null && !type.isEmpty() && !"skip".equalsIgnoreCase(type)) {
                builder.queryParam("type", switchType(type));
            }
            if (year != null && !year.isEmpty() && !"skip".equalsIgnoreCase(year)) {
                builder.queryParam("year", year);
            }
            if (rating != null && !rating.isEmpty() && !"skip".equalsIgnoreCase(rating)) {
                builder.queryParam("rating.kp", rating);
            }
            if (genre != null && !genre.isEmpty() && !"skip".equalsIgnoreCase(genre)) {
                builder.queryParam("genres.name", URLEncoder.encode(genre.toLowerCase(), StandardCharsets.UTF_8));
            }

            builder.queryParam("votes.kp", "10000-10000000");

            String url = builder.build().toUriString();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .header("X-API-KEY", TOKEN)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();

            return proccessData(mapper.readValue(response.body(), MovieResponse.class));

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }

    }
}
