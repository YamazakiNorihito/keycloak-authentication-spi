# 概要

このプロジェクトは、Keycloak の [Authenticator SPI](https://www.keycloak.org/docs/26.0.0/server_development/#_auth_spi) を使用して、ユーザーが「母親の旧姓は何ですか？」のような秘密の質問に対する回答を入力することを要求する認証機能を実装しています。この機能は、Keycloak の公式ドキュメントにある Authenticator SPI walkthrough で紹介された例に基づいています。

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

---

# Note

This README is translated using automated translation.

# Overview

This project implements an authentication feature using Keycloak's [Authenticator SPI](https://www.keycloak.org/docs/26.0.0/server_development/#_auth_spi), requiring users to answer a secret question, such as "What is your mother's maiden name?". This functionality is based on the example introduced in the Authenticator SPI walkthrough in Keycloak's official documentation.

## Development Environment Setup

1. Development is done using the Authentication SPI dev container.
2. Keycloak is started using Docker.

```bash
mvn archetype:generate -DgroupId=com.example.keycloak -DartifactId=my-auth-spi -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

## Java Version

Java 17 is used for this project to support Keycloak v22.

- As of [Keycloak 21.0.0](https://www.keycloak.org/docs/21.1.2/release_notes/#keycloak-21-0-0), Java 8, Java 11, and Java 17 are supported, with Java 8 being deprecated.
- In [Keycloak 22.0.0](https://www.keycloak.org/docs/latest/release_notes/index.html#keycloak-22-0-0), support for Java 11 was removed.
- [Keycloak 26.0.0](https://www.keycloak.org/docs/latest/release_notes/index.html#keycloak-26-0-0) supports Java 8 and Java 17.

## References

- <https://github.dev/keycloak/keycloak/tree/main/server-spi/src/main/java/org/keycloak/models>
- <https://github.com/istvano/keycloak-example-spi/blob/main/authenticator-jar/src/main/java/org/keycloak/examples/authenticator/SecretQuestionCredentialProvider.java>
- <https://github.com/keycloak/keycloak-quickstarts/blob/release/22.0/pom.xml>
- <https://www.keycloak.org/docs/latest/server_development/#_themes>

## Deployment

To deploy the Auth SPI to Keycloak, follow these steps:

1. **Build the JAR file**

   1. Move to `my-auth-spi` in the dev container and build it.

      ```bash
      mvn clean package
      ```

2. **Start Keycloak Docker**

   1. On the host machine (separate from the VSCode dev container), move to the `docker` directory and start Docker Compose.

      ```bash
      docker compose -f compose.yml up --build
      ```

3. **Start Authentication Test Application with Keycloak**

   1. Use `https://www.keycloak.org/app/` or use `keycloak-client-sample`.
   2. If using `keycloak-client-sample`:

      1. Move to the `keycloak-client-sample` directory and start Docker.

         ```bash
         docker build -t flask-keycloak-app .
         docker run -p 5151:5000 --rm flask-keycloak-app
         ```

4. **Verify Startup**

   1. Keycloak: <http://localhost:8080/>
   2. Authentication Test Application: <http://localhost:5151/>

5. **Setup**

   1. Follow the instructions on this site up to `Secure the first application`:
      - <https://www.keycloak.org/getting-started/getting-started-docker>
   2. **Auth SPI Configuration**
      1. Go to `Authentication` from the left menu.
      2. **Create Browser Flow**
         1. Duplicate the built-in `browser`.
         2. Find `Secret Question` and add it immediately after `Username Password Form` as a step, setting `Requirement` to `Required`.
         3. Bind the flow to the `browser flow`.
      3. **Required Actions**
         1. Enable `Secret Question`.
   3. **Themes Setup**
      1. Go to `Realm Settings` > `Themes` from the left menu.
         1. Set Login theme to `custom`.

6. **Authentication Flow**

   1. During the first login, the user will be prompted to set up a secret question answer (e.g., "What is your mom's first name?").
   2. For subsequent logins, the user must answer the question set in step 6-1 to proceed.
