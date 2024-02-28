package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.utils.KeyCertLoader;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class SslServiceConfig {
    private static Logger logger = LoggerFactory.getLogger(SslServiceConfig.class);

    @Autowired ApplicationConfig applicationConfig;

    @Bean
    public TomcatServletWebServerFactory newTomcatServletWebServerFactory() {

        String address = applicationConfig.getService().getAddress();
        int port = applicationConfig.getService().getPort();
        boolean sslOn = applicationConfig.getService().isSslOn();

        System.out.println(
                "Initializing TomcatServletWebServerFactory: "
                        + applicationConfig.getService().toString());

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        try {
            factory.setAddress(InetAddress.getByName(address));
            factory.setPort(port);

            if (sslOn) {
                setupSslService(factory);
            }

        } catch (Exception e) {
            System.out.println("Error loading webserver config " + e);
            System.exit(1);
        }
        return factory;
    }

    private void setupSslService(TomcatServletWebServerFactory factory) throws Exception {
        String sslKey = applicationConfig.getService().getSslKey();
        String sslCert = applicationConfig.getService().getSslCert();
        String caCert = applicationConfig.getService().getCaCert();

        Ssl ssl = new Ssl();
        ssl.setClientAuth(Ssl.ClientAuth.NEED);
        ssl.setKeyPassword("");

        KeyCertLoader keyCertLoader = new KeyCertLoader();

        for (Provider provider : Security.getProviders()) {
            logger.debug("Provider: {}", provider.getName());
        }

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource sslKeyResource = resolver.getResource(sslKey);
        PrivateKey privateKey = keyCertLoader.toPrivateKey(sslKeyResource.getInputStream(), null);

        org.springframework.core.io.Resource sslCertResource = resolver.getResource(sslCert);
        X509Certificate[] certificates =
                keyCertLoader.toX509Certificates(sslCertResource.getInputStream());
        keyStore.setKeyEntry("mykey", privateKey, "".toCharArray(), certificates);

        org.springframework.core.io.Resource caCertResource = resolver.getResource(caCert);
        X509Certificate[] caCertificates =
                keyCertLoader.toX509Certificates(caCertResource.getInputStream());

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(null);
        trustStore.setCertificateEntry("mykey", caCertificates[0]);

        factory.setSslStoreProvider(
                new SslStoreProvider() {
                    @Override
                    public KeyStore getTrustStore() throws Exception {
                        return trustStore;
                    }

                    @Override
                    public KeyStore getKeyStore() throws Exception {
                        return keyStore;
                    }
                });

        factory.setSsl(ssl);
    }
}
