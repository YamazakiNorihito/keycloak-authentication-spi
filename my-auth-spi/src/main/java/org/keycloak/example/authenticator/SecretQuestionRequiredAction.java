package org.keycloak.example.authenticator;

import org.keycloak.authentication.CredentialRegistrator;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.example.authenticator.credential.SecretQuestionCredentialModel;
import jakarta.ws.rs.core.Response;

public class SecretQuestionRequiredAction implements RequiredActionProvider, CredentialRegistrator {
    // RequiredActionProvider.requiredActionChallenge() is the initial call by the
    // flow manager into
    // the required action. This method is responsible for rendering the HTML form
    // that will drive
    // the required action.
    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form().createForm("secret-question-config.ftl");
        // The challenge() method notifies the flow manager that a required action must
        // be executed.
        context.challenge(challenge);

    }

    // responsible for processing input from the HTML form of the required action.
    // The action URL of
    // the form will be routed to the RequiredActionProvider.processAction() method
    @Override
    public void processAction(RequiredActionContext context) {
        String answer =
                (context.getHttpRequest().getDecodedFormParameters().getFirst("secret_answer"));
        SecretQuestionCredentialProvider sqcp = (SecretQuestionCredentialProvider) context
                .getSession().getProvider(CredentialProvider.class, "secret-question");
        sqcp.createCredential(context.getRealm(), context.getUser(), SecretQuestionCredentialModel
                .createSecretQuestion("What is your mom's first name?", answer));
        context.success();
    }

    public static final String PROVIDER_ID = "secret_question_config";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    @Override
    public void close() {

    }

}
