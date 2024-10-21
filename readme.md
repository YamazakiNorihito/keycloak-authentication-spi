## 開発環境構築

1. Authentication SPI の開発 dev container を使って開発をします。
2. Keycloak は Docker を使って起動します。

```bash
mvn archetype:generate -DgroupId=com.example.keycloak -DartifactId=my-auth-spi -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

## Java のバージョン

Java のバージョン 17 を使用します。理由は Keycloak v22 を使うためです。

- [Keycloak 21.0.0](https://www.keycloak.org/docs/21.1.2/release_notes/#keycloak-21-0-0) 時点では、Java 8, Java 11, Java 17 をサポートしている。Java 8 は非推奨。
- [Keycloak 22.0.0](https://www.keycloak.org/docs/latest/release_notes/index.html#keycloak-22-0-0) で、Java 11 のサポートが削除されました。
- [Keycloak 26.0.0](https://www.keycloak.org/docs/latest/release_notes/index.html#keycloak-26-0-0) で、Java 8, Java 17 をサポートしています。

## 参考サイト

- <https://github.dev/keycloak/keycloak/tree/main/server-spi/src/main/java/org/keycloak/models>
- <https://github.com/istvano/keycloak-example-spi/blob/main/authenticator-jar/src/main/java/org/keycloak/examples/authenticator/SecretQuestionCredentialProvider.java>
- <https://github.com/keycloak/keycloak-quickstarts/blob/release/22.0/pom.xml>
- <https://www.keycloak.org/docs/latest/server_development/#_themes>

## デプロイ

Keycloak への Auth SPI のデプロイ手順は次の通りです：

1. **JAR ファイルのビルド**

   1. dev container で `my-auth-spi` に移動してビルドします。

      ```bash
      mvn clean package
      ```

2. **Keycloak Docker を起動する**

   1. VSCode の dev container とは別にホストマシンで、`docker` ディレクトリに移動して Docker Compose を起動します。

      ```bash
      docker compose -f compose.yml up --build
      ```

3. **Keycloak との認証テストアプリケーション起動**

   1. `https://www.keycloak.org/app/` を使うか、`keycloak-client-sample` を使います。
   2. `keycloak-client-sample` を使う場合：

      1. `keycloak-client-sample` ディレクトリに移動して Docker を起動します。

         ```bash
         docker build -t flask-keycloak-app .
         docker run -p 5151:5000 --rm flask-keycloak-app
         ```

4. **起動確認**

   1. Keycloak: <http://localhost:8080/>
   2. 認証テストアプリケーション: <http://localhost:5151/>

5. **セットアップ**

   1. このサイトに従い `Secure the first application` まで実行します。
      - <https://www.keycloak.org/getting-started/getting-started-docker>
   2. **Auth SPI 設定**
      1. `左メニュー` > `Authentication`
      2. **Browser Flow 作成**
         1. Built-in の `browser` を Duplicate します。
         2. Add step で `Secret Question` を見つけて `Username Password Form` の直後に追加し、`Requirement` を `Required` に設定します。
         3. `Bind flow` で `browser flow` に作成した Flow をバインドします。
      3. **Required Actions**
         1. `Secret Question` の Enabled を On にします。
   3. **Themes 設定**
      1. `左メニュー` > `Realm Settings` > `Themes`
         1. Login theme を `custom` に設定します。

6. **認証連携**

   1. 初めてのログイン時には `Setup Secret Question` で `What is your mom's first name?` の回答を設定します。
   2. 2 回目以降は 6-1 で設定した回答をしないとログインできません。
