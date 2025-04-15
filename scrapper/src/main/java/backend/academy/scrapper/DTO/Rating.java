package backend.academy.scrapper.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rating {
    private Double kp;

    public Double getKp() { return kp; }
    public void setKp(Double kp) { this.kp = kp; }
}
