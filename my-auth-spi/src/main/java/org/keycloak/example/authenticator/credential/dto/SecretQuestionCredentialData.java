package org.keycloak.example.authenticator.credential.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SecretQuestionCredentialData {
    private final String question;

    @JsonCreator
    public SecretQuestionCredentialData(@JsonProperty("question") String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
