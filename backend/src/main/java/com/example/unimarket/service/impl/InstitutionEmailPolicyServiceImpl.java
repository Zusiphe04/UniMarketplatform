package com.example.unimarket.service.impl;

import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.domain.enums.InstitutionEmailEnforcementMode;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.response.InstitutionEmailValidationResponse;
import com.example.unimarket.response.InstitutionPolicyResponse;
import com.example.unimarket.service.IInstitutionEmailPolicyService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Loads evidenced institution mailbox rules from data and applies exact institution-specific checks. */
@Service
public class InstitutionEmailPolicyServiceImpl implements IInstitutionEmailPolicyService {
    private static final int POLICY_COLUMN_COUNT = 14;

    private final List<InstitutionPolicy> institutions;
    private final Map<String, InstitutionPolicy> institutionsByKey;

    public InstitutionEmailPolicyServiceImpl(
            @Value("${unimarket.identity.institution-policy-resource}") Resource policyResource)
            throws IOException {
        this.institutions = loadPolicies(policyResource);
        this.institutionsByKey = indexPolicies(institutions);
    }

    @Override
    public List<InstitutionPolicyResponse> getInstitutions() {
        return institutions.stream().map(this::toResponse).toList();
    }

    @Override
    public InstitutionEmailValidationResponse validate(String organizationName,
                                                         CommunityPersona persona,
                                                         String email) {
        if (persona != CommunityPersona.STUDENT && persona != CommunityPersona.FACULTY) {
            throw new ValidationException("Institution email validation is only available for student and faculty personas.");
        }

        InstitutionPolicy institution = institutionsByKey.get(normalizeKey(organizationName));
        if (institution == null) {
            throw new ValidationException("Select an institution from the supported institution list.");
        }
        if (!institution.active()) {
            throw new ValidationException(institution.unavailableReason());
        }

        EmailRule rule = persona == CommunityPersona.STUDENT
                ? institution.studentRule()
                : institution.facultyRule();
        if (rule == null) {
            throw new ValidationException("Email verification is not configured for this affiliation at "
                    + institution.name() + ".");
        }

        EmailParts emailParts = splitEmail(email);
        if (!emailParts.domain().equals(rule.domain())) {
            throw invalidFormat(institution, persona, rule);
        }
        if (rule.enforcementMode() == InstitutionEmailEnforcementMode.STRICT_PATTERN
                && !rule.compiledLocalPartPattern().matcher(emailParts.localPart()).matches()) {
            throw invalidFormat(institution, persona, rule);
        }
        if (rule.enforcementMode() == InstitutionEmailEnforcementMode.MANUAL_REVIEW) {
            throw new ValidationException("This institution currently requires manual academic-email review.");
        }
        if (rule.enforcementMode() == InstitutionEmailEnforcementMode.ADVISORY_PATTERN
                && rule.compiledLocalPartPattern() != null
                && !rule.compiledLocalPartPattern().matcher(emailParts.localPart()).matches()) {
            throw new ValidationException("This email needs manual review because it does not match the current "
                    + institution.name() + " format. Expected: " + rule.formatExample() + ".");
        }

        return new InstitutionEmailValidationResponse(true, institution.code(), institution.name(), persona,
                rule.domain(), rule.formatExample());
    }

    private ValidationException invalidFormat(InstitutionPolicy institution,
                                               CommunityPersona persona,
                                               EmailRule rule) {
        String affiliation = persona == CommunityPersona.STUDENT ? "student" : "faculty";
        return new ValidationException("Use your official " + institution.name() + " " + affiliation
                + " email. Expected format: " + rule.formatExample() + ".");
    }

