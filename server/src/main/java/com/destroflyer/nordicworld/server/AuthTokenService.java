package com.destroflyer.nordicworld.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.destroflyer.nordicworld.shared.FileManager;
import org.bouncycastle.openssl.PEMReader;

import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class AuthTokenService {

    public static JWTVerifier createVerifier() {
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) readPublicKey("./public_auth_key.ini"), null);
        return JWT.require(algorithm).build();
    }

    private static PublicKey readPublicKey(String pathFilePath) {
        try {
            String publicKeyPath = FileManager.getFileContent(pathFilePath);
            PEMReader pemReader = new PEMReader(new FileReader(publicKeyPath));
            byte[] publicKeyBytes = pemReader.readPemObject().getContent();
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
