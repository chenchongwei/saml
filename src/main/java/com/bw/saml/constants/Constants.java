package com.bw.saml.constants;

/**
 * @author Xiaosy
 * @date 2017-12-21 9:49
 */
public class Constants {

    /**
     * cookie相关
     */
    /**
     * sp 端cookie的key
     */
    public static final String SP_COOKIE_KEY = "sp_cookie_key";
    /**
     * sp 端cookie的value
     */
    public static final String SP_COOKIE_VALUE = "sp_cookie_value";
    /**
     * idp端cookie的key
     */
    public static final String IDP_COOKIE_KEY = "idp_cookie_key";
    /**
     * idp 端 cookie的value
     */
    public static final String IDP_COOKIE_VALUE = "idp_cookie_value";

    /**
     * idp端首页面
     */
    //public static final String IDP_DOMAIN = "https://hcm-cn10.hr.sapcloud.cn/sf/home";
    public static final String IDP_DOMAIN = "https://hcm-cn10.hr.sapcloud.cn";

    /**
     * idp公司编码
     */
    //public static final String BPLTE_COMPANY = "zhejiangluD";
    public static final String BPLTE_COMPANY = "zhejianglu";


    /**
     * sp的身份标识 https://apiesb.luyuan.cn
     */
    //public static final String SP_ENTITY_ID = "https://ccwapi.luyuan.cn/sp";
    public static final String SP_ENTITY_ID = "https://sapias.luyuan.cn/sp";

    /**
     * idp的身份标识
     */
    //public static final String IDP_ENTITY_ID = "https://ccwapi.luyuan.cn/idp";
    public static final String IDP_ENTITY_ID = "https://sapias.luyuan.cn/idp";
    /**
     * idp的sso地址
     */
    //public static final String IDP_SSO_URL = "https://ccwapi.luyuan.cn/idp/sso/login";
    public static final String IDP_SSO_URL = "https://sapias.luyuan.cn/idp/sso/login";
    /**
     * idp的sso地址
     */
    //public static final String IDP_LOGOUT_URL = "https://ccwapi.luyuan.cn/idp/sso/logout";
    public static final String IDP_LOGOUT_URL = "https://sapias.luyuan.cn/idp/logout";

    /**
     * sp的acs地址
     */
    public static final String SP_ACS_URL = "https://sapias.luyuan.cn/sp/consumer";

}
