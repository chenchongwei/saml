package com.bw.saml.idp;

import com.bw.saml.cc.security.CertificateLoader;
import com.bw.saml.constants.Constants;
import org.opensaml.saml2.core.NameID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@RestController
@RequestMapping("/sys")
public class SystemController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @GetMapping("/download")
    public void downloadMetaData(HttpServletRequest request,HttpServletResponse response) throws IOException {
        String entityId = Constants.IDP_ENTITY_ID;
        String acsUrl = Constants.IDP_SSO_URL;
        String logoutUrl = Constants.IDP_LOGOUT_URL;
        String certPem = "/saml.cer";
        // 生成元数据
        String metadata = generateSPMetadata(entityId, acsUrl, logoutUrl,certPem);
        response.reset();
        response.setContentType("application/xml;charset=utf-8");
        response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
        response.setHeader("Pragma","no-cache");
        response.setHeader("Expires", "0");
        response.setHeader("Content-Disposition", "attachment; filename=sso_idp_metadata.xml");
        PrintWriter printWriter = response.getWriter();
        printWriter.write(metadata);
        printWriter.flush();
        printWriter.close();
    }

    /**
     * 生成SAML 2.0 Service Provider元数据
     * @param entityId 实体ID
     * @param acsUrl 断言消费者服务URL
     * @param logoutUrl 单点注销服务URL
     * @param certPem PEM格式证书
     * @return 格式化的元数据XML字符串
     */
    public static String generateSPMetadata(String entityId, String acsUrl, String logoutUrl, String certPem) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // 创建EntityDescriptor根元素
            Element entityDescriptor = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:metadata", "md:EntityDescriptor");
            entityDescriptor.setAttribute("entityID", entityId);
            doc.appendChild(entityDescriptor);

            // 创建SPSSODescriptor
            Element spSSODescriptor = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:metadata", "md:IDPSSODescriptor");
            spSSODescriptor.setAttribute("AuthnRequestsSigned", "false");
            spSSODescriptor.setAttribute("WantAssertionsSigned", "true");
            spSSODescriptor.setAttribute("protocolSupportEnumeration", "urn:oasis:names:tc:SAML:2.0:protocol");
            entityDescriptor.appendChild(spSSODescriptor);

            // 添加KeyDescriptor
            Element keyDescriptor = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:metadata", "md:KeyDescriptor");
            keyDescriptor.setAttribute("use", "signing");
            spSSODescriptor.appendChild(keyDescriptor);

            // 添加KeyInfo
            Element keyInfo = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:KeyInfo");
            keyDescriptor.appendChild(keyInfo);

            // 添加X509Data
            Element x509Data = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:X509Data");
            keyInfo.appendChild(x509Data);

            // 添加X509Certificate
            Element x509Certificate = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:X509Certificate");
            String certContent = CertificateLoader.loadCertificateFromResource(certPem); // 相对于resources目录的路径
            x509Certificate.setTextContent(certContent);
            x509Data.appendChild(x509Certificate);

            // 添加KeyDescriptor
            Element keyDescriptor1 = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:metadata", "md:KeyDescriptor");
            keyDescriptor1.setAttribute("use", "encryption");
            spSSODescriptor.appendChild(keyDescriptor1);

            // 添加KeyInfo
            Element keyInfo1 = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:KeyInfo");
            keyDescriptor1.appendChild(keyInfo1);

            // 添加X509Data
            Element x509Data1 = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:X509Data");
            keyInfo1.appendChild(x509Data1);

            // 添加X509Certificate
            Element x509Certificate1 = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:X509Certificate");
            //String certContent1 = CertificateLoader.loadCertificateFromResourcePublicKey(certPem); // 相对于resources目录的路径

            x509Certificate1.setTextContent(certContent);
            x509Data1.appendChild(x509Certificate1);
            // 添加SingleLogoutService
            if (logoutUrl != null && !logoutUrl.isEmpty()) {
                Element sls = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:metadata", "md:SingleLogoutService");
                sls.setAttribute("Binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
                sls.setAttribute("Location", logoutUrl);
                spSSODescriptor.appendChild(sls);
            }
            Element NameIDDescriptor = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:metadata", "md:NameIDFormat");
            NameIDDescriptor.setTextContent(NameID.UNSPECIFIED);
            spSSODescriptor.appendChild(NameIDDescriptor);
            // 添加SingleSignOnService
            Element acs = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:metadata", "md:SingleSignOnService");
            acs.setAttribute("Binding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
            acs.setAttribute("Location", acsUrl);
//            acs.setAttribute("index", "0");
//            acs.setAttribute("isDefault", "true");
            spSSODescriptor.appendChild(acs);

            // 转换为字符串
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            //transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成元数据失败", e);
        }
    }
}
