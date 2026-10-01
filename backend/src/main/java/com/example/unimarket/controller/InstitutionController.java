package com.example.unimarket.controller;

import com.example.unimarket.request.ValidateInstitutionEmailRequest;
import com.example.unimarket.response.InstitutionEmailValidationResponse;
import com.example.unimarket.response.InstitutionPolicyResponse;
import com.example.unimarket.service.IInstitutionEmailPolicyService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public institution catalogue and academic-email validation preflight. */
@RestController
@RequestMapping("/api/v1/institutions")
public class InstitutionController {
    private final IInstitutionEmailPolicyService institutionEmailPolicyService;

    public InstitutionController(IInstitutionEmailPolicyService institutionEmailPolicyService) {
        this.institutionEmailPolicyService = institutionEmailPolicyService;
    }

    @GetMapping
    public ResponseEntity<List<InstitutionPolicyResponse>> getInstitutions() {
        return ResponseEntity.ok(institutionEmailPolicyService.getInstitutions());
    }

    @PostMapping("/validate-email")
    public ResponseEntity<InstitutionEmailValidationResponse> validateEmail(
            @Valid @RequestBody ValidateInstitutionEmailRequest request) {
        return ResponseEntity.ok(institutionEmailPolicyService.validate(
                request.organizationName(), request.persona(), request.email()));
    }
}
