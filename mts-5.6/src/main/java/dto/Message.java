package dto;

import lombok.Data;

@Data
public class Message {
    private String content;
    private EnrichmentType enrichmentType;

    public enum EnrichmentType {
        MSISDN;
    }
}
