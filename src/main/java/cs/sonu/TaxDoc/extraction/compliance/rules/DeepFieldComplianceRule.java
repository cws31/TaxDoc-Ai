package cs.sonu.TaxDoc.extraction.compliance.rules;

import cs.sonu.TaxDoc.extraction.compliance.W2ComplianceRule;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import cs.sonu.TaxDoc.extraction.service.AdvancedFieldValidator;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DeepFieldComplianceRule implements W2ComplianceRule {

        private final AdvancedFieldValidator fieldValidator;

        private static final Pattern SSN_PATTERN = Pattern.compile("^(\\d{3}-\\d{2}-\\d{4}|XXX-XX-\\d{4})$");
        private static final Pattern EIN_PATTERN = Pattern.compile("^\\d{2}-\\d{7}$");

        public DeepFieldComplianceRule(AdvancedFieldValidator fieldValidator) {
                this.fieldValidator = fieldValidator;
        }

        @Override
        public RuleEvaluationResult evaluate(W2ExtractionResult result) {
                if (result == null) {
                        return new RuleEvaluationResult("Deep Field Micro-Validation Rule", false, 1.0,
                                        "Result object is completely empty.");
                }

                double totalWeightedScore = 0.0;
                double maxPossibleWeight = 0.0;
                StringBuilder feedback = new StringBuilder();

                AdvancedFieldValidator.FieldScore ssnScore = fieldValidator.evaluateTextField(result.employeeSsn(),
                                SSN_PATTERN);
                totalWeightedScore += ssnScore.score() * 25.0;
                maxPossibleWeight += 25.0;
                feedback.append(String.format("SSN Score: %.1f (Reason: %s) | ", ssnScore.score(),
                                ssnScore.reasoning()));

                AdvancedFieldValidator.FieldScore einScore = fieldValidator.evaluateTextField(result.employerEin(),
                                EIN_PATTERN);
                totalWeightedScore += einScore.score() * 20.0;
                maxPossibleWeight += 20.0;
                feedback.append(String.format("EIN Score: %.1f (Reason: %s) | ", einScore.score(),
                                einScore.reasoning()));

                AdvancedFieldValidator.FieldScore box1Score = fieldValidator.evaluateMonetaryField(result.box1Wages());
                totalWeightedScore += box1Score.score() * 20.0;
                maxPossibleWeight += 20.0;
                feedback.append(
                                String.format("Box 1 Wages Score: %.1f (Reason: %s) | ", box1Score.score(),
                                                box1Score.reasoning()));

                AdvancedFieldValidator.FieldScore box2Score = fieldValidator
                                .evaluateMonetaryField(result.box2FederalTaxWithheld());
                totalWeightedScore += box2Score.score() * 15.0;
                maxPossibleWeight += 15.0;
                feedback.append(
                                String.format("Box 2 Tax Score: %.1f (Reason: %s) | ", box2Score.score(),
                                                box2Score.reasoning()));

                AdvancedFieldValidator.FieldScore box3Score = fieldValidator
                                .evaluateMonetaryField(result.box3SocialSecurityWages());
                AdvancedFieldValidator.FieldScore box4Score = fieldValidator
                                .evaluateMonetaryField(result.box4SocialSecurityTaxWithheld());
                double ssAverage = (box3Score.score() + box4Score.score()) / 2.0;
                totalWeightedScore += ssAverage * 10.0;
                maxPossibleWeight += 10.0;
                feedback.append(String.format("SS FICA Score: %.1f | ", ssAverage));

                AdvancedFieldValidator.FieldScore box5Score = fieldValidator
                                .evaluateMonetaryField(result.box5MedicareWages());
                AdvancedFieldValidator.FieldScore box6Score = fieldValidator
                                .evaluateMonetaryField(result.box6MedicareTaxWithheld());
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
}