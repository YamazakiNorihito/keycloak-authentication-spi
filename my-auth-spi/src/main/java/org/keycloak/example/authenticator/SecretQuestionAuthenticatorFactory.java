package org.keycloak.example.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * SecretQuestionAuthenticatorFactory クラスは、AuthenticatorFactory を実装しています。
 * 
 * ドキュメントでは ConfigurableAuthenticatorFactory も implements に指定する例がありますが、
 * ConfigurableAuthenticatorFactory は AuthenticatorFactory によって継承されています。 そのため、AuthenticatorFactory
 * を実装するだけで、ConfigurableAuthenticatorFactory で 定義されたメソッドも実装できることになり、ここでは指定を省略しています。
 */
public class SecretQuestionAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "secret-question-authenticator";
    private static final SecretQuestionAuthenticator SINGLETON = new SecretQuestionAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES =
            {AuthenticationExecutionModel.Requirement.REQUIRED,
                    AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                    AuthenticationExecutionModel.Requirement.DISABLED,
            // AuthenticationExecutionModel.Requirement.CONDITIONAL
            };

    /**
     * SecretQuestionAuthenticatorFactory クラスは、認証フローでの要件スイッチを指定します。
     * 
     * Keycloak には 4 種類の要件タイプ（ALTERNATIVE、REQUIRED、CONDITIONAL、DISABLED）があり、 AuthenticatorFactory
     * の実装は、Admin Console でフローを定義する際に表示される要件オプションを制限できます。
     * 
     * - REQUIRED: 認証ステップが必須です。 - ALTERNATIVE: 認証がオプションで、他のステップでも代替できます。 - CONDITIONAL:
     * サブフローでのみ使用されるべきです。 - DISABLED: 無効化された状態です。
     * 
     * 通常、認証ステップには REQUIRED、ALTERNATIVE、および DISABLED を設定します。 CONDITIONAL はサブフローでのみ使用することが推奨されます。
     */
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    // 認証フローの中でユーザに対する設定を行うことが許可されているかどうかを制御
    // The AuthenticatorFactory.isUserSetupAllowed() is a flag that tells the flow manager whether
    // or not Authenticator.setRequiredActions() method will be called.
    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    // the Admin Console on whether the Authenticator can be configured within a flow.
    @Override
    public boolean isConfigurable() {
        return true;
    }

    // The getConfigProperties() method returns a list of ProviderConfigProperty objects.
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    // These objects define a specific configuration attribute.
    private static final List<ProviderConfigProperty> configProperties =
            new ArrayList<ProviderConfigProperty>();

    // This is the key used in the config map stored in AuthenticatorConfigModel.
    // The label defines how the config option will be displayed in the Admin Console. The type
    // defines if it is a String, Boolean, or other type(defines in ProviderConfigProperty).
    // この例だとAuthenticatorConfigModelはSecretQuestionAuthenticator.javaのsetCookie(AuthenticationFlowContext
    // context)メソッドで使用される
    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName("cookie.max.age");
        property.setLabel("Cookie Max Age");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Max age in seconds of the SECRET_QUESTION_COOKIE.");
        configProperties.add(property);
    }

    // the methods are for the Admin Console. getHelpText() is the tooltip text that will be shown
    // when you are picking the Authenticator you want to bind to an execution.
    @Override
    public String getHelpText() {
        return "A secret question that a user has to answer. i.e. What is your mother's maiden name.";
    }

    // the methods are for the Admin Console. getDisplayType() is the text that will be shown in the
    // Admin Console when listing the Authenticator.
    @Override
    public String getDisplayType() {
        return "Secret Question";
    }

    // the methods are for the Admin Console. getReferenceCategory() is just a category the
    // Authenticator belongs to.
    @Override
    public String getReferenceCategory() {
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
