package org.keycloak.example.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

// this is just responsible for creating the required action provider instance.
public class SecretQuestionRequiredActionFactory implements RequiredActionFactory {

    private static final SecretQuestionRequiredAction SINGLETON =
            new SecretQuestionRequiredAction();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return SINGLETON;
    }


    @Override
    public String getId() {
        return SecretQuestionRequiredAction.PROVIDER_ID;
    }

    // The getDisplayText() method is just for the Admin Console when it wants to display a friendly
    // name for the required action.
    @Override
    public String getDisplayText() {
        return "Secret Question";
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

}
