package org.keycloak.example.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialTypeMetadata;
import org.keycloak.credential.CredentialTypeMetadataContext;
import org.keycloak.example.authenticator.credential.SecretQuestionCredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

public class SecretQuestionCredentialProvider
        implements CredentialProvider<SecretQuestionCredentialModel>, CredentialInputValidator {
    private static final Logger logger = Logger.getLogger(SecretQuestionCredentialProvider.class);

    protected KeycloakSession session;

    public SecretQuestionCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    /*
     * CredentialInputValidatorインターフェースを実装したいです。
     * そのためにも、このプロバイダーがAuthenticatorのためにクレデンシャルを検証するためにも使用できることを示す必要があります。
     * 
     * - getType()
     * 
     * - getCredentialFromModel(CredentialModel model)
     * 
     * - createCredential(RealmModel realm, UserModel user, SecretQuestionCredentialModel
     * credentialModel)
     * 
     * - deleteCredential(RealmModel realm, UserModel user, String credentialId)
     * 
     * - isValid(RealmModel realm, UserModel user, CredentialInput input)
     */
    @Override
    public String getType() {
        return SecretQuestionCredentialModel.TYPE;
    }

    @Override
    public SecretQuestionCredentialModel getCredentialFromModel(CredentialModel model) {
        return SecretQuestionCredentialModel.createFromCredentialModel(model);
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user,
            SecretQuestionCredentialModel credentialModel) {
        if (credentialModel.getCreatedDate() == null) {
            credentialModel.setCreatedDate(Time.currentTimeMillis());
        }
        return user.credentialManager().createStoredCredential(credentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return user.credentialManager().removeStoredCredentialById(credentialId);
    }

    // 指定されたrealm内の指定されたユーザーに対してクレデンシャルが有効かどうかをテストする
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!(input instanceof UserCredentialModel)) {
            logger.debug("Expected instance of UserCredentialModel for CredentialInput");
            return false;
        }
        if (!supportsCredentialType(input.getType())) {
            return false;
        }
        String challengeResponse = input.getChallengeResponse();
        if (challengeResponse == null) {
            return false;
        }

        // https://github.com/keycloak/keycloak/blob/47a7d9c12eac55980b083d7dc2da16643ef3dd32/services/src/main/java/org/keycloak/credential/OTPCredentialProvider.java#L96-L97
        CredentialModel credentialModel =
                getCredentialStore(user).getStoredCredentialById(input.getCredentialId());

        SecretQuestionCredentialModel sqcm = getCredentialFromModel(credentialModel);
        return sqcm.getSecretQuestionSecretData().getAnswer().equals(challengeResponse);
    }

    /*
     * ユーザーの認証情報は、以前は session.userCredentialManager().method(realm, user, ...)
     * を使用して管理されていました。新しい方法では、user.credentialManager().method(...) を使用します。この形式は、認証情報の機能をユーザーの API
     * に近づけるものであり、レルムとストレージに関するユーザー認証情報の場所の知識を前提としていません。
     * 
     * 古い API は削除されました。 //
     * https://docs.redhat.com/ja/documentation/red_hat_build_of_keycloak/22.0/html/
     * migration_guide/ deprecated-methods#credential_management_for_users
     * 
     * private UserCredentialStore getCredentialStore() { return session.userCredentialManager(); }
     */
    private SubjectCredentialManager getCredentialStore(UserModel user) {
        return user.credentialManager();
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {

        if (!supportsCredentialType(credentialType))
            return false;
        // 参考：https://github.com/keycloak/keycloak/blob/47a7d9c12eac55980b083d7dc2da16643ef3dd32/services/src/main/java/org/keycloak/credential/OTPCredentialProvider.java#L71-L72
        /*
         * return !getCredentialStore().getStoredCredentialsByType(realm, user, credentialType)
         * .isEmpty();
         */
        return !user.credentialManager().getStoredCredentialsByTypeStream(credentialType).findAny()
                .isEmpty();
    }

    // should at least include type and category of authenticator, displayName and
    // removeable
    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(
            CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder().type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName(SecretQuestionCredentialProviderFactory.PROVIDER_ID)
                .helpText("secret-question-text")
                .createAction(SecretQuestionAuthenticatorFactory.PROVIDER_ID).removeable(false)
                .build(session);
    }
}
