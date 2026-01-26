package com.bw.saml.idp;

import com.bw.saml.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static com.bw.saml.constants.Constants.IDP_DOMAIN;

@RestController
@RequestMapping("/todo")
public class TodoController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/sso")
    public void sso(HttpServletResponse response,String username,String userId,String todoUrl){
        String tag = "/todo/sso";
        try {
            logger.info(tag +"=> " +"username:"+username+",todoUrl:"+todoUrl);
            logger.info(tag +"开始接口请求！");
            Cookie cookie = new Cookie(Constants.IDP_COOKIE_KEY,username+"-"+userId);
            cookie.setPath("/");
            cookie.setMaxAge(7000*24*60*60);
            response.addCookie(cookie);
            if (todoUrl.contains("?")) {
                todoUrl = todoUrl + "&bplte_company=" + Constants.BPLTE_COMPANY;
            } else {
                todoUrl = todoUrl + "?bplte_company=" + Constants.BPLTE_COMPANY;
            }
            String actionUrl = todoUrl;
            logger.info(tag +"=> " +"执行跳转链接:"+actionUrl);
            response.sendRedirect(todoUrl.replace("zhejianglu",Constants.BPLTE_COMPANY));
        }catch (Exception e){
            logger.error(tag +"=> " + e.getMessage());
        }
    }
}
