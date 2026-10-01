package com.example.unimarket.service;

import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.response.InstitutionEmailValidationResponse;
import com.example.unimarket.response.InstitutionPolicyResponse;

import java.util.List;

public interface IInstitutionEmailPolicyService {
    List<InstitutionPolicyResponse> getInstitutions();
    InstitutionEmailValidationResponse validate(String organizationName,
                                                  CommunityPersona persona,
                                                  String email);
}
