package com.bw.saml.cc.security;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CertificateLoader {

    public static String loadCertificateFromResource(String resourceName) {
        try (InputStream is = new ClassPathResource(resourceName).getInputStream()) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(is);
            String publicKey = Base64.getEncoder().encodeToString(certificate.getEncoded());
            return publicKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load certificate", e);
        }
    }
    public static String loadCertificateFromResourcePublicKey(String resourceName) {
        try (InputStream is = new ClassPathResource(resourceName).getInputStream()) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(is);
            String publicKey = Base64.getEncoder().encodeToString(certificate.getPublicKey().getEncoded());
            return publicKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load certificate", e);
        }
    }
    public static void main(String[] args) {
        String resourceName = "/saml1.cer"; // 相对于resources目录的路径
        String publicKey = loadCertificateFromResource(resourceName);
        System.out.println("Certificate loaded successfully.");
    }
}
