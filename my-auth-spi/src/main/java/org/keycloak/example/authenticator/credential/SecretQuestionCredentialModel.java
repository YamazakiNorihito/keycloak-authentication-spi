package org.keycloak.example.authenticator.credential;

import java.io.IOException;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.example.authenticator.credential.dto.SecretQuestionCredentialData;
import org.keycloak.example.authenticator.credential.dto.SecretQuestionSecretData;
import org.keycloak.util.JsonSerialization;

/*
 * This class extends CredentialModel to handle secret question-based credentials in Keycloak.
 * 
 * In Keycloak, credentials are stored in a table with the following fields:
 * 
 * - ID: Primary key of the credential
 *
 * - user_ID: Foreign key linking to the user
 * 
 * - credential_type: Type of credential
 * 
 * - created_date: Timestamp of creation
 * 
 * - user_label: User-editable label for the credential
 * 
 * - secret_data: JSON containing non-externalizable information
 * 
 * - credential_data: JSON containing externalizable information
 * 
 * - priority: Indicates the preferred credential when multiple choices are available
 * 
 * The secret_data and credential_data fields contain JSON, and the structure is determined by the
 * implementation.
 * 
 * Example data format:
 * 
 * - credential_data: { "question": "aQuestion" }
 * 
 * - secret_data: { "answer": "anAnswer" }
 */
public class SecretQuestionCredentialModel extends CredentialModel {
    public static final String TYPE = "SECRET_QUESTION";

    private final SecretQuestionCredentialData credentialData;
    private final SecretQuestionSecretData secretData;

    // Constructor for creating a SecretQuestionCredentialModel from question and answer
    private SecretQuestionCredentialModel(String question, String answer) {
        credentialData = new SecretQuestionCredentialData(question);
        secretData = new SecretQuestionSecretData(answer);
    }

    // Constructor for deserialization of existing data
    private SecretQuestionCredentialModel(SecretQuestionCredentialData credentialData,
            SecretQuestionSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    /*
     * Creates a SecretQuestionCredentialModel instance from a question and answer.
     */
    public static SecretQuestionCredentialModel createSecretQuestion(String question,
            String answer) {
        SecretQuestionCredentialModel credentialModel =
                new SecretQuestionCredentialModel(question, answer);
        credentialModel.fillCredentialModelFields();
        return credentialModel;
    }

    // Fills the inherited CredentialModel fields with appropriate values
    private void fillCredentialModelFields() {
        try {
            setCredentialData(JsonSerialization.writeValueAsString(credentialData));
            setSecretData(JsonSerialization.writeValueAsString(secretData));
            setType(TYPE);
            setCreatedDate(Time.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Responsibilities of classes extending CredentialModel:
     * 
     * 1. Maintain the original credential data from the CredentialModel.
     * 
     * 2. Convert getCredentialData and getSecretData into a class-specific format.
     */
    public static SecretQuestionCredentialModel createFromCredentialModel(
            CredentialModel credentialModel) {
        try {
            SecretQuestionCredentialData credentialData = JsonSerialization.readValue(
                    credentialModel.getCredentialData(), SecretQuestionCredentialData.class);
            SecretQuestionSecretData secretData = JsonSerialization
                    .readValue(credentialModel.getSecretData(), SecretQuestionSecretData.class);

            SecretQuestionCredentialModel secretQuestionCredentialModel =
                    new SecretQuestionCredentialModel(credentialData, secretData);
            secretQuestionCredentialModel.setUserLabel(credentialModel.getUserLabel());
            secretQuestionCredentialModel.setCreatedDate(credentialModel.getCreatedDate());
            secretQuestionCredentialModel.setType(TYPE);
            secretQuestionCredentialModel.setId(credentialModel.getId());
            secretQuestionCredentialModel.setSecretData(credentialModel.getSecretData());
            secretQuestionCredentialModel.setCredentialData(credentialModel.getCredentialData());
            return secretQuestionCredentialModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Getters for accessing the credential data and secret data.
     */
    public SecretQuestionCredentialData getSecretQuestionCredentialData() {
        return credentialData;
    }

    public SecretQuestionSecretData getSecretQuestionSecretData() {
        return secretData;
    }
}
