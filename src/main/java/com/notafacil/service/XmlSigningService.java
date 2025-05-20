package com.notafacil.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;

@Service
public class XmlSigningService {

    @Value("classpath:${xmlsign.keystore.path}")
    private Resource keystoreResource;

    @Value("${xmlsign.keystore.password}")
    private String keystorePassword;

    @Value("${xmlsign.keystore.alias}")
    private String keyAlias;

    @Value("${xmlsign.keystore.keyPassword}")
    private String keyPassword;

    private PrivateKey privateKey;
    private X509Certificate certificate;

    @PostConstruct
    public void init() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (var is = keystoreResource.getInputStream()) {
            ks.load(is, keystorePassword.toCharArray());
        }
        Key key = ks.getKey(keyAlias, keyPassword.toCharArray());
        if (!(key instanceof PrivateKey)) {
            throw new IllegalStateException("Chave não é PrivateKey");
        }
        privateKey = (PrivateKey) key;
        certificate = (X509Certificate) ks.getCertificate(keyAlias);
    }

    /**
     * Assina o XML de entrada (enveloped) e retorna o XML com <Signature>.
     */
    public String signXml(String xml) throws Exception {
        // 1) Parse do XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

        // 2) Fábrica de assinatura
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // 3) Reference (documento inteiro, enveloped)
        Reference ref = fac.newReference(
                "",
                fac.newDigestMethod(DigestMethod.SHA256, null),
                Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                null,
                null
        );

        // 4) SignedInfo
        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                Collections.singletonList(ref)
        );

        // 5) KeyInfo com X509Data
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        X509Data xd = kif.newX509Data(Collections.singletonList(certificate));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

        // 6) Contexto: assina no elemento raiz
        DOMSignContext ctx = new DOMSignContext(privateKey, doc.getDocumentElement());

        // 7) Gera e aplica a assinatura
        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(ctx);

        // 8) Serializa de volta
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        trans.transform(new DOMSource(doc), new StreamResult(sw));

        return sw.toString();
    }
}