    private EmailParts splitEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        int separator = normalized.lastIndexOf('@');
        if (separator <= 0 || separator == normalized.length() - 1
                || normalized.indexOf('@') != separator) {
            throw new ValidationException("Enter a valid institutional email address.");
        }
        return new EmailParts(normalized.substring(0, separator), normalized.substring(separator + 1));
    }

    private List<InstitutionPolicy> loadPolicies(Resource resource) throws IOException {
        List<InstitutionPolicy> policies = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("code|")) continue;
                String[] values = line.split("\\|", -1);
                if (values.length != POLICY_COLUMN_COUNT) {
                    throw new IllegalStateException("Invalid institution policy row at line " + lineNumber + ".");
                }
                InstitutionPolicy policy = parsePolicy(values, lineNumber);
                policies.add(policy);
            }
        }
        if (policies.isEmpty()) throw new IllegalStateException("No institution email policies were loaded.");
        return policies.stream().sorted(Comparator.comparing(InstitutionPolicy::name)).toList();
    }

    private InstitutionPolicy parsePolicy(String[] values, int lineNumber) {
        String code = required(values[0], "code", lineNumber);
        String name = required(values[1], "name", lineNumber);
        String institutionType = required(values[2], "institution type", lineNumber);
        boolean active = Boolean.parseBoolean(values[3].trim());
        String checkedOn = clean(values[12]);
        String unavailableReason = clean(values[13]);

        EmailRule studentRule = null;
        EmailRule facultyRule = null;
        if (active) {
            studentRule = createRule(values[4], values[5], values[6], values[7], values[8], checkedOn,
                    "student", lineNumber);
            facultyRule = createRule(values[9], "", values[10], "DOMAIN_ONLY", values[11], checkedOn,
                    "faculty", lineNumber);
        } else if (unavailableReason == null) {
            throw new IllegalStateException("Inactive institution requires a reason at line " + lineNumber + ".");
        }

        return new InstitutionPolicy(code, name, institutionType, active, unavailableReason,
                studentRule, facultyRule);
    }

    private EmailRule createRule(String domainValue,
                                 String localPartPatternValue,
                                 String formatExampleValue,
                                 String enforcementValue,
                                 String sourceUrlValue,
                                 String checkedOn,
                                 String affiliation,
                                 int lineNumber) {
        String domain = required(domainValue, affiliation + " domain", lineNumber).toLowerCase(Locale.ROOT);
        String localPartPattern = clean(localPartPatternValue);
        String formatExample = required(formatExampleValue, affiliation + " format example", lineNumber);
        String sourceUrl = required(sourceUrlValue, affiliation + " source URL", lineNumber);
        String sourceCheckedOn = required(checkedOn, "source checked date", lineNumber);
        InstitutionEmailEnforcementMode enforcementMode;
        try {
            enforcementMode = InstitutionEmailEnforcementMode.valueOf(
                    required(enforcementValue, affiliation + " enforcement mode", lineNumber));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Invalid enforcement mode at line " + lineNumber + ".", exception);
        }

        Pattern compiledPattern = null;
        if (localPartPattern != null) {
            try {
                compiledPattern = Pattern.compile(localPartPattern, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException exception) {
                throw new IllegalStateException("Invalid local-part pattern at line " + lineNumber + ".", exception);
            }
        }
        if ((enforcementMode == InstitutionEmailEnforcementMode.STRICT_PATTERN
                || enforcementMode == InstitutionEmailEnforcementMode.ADVISORY_PATTERN)
                && compiledPattern == null) {
            throw new IllegalStateException("Pattern enforcement requires a pattern at line " + lineNumber + ".");
        }

        return new EmailRule(domain, localPartPattern, compiledPattern, formatExample,
                enforcementMode, sourceUrl, sourceCheckedOn);
    }

    private Map<String, InstitutionPolicy> indexPolicies(List<InstitutionPolicy> policies) {
        Map<String, InstitutionPolicy> result = new HashMap<>();
        for (InstitutionPolicy policy : policies) {
            putUnique(result, normalizeKey(policy.code()), policy);
            putUnique(result, normalizeKey(policy.name()), policy);
        }
        return Map.copyOf(result);
    }

    private void putUnique(Map<String, InstitutionPolicy> values, String key, InstitutionPolicy policy) {
        if (key == null || values.putIfAbsent(key, policy) != null) {
            throw new IllegalStateException("Duplicate or blank institution policy key: " + key);
        }
    }

    private InstitutionPolicyResponse toResponse(InstitutionPolicy policy) {
        return new InstitutionPolicyResponse(policy.code(), policy.name(), policy.institutionType(),
                policy.active(), policy.unavailableReason(), toResponse(policy.studentRule()),
                toResponse(policy.facultyRule()));
    }

    private InstitutionPolicyResponse.EmailPolicy toResponse(EmailRule rule) {
        if (rule == null) return null;
        return new InstitutionPolicyResponse.EmailPolicy(rule.domain(), rule.formatExample(),
                emailPattern(rule), rule.enforcementMode(), rule.sourceUrl(), rule.sourceCheckedOn());
    }

    private String emailPattern(EmailRule rule) {
        String localPart = rule.localPartPattern() == null ? "[^@\\s]+" : rule.localPartPattern();
        String escapedDomain = rule.domain().replace(".", "\\.");
        return "^" + localPart + "@" + escapedDomain + "$";
    }

    private String normalizeKey(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String required(String value, String field, int lineNumber) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new IllegalStateException("Missing " + field + " at institution policy line " + lineNumber + ".");
        }
        return cleaned;
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private record InstitutionPolicy(String code,
                                     String name,
                                     String institutionType,
                                     boolean active,
                                     String unavailableReason,
                                     EmailRule studentRule,
                                     EmailRule facultyRule) { }

    private record EmailRule(String domain,
                             String localPartPattern,
                             Pattern compiledLocalPartPattern,
                             String formatExample,
                             InstitutionEmailEnforcementMode enforcementMode,
                             String sourceUrl,
                             String sourceCheckedOn) { }

    private record EmailParts(String localPart, String domain) { }
}
