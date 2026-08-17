package cs.sonu.TaxDoc.extraction.compliance.rules;

import cs.sonu.TaxDoc.extraction.compliance.TaxDocumentComplianceRule;
import cs.sonu.TaxDoc.extraction.dto.ExtractedField;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DeepFieldComplianceRule implements TaxDocumentComplianceRule<W2ExtractionResult> {

        private static final Pattern SSN_PATTERN = Pattern.compile("^(\\d{3}-\\d{2}-\\d{4}|XXX-XX-\\d{4})$");
        private static final Pattern EIN_PATTERN = Pattern.compile("^\\d{2}-\\d{7}$");
        private static final Pattern MONETARY_PATTERN = Pattern.compile("^\\d+(\\.\\d{2})$");

        @Override
        public RuleEvaluationResult evaluate(W2ExtractionResult result) {
                if (result == null) {
                        return new RuleEvaluationResult("Deep Field Micro-Validation Rule", false, 1.0,
                                        "Result object is completely empty.");
                }

                double totalWeightedScore = 0.0;
                double maxPossibleWeight = 0.0;
                StringBuilder feedback = new StringBuilder();

                FieldScore ssnScore = evaluateTextField(result.employeeSsn(), SSN_PATTERN);
                totalWeightedScore += ssnScore.score() * 25.0;
                maxPossibleWeight += 25.0;
                feedback.append(String.format("SSN Score: %.1f (Reason: %s) | ", ssnScore.score(),
                                ssnScore.reasoning()));

                FieldScore einScore = evaluateTextField(result.employerEin(), EIN_PATTERN);
                totalWeightedScore += einScore.score() * 20.0;
                maxPossibleWeight += 20.0;
                feedback.append(String.format("EIN Score: %.1f (Reason: %s) | ", einScore.score(),
                                einScore.reasoning()));

                FieldScore box1Score = evaluateMonetaryField(result.box1Wages());
                totalWeightedScore += box1Score.score() * 20.0;
                maxPossibleWeight += 20.0;
                feedback.append(String.format("Box 1 Wages Score: %.1f (Reason: %s) | ", box1Score.score(),
                                box1Score.reasoning()));

                FieldScore box2Score = evaluateMonetaryField(result.box2FederalTaxWithheld());
                totalWeightedScore += box2Score.score() * 15.0;
                maxPossibleWeight += 15.0;
                feedback.append(String.format("Box 2 Tax Score: %.1f (Reason: %s) | ", box2Score.score(),
                                box2Score.reasoning()));

                FieldScore box3Score = evaluateMonetaryField(result.box3SocialSecurityWages());
                FieldScore box4Score = evaluateMonetaryField(result.box4SocialSecurityTaxWithheld());
                double ssAverage = (box3Score.score() + box4Score.score()) / 2.0;
                totalWeightedScore += ssAverage * 10.0;
                maxPossibleWeight += 10.0;
                feedback.append(String.format("SS FICA Score: %.1f | ", ssAverage));

                FieldScore box5Score = evaluateMonetaryField(result.box5MedicareWages());
                FieldScore box6Score = evaluateMonetaryField(result.box6MedicareTaxWithheld());
                double medAverage = (box5Score.score() + box6Score.score()) / 2.0;
                totalWeightedScore += medAverage * 10.0;
                maxPossibleWeight += 10.0;
                feedback.append(String.format("Medicare Score: %.1f", medAverage));

                double normalizedRuleScore = maxPossibleWeight > 0 ? totalWeightedScore / maxPossibleWeight : 0.0;
                boolean passed = normalizedRuleScore >= 0.85;
                double penalty = passed ? 0.0 : (1.0 - normalizedRuleScore);

                return new RuleEvaluationResult(
                                "Deep Field Micro-Validation Rule",
                                passed,
                                penalty,
                                feedback.toString());
        }

        private FieldScore evaluateTextField(ExtractedField<String> field, Pattern strictPattern) {
                if (field == null || field.value() == null || field.value().isBlank()) {
                        return new FieldScore(0.0, "Field is missing or null.");
                }

                String val = field.value().trim();
                if (strictPattern.matcher(val).matches()) {
                        return new FieldScore(1.0, "Perfect match against strict formatting pattern.");
                } else {
                        return new FieldScore(0.5, "Field present but failed strict pattern validation.");
                }
        }

        private FieldScore evaluateMonetaryField(ExtractedField<Double> field) {
                if (field == null || field.value() == null) {
                        return new FieldScore(0.0, "Monetary field is missing.");
                }

                double val = field.value();
                if (val < 0) {
                        return new FieldScore(0.0, "Monetary value cannot be negative.");
                }

                if (field.rawSnippet() != null && !field.rawSnippet().isBlank()) {
                        String cleanSnippet = field.rawSnippet().replace(",", "").replace("$", "").trim();
                        if (MONETARY_PATTERN.matcher(cleanSnippet).matches()) {
                                return new FieldScore(1.0, "Monetary value and snippet formatting are 100% compliant.");
                        }
                }
                return new FieldScore(0.9, "Valid numeric value present.");
        }

        private record FieldScore(double score, String reasoning) {
        }
}