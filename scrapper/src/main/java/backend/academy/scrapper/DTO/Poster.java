package backend.academy.scrapper.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Poster {
    private String url;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
