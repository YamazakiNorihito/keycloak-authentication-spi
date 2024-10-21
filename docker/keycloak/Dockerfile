ARG dbflavor=mysql
ARG kcversion=22.0.3

FROM quay.io/keycloak/keycloak:${kcversion} AS builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

ARG dbflavor
ENV KC_DB=${dbflavor}

COPY docker/keycloak/cache-ispn-jdbc-ping-${dbflavor}.xml /opt/keycloak/conf/cache-ispn-jdbc-ping.xml

# カスタムプロバイダ(JAR)を追加
COPY my-auth-spi/target/my-auth-spi-1.0-SNAPSHOT.jar /opt/keycloak/providers/
# カスタムテーマ(FTL)ファイルを追加
COPY my-auth-spi/src/main/resources/themes/custom/login/ /opt/keycloak/themes/custom/login/

# RUN /opt/keycloak/bin/kc.sh build --http-relative-path=/auth
RUN /opt/keycloak/bin/kc.sh build --cache-config-file=cache-ispn-jdbc-ping.xml --http-relative-path=/auth --spi-theme-cache-themes=false --spi-theme-cache-templates=false

FROM quay.io/keycloak/keycloak:${kcversion}
COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]