package org.keycloak.example.authenticator;

import java.net.URI;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.common.util.ServerCookie;
import org.keycloak.common.util.ServerCookie.SameSiteAttributeValue;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import javax.ws.rs.core.HttpHeaders;

public class SecretQuestionAuthenticator
        implements CredentialValidator<SecretQuestionCredentialProvider>, Authenticator {

    // implements CredentialValidator<SecretQuestionCredentialProvider>
    @Override
    public SecretQuestionCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (SecretQuestionCredentialProvider) session.getProvider(CredentialProvider.class,
                SecretQuestionCredentialProviderFactory.PROVIDER_ID);
    }

    // implements Authenticator

    // this method must return true as we need to validate the secret question
    // associated with the
    // user.
    // カスタム認証プロバイダが処理を行うために事前にユーザが識別されている必要があるかを示します。
    // この例では、ユーザに関連する「秘密の質問」を使って認証を行います。そのため、まず誰の秘密の質問を検証するのかをユーザが特定された後に実行しなければなりません。
    @Override
    public boolean requiresUser() {
        return true;
    }

    // This method is responsible for determining if the user is configured for this
    // particular
    // authenticator.
    // ユーザがこの認証プロバイダを利用するために必要な設定をしているかどうかを判断するためのものです。
    // 例えば、秘密の質問が設定されているか、秘密の質問の利用が有効になっているかなど、仕様にあわせて実装します。
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return getCredentialProvider(session).isConfiguredFor(realm, user, getType(session));
    }

    // ユーザが実行する必要があるアクションを指定（登録）
    // 1. configuredFor() が false を返す場合: ユーザがこの認証プロバイダを使用するための設定が完了していないことを示します。
    // 2. AuthenticatorFactory の isUserSetupAllowed() が true を返す場合:
    // ユーザに設定を行うことが許可されている場合に、setRequiredActions() メソッドが実行されます。
    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction(SecretQuestionRequiredAction.PROVIDER_ID);
    }

    /*
     * The authenticate() method isn’t responsible for processing the secret question form. Its sole
     * purpose is to render the page or to continue the flow.
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (hasCookie(context)) {
            context.success();
            return;
        }
        // AuthenticationFlowContext の form() メソッドが
        // org.keycloak.login.LoginFormsProvider を呼び出している
        // builds an HTML page from a file within your login theme: secret-question.ftl.
        // This file
        // should be added to the theme-resources/templates in your JAR
        // AuthenticationFlowContext の form() メソッドから LoginFormsProvider インスタンスを取得
        // setAttribute
        // を使ってテンプレート内で使用する変数を設定できるテンプレートファイル (例: secret-question.ftl) はカスタムテーマの
        // templates ディレクトリに配置する必要がある
        LoginFormsProvider loginFormsProvider = context.form();
        // loginFormsProvider.setAttribute("username", context.getUser().getUsername());
        Response challenge = loginFormsProvider.createForm("secret-question.ftl");
        context.challenge(challenge);
    }

    protected boolean hasCookie(AuthenticationFlowContext context) {
        Cookie cookie = context.getHttpRequest().getHttpHeaders().getCookies()
                .get("SECRET_QUESTION_ANSWERED");
        boolean result = cookie != null;
        if (result) {
            System.out.println("Bypassing secret question because cookie is set");
        }
        return result;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        boolean validated = validateAnswer(context);
        if (!validated) {
            Response challenge =
                    context.form().setError("badSecret").createForm("secret-question.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }
        setCookie(context);
        context.success();
    }

    /**
     * 認証が成功した際にクッキーを設定するメソッド。
     * 
     * このメソッドは、ユーザが秘密の質問に正しく回答した後に、特定のクッキーを設定することで、 次回の認証時に秘密の質問をスキップできるようにします。クッキーの有効期間（max age）は
     * デフォルトで30日間に設定されていますが、認証プロバイダの設定からカスタマイズすることも可能です。
     * 
     * クッキーの設定値は、AuthenticatorFactory を通じて管理コンソールから設定できるようになっています。 AuthenticatorFactory
     * では、認証プロバイダに必要な設定項目（たとえば "cookie.max.age" など）を 定義し、それを管理コンソールで変更可能にします。これにより、管理者は管理コンソールを使用して、
     * 認証プロバイダの動作を簡単にカスタマイズできます。
     * 
     * 具体的な手順:
     * 
     * 1. AuthenticatorFactory で設定項目を定義します（例: "cookie.max.age"）。
     * 
     * 2. 定義された設定項目は管理コンソール（Admin Console）で表示され、管理者が設定を変更できます。
     * 
     * 3. このメソッドでは、AuthenticatorConfigModel を使用して管理コンソールから設定された値を取得し、 クッキーの有効期限に反映します。
     * 
     */
    protected void setCookie(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int maxCookieAge = 60 * 60 * 24 * 30; // 30 days
        if (config != null) {
            maxCookieAge = Integer.valueOf(config.getConfig().get("cookie.max.age"));

        }
        URI uri = context.getUriInfo().getBaseUriBuilder().path("realms")
                .path(context.getRealm().getName()).build();
        addCookie(context, "SECRET_QUESTION_ANSWERED", "true", uri.getRawPath(), null, null,
                maxCookieAge, false, true);
    }

    public void addCookie(AuthenticationFlowContext context, String name, String value, String path,
            String domain, String comment, int maxAge, boolean secure, boolean httpOnly) {
        HttpResponse response = context.getSession().getContext().getHttpResponse();
        StringBuilder cookieBuilder = new StringBuilder();
        ServerCookie.appendCookieValue(cookieBuilder, 1, name, value, path, domain, comment, maxAge,
                secure, httpOnly, SameSiteAttributeValue.NONE);
        String cookie = cookieBuilder.toString();

        // https://github.com/keycloak/keycloak/blob/47a7d9c12eac55980b083d7dc2da16643ef3dd32/services/src/test/java/org/keycloak/services/resteasy/HttpResponseImpl.java#L46-L47
        // response.getOutputHeaders().add(HttpHeaders.SET_COOKIE, cookie);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
    }

    /**
     * ユーザが入力した秘密の質問の回答を検証するメソッド。
     * 
     * このメソッドは、認証フローの一環として呼び出され、ユーザがフォームに入力したデータを基に 秘密の質問の回答が正しいかどうかをチェックします。複数の資格情報（秘密の質問の回答）が
     * 設定されている場合や、資格情報のIDが指定されていない場合には、デフォルトの資格情報を使用して 検証を行います。
     * 
     * 検証の手順:
     * 
     * 1. フォームからユーザの入力データ（回答と資格情報ID）を取得します。
     * 
     * 2. 資格情報IDが指定されていない場合は、デフォルトの資格情報IDを取得します。
     * 
     * 3. ユーザの入力を資格情報モデルに設定し、登録された資格情報と比較して一致するかを検証します。
     * 
     * @param context 認証フローのコンテキスト。ユーザやセッション、リクエストの情報が含まれる。
     * @return 回答が正しい場合は true、不正な場合は false を返す。
     */
    protected boolean validateAnswer(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData =
                context.getHttpRequest().getDecodedFormParameters();
        String secret = formData.getFirst("secret_answer");
        String credentialId = formData.getFirst("credentialId");
        if (credentialId == null || credentialId.isEmpty()) {
            credentialId = getCredentialProvider(context.getSession()).getDefaultCredential(
                    context.getSession(), context.getRealm(), context.getUser()).getId();
        }

        UserCredentialModel input =
                new UserCredentialModel(credentialId, getType(context.getSession()), secret);
        return getCredentialProvider(context.getSession()).isValid(context.getRealm(),
                context.getUser(), input);
    }

    @Override
    public void close() {

    }
}
