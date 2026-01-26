package com.bw.saml.cc.service;

import com.bw.saml.cc.pojo.AuthnRequestField;
import com.bw.saml.cc.saml.SAML;
import com.bw.saml.cc.saml.SAMLAssertion;
import com.bw.saml.cc.saml.SAMLSignature;
import com.bw.saml.cc.security.KeyStoreUtil;
import com.bw.saml.constants.Constants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.security.KeyStoreException;
import java.util.UUID;

/**
 * 生成SAMLResponse字符串
 *
 * @author Xiaosy
 * @date 2017-12-14 14:31
 */
@Service
public class SamlResponseGenerator {

    private String userName;
    private String spEntityId;
    private String acsUrl;
    private String inResponseTo;

    public void init(String userName,AuthnRequestField requestField){
        this.userName = userName;
        if(requestField == null){
            this.spEntityId = Constants.SP_ENTITY_ID;
            this.acsUrl = Constants.SP_ACS_URL;
        }else {
            this.spEntityId = requestField.getSpIssuer();
            this.acsUrl = requestField.getAssertionConsumerServiceUrl();
            this.inResponseTo = requestField.getRequestId();
        }
    }

    /**
     * 生成response字符串
     * @param userName
     * @param requestField
     * @return
     * @throws Exception
     */
    public String generateSamlResponse(AuthnRequestField requestField,String userName,String userID) throws Exception {
        init(userName,requestField);
        SAML saml = new SAML(Constants.IDP_ENTITY_ID);
        //创建Subject
        Subject subject = saml.createSubject(userName, NameID.UNSPECIFIED,"bearer",this.acsUrl,inResponseTo);
        //创建断言Assertion
        String assertionId = UUID.randomUUID().toString();
        SAMLAssertion samlAssertion = new SAMLAssertion();
        Assertion assertion = samlAssertion.createStockAuthnAssertion(Constants.IDP_ENTITY_ID,assertionId,spEntityId,userID,userName,inResponseTo);
        assertion.setSubject(subject);
        //创建response
        Response response = saml.createResponse(assertion,inResponseTo);
        response.setDestination(this.acsUrl);
        //签名
        SAMLSignature samlSignature = new SAMLSignature();
        Document document = saml.asDOMDocument(response);
        samlSignature.signSAMLObject(document,assertionId, document.getElementsByTagName("saml:Assertion").item(0));
        DOMSource source=new DOMSource(document);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer former=tf.newTransformer();
        former.setOutputProperty(OutputKeys.STANDALONE, "yes");
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        former.transform(source, sr);
        String result=sw.toString();
        return result;
        //return replacrString(result,userName,userID);
    }

    public String replacrString(String xmlResult,String userName,String userID){
        String xml =
                "<saml:Attribute Name=\"id\"" +
                        " NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:basic\"" +
                        "><saml:Attributevalue xmins:xs=\"http://ww.w3.org/2001/XMLSchena\" " +
                        "xmins:xsi=\"http://ww.w3.org/2001/XMLSchema-instance\"" +
                        "xsi:type=\"xs:string\"" +
                        ">"+userID+"" +
                        "</saml:Attributevalue> </saml:Attribute><saml:Attribute Name=\"username\" " +
                        "NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:basic\" " +
                        ">" +
                        "<saml:Attributevalue xmins:xs=\"http://ww.w3.org/2001/XMLSchena\" " +
                        "xmins:xsi=\"http://ww.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:type=\"xs:string\" " +
                        ">"+userName+"" +
                        "</saml:Attributevalue></saml:Attribute>" ;
        xmlResult = xmlResult.replace("<saml:Attribute/>",xml);
        return xmlResult;
    }
    public String getForm(String url,String response,String RelayState){
        return "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
                "<title>POST data</title>\n" +
                "</head>\n" +
                "<body onload=\"document.forms[0].submit()\">\n" +
                "<noscript>\n" +
                "<p><strong>Note:</strong> Since your browser does not support JavaScript, you must press the button below once to proceed.</p>\n" +
                "</noscript>\n" +
                "\t<form method=\"post\" action=\"" + url + "\">\n" +
                "\t\t<input type=\"hidden\" name=\"SAMLResponse\" value=\"" + response + "\"/><br/>\n" +
                "\t\t<input type=\"hidden\" name=\"RelayState\" value=\"" + RelayState + "\"/><br/>\n" +
                "\t\t<noscript><input type=\"submit\" value=\"Submit\" /></noscript>\n" +
                "\t</form>\n" +
                "</body>\n" +
                "</html>";
    }
    /**
     * 生成SAML AttributeStatement XML片段
     * @param userId 用户ID
     * @param username 用户名
     * @return 格式化的XML字符串
     */
    public static Element generateAttributeStatement(String userId, String username) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // 创建AttributeStatement元素
            Element attributeStatement = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:assertion", "saml:AttributeStatement");
            doc.appendChild(attributeStatement);

            // 创建id属性
            Element idAttribute = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:assertion", "saml:Attribute");
            idAttribute.setAttribute("Name", "id");
            idAttribute.setAttribute("NameFormat", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
            attributeStatement.appendChild(idAttribute);

            // 创建id属性值
            Element idAttributeValue = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:assertion", "saml:AttributeValue");
            idAttributeValue.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "xs:string");
            idAttributeValue.setTextContent(userId);
            idAttribute.appendChild(idAttributeValue);

            // 创建username属性
            Element usernameAttribute = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:assertion", "saml:Attribute");
            usernameAttribute.setAttribute("Name", "username");
            usernameAttribute.setAttribute("NameFormat", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
            attributeStatement.appendChild(usernameAttribute);

            // 创建username属性值
            Element usernameAttributeValue = doc.createElementNS("urn:oasis:names:tc:SAML:2.0:assertion", "saml:AttributeValue");
            usernameAttributeValue.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "xs:string");
            usernameAttributeValue.setTextContent(username);
            usernameAttribute.appendChild(usernameAttributeValue);
            return attributeStatement;
        } catch (Exception e) {
            throw new RuntimeException("生成AttributeStatement失败", e);
        }
    }
    public static void main(String[] args) throws KeyStoreException {
        KeyStoreUtil.getKeyStore("/saml.jks","123456");
        System.out.println(UUID.randomUUID().toString());
    }
}
