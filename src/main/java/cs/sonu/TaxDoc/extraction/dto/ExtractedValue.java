package cs.sonu.TaxDoc.extraction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class ExtractedValue {

    public ExtractedValue() {
    }

    private String value;
    private double confidence;
    private String sourceSnippet;
}
