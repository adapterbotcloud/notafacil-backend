package com.notafacil.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;

@Service
public class AzureVaultXmlSigningService {

    @Value("${azure.keyvault.url}")
    private String vaultUrl;

    @Value("${azure.client.id}")
    private String clientId;

    @Value("${azure.client.secret}")
    private String clientSecret;

    @Value("${azure.tenant.id}")
    private String tenantId;

    private PrivateKey privateKey;
    private X509Certificate certificate;

    @PostConstruct
    public void init() throws Exception {
        // 1) Configure credenciais e propriedades do sistema para JCA
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        System.setProperty("azure.keyvault.uri", vaultUrl);
        System.setProperty("azure.keyvault.tenant-id", tenantId);
        System.setProperty("azure.keyvault.client-id", clientId);
        System.setProperty("azure.keyvault.client-secret", clientSecret);

        // 2) Registrar o JCA Provider (usa propriedades definidas)
        var provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        // 3) Carregar KeyStore do Key Vault via JCA
        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        ks.load(null, null);

        // 4) Extrair chave privada e certificado
        this.privateKey = (PrivateKey) ks.getKey(vaultUrl + "/keys/" + provider, null);
        // Na verdade, basta usar alias que é o nome do certificado/import
        this.privateKey = (PrivateKey) ks.getKey("27288254000103", null);
        this.certificate = (X509Certificate) ks.getCertificate("27288254000103");
    }

    /**
     * Assina o XML de entrada (enveloped) e retorna o XML com <Signature> inserido.
     */
    public String signXml(String xml) throws Exception {
        // 1) Parse do XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));

        // 2) Fábrica de assinatura
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // 3) Reference (enveloped, documento inteiro)
        Reference ref = fac.newReference(
                "",
                fac.newDigestMethod(DigestMethod.SHA256, null),
                Collections.singletonList(
                        fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)
                ),
                null,
                null
        );

        // 4) SignedInfo
        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(
                        CanonicalizationMethod.INCLUSIVE,
                        (C14NMethodParameterSpec) null
                ),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                Collections.singletonList(ref)
        );

        // 5) KeyInfo com X509Data
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        X509Data x509Data = kif.newX509Data(Collections.singletonList(certificate));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));

        // 6) Contexto de assinatura no elemento raiz
        DOMSignContext ctx = new DOMSignContext(privateKey, doc.getDocumentElement());

        // 7) Gerar e aplicar assinatura
        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(ctx);

        // 8) Serializar de volta para String
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));

        return sw.toString();
    }

    /**
     * Assina o XML de entrada (enveloped) e retorna o XML com <Signature> inserido.
     */
    public String signXml(String xml,String idToSign) throws Exception {
        // 1) Parse do XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));

        // 2) Fábrica de assinatura
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // 3) Reference (enveloped, documento inteiro)
        Reference ref = fac.newReference(
                "#" + idToSign,
                fac.newDigestMethod(DigestMethod.SHA256, null),
                Collections.singletonList(
                        fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)
                ),
                null,
                null
        );

        // 4) SignedInfo
        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(
                        CanonicalizationMethod.INCLUSIVE,
                        (C14NMethodParameterSpec) null
                ),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                Collections.singletonList(ref)
        );

        // 5) KeyInfo com X509Data
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        X509Data x509Data = kif.newX509Data(Collections.singletonList(certificate));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));

        Element loteRpsElement = (Element) doc.getElementsByTagNameNS("*", "LoteRps").item(0);
        loteRpsElement.setIdAttribute("Id", true);

        // 6) Contexto de assinatura no elemento raiz
        DOMSignContext ctx = new DOMSignContext(privateKey, loteRpsElement.getParentNode());

        // 7) Gerar e aplicar assinatura
        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(ctx);

        // 8) Serializar de volta para String
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));

        return sw.toString();
    }
}
