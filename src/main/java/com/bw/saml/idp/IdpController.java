package com.bw.saml.idp;

import com.bw.saml.cc.pojo.AuthnRequestField;
import com.bw.saml.cc.service.AuthnRequestHandler;
import com.bw.saml.cc.service.SamlResponseGenerator;
import com.bw.saml.constants.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * @author ccw
 * @date 2026-02-07 14:59
 */
@RestController
@RequestMapping("/idp")
public class IdpController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthnRequestHandler authnRequestHandler;
    @Autowired
    private SamlResponseGenerator samlResponseGenerator;
    @Autowired
    private SamlRequestCache samlRequestCache;
    @GetMapping("/sso/login")
    public void sso(String SAMLRequest,String RelayState, HttpServletRequest request,HttpServletResponse response) throws Exception {
        /**
         * 是否在idp端已登录
         */

        boolean loginFlag = false;
        Cookie[]cookies = request.getCookies();
        String cookie_value = null;
        if(cookies != null){
            for(Cookie cookie:cookies){
                if((Constants.IDP_COOKIE_KEY).equalsIgnoreCase(cookie.getName())){
                    cookie_value = cookie.getValue();
                    if(StringUtils.isNotEmpty(cookie_value)){
                        loginFlag = true;
                    }
                }
            }
        }
        String[] valueArray = new String[0];
        if(cookie_value!=null){
            valueArray = cookie_value.split("-");
        }
        if(loginFlag && valueArray.length>1){
            //已登录，解析SAMLRequest对象,查找出用户信息
            AuthnRequestField authnRequestField = authnRequestHandler.handleAuthnRequest(SAMLRequest);
            String result = samlResponseGenerator.generateSamlResponse(authnRequestField,valueArray[0],valueArray[1]);
            response.reset();
            PrintWriter printWriter = response.getWriter();
            printWriter.write(samlResponseGenerator.getForm(authnRequestField.getAssertionConsumerServiceUrl(), new Base64().encodeAsString(result.getBytes("utf-8")),RelayState));
            printWriter.flush();
            printWriter.close();
        }else {
            //重定向到登陆页面
            response.reset();
            response.setContentType("text/html;charset=utf-8");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "e");
            response.sendRedirect(Constants.IDP_DOMAIN + "/LandingPage_logout.html"+ "?bplte_company=" + Constants.BPLTE_COMPANY);
        }
    }
    @GetMapping("/logout")
    public void sso(HttpServletRequest request,HttpServletResponse response) throws Exception {
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie:cookies){
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        response.reset();
        response.setContentType("text/html;charset=utf-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "e");
        response.sendRedirect(Constants.IDP_DOMAIN + "/LandingPage_logout.html"+ "?bplte_company=" + Constants.BPLTE_COMPANY);
    }

    @PostMapping("/auth")
    public LoginResponse login(String username, String password, HttpServletRequest req, HttpServletResponse res) throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        if ("admin".equals(username) && "admin".equals(password)) {
            String email = "test@qq.com";
            //鉴权通过
            System.out.println("auth pass...");
            AuthnRequestField authnRequestField = authnRequestHandler.handleAuthnRequest(samlRequestCache.getSAMLRequest());
            System.out.println(authnRequestField);
            String result = samlResponseGenerator.generateSamlResponse(authnRequestField,email, email);
            res.reset();
            Cookie cookie = new Cookie(Constants.IDP_COOKIE_KEY,Constants.IDP_COOKIE_VALUE);
            cookie.setPath("/");
            res.addCookie(cookie);
            PrintWriter printWriter = res.getWriter();
            //printWriter.write(samlResponseGenerator.getForm(authnRequestField.getAssertionConsumerServiceUrl(), new Base64().encodeAsString(result.getBytes("utf-8"))));
            printWriter.flush();
            printWriter.close();
            return null;
        }
        loginResponse.setCode(1);
        return loginResponse;
    }
}
