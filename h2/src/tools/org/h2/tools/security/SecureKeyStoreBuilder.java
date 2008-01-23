/*
 * Copyright 2004-2008 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.tools.security;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Enumeration;

import org.h2.security.SecureSocketFactory;
import org.h2.util.ByteUtils;

/**
 * Tool to generate source code for the SecureSocketFactory.
 * First, create a keystore using:
 * <pre>
 * keytool -genkey -alias h2 -keyalg RSA -dname "cn=H2" -validity 25000 -keypass h2pass -keystore h2.keystore -storepass h2pass
 * </pre>
 * Then run this application to generate the source code.
 * Then replace the code in the function SecureSocketFactory.getKeyStore as specified
 */
public class SecureKeyStoreBuilder {

    public static void main(String[] a) throws Exception {
        String password = SecureSocketFactory.KEYSTORE_PASSWORD;
        KeyStore store = SecureSocketFactory.getKeyStore(password);
        printKeystore(store, password);
    }
        
    private static void printKeystore(KeyStore store, String password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateEncodingException {
        System.out.println("KeyStore store = KeyStore.getInstance(\""+store.getType()+"\");");
        System.out.println("store.load(null, password.toCharArray());");
        //System.out.println("keystore provider="+store.getProvider().getName());
        Enumeration en = store.aliases();
        while (en.hasMoreElements()) {
            String alias = (String) en.nextElement();
            Key key = store.getKey(alias, password.toCharArray());
            System.out.println("KeyFactory keyFactory = KeyFactory.getInstance(\"" + key.getAlgorithm() + "\");");
            System.out.println("store.load(null, password.toCharArray());");
            String pkFormat = key.getFormat();
            String encoded = ByteUtils.convertBytesToString(key.getEncoded());
            System.out.println(pkFormat + "EncodedKeySpec keySpec = new " + pkFormat + "EncodedKeySpec(getBytes(\""
                    + encoded + "\"));");
            System.out.println("PrivateKey privateKey = keyFactory.generatePrivate(keySpec);");
            System.out.println("Certificate[] certs = new Certificate[]{");
            Certificate[] certs = store.getCertificateChain(alias);
            for (int i = 0; i < certs.length; i++) {
                Certificate cert = certs[i];
                System.out.println("  CertificateFactory.getInstance(\""+cert.getType()+"\").");
                String enc = ByteUtils.convertBytesToString(cert.getEncoded());
                System.out.println("        generateCertificate(new ByteArrayInputStream(getBytes(\""+enc+"\"))),");
                // PublicKey pubKey = cert.getPublicKey();
                // System.out.println("    publicKey algorithm="+pubKey.getAlgorithm());
                // System.out.println("    publicKey format="+pubKey.getFormat());
                // System.out.println("    publicKey format="+ByteUtils.convertBytesToString(pubKey.getEncoded()));
            }
            System.out.println("};");
            System.out.println("store.setKeyEntry(\""+alias+"\", privateKey, password.toCharArray(), certs);");
        }          
    }
    
//     private void listCipherSuites(SSLServerSocketFactory f) {
//        String[] def = f.getDefaultCipherSuites();
//        for (int i = 0; i < def.length; i++) {
//            System.out.println("default = " + def[i]);
//        }
//        String[] sup = f.getSupportedCipherSuites();
//        for (int i = 0; i < sup.length; i++) {
//            System.out.println("supported = " + sup[i]);
//        }
//    }

}
