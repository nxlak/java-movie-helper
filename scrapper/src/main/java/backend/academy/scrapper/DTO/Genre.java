package backend.academy.scrapper.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Genre {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
