package org.keycloak.example.authenticator;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.example.authenticator.credential.SecretQuestionCredentialModel;
import org.keycloak.models.KeycloakSession;

// The CredentialProvider interface takes a generic parameter that extends a CredentialModel.
public class SecretQuestionCredentialProviderFactory
        implements CredentialProviderFactory<SecretQuestionCredentialProvider> {
    public static final String PROVIDER_ID = "secret-question";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public CredentialProvider<SecretQuestionCredentialModel> create(KeycloakSession session) {
        return new SecretQuestionCredentialProvider(session);
    }
}
