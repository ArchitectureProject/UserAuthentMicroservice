package com.efrei.usermicroservice.utils;


import com.efrei.usermicroservice.exceptions.custom.ExpiredJWTException;
import com.efrei.usermicroservice.exceptions.custom.JWTException;
import com.efrei.usermicroservice.model.AppUser;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.*;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.PrivateKey;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class JWTUtils {

    @Value("classpath:keys/publicKey.pem")
    private Resource publicKeyFile;

    @Value("classpath:keys/privateKey.pem")
    private Resource privateKeyFile;

    public String createJWT(AppUser user) {

        RsaJsonWebKey rsaJsonWebKey = createRsaJsonWebKeyFromPemFiles();

        JwtClaims claims = getJwtClaims(user);

        JsonWebSignature jws = new JsonWebSignature();

        jws.setPayload(claims.toJson());
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        String result;
        try {
            result = jws.getCompactSerialization();
        }
        catch (JoseException e) {
            throw new JWTException("Erreur lors du parsing du JWT en chaine de caractère");
        }
        return result;
    }

    public JwtClaims validateJwt(String jwt){
        RsaJsonWebKey rsaJsonWebKey = createRsaJsonWebKeyFromPemFiles();

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime() // the JWT must have an expiration time
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setExpectedIssuer("UserMicroservice") // whom the JWT needs to have been issued by
                .setExpectedAudience("OtherMicroservices") // to whom the JWT is intended for
                .setVerificationKey(rsaJsonWebKey.getKey()) // verify the signature with the public key
                .setJwsAlgorithmConstraints( // only allow the expected signature algorithm(s) in the given context
                        AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.RSA_USING_SHA256) // which is only RS256 here
                .build();
        try
        {
            //  Validate the JWT and process it to the Claims
            return jwtConsumer.processToClaims(jwt);
        }
        catch (InvalidJwtException e)
        {
            if (e.hasExpired())
            {
                throw new ExpiredJWTException("JWT expiré, essayez d'en avoir un nouveau en vous logguant à nouveau");
            }
            throw new JWTException("JWT invalide, essayez de vous logguer à nouveau", e);
        }
    }

    public String createJwks() {
        try {
            RSAPublicKey publicKey = getRSAPublicKeyFromPEM(publicKeyFile);
            PublicJsonWebKey publicJwk = PublicJsonWebKey.Factory.newPublicJwk(publicKey);
            publicJwk.setKeyId("k1");
            JsonWebKeySet jwks = new JsonWebKeySet(publicJwk);
            return jwks.toJson();
        } catch (Exception e) {
            throw new JWTException("Erreur lors de la création du JWKS", e);
        }
    }

    private RsaJsonWebKey createRsaJsonWebKeyFromPemFiles() {
        RsaJsonWebKey rsaJsonWebKey = new RsaJsonWebKey(getRSAPublicKeyFromPEM(publicKeyFile));
        rsaJsonWebKey.setPrivateKey(getRSAPrivateKeyFromPEM(privateKeyFile));
        rsaJsonWebKey.setKeyId("k1");
        return rsaJsonWebKey;
    }

    private JwtClaims getJwtClaims(AppUser user) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("UserMicroservice");  // who creates the token and signs it
        claims.setAudience("OtherMicroservices"); // to whom the token is intended to be sent
        claims.setExpirationTimeMinutesInTheFuture(99999); // time when the token will expire
        claims.setGeneratedJwtId(); // a unique identifier for the token
        claims.setIssuedAtToNow();  // when the token was issued/created
        claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid
        claims.setClaim("userId",user.getId()); // additional claims/attributes about the subject can be added
        claims.setClaim("email",user.getEmail()); // additional claims/attributes about the subject can be added
        claims.setClaim("role",user.getUserRole().toString()); // additional claims/attributes about the subject can be added
        return claims;
    }

    private RSAPublicKey getRSAPublicKeyFromPEM(Resource pemFile) {
        String pemContent = asString(pemFile);
        try (StringReader stringReader = new StringReader(pemContent);
             PEMParser pemParser = new PEMParser(stringReader)) {

            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PublicKey publicKey;

            if (object instanceof SubjectPublicKeyInfo) {
                // Directly convert SubjectPublicKeyInfo to PublicKey
                publicKey = converter.getPublicKey((SubjectPublicKeyInfo) object);
            } else if (object instanceof org.bouncycastle.openssl.PEMKeyPair) {
                // This case handles public keys that are part of a PEMKeyPair
                publicKey = converter.getPublicKey(((org.bouncycastle.openssl.PEMKeyPair) object).getPublicKeyInfo());
            } else {
                throw new IllegalArgumentException("Unsupported PEM object type: " + object.getClass().getSimpleName());
            }

            if (!(publicKey instanceof RSAPublicKey)) {
                throw new IllegalArgumentException("The PEM content does not contain an RSA public key.");
            }

            return (RSAPublicKey) publicKey;
        }
        catch (IOException exception){
            throw new JWTException("Erreur lors du parsing du fichier de clé publique");
        }
    }

    private RSAPrivateKey getRSAPrivateKeyFromPEM(Resource pemFile) {
        String pemContent = asString(pemFile);
        try (StringReader stringReader = new StringReader(pemContent);
             PEMParser pemParser = new PEMParser(stringReader)) {

            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKey privateKey;

            if (object instanceof PEMKeyPair) {
                privateKey = converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo) {
                privateKey = converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new IllegalArgumentException("Unsupported PEM object type: " + object.getClass().getSimpleName());
            }

            if (!(privateKey instanceof RSAPrivateKey)) {
                throw new IllegalArgumentException("The PEM content does not contain an RSA private key.");
            }

            return (RSAPrivateKey) privateKey;
        }
        catch (IOException exception){
            throw new JWTException("Erreur lors du parsing du fichier de clé privée");
        }
    }

    private String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
