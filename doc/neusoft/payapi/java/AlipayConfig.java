package com.neusoft.pay.alipay.model;

public class AlipayConfig {
    /**
     * 签约的支付宝账号对应的支付宝唯一用户号，以2088开头的16位纯数字组成。
     */
    String merchantId;
    /**
     * 商户密钥
     */
    String secret;
	 /**
     * 支付宝后置通知url
     * {@link me.hao0.alipay.model.enums.AlipayField#NOTIFY_URL}
     */
    private String notifyUrl;
    /**
     * 支付宝前端跳转url :手机网页端
     * {@link me.hao0.alipay.model.enums.AlipayField#RETURN_URL}
     */
    private String wapReturnUrl;
    /**
     * 支付宝前端跳转url :pc网页端
     * {@link me.hao0.alipay.model.enums.AlipayField#RETURN_URL}
     */
    private String webReturnUrl;
    /**
     * 支付宝在微信内的手机号 的返回回调 优化使用
     */
    private String phoneCallbackUrl;
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
	public String getPhoneCallbackUrl() {
		return phoneCallbackUrl;
	}
	public void setPhoneCallbackUrl(String phoneCallbackUrl) {
		this.phoneCallbackUrl = phoneCallbackUrl;
	}

}
