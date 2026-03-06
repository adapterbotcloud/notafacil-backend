package br.com.notafacil.service;


import br.com.notafacil.strategy.CertificateAliasStrategy;
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
import java.security.cert.X509Certificate;
import java.util.Collections;

@Service
public class AzureVaultXmlSigningService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AzureVaultXmlSigningService.class);

    private final KeyStore keyStore;
    private final CertificateAliasStrategy aliasStrategy;

    public AzureVaultXmlSigningService(KeyStore keyStore, CertificateAliasStrategy aliasStrategy) {
        this.keyStore = keyStore;
        this.aliasStrategy = aliasStrategy;
    }

    /**
     * Assina o XML de entrada (enveloped) e retorna o XML com <Signature> inserido.
     */
    public String signXml(String xml) throws Exception {
        String alias = aliasStrategy.resolveAlias();
        log.info("Assinando XML com alias: {}", alias);

        // Forçar refresh do KeyStore Azure JCA
        try { keyStore.load(null, null); } catch (Exception e) { log.debug("KeyStore reload: {}", e.getMessage()); }

        // Listar aliases disponíveis
        try {
            java.util.Enumeration<String> aliases = keyStore.aliases();
            java.util.List<String> aliasList = new java.util.ArrayList<>();
            while (aliases.hasMoreElements()) aliasList.add(aliases.nextElement());
            log.info("Aliases disponíveis no KeyStore: {}", aliasList);
        } catch (Exception e) { log.warn("Erro ao listar aliases: {}", e.getMessage()); }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

        if (certificate == null) {
            throw new IllegalStateException("Certificado não encontrado no KeyStore para alias: " + alias);
        }
        if (privateKey == null) {
            throw new IllegalStateException("Chave privada não encontrada no KeyStore para alias: " + alias);
        }

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
    public String signXml(String xml, String idToSign) throws Exception {
        String alias = aliasStrategy.resolveAlias();
        log.info("Assinando XML (idToSign={}) com alias: {}", idToSign, alias);
        try { keyStore.load(null, null); } catch (Exception e) { log.debug("KeyStore reload: {}", e.getMessage()); }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

        if (certificate == null) {
            throw new IllegalStateException("Certificado não encontrado no KeyStore para alias: " + alias);
        }
        if (privateKey == null) {
            throw new IllegalStateException("Chave privada não encontrada no KeyStore para alias: " + alias);
        }
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
