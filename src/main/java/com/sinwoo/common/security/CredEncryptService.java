package com.sinwoo.common.security;

import com.sinwoo.platform.auth.dto.CredKeyResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.springframework.stereotype.Component;

@Component
public class CredEncryptService {

    private static final String ALGORITHM = "RSA_OAEP_SHA256";
    private static final String KEY_FORMAT = "SPKI";
    private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
    );

    private final KeyPair keyPair;

    public CredEncryptService() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            this.keyPair = generator.generateKeyPair();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to initialize credential encryption service.", exception);
        }
    }

    public CredKeyResponse getCredKey() {
        return new CredKeyResponse(
                ALGORITHM,
                KEY_FORMAT,
                Base64.getEncoder().encodeToString(getPublicKey().getEncoded())
        );
    }

    public String decryptPassword(String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(), OAEP_SHA256);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new IllegalArgumentException("Unable to decrypt the credential password payload.", exception);
        }
    }

    private PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    private PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}
