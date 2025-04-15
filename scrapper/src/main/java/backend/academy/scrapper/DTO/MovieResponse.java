package backend.academy.scrapper.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieResponse {
    private List<MovieDoc> docs;
    private int total;
    private int limit;
    private int page;
    private int pages;

    public MovieResponse() {
    }

    public List<MovieDoc> getDocs() { return docs; }
    public void setDocs(List<MovieDoc> docs) { this.docs = docs; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
}
