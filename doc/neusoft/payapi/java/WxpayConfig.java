package com.neusoft.pay.wxpay.model;

public class WxpayConfig {
    /**
     * 微信APP ID
     */
    private  String appId;

    /**
     * 微信APP Key
     */
    private  String appKey;
	/**
     * 商户ID
     */
    String merchantId;
    /**
     * 商户密钥
     */
    String secret;
	 /**
     * 微信后置通知url
     */
    private String notifyUrl;
    /**
     * 微信前端跳转url :手机网页端
     */
    private String wapReturnUrl;
    /**
     * 微信前端跳转url :pc网页端
     */
    private String webReturnUrl;
    /**
     *  微信支付授权目录 如果空 则是wx/wxpay/  否则是你自定义的(需要开发 代理请求)
     *  一般为空
     */
    private String myWxPayUrl;
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	public String getWapReturnUrl() {
		return wapReturnUrl;
	}
	public void setWapReturnUrl(String wapReturnUrl) {
		this.wapReturnUrl = wapReturnUrl;
	}
	public String getWebReturnUrl() {
		return webReturnUrl;
	}
	public void setWebReturnUrl(String webReturnUrl) {
		this.webReturnUrl = webReturnUrl;
	}
	public String getMyWxPayUrl() {
		return myWxPayUrl;
	}
	public void setMyWxPayUrl(String myWxPayUrl) {
		this.myWxPayUrl = myWxPayUrl;
	}
    
}
