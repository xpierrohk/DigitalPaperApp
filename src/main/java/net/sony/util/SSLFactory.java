package net.sony.util;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

public class SSLFactory {

    private static final String TEMPORARY_KEY_PASSWORD = "changeit";

    private final SSLContext sslContext;
    private final CryptographyUtil cryptographyUtil;

    public SSLFactory(final String certificatePem, final String privateKeyPem, final CryptographyUtil cryptographyUtil) throws GeneralSecurityException, IOException {
        this.cryptographyUtil = cryptographyUtil;

        KeyStore keyStore = getKeyStore(certificatePem, privateKeyPem);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, TEMPORARY_KEY_PASSWORD.toCharArray());
        KeyManager[] keyManagers = kmf.getKeyManagers();

        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(keyStore);
        TrustManager[] trustManagers = tmfactory.getTrustManagers();

        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);
    }

    private KeyStore getKeyStore(String certificatePem, String privateKeyPem) throws GeneralSecurityException, IOException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = factory.generatePrivate(
            new PKCS8EncodedKeySpec(cryptographyUtil.loadPemPrivateKey(privateKeyPem))
        );
        Certificate caCertificate = loadCertificate(certificatePem);

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca-cert", caCertificate);
        keyStore.setCertificateEntry("client-cert", caCertificate);
        keyStore.setKeyEntry("client-key", privateKey, TEMPORARY_KEY_PASSWORD.toCharArray(), new Certificate[]{caCertificate});
        return keyStore;

    }

    private Certificate loadCertificate(String certificatePem) throws IOException, GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        final byte[] content = readPemContent(certificatePem);
        return certificateFactory.generateCertificate(new ByteArrayInputStream(content));
    }

    private byte[] readPemContent(String pem) throws IOException {
        final byte[] content;
        try (PemReader pemReader = new PemReader(new StringReader(pem))) {
            final PemObject pemObject = pemReader.readPemObject();
            content = pemObject.getContent();
        }
        return content;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }
}