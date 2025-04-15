package backend.academy.scrapper.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieDoc {
    private String name;
    private Integer year;
    private String description;
    private Rating rating;
    private Poster poster;
    private List<Genre> genres;
    private List<Country> countries;

    public MovieDoc() {
    }

    public List<Genre> getGenres() { return genres; }
    public void setGenres(List<Genre> genres) { this.genres = genres; }

    public List<Country> getCountries() { return countries; }
    public void setCountries(List<Country> countries) { this.countries = countries; }

    public Poster getPoster() { return poster; }
    public void setPoster(Poster poster) { this.poster = poster; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Rating getRating() { return rating; }
    public void setRating(Rating rating) { this.rating = rating; }
}
