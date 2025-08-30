package com.odte.topicurator.ai.dto;

public class NewsResponse {
    private String link;
    private String summary;
    private String pros;
    private String cons;
    private String neutralBias;
    private String unneutralBias;

    public NewsResponse(String link, String summary, String pros, String cons, String neutralBias, String unneutralBias) {
        this.link = link;
        this.summary = summary;
        this.pros = pros;
        this.cons = cons;
        this.neutralBias = neutralBias;
        this.unneutralBias = unneutralBias;
    }

    public String getLink() { return link; }
    public String getSummary() { return summary; }
    public String getPros() { return pros; }
    public String getCons() { return cons; }
    public String getNeutralBias() { return neutralBias; }
    public String getUnneutralBias() { return unneutralBias; }
}
