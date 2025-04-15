package backend.academy.scrapper.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MovieInfo {
    private String name;
    private Integer year;
    private String description;

    @JsonProperty("ratingKp")
    private Double ratingKp;

    private String posterUrl;

    private List<String> genres;
    private List<String> countries;

    public MovieInfo() {
    }

    public MovieInfo(String name, Integer year, String description, Double ratingKp, String posterUrl, List<String> genres, List<String> countries) {
        this.name = name;
        this.year = year;
        this.description = description;
        this.ratingKp = ratingKp;
        this.posterUrl = posterUrl;
        this.genres = genres;
        this.countries = countries;
    }

    public List<String> getGenres() { return genres; }
    public List<String> getCountries() { return countries; }
    public String getPosterUrl() { return posterUrl; }
    public String getName() { return name; }
    public Integer getYear() { return year; }
    public String getDescription() { return description; }
    public Double getRatingKp() { return ratingKp; }
}
